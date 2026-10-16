package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Primary Emerald Palette (Sophisticated & Crisp)
val EmeraldPrimary = Color(0xFF059669)
val EmeraldLight = Color(0xFF10B981)
val EmeraldDark = Color(0xFF047857)
val EmeraldContainer = Color(0xFFD1FAE5)
val OnEmeraldContainer = Color(0xFF064E3B)
val EmeraldGlow = Color(0x3310B981)

// Neutral & Slate Surfaces
val SlateDark = Color(0xFF0B132B)
val SlateBackgroundDark = Color(0xFF0F172A)
val SlateCardDark = Color(0xFF1E293B)
val SlateSurfaceVariantDark = Color(0xFF334155)
val SlateBorderDark = Color(0xFF334155)

val SlateLightBg = Color(0xFFF8FAFC)
val SlateCardLight = Color(0xFFFFFFFF)
val SlateSurfaceVariantLight = Color(0xFFF1F5F9)
val SlateBorderLight = Color(0xFFE2E8F0)
val SlateMuted = Color(0xFF64748B)

// Financial Semantic Tokens
val PositiveGreen = Color(0xFF16A34A)
val PositiveGreenLight = Color(0xFF22C55E)
val PositiveGreenBg = Color(0xFFDCFCE7)
val PositiveGreenBorder = Color(0xFF86EFAC)

val NegativeDebtRed = Color(0xFFE11D48)
val NegativeDebtLight = Color(0xFFF43F5E)
val NegativeDebtBg = Color(0xFFFFE4E6)
val NegativeDebtBorder = Color(0xFFFDA4AF)

// 5 Fixed Categories from PRD
val CategoryVegetables = Color(0xFF16A34A)
val CategoryVegetablesBg = Color(0xFFDCFCE7)
val CategoryVegetablesBorder = Color(0xFF86EFAC)

val CategoryMilk = Color(0xFF0284C7)
val CategoryMilkBg = Color(0xFFE0F2FE)
val CategoryMilkBorder = Color(0xFF7DD3FC)

val CategoryGas = Color(0xFFEA580C)
val CategoryGasBg = Color(0xFFFFEDD5)
val CategoryGasBorder = Color(0xFFFDBA74)

val CategoryRent = Color(0xFF7C3AED)
val CategoryRentBg = Color(0xFFEDE9FE)
val CategoryRentBorder = Color(0xFFC4B5FD)

val CategoryMisc = Color(0xFFD97706)
val CategoryMiscBg = Color(0xFFFEF3C7)
val CategoryMiscBorder = Color(0xFFFCD34D)

// Curated Vibrant Member Avatar Palette
val AvatarColors = listOf(
  Color(0xFF059669), // Emerald
  Color(0xFF2563EB), // Royal Blue
  Color(0xFFD97706), // Amber
  Color(0xFF7C3AED), // Violet
  Color(0xFFDB2777), // Rose Pink
  Color(0xFF0D9488), // Teal
  Color(0xFF4F46E5), // Indigo
  Color(0xFFEA580C)  // Tangerine
)

// Gradients for Hero surfaces
val EmeraldHeroGradient = Brush.linearGradient(
  listOf(Color(0xFF059669), Color(0xFF0D9488), Color(0xFF047857))
)

val DarkHeroGradient = Brush.linearGradient(
  listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF064E3B))
)
