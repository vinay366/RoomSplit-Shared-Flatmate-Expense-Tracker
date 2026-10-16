package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EntryWithMember
import com.example.data.model.ExpenseCategory
import com.example.ui.theme.NegativeDebtRed
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

@Composable
fun EntryCard(
  item: EntryWithMember,
  isOwnEntry: Boolean,
  onDelete: () -> Unit,
  modifier: Modifier = Modifier
) {
  val category = ExpenseCategory.fromId(item.entry.category)
  val relativeDate = formatRelativeDate(item.entry.date)
  val formattedAmount = String.format(Locale.US, "₹%.2f", item.entry.amount)
  var showDeleteDialog by remember { mutableStateOf(false) }

  if (showDeleteDialog) {
    AlertDialog(
      onDismissRequest = { showDeleteDialog = false },
      title = {
        Text("Delete Expense?", fontWeight = FontWeight.Bold)
      },
      text = {
        Text("Are you sure you want to delete '${item.entry.itemName}' ($formattedAmount)? This will update flat balances in real time.")
      },
      confirmButton = {
        TextButton(
          onClick = {
            showDeleteDialog = false
            onDelete()
          }
        ) {
          Text("Delete", color = NegativeDebtRed, fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showDeleteDialog = false }) {
          Text("Cancel")
        }
      },
      shape = RoundedCornerShape(20.dp)
    )
  }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("entry_card_${item.entry.id}"),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    border = BorderStroke(
      width = 1.dp,
      color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalAlignment = Alignment.Top
    ) {
      // Member Avatar with Gradient
      val avatarBase = item.member.avatarColor
      val avatarBrush = remember(avatarBase) {
        Brush.linearGradient(
          listOf(avatarBase, avatarBase.copy(alpha = 0.8f))
        )
      }

      Box(
        modifier = Modifier
          .size(46.dp)
          .clip(CircleShape)
          .background(avatarBrush),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = item.member.initials,
          color = Color.White,
          fontWeight = FontWeight.Bold,
          fontSize = 15.sp
        )
      }

      Spacer(modifier = Modifier.width(14.dp))

      // Center Column: Title, Category Pill, Buyer, Date
      Column(modifier = Modifier.weight(1f)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.Top
        ) {
          Text(
            text = item.entry.itemName,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
          )

          Spacer(modifier = Modifier.width(10.dp))

          // Large Bold Currency Amount
          Text(
            text = formattedAmount,
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary,
              letterSpacing = (-0.3).sp
            )
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Category Tag & Member Name
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth()
        ) {
          // Category pill
          Surface(
            color = category.bgColor,
            shape = RoundedCornerShape(8.dp)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(text = category.emoji, fontSize = 12.sp)
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = category.displayName,
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = category.color
                )
              )
            }
          }

          Spacer(modifier = Modifier.width(8.dp))

          Text(
            text = "• ${item.member.name} • $relativeDate",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }

        // Optional Note container
        if (item.entry.note.isNotBlank()) {
          Spacer(modifier = Modifier.height(8.dp))
          Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(3.dp, 16.dp)
                  .clip(RoundedCornerShape(2.dp))
                  .background(MaterialTheme.colorScheme.primary)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = item.entry.note,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
              )
            }
          }
        }
      }

      // Delete action button
      if (isOwnEntry) {
        IconButton(
          onClick = { showDeleteDialog = true },
          modifier = Modifier
            .size(32.dp)
            .padding(start = 4.dp)
            .testTag("delete_entry_${item.entry.id}")
        ) {
          Icon(
            imageVector = Icons.Outlined.DeleteOutline,
            contentDescription = "Delete entry",
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.size(19.dp)
          )
        }
      }
    }
  }
}

private fun formatRelativeDate(dateString: String): String {
  return try {
    val date = LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    val today = LocalDate.now()
    val daysBetween = ChronoUnit.DAYS.between(date, today)
    when {
      daysBetween == 0L -> "Today"
      daysBetween == 1L -> "Yesterday"
      daysBetween in 2L..6L -> "$daysBetween days ago"
      else -> date.format(DateTimeFormatter.ofPattern("d MMM"))
    }
  } catch (_: Exception) {
    dateString
  }
}
