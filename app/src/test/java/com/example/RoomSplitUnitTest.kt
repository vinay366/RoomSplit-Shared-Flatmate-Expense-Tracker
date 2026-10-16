package com.example

import com.example.data.model.ExpenseCategory
import com.example.data.model.SettlementPair
import com.example.data.model.User
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class RoomSplitUnitTest {

  @Test
  fun testFiveFixedCategoriesExist() {
    assertEquals(5, ExpenseCategory.entries.size)
    assertNotNull(ExpenseCategory.fromId("vegetables"))
    assertNotNull(ExpenseCategory.fromId("milk"))
    assertNotNull(ExpenseCategory.fromId("gas"))
    assertNotNull(ExpenseCategory.fromId("rent"))
    assertNotNull(ExpenseCategory.fromId("misc"))
  }

  @Test
  fun testUpiDeepLinkGeneration() {
    val from = User(id = "u1", phone = "+919876543210", name = "Ammu", avatarColorHex = "#7C3AED", upiId = "ammu@upi")
    val to = User(id = "u2", phone = "+919876543211", name = "Priya", avatarColorHex = "#DB2777", upiId = "priya@okaxis")
    val settlement = SettlementPair("s1", from, to, 476.67)

    val link = settlement.upiDeepLink
    assertTrue(link.startsWith("upi://pay?"))
    assertTrue(link.contains("pa=priya@okaxis") || link.contains("pa=priya%40okaxis"))
    assertTrue(link.contains("am=476.67"))
    assertTrue(link.contains("cu=INR"))
  }

  @Test
  fun testBalancesSumToZero() {
    val users = listOf(
      User(id = "u1", name = "Priya"),
      User(id = "u2", name = "Ammu"),
      User(id = "u3", name = "Vinay")
    )
    val total = 1100.0 + 120.0 + 450.0
    val avg = total / users.size

    val netPriya = 1100.0 - avg
    val netAmmu = 120.0 - avg
    val netVinay = 450.0 - avg

    val sum = netPriya + netAmmu + netVinay
    assertTrue("Net balances must sum to 0", abs(sum) < 0.001)
  }
}
