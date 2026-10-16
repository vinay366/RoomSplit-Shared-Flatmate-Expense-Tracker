package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.ui.theme.EmeraldPrimary
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun SignInScreen(
  onAuthSuccess: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val credentialManager = remember { CredentialManager.create(context) }
  var isLoading by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  val bgBrush = Brush.verticalGradient(
    listOf(
      MaterialTheme.colorScheme.background,
      MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
      MaterialTheme.colorScheme.background
    )
  )

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(bgBrush)
      .padding(20.dp),
    contentAlignment = Alignment.Center
  ) {
    Card(
      shape = RoundedCornerShape(28.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
      elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
      modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
        .testTag("sign_in_card")
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // App Icon with Luminous Glow
        Box(
          modifier = Modifier
            .size(76.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(
              Brush.linearGradient(
                listOf(EmeraldPrimary, Color(0xFF047857))
              )
            ),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.ReceiptLong,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(40.dp)
          )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
          text = "RoomSplit",
          style = MaterialTheme.typography.displayLarge.copy(
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            letterSpacing = (-0.5).sp
          )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = "Split daily flat & PG costs in real time with your roommates. Zero spreadsheets, zero awkward reminders.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Feature Highlights in Aesthetic Containers
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          FeatureHighlightRow(
            icon = Icons.Default.FlashOn,
            title = "10-Second Quick Log",
            subtitle = "Log daily vegetables, milk, gas & rent in 3 taps"
          )
          FeatureHighlightRow(
            icon = Icons.Default.Groups,
            title = "Live Cloud Sync",
            subtitle = "Invite flatmates with 6-character code"
          )
          FeatureHighlightRow(
            icon = Icons.Default.AccountBalanceWallet,
            title = "Automated UPI Settlements",
            subtitle = "Greedy debt solver with 1-tap UPI deep links"
          )
        }

        Spacer(modifier = Modifier.height(28.dp))

        if (errorMessage != null) {
          Surface(
            color = MaterialTheme.colorScheme.errorContainer,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .fillMaxWidth()
              .padding(bottom = 16.dp)
          ) {
            Text(
              text = errorMessage ?: "",
              color = MaterialTheme.colorScheme.onErrorContainer,
              style = MaterialTheme.typography.bodySmall,
              modifier = Modifier.padding(12.dp),
              textAlign = TextAlign.Center
            )
          }
        }

        // Google Sign-In Button
        Button(
          onClick = {
            isLoading = true
            errorMessage = null
            signInWithGoogle(
              context = context,
              credentialManager = credentialManager,
              scope = coroutineScope,
              onSuccess = {
                isLoading = false
                onAuthSuccess()
              },
              onError = { err ->
                isLoading = false
                errorMessage = err
              },
              onCancelled = {
                isLoading = false
              }
            )
          },
          enabled = !isLoading,
          colors = ButtonDefaults.buttonColors(
            containerColor = EmeraldPrimary
          ),
          shape = RoundedCornerShape(16.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .testTag("google_sign_in_button")
        ) {
          if (isLoading) {
            CircularProgressIndicator(
              modifier = Modifier.size(24.dp),
              color = Color.White,
              strokeWidth = 2.dp
            )
          } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "Sign in with Google",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun FeatureHighlightRow(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  title: String,
  subtitle: String
) {
  Surface(
    shape = RoundedCornerShape(14.dp),
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(38.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = EmeraldPrimary,
          modifier = Modifier.size(20.dp)
        )
      }
      Spacer(modifier = Modifier.width(12.dp))
      Column {
        Text(
          text = title,
          style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
        )
        Text(
          text = subtitle,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
  }
}

private fun signInWithGoogle(
  context: Context,
  credentialManager: CredentialManager,
  scope: CoroutineScope,
  onSuccess: () -> Unit,
  onError: (String) -> Unit,
  onCancelled: () -> Unit
) {
  val clientId = try {
    context.getString(R.string.default_web_client_id)
  } catch (e: Exception) {
    onError("Configuration error: default_web_client_id not found")
    return
  }

  val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
  val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

  scope.launch {
    try {
      val result = credentialManager.getCredential(context as Activity, request)
      val credential = result.credential
      if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
        val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
        val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
        Firebase.auth.signInWithCredential(authCredential).await()
        onSuccess()
      } else {
        onError("Unexpected credential format received")
      }
    } catch (e: GetCredentialCancellationException) {
      Log.w("Auth", "Google Sign-In flow cancelled or dismissed: ${e.message}", e)
      onCancelled()
    } catch (e: Exception) {
      Log.e("Auth", "Google Sign-In failed", e)
      onError(e.localizedMessage ?: "Sign in failed")
    }
  }
}
