package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.CategoryGas
import com.example.ui.theme.CategoryGasBg
import com.example.ui.theme.CategoryMilk
import com.example.ui.theme.CategoryMilkBg
import com.example.ui.theme.CategoryMisc
import com.example.ui.theme.CategoryMiscBg
import com.example.ui.theme.CategoryRent
import com.example.ui.theme.CategoryRentBg
import com.example.ui.theme.CategoryVegetables
import com.example.ui.theme.CategoryVegetablesBg

enum class ExpenseCategory(
  val id: String,
  val displayName: String,
  val emoji: String,
  val color: Color,
  val bgColor: Color,
  val sampleSuggestions: List<String>
) {
  VEGETABLES(
    id = "vegetables",
    displayName = "Vegetables",
    emoji = "🥦",
    color = CategoryVegetables,
    bgColor = CategoryVegetablesBg,
    sampleSuggestions = listOf("Tomatoes & Onions", "Potatoes & Chillies", "Fresh Coriander & Mint", "Sabzi Mandi basket")
  ),
  MILK(
    id = "milk",
    displayName = "Milk",
    emoji = "🥛",
    color = CategoryMilk,
    bgColor = CategoryMilkBg,
    sampleSuggestions = listOf("Amul Gold 1L", "Mother Dairy 2L", "Curd & Paneer", "Oat Milk carton")
  ),
  GAS(
    id = "gas",
    displayName = "Gas",
    emoji = "🔥",
    color = CategoryGas,
    bgColor = CategoryGasBg,
    sampleSuggestions = listOf("Indane cylinder refill", "HP Gas booking", "Piped gas bill", "Lighter & regulator")
  ),
  RENT(
    id = "rent",
    displayName = "Rent",
    emoji = "🏠",
    color = CategoryRent,
    bgColor = CategoryRentBg,
    sampleSuggestions = listOf("Monthly Flat Rent", "Maintenance & Society charges", "Security advance", "Parking slot")
  ),
  MISC(
    id = "misc",
    displayName = "Misc",
    emoji = "🛒",
    color = CategoryMisc,
    bgColor = CategoryMiscBg,
    sampleSuggestions = listOf("Wi-Fi broadband bill", "House cleaning & Maid", "Garbage disposal", "Spices, Oil & Atta")
  );

  companion object {
    fun fromId(id: String): ExpenseCategory {
      return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: MISC
    }
  }
}
