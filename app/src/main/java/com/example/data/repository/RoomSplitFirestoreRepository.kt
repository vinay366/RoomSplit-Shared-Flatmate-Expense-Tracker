package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.Entry
import com.example.data.model.Room
import com.example.data.model.RoomMember
import com.example.data.model.UserProfile
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class RoomSplitFirestoreRepository(private val context: Context) {
    private val auth = FirebaseAuth.getInstance()

    private val firestore: FirebaseFirestore by lazy {
        val databaseId = try {
            context.getString(R.string.firestore_database_id)
        } catch (e: Exception) {
            "(default)"
        }
        if (databaseId.isNotEmpty() && databaseId != "(default)") {
            FirebaseFirestore.getInstance(FirebaseApp.getInstance(), databaseId)
        } else {
            FirebaseFirestore.getInstance(FirebaseApp.getInstance())
        }
    }

    val currentUserId: String?
        get() = auth.currentUser?.uid

    // User profile observation
    fun observeUserProfile(userId: String): Flow<UserProfile?> = callbackFlow {
        val docRef = firestore.collection("users").document(userId)
        val listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("RoomSplitRepo", "Error observing user profile", error)
                trySend(null)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val profile = snapshot.toObject(UserProfile::class.java)
                trySend(profile)
            } else {
                trySend(null)
            }
        }
        awaitClose { listener.remove() }
    }

    suspend fun saveUserProfile(profile: UserProfile) {
        firestore.collection("users").document(profile.id).set(profile).await()
    }

    suspend fun updateUserActiveRoom(userId: String, roomId: String?) {
        firestore.collection("users").document(userId)
            .update("activeRoomId", roomId)
            .await()
    }

    suspend fun updateUserUpiId(userId: String, upiId: String) {
        firestore.collection("users").document(userId)
            .update("upiId", upiId)
            .await()
    }

    // Room observation
    fun observeRoom(roomId: String): Flow<Room?> = callbackFlow {
        val docRef = firestore.collection("rooms").document(roomId)
        val listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("RoomSplitRepo", "Error observing room", error)
                trySend(null)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val room = snapshot.toObject(Room::class.java)
                trySend(room)
            } else {
                trySend(null)
            }
        }
        awaitClose { listener.remove() }
    }

    // Members observation
    fun observeRoomMembers(roomId: String): Flow<List<RoomMember>> = callbackFlow {
        val colRef = firestore.collection("rooms").document(roomId).collection("members")
        val listener = colRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("RoomSplitRepo", "Error observing room members", error)
                trySend(emptyList())
                return@addSnapshotListener
            }
            val members = snapshot?.documents?.mapNotNull { it.toObject(RoomMember::class.java) } ?: emptyList()
            trySend(members)
        }
        awaitClose { listener.remove() }
    }

    // Entries observation
    fun observeRoomEntries(roomId: String): Flow<List<Entry>> = callbackFlow {
        val colRef = firestore.collection("rooms").document(roomId).collection("entries")
            .orderBy("createdAt", Query.Direction.DESCENDING)
        val listener = colRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("RoomSplitRepo", "Error observing entries", error)
                trySend(emptyList())
                return@addSnapshotListener
            }
            val entries = snapshot?.documents?.mapNotNull { it.toObject(Entry::class.java) } ?: emptyList()
            trySend(entries)
        }
        awaitClose { listener.remove() }
    }

    suspend fun createRoom(roomName: String, creatorUser: UserProfile): Room {
        val roomId = firestore.collection("rooms").document().id
        val inviteCode = generateInviteCode()
        val room = Room(
            id = roomId,
            name = roomName,
            inviteCode = inviteCode,
            createdBy = creatorUser.id,
            createdAt = System.currentTimeMillis()
        )
        firestore.collection("rooms").document(roomId).set(room).await()

        val member = RoomMember(
            userId = creatorUser.id,
            displayName = creatorUser.displayName,
            avatarColorHex = creatorUser.avatarColorHex,
            upiId = creatorUser.upiId,
            joinedAt = System.currentTimeMillis()
        )
        firestore.collection("rooms").document(roomId).collection("members").document(creatorUser.id).set(member).await()

        updateUserActiveRoom(creatorUser.id, roomId)
        return room
    }

    suspend fun joinRoomByInviteCode(inviteCode: String, user: UserProfile): Room {
        val querySnapshot = firestore.collection("rooms")
            .whereEqualTo("inviteCode", inviteCode.trim().uppercase())
            .limit(1)
            .get()
            .await()

        if (querySnapshot.isEmpty) {
            throw IllegalArgumentException("Invalid invite code. No room found with code $inviteCode")
        }

        val roomDoc = querySnapshot.documents.first()
        val room = roomDoc.toObject(Room::class.java) ?: throw IllegalStateException("Could not parse room")

        val member = RoomMember(
            userId = user.id,
            displayName = user.displayName,
            avatarColorHex = user.avatarColorHex,
            upiId = user.upiId,
            joinedAt = System.currentTimeMillis()
        )
        firestore.collection("rooms").document(room.id).collection("members").document(user.id).set(member).await()
        updateUserActiveRoom(user.id, room.id)
        return room
    }

    suspend fun addEntry(roomId: String, entry: Entry) {
        val docRef = if (entry.id.isNotEmpty()) {
            firestore.collection("rooms").document(roomId).collection("entries").document(entry.id)
        } else {
            firestore.collection("rooms").document(roomId).collection("entries").document()
        }
        val finalEntry = entry.copy(id = docRef.id)
        docRef.set(finalEntry).await()
    }

    suspend fun deleteEntry(roomId: String, entryId: String) {
        firestore.collection("rooms").document(roomId).collection("entries").document(entryId).delete().await()
    }

    suspend fun updateMemberUpi(roomId: String, userId: String, upiId: String) {
        firestore.collection("rooms").document(roomId).collection("members").document(userId)
            .update("upiId", upiId)
            .await()
    }

    private fun generateInviteCode(): String {
        val allowedChars = ('A'..'Z') + ('0'..'9')
        return (1..6)
            .map { allowedChars.random() }
            .joinToString("")
    }
}
