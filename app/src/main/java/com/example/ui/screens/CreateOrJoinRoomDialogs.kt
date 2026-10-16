package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.EmeraldPrimary

@Composable
fun CreateRoomDialog(
  onDismiss: () -> Unit,
  onCreateRoom: (name: String, emoji: String) -> Unit
) {
  var roomName by remember { mutableStateOf("") }
  var selectedEmoji by remember { mutableStateOf("🛋️") }
  val emojis = listOf("🛋️", "🏠", "🪴", "🏢", "🍕", "⚡", "🏖️")

  Dialog(onDismissRequest = onDismiss) {
    Card(
      shape = RoundedCornerShape(26.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
      elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("create_room_dialog")
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(24.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Create Flat Room",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
          )
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close")
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = "Pick Flat Emoji",
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          emojis.forEach { emoji ->
            val isSelected = selectedEmoji == emoji
            Box(
              modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                  if (isSelected) MaterialTheme.colorScheme.primaryContainer
                  else MaterialTheme.colorScheme.surfaceVariant
                )
                .clickable { selectedEmoji = emoji },
              contentAlignment = Alignment.Center
            ) {
              Text(text = emoji, fontSize = 22.sp)
            }
          }
        }

        Spacer(modifier = Modifier.height(18.dp))

        OutlinedTextField(
          value = roomName,
          onValueChange = { roomName = it },
          label = { Text("Flat Name") },
          placeholder = { Text("e.g. Flat 4B, Indiranagar 3BHK") },
          singleLine = true,
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("create_room_name_input")
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
          onClick = {
            if (roomName.isNotBlank()) {
              onCreateRoom(roomName.trim(), selectedEmoji)
            }
          },
          enabled = roomName.isNotBlank(),
          colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("submit_create_room_button")
        ) {
          Text("Generate Room & 6-Char Code", fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

@Composable
fun JoinRoomDialog(
  onDismiss: () -> Unit,
  onJoinRoom: (code: String, onResult: (Boolean, String) -> Unit) -> Unit
) {
  var inviteCode by remember { mutableStateOf("") }
  var errorMessage by remember { mutableStateOf<String?>(null) }
  val context = LocalContext.current

  Dialog(onDismissRequest = onDismiss) {
    Card(
      shape = RoundedCornerShape(26.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
      elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("join_room_dialog")
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(24.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Join with Invite Code",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
          )
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close")
          }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
          text = "Ask your flatmate for the 6-character room code (e.g. FLAT4B)",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(18.dp))

        if (errorMessage != null) {
          Surface(
            color = MaterialTheme.colorScheme.errorContainer,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .fillMaxWidth()
              .padding(bottom = 14.dp)
          ) {
            Text(
              text = errorMessage ?: "",
              color = MaterialTheme.colorScheme.onErrorContainer,
              style = MaterialTheme.typography.bodySmall,
              modifier = Modifier.padding(12.dp)
            )
          }
        }

        OutlinedTextField(
          value = inviteCode,
          onValueChange = {
            if (it.length <= 6) {
              inviteCode = it.uppercase()
              errorMessage = null
            }
          },
          label = { Text("6-Char Invite Code") },
          placeholder = { Text("FLAT4B") },
          singleLine = true,
          textStyle = MaterialTheme.typography.headlineMedium.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 4.sp,
            textAlign = TextAlign.Center
          ),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("join_room_code_input")
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
          onClick = {
            if (inviteCode.trim().length >= 4) {
              onJoinRoom(inviteCode.trim()) { success, msg ->
                if (!success) {
                  errorMessage = msg
                } else {
                  Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
              }
            }
          },
          enabled = inviteCode.trim().length >= 4,
          colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("submit_join_room_button")
        ) {
          Text("Join Flat Feed", fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}
