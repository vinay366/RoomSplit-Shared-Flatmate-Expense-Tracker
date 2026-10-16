package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.EntryWithMember
import com.example.data.model.MemberBalance
import com.example.data.model.MonthlySummary
import com.example.data.model.Room
import com.example.data.model.SettlementPair
import com.example.data.model.User
import com.example.data.repository.RoomSplitFirestoreRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class NavTab {
  FEED,
  BALANCES,
  SUMMARY
}

data class RoomSplitUiState(
  val currentTab: NavTab = NavTab.FEED,
  val currentUser: User? = null,
  val activeRoom: Room? = null,
  val roomMembers: List<User> = emptyList(),
  val feedEntries: List<EntryWithMember> = emptyList(),
  val filteredEntries: List<EntryWithMember> = emptyList(),

  // Month navigation
  val selectedMonthKey: String = "",
  val selectedMonthName: String = "",
  val availableMonths: List<Pair<String, String>> = emptyList(),

  // Balances & Settlements
  val totalSpendForMonth: Double = 0.0,
  val memberBalances: List<MemberBalance> = emptyList(),
  val settlements: List<SettlementPair> = emptyList(),

  // Summary
  val monthlySummary: MonthlySummary? = null,

  // Filters & Search
  val searchQuery: String = "",
  val selectedCategoryFilter: String? = null,

  // UI Modals
  val isAddEntrySheetOpen: Boolean = false,
  val isRoomDetailsOpen: Boolean = false,
  val isCreateRoomOpen: Boolean = false,
  val isJoinRoomOpen: Boolean = false,
  val isProfileDialogOpen: Boolean = false,

  // In-app Notification Banner
  val activeNotification: String? = null,

  // Profile Edit
  val editName: String = "",
  val editPhone: String = "",
  val editUpiId: String = "",
  val editAvatarColorHex: String = "#059669"
)

class RoomSplitViewModel(application: Application) : AndroidViewModel(application) {
  private val repository = RoomSplitFirestoreRepository.getInstance(application)

  private val _uiState = MutableStateFlow(RoomSplitUiState())
  val uiState: StateFlow<RoomSplitUiState> = _uiState.asStateFlow()

  private var feedJob: Job? = null
  private var balanceJob: Job? = null
  private var summaryJob: Job? = null
  private var membersJob: Job? = null
  private var roomJob: Job? = null
  private var notifTimerJob: Job? = null

  init {
    setupMonths()
    observeState()
  }

  private fun setupMonths() {
    val current = YearMonth.now()
    val fmtKey = DateTimeFormatter.ofPattern("yyyy-MM")
    val fmtName = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.US)

    val months = (0..3).map { i ->
      val ym = current.minusMonths(i.toLong())
      Pair(ym.format(fmtKey), ym.format(fmtName))
    }

