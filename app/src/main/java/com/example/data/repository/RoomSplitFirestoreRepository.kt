package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.R
import com.example.data.model.CategorySpend
import com.example.data.model.Entry
import com.example.data.model.EntryWithMember
import com.example.data.model.ExpenseCategory
import com.example.data.model.MemberBalance
import com.example.data.model.MemberSpend
import com.example.data.model.MonthlySummary
import com.example.data.model.Room
import com.example.data.model.RoomMembership
import com.example.data.model.SettlementPair
import com.example.data.model.User
import com.example.data.remote.OperationType
import com.example.data.remote.handleFirestoreError
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.tasks.await
import java.util.Locale
import java.util.UUID
import kotlin.math.abs
import kotlin.math.roundToInt

class RoomSplitFirestoreRepository(
  private val db: FirebaseFirestore,
  private val auth: FirebaseAuth,
  private val prefs: SharedPreferences
) {
  constructor(context: Context) : this(
    db = FirebaseFirestore.getInstance(context.getString(R.string.firestore_database_id)),
    auth = FirebaseAuth.getInstance(),
    prefs = context.getSharedPreferences("roomsplit_cloud_prefs", Context.MODE_PRIVATE)
  )

  private val _activeRoomId = MutableStateFlow<String?>(prefs.getString(KEY_ACTIVE_ROOM_ID, null))
  val activeRoomId: Flow<String?> = _activeRoomId.asStateFlow()

  private val _notificationEvents = MutableStateFlow<String?>(null)
  val notificationEvents: Flow<String?> = _notificationEvents.asStateFlow()

  fun setActiveRoomId(roomId: String?) {
    _activeRoomId.value = roomId
    prefs.edit().putString(KEY_ACTIVE_ROOM_ID, roomId).apply()
  }

  fun emitNotification(message: String) {
    _notificationEvents.value = message
  }

  fun clearNotification() {
    _notificationEvents.value = null
  }

  fun requireUserId(): String {
    return auth.currentUser?.uid ?: throw IllegalStateException("User must be signed in with Google")
  }

  // Real-time Current User Profile Flow
  fun getCurrentUserFlow(): Flow<User?> = callbackFlow {
    val uid = auth.currentUser?.uid
    if (uid == null) {
      trySend(null)
      close()
      return@callbackFlow
    }

    val docRef = db.collection("users").document(uid)
    val registration = docRef.addSnapshotListener { snapshot, error ->
      if (error != null) {
        handleFirestoreError(error, OperationType.GET, docRef.path)
        return@addSnapshotListener
      }
      if (snapshot != null && snapshot.exists()) {
        trySend(snapshot.toObject(User::class.java))
      } else {
        val googleUser = auth.currentUser
        val fallbackUser = User(
          id = uid,
          name = googleUser?.displayName ?: "Roommate",
          email = googleUser?.email ?: "",
          phone = googleUser?.phoneNumber ?: "",
          avatarColorHex = "#059669",
          upiId = ""
        )
        trySend(fallbackUser)
      }
    }

    awaitClose { registration.remove() }
  }

  suspend fun saveUserProfile(
    name: String,
    phone: String,
    avatarHex: String,
    upiId: String
  ) {
    val uid = requireUserId()
    val docRef = db.collection("users").document(uid)
    val user = User(
      id = uid,
      name = name.trim(),
      email = auth.currentUser?.email ?: "",
      phone = phone.trim(),
      avatarColorHex = avatarHex.ifBlank { "#059669" },
      upiId = upiId.trim(),
      notifEnabled = true
    )
    try {
      docRef.set(user).await()
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, docRef.path)
      throw e
    }
  }

  // Room Creation
  suspend fun createRoom(name: String, emoji: String = "🏠"): Room {
    val uid = requireUserId()
    val user = getCurrentUserOnce() ?: User(id = uid, name = auth.currentUser?.displayName ?: "Roommate")
    val roomId = "room-" + UUID.randomUUID().toString().take(8)
    val inviteCode = generateInviteCode()

    val room = Room(
      id = roomId,
      name = name.trim(),
      inviteCode = inviteCode,
      emoji = emoji,
      createdBy = uid
    )

    val docRef = db.collection("rooms").document(roomId)
    try {
      docRef.set(room).await()

      // Add creator as first member
      val memberRef = docRef.collection("members").document(uid)
      val membership = RoomMembership(
        id = uid,
        roomId = roomId,
        userId = uid,
        name = user.name,
        avatarColorHex = user.avatarColorHex,
        upiId = user.upiId,
        isActive = true
      )
      memberRef.set(membership).await()

      setActiveRoomId(roomId)
      return room
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, docRef.path)
      throw e
    }
  }

  // Join Room by 6-Char Code
  suspend fun joinRoomByInviteCode(code: String): Result<Room> {
    val cleanCode = code.trim().uppercase()
    val uid = requireUserId()
    val user = getCurrentUserOnce() ?: User(id = uid, name = auth.currentUser?.displayName ?: "Roommate")

    val colRef = db.collection("rooms")
    try {
      val query = colRef.whereEqualTo("inviteCode", cleanCode).limit(1).get().await()
      if (query.isEmpty) {
        return Result.failure(IllegalArgumentException("No room found with invite code '$cleanCode'. Check with flatmates."))
      }

      val roomDoc = query.documents[0]
      val room = roomDoc.toObject(Room::class.java)
        ?: return Result.failure(IllegalStateException("Failed to parse room data"))

      // Add user to room's members subcollection
      val memberRef = roomDoc.reference.collection("members").document(uid)
      val membership = RoomMembership(
        id = uid,
        roomId = room.id,
        userId = uid,
        name = user.name,
        avatarColorHex = user.avatarColorHex,
        upiId = user.upiId,
        isActive = true
      )
      memberRef.set(membership).await()

      setActiveRoomId(room.id)
      return Result.success(room)
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.GET, colRef.path)
      return Result.failure(e)
    }
  }

  suspend fun updateRoomName(roomId: String, newName: String) {
    val docRef = db.collection("rooms").document(roomId)
    try {
      docRef.update("name", newName.trim()).await()
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docRef.path)
      throw e
    }
  }

  // Real-time Active Room Observation
  fun getActiveRoomFlow(roomId: String): Flow<Room?> = callbackFlow {
    val docRef = db.collection("rooms").document(roomId)
    val registration = docRef.addSnapshotListener { snapshot, error ->
      if (error != null) {
        handleFirestoreError(error, OperationType.GET, docRef.path)
        return@addSnapshotListener
      }
      trySend(snapshot?.toObject(Room::class.java))
    }
    awaitClose { registration.remove() }
  }

  // Real-time Members Flow
  fun getRoomMembersFlow(roomId: String): Flow<List<User>> = callbackFlow {
    val colRef = db.collection("rooms").document(roomId).collection("members")
    val registration = colRef.addSnapshotListener { snapshot, error ->
      if (error != null) {
        handleFirestoreError(error, OperationType.LIST, colRef.path)
        return@addSnapshotListener
      }
      val members = snapshot?.toObjects(RoomMembership::class.java)
        ?.filter { it.isActive }
        ?.map { it.toUser() }
        ?: emptyList()
      trySend(members)
    }
    awaitClose { registration.remove() }
  }

  // Real-time Shared Feed Entries Flow
  fun getFeedEntriesFlow(roomId: String): Flow<List<EntryWithMember>> = callbackFlow {
    val colRef = db.collection("rooms").document(roomId).collection("entries")
      .orderBy("date", Query.Direction.DESCENDING)
      .orderBy("createdAt", Query.Direction.DESCENDING)

    val registration = colRef.addSnapshotListener { snapshot, error ->
      if (error != null) {
        handleFirestoreError(error, OperationType.LIST, "rooms/$roomId/entries")
        return@addSnapshotListener
      }
      val entries = snapshot?.toObjects(Entry::class.java) ?: emptyList()
      trySend(entries.map { it.toEntryWithMember() })
    }
    awaitClose { registration.remove() }
  }

  // Add Real Shared Entry
  suspend fun addEntry(
    roomId: String,
    itemName: String,
    category: String,
    amount: Double,
    date: String,
    note: String = "",
    paidBy: User
  ): Entry {
    require(itemName.isNotBlank()) { "Item name cannot be empty" }
    require(amount > 0) { "Amount must be greater than ₹0" }

    val entryId = "entry-" + UUID.randomUUID().toString().take(8)
    val roundedAmount = (amount * 100).roundToInt() / 100.0

    val entry = Entry(
      id = entryId,
      roomId = roomId,
      memberId = paidBy.id,
      memberName = paidBy.name,
      memberAvatarColorHex = paidBy.avatarColorHex,
      itemName = itemName.trim(),
      category = category.lowercase().trim(),
      amount = roundedAmount,
      date = date,
      note = note.trim()
    )

    val docRef = db.collection("rooms").document(roomId).collection("entries").document(entryId)
    try {
      docRef.set(entry).await()

      // In-app push notification
      val formattedAmt = String.format(Locale.US, "₹%.0f", roundedAmount)
      emitNotification("${paidBy.name} added ${entry.itemName} $formattedAmt")

      return entry
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, docRef.path)
      throw e
    }
  }

  suspend fun deleteEntry(roomId: String, entryId: String) {
    val docRef = db.collection("rooms").document(roomId).collection("entries").document(entryId)
    try {
      docRef.delete().await()
      emitNotification("Entry deleted")
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, docRef.path)
      throw e
    }
  }

  // Live Balances & Greedy Settlements
  fun getBalancesFlow(roomId: String, monthPrefix: String): Flow<Triple<Double, List<MemberBalance>, List<SettlementPair>>> {
    return combine(
      getFeedEntriesFlow(roomId),
      getRoomMembersFlow(roomId)
    ) { entries, members ->
      val monthEntries = entries.map { it.entry }.filter { it.date.startsWith(monthPrefix) }
      calculateBalances(monthEntries, members)
    }
  }

  private fun calculateBalances(
    entries: List<Entry>,
    members: List<User>
  ): Triple<Double, List<MemberBalance>, List<SettlementPair>> {
    if (members.isEmpty()) {
      return Triple(0.0, emptyList(), emptyList())
    }

    val totalSpend = entries.sumOf { it.amount }
    val memberCount = members.size
    val averagePerPerson = totalSpend / memberCount

    val paidByMember = entries.groupBy { it.memberId }
      .mapValues { (_, list) -> list.sumOf { it.amount } }

    val memberBalances = members.map { user ->
      val paid = paidByMember[user.id] ?: 0.0
      val net = paid - averagePerPerson
      MemberBalance(
        user = user,
        totalPaid = (paid * 100).roundToInt() / 100.0,
        netBalance = (net * 100).roundToInt() / 100.0
      )
    }.sortedByDescending { it.netBalance }

    // Greedy settlement pairs (largest debtor pays largest creditor first)
    class TempBalance(val user: User, var amount: Double)

    val creditors = memberBalances
      .filter { it.netBalance > 0.01 }
      .map { TempBalance(it.user, it.netBalance) }
      .toMutableList()

    val debtors = memberBalances
      .filter { it.netBalance < -0.01 }
      .map { TempBalance(it.user, abs(it.netBalance)) }
      .toMutableList()

    val settlements = mutableListOf<SettlementPair>()
    var dIdx = 0
    var cIdx = 0

    while (dIdx < debtors.size && cIdx < creditors.size) {
      val debtor = debtors[dIdx]
      val creditor = creditors[cIdx]

      val settleAmount = minOf(debtor.amount, creditor.amount)
      if (settleAmount > 0.01) {
        val roundedAmount = (settleAmount * 100).roundToInt() / 100.0
        settlements.add(
          SettlementPair(
            id = "settle-${debtor.user.id}-${creditor.user.id}",
            fromUser = debtor.user,
            toUser = creditor.user,
            amount = roundedAmount
          )
        )
      }

      debtor.amount -= settleAmount
      creditor.amount -= settleAmount

      if (debtor.amount <= 0.01) dIdx++
      if (creditor.amount <= 0.01) cIdx++
    }

    return Triple(
      (totalSpend * 100).roundToInt() / 100.0,
      memberBalances,
      settlements
    )
  }

  // Monthly Summary Flow
  fun getMonthlySummaryFlow(roomId: String, monthPrefix: String, monthDisplayName: String): Flow<MonthlySummary> {
    return combine(
      getFeedEntriesFlow(roomId),
      getRoomMembersFlow(roomId)
    ) { entries, members ->
      val monthEntries = entries.map { it.entry }.filter { it.date.startsWith(monthPrefix) }
      val totalSpend = monthEntries.sumOf { it.amount }
      val memberCount = if (members.isNotEmpty()) members.size else 1
      val average = totalSpend / memberCount

      val catEntries = monthEntries.groupBy { it.category.lowercase() }
      val catBreakdowns = ExpenseCategory.entries.map { cat ->
        val amount = catEntries[cat.id]?.sumOf { it.amount } ?: 0.0
        val pct = if (totalSpend > 0) ((amount / totalSpend) * 100f).toFloat() else 0f
        CategorySpend(
          category = cat,
          amount = (amount * 100).roundToInt() / 100.0,
          percentage = pct
        )
      }.sortedByDescending { it.amount }

      val memberEntries = monthEntries.groupBy { it.memberId }
      val memberBreakdowns = members.map { user ->
        val amount = memberEntries[user.id]?.sumOf { it.amount } ?: 0.0
        val pct = if (totalSpend > 0) ((amount / totalSpend) * 100f).toFloat() else 0f
        MemberSpend(
          user = user,
          amount = (amount * 100).roundToInt() / 100.0,
          percentage = pct
        )
      }.sortedByDescending { it.amount }

      MonthlySummary(
        monthKey = monthPrefix,
        monthDisplayName = monthDisplayName,
        totalSpend = (totalSpend * 100).roundToInt() / 100.0,
        memberCount = members.size,
        averagePerPerson = (average * 100).roundToInt() / 100.0,
        categoryBreakdowns = catBreakdowns,
        memberBreakdowns = memberBreakdowns
      )
    }
  }

  private suspend fun getCurrentUserOnce(): User? {
    val uid = auth.currentUser?.uid ?: return null
    return try {
      val snap = db.collection("users").document(uid).get().await()
      snap.toObject(User::class.java)
    } catch (_: Exception) {
      null
    }
  }

  private fun generateInviteCode(): String {
    val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    return (1..6).map { chars.random() }.joinToString("")
  }

  companion object {
    private const val KEY_ACTIVE_ROOM_ID = "active_room_id"

    @Volatile
    private var INSTANCE: RoomSplitFirestoreRepository? = null

    fun getInstance(context: Context): RoomSplitFirestoreRepository {
      return INSTANCE ?: synchronized(this) {
        val repo = RoomSplitFirestoreRepository(context)
        INSTANCE = repo
        repo
      }
    }
  }
}
