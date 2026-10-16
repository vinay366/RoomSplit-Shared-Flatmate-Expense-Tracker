package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.google.firebase.firestore.IgnoreExtraProperties
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@IgnoreExtraProperties
data class User(
  val id: String = "",
  val phone: String = "",
  val name: String = "",
  val email: String = "",
  val avatarColorHex: String = "#059669",
  val upiId: String = "",
  val notifEnabled: Boolean = true,
  val createdAt: Long = System.currentTimeMillis()
) {
  val avatarColor: Color
    get() = try {
      Color(android.graphics.Color.parseColor(avatarColorHex))
    } catch (_: Exception) {
      Color(0xFF059669)
    }

  val initials: String
    get() {
      val trimmed = name.trim()
      if (trimmed.isBlank()) return "U"
      val parts = trimmed.split(" ")
      return if (parts.size >= 2) {
        "${parts[0].take(1)}${parts[1].take(1)}".uppercase()
      } else {
        trimmed.take(2).uppercase()
      }
    }
}

@IgnoreExtraProperties
data class Room(
  val id: String = "",
  val name: String = "",
  val inviteCode: String = "",
  val emoji: String = "🏠",
  val createdBy: String = "",
  val createdAt: Long = System.currentTimeMillis()
)

@IgnoreExtraProperties
data class RoomMembership(
  val id: String = "",
  val roomId: String = "",
  val userId: String = "",
  val name: String = "",
  val avatarColorHex: String = "#059669",
  val upiId: String = "",
  val joinedAt: Long = System.currentTimeMillis(),
  val isActive: Boolean = true
) {
  fun toUser(): User = User(
    id = userId,
    name = name,
    avatarColorHex = avatarColorHex,
    upiId = upiId
  )
}

@IgnoreExtraProperties
data class Entry(
  val id: String = "",
  val roomId: String = "",
  val memberId: String = "",
  val memberName: String = "",
  val memberAvatarColorHex: String = "#059669",
  val itemName: String = "",
  val category: String = "misc",
  val amount: Double = 0.0,
  val date: String = "",
  val note: String = "",
  val createdAt: Long = System.currentTimeMillis()
) {
  fun toEntryWithMember(): EntryWithMember {
    val user = User(
      id = memberId,
      name = memberName.ifBlank { "Roommate" },
      avatarColorHex = memberAvatarColorHex
    )
    return EntryWithMember(entry = this, member = user)
  }
}

data class EntryWithMember(
  val entry: Entry = Entry(),
  val member: User = User()
)

data class MemberBalance(
  val user: User,
  val totalPaid: Double,
  val netBalance: Double
)

data class SettlementPair(
  val id: String,
  val fromUser: User,
  val toUser: User,
  val amount: Double
) {
  val upiDeepLink: String
    get() {
      val pa = if (toUser.upiId.isNotBlank()) toUser.upiId else "${toUser.name.lowercase().replace(" ", "")}@upi"
      val pn = URLEncoder.encode(toUser.name, StandardCharsets.UTF_8.toString())
      val am = String.format(java.util.Locale.US, "%.2f", amount)
      val tn = URLEncoder.encode("RoomSplit settlement to ${toUser.name}", StandardCharsets.UTF_8.toString())
      return "upi://pay?pa=$pa&pn=$pn&am=$am&cu=INR&tn=$tn"
    }
}

data class CategorySpend(
  val category: ExpenseCategory,
  val amount: Double,
  val percentage: Float
)

data class MemberSpend(
  val user: User,
  val amount: Double,
  val percentage: Float
)

data class MonthlySummary(
  val monthKey: String,
  val monthDisplayName: String,
  val totalSpend: Double,
  val memberCount: Int,
  val averagePerPerson: Double,
  val categoryBreakdowns: List<CategorySpend>,
  val memberBreakdowns: List<MemberSpend>
)
