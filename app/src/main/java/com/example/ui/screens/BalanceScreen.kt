package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Payment
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MemberBalance
import com.example.data.model.SettlementPair
import com.example.data.model.User
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.NegativeDebtBg
import com.example.ui.theme.NegativeDebtBorder
import com.example.ui.theme.NegativeDebtRed
import com.example.ui.theme.PositiveGreen
import com.example.ui.theme.PositiveGreenBg
import com.example.ui.theme.PositiveGreenBorder
import java.util.Locale
import kotlin.math.abs

@Composable
fun BalanceScreen(
  currentUser: User?,
  totalSpend: Double,
  memberBalances: List<MemberBalance>,
  settlements: List<SettlementPair>,
  selectedMonthKey: String,
  availableMonths: List<Pair<String, String>>,
  onMonthSelected: (String, String) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val memberCount = if (memberBalances.isNotEmpty()) memberBalances.size else 1
  val average = totalSpend / memberCount

  // Check sum of net balances
  val netSum = memberBalances.sumOf { it.netBalance }
  val isBalanced = abs(netSum) < 0.05

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
            modifier = Modifier.testTag("month_filter_$key")
          )
        }
      }
    }

    // Hero Total Spend & Average Card
    item {
      Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("balance_overview_card")
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
              text = "Total Flat Spend",
              style = MaterialTheme.typography.labelLarge,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (isBalanced && totalSpend > 0) {
              Surface(
                color = PositiveGreenBg,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, PositiveGreenBorder)
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = PositiveGreen,
                    modifier = Modifier.size(13.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "Balanced (₹0 sum)",
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontWeight = FontWeight.Bold,
                      color = PositiveGreen
                    )
                  )
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(6.dp))

          Text(
            text = String.format(Locale.US, "₹%.2f", totalSpend),
            style = MaterialTheme.typography.displayLarge.copy(
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary,
              letterSpacing = (-0.5).sp
            )
          )

          Spacer(modifier = Modifier.height(12.dp))

          Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Equal Split (1/$memberCount)",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = String.format(Locale.US, "₹%.2f / person", average),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
              )
            }
          }
        }
      }
    }

    // Section 1: Member Net Balances
    item {
      Text(
        text = "Roommate Balances",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
      )
    }

    if (memberBalances.isEmpty()) {
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = "No flatmates in room yet. Invite roommates to see balances.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(18.dp)
          )
        }
      }
    } else {
      items(
        items = memberBalances,
        key = { it.user.id }
      ) { balance ->
        val isMe = balance.user.id == currentUser?.id
        val isOverpaid = balance.netBalance > 0.01
        val isUnderpaid = balance.netBalance < -0.01

        val badgeBg = when {
          isOverpaid -> PositiveGreenBg
          isUnderpaid -> NegativeDebtBg
          else -> MaterialTheme.colorScheme.surfaceVariant
        }
        val badgeBorder = when {
          isOverpaid -> PositiveGreenBorder
          isUnderpaid -> NegativeDebtBorder
          else -> MaterialTheme.colorScheme.outline
        }
        val badgeColor = when {
          isOverpaid -> PositiveGreen
          isUnderpaid -> NegativeDebtRed
          else -> MaterialTheme.colorScheme.onSurfaceVariant
        }

        val statusText = when {
          isOverpaid -> "Gets back"
          isUnderpaid -> "Owes"
          else -> "Settled up"
        }

        Card(
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("member_balance_card_${balance.user.id}")
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Member Avatar & Name
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(44.dp)
                  .clip(CircleShape)
                  .background(balance.user.avatarColor),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = balance.user.initials,
                  color = Color.White,
                  fontWeight = FontWeight.Bold,
                  fontSize = 15.sp
                )
              }
              Spacer(modifier = Modifier.width(14.dp))
              Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = balance.user.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                  )
                  if (isMe) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                      color = MaterialTheme.colorScheme.primaryContainer,
                      shape = RoundedCornerShape(6.dp)
                    ) {
                      Text(
                        text = "YOU",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = EmeraldPrimary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                      )
                    }
                  }
                }
                Text(
                  text = "Paid ₹${String.format(Locale.US, "%.2f", balance.totalPaid)}",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            // Net Balance Pill
            Surface(
              color = badgeBg,
              shape = RoundedCornerShape(10.dp),
              border = BorderStroke(1.dp, badgeBorder)
            ) {
              Text(
                text = "$statusText ${String.format(Locale.US, "₹%.2f", abs(balance.netBalance))}",
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = badgeColor
                ),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
              )
            }
          }
        }
      }
    }

    // Section 2: Simplified Settlements (Greedy Algorithm)
    item {
      Spacer(modifier = Modifier.height(8.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Simplified Settlements",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )
          Text(
            text = "Greedy settlement: minimum transfers to resolve all debts",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }

    if (settlements.isEmpty()) {
      item {
        Card(
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.CheckCircle,
              contentDescription = null,
              tint = PositiveGreen,
              modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "All settled up!",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
              )
              Text(
                text = "Everyone has paid their exact share for this month.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }
    } else {
      items(
        items = settlements,
        key = { it.id }
      ) { pair ->
        SettlementCard(
          pair = pair,
          currentUser = currentUser,
          onSettleUpi = {
            val deepLink = pair.upiDeepLink
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("UPI Payment Link", deepLink))

            Toast.makeText(
              context,
              "Copied UPI payment link for ₹${String.format(Locale.US, "%.2f", pair.amount)} to ${pair.toUser.name}",
              Toast.LENGTH_LONG
            ).show()

            try {
              val intent = Intent(Intent.ACTION_VIEW, Uri.parse(deepLink))
              context.startActivity(intent)
            } catch (_: Exception) {}
          }
        )
      }
    }

    item {
      Spacer(modifier = Modifier.height(28.dp))
    }
  }
}

@Composable
fun SettlementCard(
  pair: SettlementPair,
  currentUser: User?,
  onSettleUpi: () -> Unit
) {
  val isPayer = pair.fromUser.id == currentUser?.id
  val isReceiver = pair.toUser.id == currentUser?.id
  val formattedAmt = String.format(Locale.US, "₹%.2f", pair.amount)

  Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isPayer) NegativeDebtBg.copy(alpha = 0.5f)
      else if (isReceiver) PositiveGreenBg.copy(alpha = 0.5f)
      else MaterialTheme.colorScheme.surface
    ),
    border = BorderStroke(
      width = 1.dp,
      color = if (isPayer) NegativeDebtBorder else if (isReceiver) PositiveGreenBorder else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("settlement_pair_${pair.id}")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(18.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // From -> To Flow Visual
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f)
        ) {
          // From Avatar & Name
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(pair.fromUser.avatarColor),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = pair.fromUser.initials,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = if (isPayer) "You" else pair.fromUser.name,
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
            )
          }

          Spacer(modifier = Modifier.width(10.dp))

          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = "pays to",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
          )

          Spacer(modifier = Modifier.width(10.dp))

          // To Avatar & Name
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(pair.toUser.avatarColor),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = pair.toUser.initials,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = if (isReceiver) "You" else pair.toUser.name,
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
            )
          }
        }

        // Amount
        Text(
          text = formattedAmt,
          style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Bold,
            color = if (isPayer) NegativeDebtRed else EmeraldPrimary
          )
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      // UPI Settle Action Button
      Button(
        onClick = onSettleUpi,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = if (isPayer) NegativeDebtRed else EmeraldPrimary
        ),
        modifier = Modifier
          .fillMaxWidth()
          .height(44.dp)
          .testTag("settle_upi_button_${pair.id}")
      ) {
        Icon(
          imageVector = Icons.Default.Payment,
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.size(17.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = if (isPayer) "Pay $formattedAmt via UPI" else "Copy UPI Link ($formattedAmt to ${pair.toUser.name})",
          style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
        )
      }
    }
  }
}
