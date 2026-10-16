package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RoomSplitDao {
  // Users
  @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
  suspend fun getUserById(userId: String): UserEntity?

  @Query("SELECT * FROM users WHERE phone = :phone LIMIT 1")
  suspend fun getUserByPhone(phone: String): UserEntity?

  @Query("SELECT * FROM users")
  fun getAllUsersFlow(): Flow<List<UserEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertUser(user: UserEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertUsers(users: List<UserEntity>)

  @Update
  suspend fun updateUser(user: UserEntity)

  // Rooms
  @Query("SELECT * FROM rooms WHERE id = :roomId LIMIT 1")
  suspend fun getRoomById(roomId: String): RoomEntity?

  @Query("SELECT * FROM rooms WHERE id = :roomId LIMIT 1")
  fun getRoomFlow(roomId: String): Flow<RoomEntity?>

  @Query("SELECT * FROM rooms WHERE inviteCode = :inviteCode LIMIT 1")
  suspend fun getRoomByInviteCode(inviteCode: String): RoomEntity?

  @Query("SELECT * FROM rooms")
  fun getAllRoomsFlow(): Flow<List<RoomEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertRoom(room: RoomEntity)

  @Update
  suspend fun updateRoom(room: RoomEntity)

  // Memberships
  @Query("SELECT * FROM memberships WHERE roomId = :roomId AND isActive = 1")
  fun getActiveMembershipsFlow(roomId: String): Flow<List<MembershipEntity>>

  @Query("SELECT * FROM memberships WHERE roomId = :roomId AND isActive = 1")
  suspend fun getActiveMemberships(roomId: String): List<MembershipEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMembership(membership: MembershipEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMemberships(memberships: List<MembershipEntity>)

  // Entries
  @Query("SELECT * FROM entries WHERE roomId = :roomId ORDER BY date DESC, createdAt DESC")
  fun getEntriesForRoomFlow(roomId: String): Flow<List<EntryEntity>>

  @Query("SELECT * FROM entries WHERE roomId = :roomId AND date LIKE :monthPrefix || '%' ORDER BY date DESC, createdAt DESC")
  fun getEntriesForRoomAndMonthFlow(roomId: String, monthPrefix: String): Flow<List<EntryEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertEntry(entry: EntryEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertEntries(entries: List<EntryEntity>)

  @Query("DELETE FROM entries WHERE id = :entryId")
  suspend fun deleteEntry(entryId: String)

  @Query("SELECT COUNT(*) FROM rooms")
  suspend fun getRoomCount(): Int
}
