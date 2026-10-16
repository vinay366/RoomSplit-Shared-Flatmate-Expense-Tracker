package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Room
import com.example.data.model.User
import com.example.ui.theme.EmeraldPrimary

@Composable
fun RoomHeader(
  room: Room?,
  currentUser: User?,
  members: List<User>,
  notification: String?,
  onDismissNotification: () -> Unit,
  onOpenRoomDetails: () -> Unit,
  onOpenProfile: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current

  Column(modifier = modifier.fillMaxWidth()) {
    // In-app Notification Banner
    AnimatedVisibility(
      visible = notification != null,
      enter = slideInVertically() + fadeIn(),
      exit = slideOutVertically() + fadeOut()
    ) {
      if (notification != null) {
        Surface(
          color = EmeraldPrimary,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("in_app_notification_banner")
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Notifications,
              contentDescription = "Notification",
              tint = Color.White,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = notification,
              color = Color.White,
              style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
              modifier = Modifier.weight(1f),
              maxLines = 2,
              overflow = TextOverflow.Ellipsis
            )
            IconButton(
              onClick = onDismissNotification,
              modifier = Modifier.size(28.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Dismiss notification",
                tint = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.size(18.dp)
              )
            }
          }
        }
      }
    }

    // Top Navigation & Room Status Bar
    Surface(
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 2.dp,
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Room Title & Invite Code Chip
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onOpenRoomDetails)
            .padding(4.dp)
            .testTag("room_details_button")
        ) {
          Box(
            modifier = Modifier
              .size(40.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = room?.emoji ?: "🏠",
              fontSize = 20.sp
            )
          }

          Spacer(modifier = Modifier.width(10.dp))

          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = room?.name ?: "Create / Join Flat",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              Spacer(modifier = Modifier.width(6.dp))
              // Invite Code badge
              if (room != null && room.inviteCode.isNotBlank()) {
                Surface(
                  color = MaterialTheme.colorScheme.surfaceVariant,
                  shape = RoundedCornerShape(6.dp),
                  modifier = Modifier.clickable {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Room Invite Code", room.inviteCode))
                    Toast.makeText(context, "Copied code: ${room.inviteCode}", Toast.LENGTH_SHORT).show()
                  }
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(
                      text = room.inviteCode,
                      style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                      color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Icon(
                      imageVector = Icons.Default.ContentCopy,
                      contentDescription = "Copy code",
                      tint = MaterialTheme.colorScheme.primary,
                      modifier = Modifier.size(11.dp)
                    )
                  }
                }
              }
            }

            Text(
              text = if (room != null) "${members.size} flatmates • Tap to manage" else "Tap to set up your flat",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Profile Pill
        Surface(
          color = MaterialTheme.colorScheme.surfaceVariant,
          shape = RoundedCornerShape(20.dp),
          modifier = Modifier
            .clickable(onClick = onOpenProfile)
            .testTag("user_profile_button")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(currentUser?.avatarColor ?: EmeraldPrimary),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = currentUser?.initials ?: "ME",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
              )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = currentUser?.name ?: "Profile",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
              maxLines = 1
            )
          }
        }
      }
    }
  }
}
