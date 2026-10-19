package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.ExpenseCategory

@Composable
fun CategoryChip(
    category: ExpenseCategory,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) category.color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
        label = "chip_bg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) category.color else Color.Transparent,
        label = "chip_border"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) category.color else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "chip_content"
    )

    Row(
        modifier = modifier
            .testTag("category_chip_${category.id}")
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .background(backgroundColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = category.color),
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = category.icon,
            contentDescription = category.displayName,
            tint = contentColor,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = category.displayName,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor
        )
    }
}
