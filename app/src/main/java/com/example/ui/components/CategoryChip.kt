package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExpenseCategory

@Composable
fun CategoryChip(
  category: ExpenseCategory,
  isSelected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val scale by animateFloatAsState(
    targetValue = if (isSelected) 1.04f else 1.0f,
    animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
    label = "chip_scale"
  )

  val backgroundColor by animateColorAsState(
    targetValue = if (isSelected) category.color else category.bgColor.copy(alpha = 0.65f),
    label = "chip_bg"
  )

  val textColor by animateColorAsState(
    targetValue = if (isSelected) Color.White else category.color,
    label = "chip_text"
  )

  val borderColor by animateColorAsState(
    targetValue = if (isSelected) category.color else category.color.copy(alpha = 0.25f),
    label = "chip_border"
  )

  Row(
    modifier = modifier
      .scale(scale)
      .shadow(
        elevation = if (isSelected) 4.dp else 0.dp,
        shape = RoundedCornerShape(20.dp),
        ambientColor = category.color,
        spotColor = category.color
      )
      .clip(RoundedCornerShape(20.dp))
      .background(backgroundColor)
      .border(
        width = if (isSelected) 1.5.dp else 1.dp,
        color = borderColor,
        shape = RoundedCornerShape(20.dp)
      )
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = ripple(bounded = true, color = category.color),
        onClick = onClick
      )
      .padding(horizontal = 14.dp, vertical = 8.dp)
      .testTag("category_chip_${category.id}"),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = category.emoji,
      fontSize = 15.sp
    )
    Spacer(modifier = Modifier.width(6.dp))
    Text(
      text = category.displayName,
      color = textColor,
      style = MaterialTheme.typography.labelMedium.copy(
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
      )
    )
  }
}
