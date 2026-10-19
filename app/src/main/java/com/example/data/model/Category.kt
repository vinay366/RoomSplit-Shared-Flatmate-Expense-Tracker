package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class ExpenseCategory(
    val id: String,
    val displayName: String,
    val icon: ImageVector,
    val color: Color
) {
    GROCERIES(
        id = "Groceries",
        displayName = "Groceries",
        icon = Icons.Default.ShoppingCart,
        color = Color(0xFF10B981) // Emerald Green
    ),
    UTILITIES(
        id = "WiFi/Electricity",
        displayName = "WiFi / Power",
        icon = Icons.Default.ElectricBolt,
        color = Color(0xFFF59E0B) // Amber
    ),
    MAID_COOK(
        id = "Maid/Cook",
        displayName = "Maid / Cook",
        icon = Icons.Default.Person,
        color = Color(0xFF8B5CF6) // Purple
    ),
    CLEANING(
        id = "Cleaning",
        displayName = "Cleaning Supplies",
        icon = Icons.Default.CleaningServices,
        color = Color(0xFF06B6D4) // Cyan
    ),
    OTHER(
        id = "Other",
        displayName = "Other Essentials",
        icon = Icons.Default.Inventory2,
        color = Color(0xFF64748B) // Slate
    );

    companion object {
        fun fromId(id: String?): ExpenseCategory {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) || it.name.equals(id, ignoreCase = true) }
                ?: OTHER
        }
    }
}
