package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.Entry
import com.example.data.model.Room
import com.example.data.model.RoomMembership
import com.example.data.model.User

@Entity(tableName = "users")
data class UserEntity(
  @PrimaryKey val id: String,
  val phone: String,
  val name: String,
  val avatarColorHex: String,
  val upiId: String,
  val notifEnabled: Boolean,
  val createdAt: Long
) {
  fun toDomain(): User = User(
    id = id,
    phone = phone,
    name = name,
    avatarColorHex = avatarColorHex,
    upiId = upiId,
    notifEnabled = notifEnabled,
    createdAt = createdAt
  )

  companion object {
    fun fromDomain(user: User): UserEntity = UserEntity(
      id = user.id,
      phone = user.phone,
      name = user.name,
      avatarColorHex = user.avatarColorHex,
      upiId = user.upiId,
      notifEnabled = user.notifEnabled,
      createdAt = user.createdAt
    )
  }
}

@Entity(
  tableName = "rooms",
  indices = [Index(value = ["inviteCode"], unique = true)]
)
data class RoomEntity(
  @PrimaryKey val id: String,
  val name: String,
  val inviteCode: String,
  val emoji: String,
  val createdBy: String,
  val createdAt: Long
) {
  fun toDomain(): Room = Room(
    id = id,
    name = name,
    inviteCode = inviteCode,
    emoji = emoji,
    createdBy = createdBy,
    createdAt = createdAt
  )

  companion object {
    fun fromDomain(room: Room): RoomEntity = RoomEntity(
      id = room.id,
      name = room.name,
      inviteCode = room.inviteCode,
      emoji = room.emoji,
      createdBy = room.createdBy,
      createdAt = room.createdAt
    )
  }
}

@Entity(
  tableName = "memberships",
  indices = [Index(value = ["roomId", "userId"], unique = true)]
)
data class MembershipEntity(
  @PrimaryKey val id: String,
  val roomId: String,
  val userId: String,
  val joinedAt: Long,
  val isActive: Boolean
) {
  fun toDomain(): RoomMembership = RoomMembership(
    id = id,
    roomId = roomId,
    userId = userId,
    joinedAt = joinedAt,
    isActive = isActive
  )

  companion object {
    fun fromDomain(m: RoomMembership): MembershipEntity = MembershipEntity(
      id = m.id,
      roomId = m.roomId,
      userId = m.userId,
      joinedAt = m.joinedAt,
      isActive = m.isActive
    )
  }
}

@Entity(
  tableName = "entries",
  indices = [Index(value = ["roomId"]), Index(value = ["date"])]
)
data class EntryEntity(
  @PrimaryKey val id: String,
  val roomId: String,
  val memberId: String,
  val itemName: String,
  val category: String,
  val amount: Double,
  val date: String,
  val note: String,
  val createdAt: Long
) {
  fun toDomain(): Entry = Entry(
    id = id,
    roomId = roomId,
    memberId = memberId,
    itemName = itemName,
    category = category,
    amount = amount,
    date = date,
    note = note,
    createdAt = createdAt
  )

  companion object {
    fun fromDomain(entry: Entry): EntryEntity = EntryEntity(
      id = entry.id,
      roomId = entry.roomId,
      memberId = entry.memberId,
      itemName = entry.itemName,
      category = entry.category,
      amount = entry.amount,
      date = entry.date,
      note = entry.note,
      createdAt = entry.createdAt
    )
  }
}
