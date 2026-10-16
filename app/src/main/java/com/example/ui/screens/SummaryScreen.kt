package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CategorySpend
import com.example.data.model.MemberSpend
import com.example.data.model.MonthlySummary
import com.example.data.model.Room
import com.example.ui.theme.EmeraldPrimary
import java.util.Locale

@Composable
fun SummaryScreen(
  summary: MonthlySummary?,
  room: Room?,
  selectedMonthKey: String,
  availableMonths: List<Pair<String, String>>,
  onMonthSelected: (String, String) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current

  fun shareToWhatsApp() {
    if (summary == null) return
    val roomName = room?.name ?: "Flat"
    val code = room?.inviteCode ?: ""
    val textBuilder = StringBuilder()
    textBuilder.append("📊 *RoomSplit Monthly Summary • $roomName* (${summary.monthDisplayName})\n")
    textBuilder.append("━━━━━━━━━━━━━━━━━━━\n")
    textBuilder.append("💰 *Total Spend:* ₹${String.format(Locale.US, "%.2f", summary.totalSpend)}\n")
    textBuilder.append("👥 *Average / Person:* ₹${String.format(Locale.US, "%.2f", summary.averagePerPerson)}\n\n")

    textBuilder.append("🏷️ *Category Breakdown:*\n")
    summary.categoryBreakdowns.forEach { cat ->
      if (cat.amount > 0) {
        textBuilder.append("${cat.category.emoji} ${cat.category.displayName}: ₹${String.format(Locale.US, "%.2f", cat.amount)} (${String.format(Locale.US, "%.1f", cat.percentage)}%)\n")
      }
    }

    textBuilder.append("\n👤 *Who Paid What:*\n")
    summary.memberBreakdowns.forEach { mem ->
      textBuilder.append("• ${mem.user.name}: ₹${String.format(Locale.US, "%.2f", mem.amount)} (${String.format(Locale.US, "%.1f", mem.percentage)}%)\n")
    }

    textBuilder.append("━━━━━━━━━━━━━━━━━━━\n")
    textBuilder.append("Shared via RoomSplit ($code)")

    val shareText = textBuilder.toString()

    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("RoomSplit Summary", shareText))
    Toast.makeText(context, "Summary copied to clipboard!", Toast.LENGTH_SHORT).show()

    val intent = Intent(Intent.ACTION_SEND).apply {
      type = "text/plain"
      putExtra(Intent.EXTRA_TEXT, shareText)
    }
    context.startActivity(Intent.createChooser(intent, "Share RoomSplit Summary"))
  }

  LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Month Selector Pill Bar
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        availableMonths.forEach { (key, name) ->
          val isSelected = key == selectedMonthKey
          FilterChip(
            selected = isSelected,
            onClick = { onMonthSelected(key, name) },
            label = { Text(name) },
            shape = RoundedCornerShape(16.dp),
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = EmeraldPrimary,
              selectedLabelColor = Color.White
            ),
            modifier = Modifier.testTag("summary_month_filter_$key")
          )
        }
      }
    }

    if (summary == null || summary.totalSpend == 0.0) {
      item {
        Card(
          shape = RoundedCornerShape(22.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Box(
              modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.PieChart,
                contentDescription = null,
                tint = EmeraldPrimary,
                modifier = Modifier.size(36.dp)
              )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
              text = "No expenses in ${summary?.monthDisplayName ?: "this month"}",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Log flat expenses to see visual category breakdowns and roommate contribution shares.",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
          }
        }
      }
    } else {
      // Summary Overview Card
      item {
        Card(
          shape = RoundedCornerShape(22.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("summary_overview_card")
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(20.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "${summary.monthDisplayName} Spend",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              // Top Buyer badge
              val topSpender = summary.memberBreakdowns.firstOrNull()
              if (topSpender != null && topSpender.amount > 0) {
                Surface(
                  color = MaterialTheme.colorScheme.primaryContainer,
                  shape = RoundedCornerShape(8.dp)
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Icon(
                      imageVector = Icons.Default.EmojiEvents,
                      contentDescription = null,
                      tint = EmeraldPrimary,
                      modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                      text = "Top Buyer: ${topSpender.user.name}",
                      style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary
                      )
                    )
                  }
                }
              }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
              text = String.format(Locale.US, "₹%.2f", summary.totalSpend),
              style = MaterialTheme.typography.displayLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = (-0.5).sp
              )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Share Summary Button
            Button(
              onClick = { shareToWhatsApp() },
              shape = RoundedCornerShape(14.dp),
              colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("share_whatsapp_button")
            ) {
              Icon(
                imageVector = Icons.Default.Share,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Share to Room WhatsApp Group",
                style = MaterialTheme.typography.labelLarge.copy(
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
              )
            }
          }
        }
      }

      // Category Breakdown Card (Horizontal Bar Charts)
      item {
        Card(
          shape = RoundedCornerShape(22.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("category_breakdown_card")
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(20.dp)
          ) {
            Text(
              text = "Category Breakdown",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
              text = "5 fixed flatmate expense categories",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            summary.categoryBreakdowns.forEach { catSpend ->
              CategoryProgressBar(catSpend = catSpend)
              Spacer(modifier = Modifier.height(12.dp))
            }
          }
        }
      }

      // Per-Person Breakdown Card (Horizontal Bar Charts)
      item {
        Card(
          shape = RoundedCornerShape(22.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("member_breakdown_card")
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(20.dp)
          ) {
            Text(
              text = "Flatmate Contributions",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
              text = "Who contributed what towards the total spend",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            summary.memberBreakdowns.forEachIndexed { index, memSpend ->
              MemberProgressBar(rank = index + 1, memSpend = memSpend)
              Spacer(modifier = Modifier.height(12.dp))
            }
          }
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(28.dp))
    }
  }
}

