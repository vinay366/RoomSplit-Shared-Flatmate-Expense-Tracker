package com.example

import com.example.data.model.DebtTransfer
import com.example.data.model.ExpenseCategory
import com.example.data.model.RoomMember
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.min

class RoomSplitUnitTest {

    @Test
    fun testCategoryResolution() {
        assertEquals(ExpenseCategory.GROCERIES, ExpenseCategory.fromId("Groceries"))
        assertEquals(ExpenseCategory.UTILITIES, ExpenseCategory.fromId("WiFi/Electricity"))
        assertEquals(ExpenseCategory.MAID_COOK, ExpenseCategory.fromId("Maid/Cook"))
        assertEquals(ExpenseCategory.CLEANING, ExpenseCategory.fromId("Cleaning"))
        assertEquals(ExpenseCategory.OTHER, ExpenseCategory.fromId("Random"))
    }

    @Test
    fun testBilateralDebtCalculation() {
        val memberA = RoomMember(userId = "A", displayName = "Alice")
        val memberB = RoomMember(userId = "B", displayName = "Bob")
        val memberC = RoomMember(userId = "C", displayName = "Charlie")

        // Alice paid 300, Bob paid 0, Charlie paid 0
        // Total = 300, Share = 100 each
        // Alice net = +200, Bob net = -100, Charlie net = -100
        val netMap = mapOf(
            memberA to 200.0,
            memberB to -100.0,
            memberC to -100.0
        )

        val creditors = netMap.filter { it.value > 0.01 }.map { it.key to it.value }.toMutableList()
        val debtors = netMap.filter { it.value < -0.01 }.map { it.key to -it.value }.toMutableList()

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

        assertEquals(2, transfers.size)
        assertEquals("B", transfers[0].debtor.userId)
        assertEquals("A", transfers[0].creditor.userId)
        assertEquals(100.0, transfers[0].amount, 0.001)

        assertEquals("C", transfers[1].debtor.userId)
        assertEquals("A", transfers[1].creditor.userId)
        assertEquals(100.0, transfers[1].amount, 0.001)
    }
}
