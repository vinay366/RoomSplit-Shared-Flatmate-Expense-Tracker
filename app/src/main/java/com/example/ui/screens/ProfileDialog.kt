package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.User
import com.example.ui.theme.AvatarColors
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.NegativeDebtRed

@Composable
fun ProfileDialog(
  currentUser: User?,
  onSaveProfile: (name: String, phone: String, avatarHex: String, upiId: String) -> Unit,
  onSignOut: () -> Unit,
  onDismiss: () -> Unit
) {
  var name by remember { mutableStateOf(currentUser?.name ?: "") }
  var phone by remember { mutableStateOf(currentUser?.phone ?: "") }
  var upiId by remember { mutableStateOf(currentUser?.upiId ?: "") }
  var selectedAvatarHex by remember { mutableStateOf(currentUser?.avatarColorHex ?: "#059669") }

  Dialog(onDismissRequest = onDismiss) {
    Card(
      shape = RoundedCornerShape(26.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
      elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("profile_dialog")
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(22.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Your Profile",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
          )
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close")
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Display Name") },
          placeholder = { Text("Your name") },
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("profile_name_input")
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = phone,
          onValueChange = { phone = it },
          label = { Text("Mobile Number") },
          placeholder = { Text("+91 98765 43210") },
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("profile_phone_input")
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = upiId,
          onValueChange = { upiId = it },
          label = { Text("UPI ID (for receiving settlement payments)") },
          placeholder = { Text("e.g. name@okaxis, 9876543210@paytm") },
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("profile_upi_input")
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
          text = "Avatar Color",
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          val hexList = listOf("#059669", "#2563EB", "#D97706", "#7C3AED", "#DB2777", "#0D9488")
          AvatarColors.take(6).forEachIndexed { index, color ->
            val hex = hexList.getOrElse(index) { "#059669" }
            val isSelected = selectedAvatarHex == hex
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(color)
                .clickable { selectedAvatarHex = hex },
              contentAlignment = Alignment.Center
            ) {
              if (isSelected) {
                Box(
                  modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
          onClick = { onSaveProfile(name, phone, selectedAvatarHex, upiId) },
          enabled = name.isNotBlank(),
          colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("save_profile_button")
        ) {
          Text("Save Profile", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
          onClick = {
            onDismiss()
            onSignOut()
          },
          colors = ButtonDefaults.outlinedButtonColors(contentColor = NegativeDebtRed),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .testTag("sign_out_button")
        ) {
          Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Sign Out")
        }
      }
    }
  }
}