@Composable
fun CategoryProgressBar(catSpend: CategorySpend) {
  val animPct by animateFloatAsState(
    targetValue = catSpend.percentage / 100f,
    animationSpec = tween(durationMillis = 600),
    label = "cat_progress"
  )

  Column(modifier = Modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = catSpend.category.emoji, fontSize = 16.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = catSpend.category.displayName,
          style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
        )
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = String.format(Locale.US, "₹%.2f", catSpend.amount),
          style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Surface(
          color = catSpend.category.bgColor,
          shape = RoundedCornerShape(6.dp)
        ) {
          Text(
            text = "${String.format(Locale.US, "%.1f", catSpend.percentage)}%",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              color = catSpend.category.color
            ),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Track & Fill Bar
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(10.dp)
        .clip(RoundedCornerShape(5.dp))
        .background(catSpend.category.bgColor)
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth(fraction = animPct.coerceIn(0f, 1f))
          .fillMaxHeight()
          .clip(RoundedCornerShape(5.dp))
          .background(catSpend.category.color)
      )
    }
  }
}

@Composable
fun MemberProgressBar(rank: Int, memSpend: MemberSpend) {
  val animPct by animateFloatAsState(
    targetValue = memSpend.percentage / 100f,
    animationSpec = tween(durationMillis = 600),
    label = "mem_progress"
  )

  Column(modifier = Modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        val medal = when (rank) {
          1 -> "🥇"
          2 -> "🥈"
          3 -> "🥉"
          else -> "#$rank"
        }
        Text(text = medal, fontSize = 13.sp)
        Spacer(modifier = Modifier.width(6.dp))

        Box(
          modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(memSpend.user.avatarColor),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = memSpend.user.initials.take(1),
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
          )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = memSpend.user.name,
          style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
        )
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = String.format(Locale.US, "₹%.2f", memSpend.amount),
          style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Surface(
          color = MaterialTheme.colorScheme.surfaceVariant,
          shape = RoundedCornerShape(6.dp)
        ) {
          Text(
            text = "${String.format(Locale.US, "%.1f", memSpend.percentage)}%",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(10.dp)
        .clip(RoundedCornerShape(5.dp))
        .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth(fraction = animPct.coerceIn(0f, 1f))
          .fillMaxHeight()
          .clip(RoundedCornerShape(5.dp))
          .background(memSpend.user.avatarColor)
      )
    }
  }
}
