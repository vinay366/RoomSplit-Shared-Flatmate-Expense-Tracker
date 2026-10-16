package com.example.ui.components

import android.app.DatePickerDialog
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExpenseCategory
import com.example.data.model.User
import com.example.ui.theme.EmeraldPrimary
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEntryBottomSheet(
  currentUser: User?,
  members: List<User>,
  onDismiss: () -> Unit,
  onAddEntry: (itemName: String, category: String, amount: Double, date: String, note: String, paidBy: User) -> Unit
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val context = LocalContext.current
  val focusManager = LocalFocusManager.current

  var selectedCategory by remember { mutableStateOf(ExpenseCategory.VEGETABLES) }
  var itemName by remember { mutableStateOf("") }
  var amountText by remember { mutableStateOf("") }
  var noteText by remember { mutableStateOf("") }
  var selectedMemberId by remember { mutableStateOf(currentUser?.id ?: members.firstOrNull()?.id ?: "") }

  val today = remember { LocalDate.now() }
  var selectedDate by remember { mutableStateOf(today) }

  var itemNameError by remember { mutableStateOf<String?>(null) }
  var amountError by remember { mutableStateOf<String?>(null) }

  // Date picker setup (up to 30 days back)
  val datePickerDialog = remember {
    val cal = Calendar.getInstance()
    val dialog = DatePickerDialog(
      context,
      { _, year, month, dayOfMonth ->
        selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
      },
      cal.get(Calendar.YEAR),
      cal.get(Calendar.MONTH),
      cal.get(Calendar.DAY_OF_MONTH)
    )
    val maxDate = System.currentTimeMillis()
    val minDate = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
    dialog.datePicker.maxDate = maxDate
    dialog.datePicker.minDate = minDate
    dialog
  }

  fun validateAndSubmit() {
    var valid = true
    if (itemName.trim().isBlank()) {
      itemNameError = "Enter what you bought"
      valid = false
    } else {
      itemNameError = null
    }

    val parsedAmount = amountText.toDoubleOrNull()
    if (parsedAmount == null || parsedAmount <= 0.0) {
      amountError = "Enter an amount > ₹0"
      valid = false
    } else {
      amountError = null
    }

    if (valid && parsedAmount != null) {
      val payingUser = members.firstOrNull { it.id == selectedMemberId }
        ?: currentUser
        ?: User(id = selectedMemberId, name = "You")
      onAddEntry(
        itemName.trim(),
        selectedCategory.id,
        parsedAmount,
        selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
        noteText.trim(),
        payingUser
      )
    }
  }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    dragHandle = {
      Box(
        modifier = Modifier
          .padding(top = 12.dp, bottom = 8.dp)
          .size(40.dp, 4.dp)
          .clip(RoundedCornerShape(2.dp))
          .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
      )
    },
    modifier = Modifier.testTag("add_entry_bottom_sheet")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 22.dp)
        .padding(bottom = 28.dp)
        .imePadding()
    ) {
      // Header: Speed Badge + Title
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(10.dp)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.FlashOn,
                contentDescription = null,
                tint = EmeraldPrimary,
                modifier = Modifier.size(15.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "10s Fast Entry",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = EmeraldPrimary
              )
            }
          }
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = "Log Flat Expense",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
          )
        }

        IconButton(onClick = onDismiss) {
          Icon(Icons.Default.Close, contentDescription = "Close sheet")
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // 1. Five Fixed Category Chips
      Text(
        text = "Select Category",
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
        ExpenseCategory.entries.forEach { cat ->
          CategoryChip(
            category = cat,
            isSelected = selectedCategory == cat,
            onClick = { selectedCategory = cat }
          )
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // 2. What did you buy? Text field + Smart Suggestions
      Text(
        text = "Item Name",
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Spacer(modifier = Modifier.height(6.dp))
      OutlinedTextField(
        value = itemName,
        onValueChange = {
          itemName = it
          if (itemNameError != null) itemNameError = null
        },
        placeholder = { Text("e.g. Tomatoes 1kg, Mother Dairy 2L, Cylinder...") },
        singleLine = true,
        isError = itemNameError != null,
        supportingText = itemNameError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = selectedCategory.color,
          cursorColor = selectedCategory.color
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("item_name_input")
      )

      // Quick Suggestion Chips for the selected category
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState())
          .padding(top = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        selectedCategory.sampleSuggestions.forEach { suggestion ->
          Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
            modifier = Modifier.clickable {
              itemName = suggestion
              itemNameError = null
            }
          ) {
            Text(
              text = "+ $suggestion",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // 3. Amount & Quick Increment Buttons
      Text(
        text = "Amount (₹)",
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Spacer(modifier = Modifier.height(6.dp))
      OutlinedTextField(
        value = amountText,
        onValueChange = {
          if (it.isEmpty() || it.matches(Regex("^\\d*(\\.\\d{0,2})?$"))) {
            amountText = it
            if (amountError != null) amountError = null
          }
        },
        leadingIcon = {
          Text(
            text = "₹",
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.padding(start = 6.dp)
          )
        },
        placeholder = { Text("0.00") },
        singleLine = true,
        isError = amountError != null,
        supportingText = amountError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
        keyboardOptions = KeyboardOptions(
          keyboardType = KeyboardType.Decimal,
          imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(onDone = {
          focusManager.clearFocus()
          validateAndSubmit()
        }),
        shape = RoundedCornerShape(14.dp),
        textStyle = MaterialTheme.typography.headlineMedium.copy(
          fontWeight = FontWeight.Bold,
          letterSpacing = (-0.5).sp
        ),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("amount_input")
      )

      // Quick Amount Addition Chips (+₹50, +₹100, +₹200, +₹500, +₹1000)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState())
          .padding(top = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        listOf(50, 100, 200, 500, 1000).forEach { inc ->
          Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.2f)),
            modifier = Modifier.clickable {
              val current = amountText.toDoubleOrNull() ?: 0.0
              val updated = current + inc
              amountText = if (updated % 1.0 == 0.0) {
                updated.toInt().toString()
              } else {
                String.format(java.util.Locale.US, "%.2f", updated)
              }
              amountError = null
            }
          ) {
            Text(
              text = "+₹$inc",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = EmeraldPrimary
              ),
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // 4. Paid By Flatmate Selector
      Text(
        text = "Paid By",
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
        val displayMembers = if (members.isNotEmpty()) members else listOfNotNull(currentUser)
        displayMembers.forEach { member ->
          val isSelected = member.id == selectedMemberId
          FilterChip(
            selected = isSelected,
            onClick = { selectedMemberId = member.id },
            label = {
              Text(
                text = if (member.id == currentUser?.id) "${member.name} (You)" else member.name,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
              )
            },
            leadingIcon = {
              Box(
                modifier = Modifier
                  .size(20.dp)
                  .clip(CircleShape)
                  .background(member.avatarColor),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = member.initials.take(1),
                  color = Color.White,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            },
            shape = RoundedCornerShape(18.dp),
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
              selectedLabelColor = EmeraldPrimary
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // 5. Date Selector (Defaults to Today, up to 30 days back)
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
        modifier = Modifier
          .fillMaxWidth()
          .clickable { datePickerDialog.show() }
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.CalendarMonth,
              contentDescription = "Pick date",
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "Purchase Date",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = if (selectedDate == today) "Today (${selectedDate.format(DateTimeFormatter.ofPattern("d MMM"))})"
                else selectedDate.format(DateTimeFormatter.ofPattern("EEE, d MMM yyyy")),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
              )
            }
          }

          Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(8.dp)
          ) {
            Text(
              text = "Change",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = EmeraldPrimary,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // 6. Optional Note Input
      OutlinedTextField(
        value = noteText,
        onValueChange = { noteText = it },
        placeholder = { Text("Add optional note (e.g. Split with 3 people, UPI receipt)") },
        leadingIcon = {
          Icon(
            imageVector = Icons.Default.EditNote,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
          )
        },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(24.dp))

      // Submit Button
      Button(
        onClick = { validateAndSubmit() },
        modifier = Modifier
          .fillMaxWidth()
          .height(54.dp)
          .testTag("submit_entry_button"),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
      ) {
        Text(
          text = "Add to Flat Feed",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
        )
      }
    }
  }
}
