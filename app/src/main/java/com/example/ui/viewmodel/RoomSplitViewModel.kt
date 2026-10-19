package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.DebtTransfer
import com.example.data.model.Entry
import com.example.data.model.MemberBalance
import com.example.data.model.Room
import com.example.data.model.RoomMember
import com.example.data.model.UserProfile
import com.example.data.remote.FirestoreErrorHandler
import com.example.data.repository.RoomSplitFirestoreRepository
import com.example.ui.theme.AvatarColors
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.min

class RoomSplitViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = RoomSplitFirestoreRepository(application.applicationContext)
    private val auth = FirebaseAuth.getInstance()

    private val _currentUserProfile = MutableStateFlow<UserProfile?>(null)
    val currentUserProfile: StateFlow<UserProfile?> = _currentUserProfile.asStateFlow()

    private val _currentRoom = MutableStateFlow<Room?>(null)
    val currentRoom: StateFlow<Room?> = _currentRoom.asStateFlow()

    private val _members = MutableStateFlow<List<RoomMember>>(emptyList())
    val members: StateFlow<List<RoomMember>> = _members.asStateFlow()

    private val _entries = MutableStateFlow<List<Entry>>(emptyList())
    val entries: StateFlow<List<Entry>> = _entries.asStateFlow()

    private val _balances = MutableStateFlow<List<MemberBalance>>(emptyList())
    val balances: StateFlow<List<MemberBalance>> = _balances.asStateFlow()

    private val _debtTransfers = MutableStateFlow<List<DebtTransfer>>(emptyList())
    val debtTransfers: StateFlow<List<DebtTransfer>> = _debtTransfers.asStateFlow()

    private val _recentAlertMessage = MutableStateFlow<String?>(null)
    val recentAlertMessage: StateFlow<String?> = _recentAlertMessage.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var profileJob: Job? = null
    private var roomJob: Job? = null
    private var membersJob: Job? = null
    private var entriesJob: Job? = null

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                initUser(user.uid, user.displayName ?: "Flatmate", user.email ?: "")
            } else {
                cleanupState()
            }
        }
    }

    fun initUser(userId: String, displayName: String, email: String) {
        profileJob?.cancel()
        profileJob = viewModelScope.launch {
            repository.observeUserProfile(userId).collect { profile ->
                if (profile == null) {
                    val newProfile = UserProfile(
                        id = userId,
                        displayName = displayName,
                        email = email,
                        avatarColorHex = AvatarColors.random(),
                        createdAt = System.currentTimeMillis()
                    )
                    try {
                        repository.saveUserProfile(newProfile)
                        _currentUserProfile.value = newProfile
                    } catch (e: Exception) {
                        _errorMessage.value = FirestoreErrorHandler.handle(e, "create user profile")
                    }
                } else {
                    _currentUserProfile.value = profile
                    val roomId = profile.activeRoomId
                    if (!roomId.isNullOrEmpty()) {
                        observeActiveRoom(roomId)
                    } else {
                        clearRoomState()
                    }
                }
            }
        }
    }

    private fun observeActiveRoom(roomId: String) {
        roomJob?.cancel()
        roomJob = viewModelScope.launch {
            repository.observeRoom(roomId).collect { room ->
                _currentRoom.value = room
            }
        }

        membersJob?.cancel()
        membersJob = viewModelScope.launch {
            repository.observeRoomMembers(roomId).collect { membersList ->
                _members.value = membersList
                recalculateBalancesAndDebts(_entries.value, membersList)
            }
        }

        entriesJob?.cancel()
        entriesJob = viewModelScope.launch {
            repository.observeRoomEntries(roomId).collect { entriesList ->
                val prevList = _entries.value
                _entries.value = entriesList
                recalculateBalancesAndDebts(entriesList, _members.value)

                // Trigger in-app notification banner for new entry
                if (prevList.isNotEmpty() && entriesList.size > prevList.size) {
                    val newest = entriesList.firstOrNull()
                    if (newest != null && newest.paidByMemberId != _currentUserProfile.value?.id) {
                        _recentAlertMessage.value = "${newest.paidByMemberName} logged ₹${String.format(Locale.getDefault(), "%.0f", newest.amount)} for ${newest.itemName}"
                        kotlinx.coroutines.delay(4000)
                        _recentAlertMessage.value = null
                    }
                }
            }
        }
    }

    private fun recalculateBalancesAndDebts(entriesList: List<Entry>, membersList: List<RoomMember>) {
        if (membersList.isEmpty()) {
            _balances.value = emptyList()
            _debtTransfers.value = emptyList()
            return
        }

        val totalExpenses = entriesList.filter { !it.isSettlement }.sumOf { it.amount }
        val fairShare = totalExpenses / membersList.size.toDouble()

        // 1. Calculate net balance for each member
        val paidMap = mutableMapOf<String, Double>()
        membersList.forEach { paidMap[it.userId] = 0.0 }

        entriesList.forEach { entry ->
            if (entry.isSettlement) {
                // Settlement: payer paid, receiver got paid
                val currentPayer = paidMap[entry.paidByMemberId] ?: 0.0
                paidMap[entry.paidByMemberId] = currentPayer + entry.amount

                val receiverId = entry.settlementReceiverId
                if (receiverId != null) {
                    val currentReceiver = paidMap[receiverId] ?: 0.0
                    paidMap[receiverId] = currentReceiver - entry.amount
                }
            } else {
                val current = paidMap[entry.paidByMemberId] ?: 0.0
                paidMap[entry.paidByMemberId] = current + entry.amount
            }
        }

        val calculatedBalances = membersList.map { member ->
            val paid = paidMap[member.userId] ?: 0.0
            val net = paid - fairShare
            MemberBalance(
                member = member,
                totalPaid = paid,
                fairShare = fairShare,
                netBalance = net
            )
        }
        _balances.value = calculatedBalances

        // 2. Simplified bilateral debt calculation (greedy)
        val creditors = calculatedBalances
            .filter { it.netBalance > 0.01 }
            .map { it.member to it.netBalance }
            .toMutableList()

        val debtors = calculatedBalances
            .filter { it.netBalance < -0.01 }
            .map { it.member to -it.netBalance }
            .toMutableList()

        val transfers = mutableListOf<DebtTransfer>()

        var cIdx = 0
        var dIdx = 0
        while (cIdx < creditors.size && dIdx < debtors.size) {
            val (creditor, creditAmt) = creditors[cIdx]
            val (debtor, debtAmt) = debtors[dIdx]
            val settlementAmt = min(creditAmt, debtAmt)

            if (settlementAmt > 0.01) {
                transfers.add(
                    DebtTransfer(
                        debtor = debtor,
                        creditor = creditor,
                        amount = settlementAmt
                    )
                )
            }

            creditors[cIdx] = creditor to (creditAmt - settlementAmt)
            debtors[dIdx] = debtor to (debtAmt - settlementAmt)

            if (creditors[cIdx].second < 0.01) cIdx++
            if (debtors[dIdx].second < 0.01) dIdx++
        }

        _debtTransfers.value = transfers
    }

    fun createRoom(name: String) {
        val user = _currentUserProfile.value ?: return
        viewModelScope.launch {
            try {
                val room = repository.createRoom(name, user)
                observeActiveRoom(room.id)
            } catch (e: Exception) {
                _errorMessage.value = FirestoreErrorHandler.handle(e, "create room")
            }
        }
    }

    fun joinRoom(inviteCode: String) {
        val user = _currentUserProfile.value ?: return
        viewModelScope.launch {
            try {
                val room = repository.joinRoomByInviteCode(inviteCode, user)
                observeActiveRoom(room.id)
            } catch (e: Exception) {
                _errorMessage.value = FirestoreErrorHandler.handle(e, "join room")
            }
        }
    }

    fun addEntry(entry: Entry) {
        val room = _currentRoom.value ?: return
        viewModelScope.launch {
            try {
                repository.addEntry(room.id, entry)
            } catch (e: Exception) {
                _errorMessage.value = FirestoreErrorHandler.handle(e, "log expense")
            }
        }
    }

    fun deleteEntry(entryId: String) {
        val room = _currentRoom.value ?: return
        viewModelScope.launch {
            try {
                repository.deleteEntry(room.id, entryId)
            } catch (e: Exception) {
                _errorMessage.value = FirestoreErrorHandler.handle(e, "delete expense")
            }
        }
    }

    fun recordSettlement(debt: DebtTransfer) {
        val room = _currentRoom.value ?: return
        viewModelScope.launch {
            try {
                val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val settlementEntry = Entry(
                    amount = debt.amount,
                    category = "Settlement",
                    itemName = "Settlement: ${debt.debtor.displayName} → ${debt.creditor.displayName}",
                    paidByMemberId = debt.debtor.userId,
                    paidByMemberName = debt.debtor.displayName,
                    paidByMemberAvatarColorHex = debt.debtor.avatarColorHex,
                    date = todayStr,
                    note = "Direct UPI settlement",
                    isSettlement = true,
                    settlementReceiverId = debt.creditor.userId,
                    createdAt = System.currentTimeMillis()
                )
                repository.addEntry(room.id, settlementEntry)
            } catch (e: Exception) {
                _errorMessage.value = FirestoreErrorHandler.handle(e, "record settlement")
            }
        }
    }

    fun updateUpiId(upiId: String) {
        val user = _currentUserProfile.value ?: return
        viewModelScope.launch {
            try {
                repository.updateUserUpiId(user.id, upiId)
                _currentUserProfile.value = user.copy(upiId = upiId)
                val room = _currentRoom.value
                if (room != null) {
                    repository.updateMemberUpi(room.id, user.id, upiId)
                }
            } catch (e: Exception) {
                _errorMessage.value = FirestoreErrorHandler.handle(e, "update UPI ID")
            }
        }
    }

    fun clearActiveRoom() {
        val user = _currentUserProfile.value ?: return
        viewModelScope.launch {
            try {
                repository.updateUserActiveRoom(user.id, null)
                clearRoomState()
            } catch (e: Exception) {
                _errorMessage.value = FirestoreErrorHandler.handle(e, "leave room")
            }
        }
    }

    fun signOut() {
        auth.signOut()
        cleanupState()
    }

    private fun clearRoomState() {
        roomJob?.cancel()
        membersJob?.cancel()
        entriesJob?.cancel()
        _currentRoom.value = null
        _members.value = emptyList()
        _entries.value = emptyList()
        _balances.value = emptyList()
        _debtTransfers.value = emptyList()
    }

    private fun cleanupState() {
        profileJob?.cancel()
        clearRoomState()
        _currentUserProfile.value = null
    }
}
