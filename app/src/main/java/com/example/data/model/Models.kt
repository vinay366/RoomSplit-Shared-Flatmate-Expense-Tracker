package com.example.data.model

data class UserProfile(
    val id: String = "",
    val displayName: String = "",
    val email: String = "",
    val activeRoomId: String? = null,
    val avatarColorHex: String = "#10B981",
    val upiId: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

data class Room(
    val id: String = "",
    val name: String = "",
    val inviteCode: String = "",
    val createdBy: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class RoomMember(
    val userId: String = "",
    val displayName: String = "",
    val avatarColorHex: String = "#10B981",
    val upiId: String? = null,
    val joinedAt: Long = System.currentTimeMillis()
)

data class Entry(
    val id: String = "",
    val amount: Double = 0.0,
    val category: String = ExpenseCategory.GROCERIES.id,
    val itemName: String = "",
    val paidByMemberId: String = "",
    val paidByMemberName: String = "",
    val paidByMemberAvatarColorHex: String = "#10B981",
    val date: String = "",
    val note: String? = null,
    val isSettlement: Boolean = false,
    val settlementReceiverId: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

data class MemberBalance(
    val member: RoomMember,
    val totalPaid: Double,
    val fairShare: Double,
    val netBalance: Double // > 0 means gets back (+), < 0 means owes (-)
)

data class DebtTransfer(
    val debtor: RoomMember,
    val creditor: RoomMember,
    val amount: Double
)

data class CategorySummary(
    val category: ExpenseCategory,
    val totalAmount: Double,
    val count: Int,
    val percentage: Float
)