    _uiState.update {
      it.copy(
        availableMonths = months,
        selectedMonthKey = months.first().first,
        selectedMonthName = months.first().second
      )
    }
  }

  private fun observeState() {
    // 1. Current Authenticated User Profile
    viewModelScope.launch {
      repository.getCurrentUserFlow().collectLatest { user ->
        _uiState.update { state ->
          state.copy(
            currentUser = user,
            editName = user?.name ?: "",
            editPhone = user?.phone ?: "",
            editUpiId = user?.upiId ?: "",
            editAvatarColorHex = user?.avatarColorHex ?: "#059669"
          )
        }
      }
    }

    // 2. In-app Push Notification stream
    viewModelScope.launch {
      repository.notificationEvents.collectLatest { msg ->
        _uiState.update { it.copy(activeNotification = msg) }
        if (msg != null) {
          notifTimerJob?.cancel()
          notifTimerJob = viewModelScope.launch {
            delay(5000)
            dismissNotification()
          }
        }
      }
    }

    // 3. Active Room & Real-time Live Collections
    viewModelScope.launch {
      repository.activeRoomId.collectLatest { roomId ->
        if (roomId.isNullOrBlank()) {
          _uiState.update {
            it.copy(
              activeRoom = null,
              roomMembers = emptyList(),
              feedEntries = emptyList(),
              filteredEntries = emptyList(),
              totalSpendForMonth = 0.0,
              memberBalances = emptyList(),
              settlements = emptyList(),
              monthlySummary = null
            )
          }
          return@collectLatest
        }

        // Active room doc
        roomJob?.cancel()
        roomJob = viewModelScope.launch {
          repository.getActiveRoomFlow(roomId).collectLatest { room ->
            _uiState.update { it.copy(activeRoom = room) }
          }
        }

        // Members subcollection
        membersJob?.cancel()
        membersJob = viewModelScope.launch {
          repository.getRoomMembersFlow(roomId).collectLatest { members ->
            _uiState.update { it.copy(roomMembers = members) }
          }
        }

        // Feed entries subcollection
        feedJob?.cancel()
        feedJob = viewModelScope.launch {
          repository.getFeedEntriesFlow(roomId).collectLatest { entries ->
            _uiState.update { state ->
              state.copy(
                feedEntries = entries,
                filteredEntries = filterEntries(entries, state.searchQuery, state.selectedCategoryFilter)
              )
            }
          }
        }

        // Balances & Summary
        refreshBalances(roomId, _uiState.value.selectedMonthKey)
        refreshSummary(roomId, _uiState.value.selectedMonthKey, _uiState.value.selectedMonthName)
      }
    }
  }

  private fun refreshBalances(roomId: String, monthKey: String) {
    balanceJob?.cancel()
    balanceJob = viewModelScope.launch {
      repository.getBalancesFlow(roomId, monthKey).collectLatest { (total, balances, settlements) ->
        _uiState.update {
          it.copy(
            totalSpendForMonth = total,
            memberBalances = balances,
            settlements = settlements
          )
        }
      }
    }
  }

  private fun refreshSummary(roomId: String, monthKey: String, monthName: String) {
    summaryJob?.cancel()
    summaryJob = viewModelScope.launch {
      repository.getMonthlySummaryFlow(roomId, monthKey, monthName).collectLatest { summary ->
        _uiState.update { it.copy(monthlySummary = summary) }
      }
    }
  }

  fun selectTab(tab: NavTab) {
    _uiState.update { it.copy(currentTab = tab) }
  }

  fun selectMonth(monthKey: String, monthName: String) {
    _uiState.update {
      it.copy(
        selectedMonthKey = monthKey,
        selectedMonthName = monthName
      )
    }
    val roomId = _uiState.value.activeRoom?.id ?: return
    refreshBalances(roomId, monthKey)
    refreshSummary(roomId, monthKey, monthName)
  }

  fun setSearchQuery(query: String) {
    _uiState.update { state ->
      state.copy(
        searchQuery = query,
        filteredEntries = filterEntries(state.feedEntries, query, state.selectedCategoryFilter)
      )
    }
  }

  fun setCategoryFilter(categoryId: String?) {
    _uiState.update { state ->
      state.copy(
        selectedCategoryFilter = categoryId,
        filteredEntries = filterEntries(state.feedEntries, state.searchQuery, categoryId)
      )
    }
  }

  private fun filterEntries(
    entries: List<EntryWithMember>,
    query: String,
    category: String?
  ): List<EntryWithMember> {
    return entries.filter { item ->
      val matchesQuery = query.isBlank() ||
        item.entry.itemName.contains(query, ignoreCase = true) ||
        item.member.name.contains(query, ignoreCase = true) ||
        item.entry.note.contains(query, ignoreCase = true)

      val matchesCat = category == null || item.entry.category.equals(category, ignoreCase = true)

      matchesQuery && matchesCat
    }
  }

  // Add Entry to live cloud Firestore
  fun openAddEntry() { _uiState.update { it.copy(isAddEntrySheetOpen = true) } }
  fun closeAddEntry() { _uiState.update { it.copy(isAddEntrySheetOpen = false) } }

  fun addEntry(
    itemName: String,
    category: String,
    amount: Double,
    date: String,
    note: String = "",
    paidBy: User
  ) {
    val roomId = _uiState.value.activeRoom?.id ?: return
    viewModelScope.launch {
      try {
        repository.addEntry(
          roomId = roomId,
          itemName = itemName,
          category = category,
          amount = amount,
          date = date,
          note = note,
          paidBy = paidBy
        )
        closeAddEntry()
      } catch (e: Exception) {
        _uiState.update { it.copy(activeNotification = "Failed to add entry: ${e.message}") }
      }
    }
  }

  fun deleteEntry(entryId: String) {
    val roomId = _uiState.value.activeRoom?.id ?: return
    viewModelScope.launch {
      try {
        repository.deleteEntry(roomId, entryId)
      } catch (e: Exception) {
        _uiState.update { it.copy(activeNotification = "Failed to delete: ${e.message}") }
      }
    }
  }

  // Real Room Creation & Joining
  fun openCreateRoom() { _uiState.update { it.copy(isCreateRoomOpen = true, isRoomDetailsOpen = false) } }
  fun closeCreateRoom() { _uiState.update { it.copy(isCreateRoomOpen = false) } }

  fun openJoinRoom() { _uiState.update { it.copy(isJoinRoomOpen = true, isRoomDetailsOpen = false) } }
  fun closeJoinRoom() { _uiState.update { it.copy(isJoinRoomOpen = false) } }

  fun openRoomDetails() { _uiState.update { it.copy(isRoomDetailsOpen = true) } }
  fun closeRoomDetails() { _uiState.update { it.copy(isRoomDetailsOpen = false) } }

  fun openProfileDialog() { _uiState.update { it.copy(isProfileDialogOpen = true) } }
  fun closeProfileDialog() { _uiState.update { it.copy(isProfileDialogOpen = false) } }

  fun dismissNotification() {
    repository.clearNotification()
    _uiState.update { it.copy(activeNotification = null) }
  }

  fun createRoom(name: String, emoji: String = "🏠") {
    viewModelScope.launch {
      try {
        val room = repository.createRoom(name, emoji)
        closeCreateRoom()
        repository.emitNotification("Room '${room.name}' created! Code: ${room.inviteCode}")
      } catch (e: Exception) {
        _uiState.update { it.copy(activeNotification = "Error creating room: ${e.message}") }
      }
    }
  }

  fun joinRoom(code: String, onResult: (Boolean, String) -> Unit) {
    viewModelScope.launch {
      val result = repository.joinRoomByInviteCode(code)
      if (result.isSuccess) {
        val room = result.getOrThrow()
        closeJoinRoom()
        repository.emitNotification("Joined '${room.name}'!")
        onResult(true, "Joined ${room.name}")
      } else {
        val msg = result.exceptionOrNull()?.message ?: "Failed to join room"
        onResult(false, msg)
      }
    }
  }

  fun updateRoomName(newName: String) {
    val roomId = _uiState.value.activeRoom?.id ?: return
    viewModelScope.launch {
      try {
        repository.updateRoomName(roomId, newName)
        repository.emitNotification("Room renamed to $newName")
      } catch (e: Exception) {
        _uiState.update { it.copy(activeNotification = "Failed to rename: ${e.message}") }
      }
    }
  }

  fun saveProfile(name: String, phone: String, avatarHex: String, upiId: String) {
    viewModelScope.launch {
      try {
        repository.saveUserProfile(name, phone, avatarHex, upiId)
        closeProfileDialog()
        repository.emitNotification("Profile updated")
      } catch (e: Exception) {
        _uiState.update { it.copy(activeNotification = "Failed to update profile: ${e.message}") }
      }
    }
  }

  fun signOut() {
    Firebase.auth.signOut()
    repository.setActiveRoomId(null)
  }
}
