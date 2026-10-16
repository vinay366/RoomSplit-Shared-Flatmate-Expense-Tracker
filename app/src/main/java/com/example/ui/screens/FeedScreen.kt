package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EntryWithMember
import com.example.data.model.ExpenseCategory
import com.example.data.model.Room
import com.example.data.model.User
import com.example.ui.components.EntryCard
import com.example.ui.theme.EmeraldPrimary
import java.util.Locale

@Composable
fun FeedScreen(
  entries: List<EntryWithMember>,
  currentUser: User?,
  activeRoom: Room?,
  searchQuery: String,
  selectedCategory: String?,
  onSearchChange: (String) -> Unit,
  onCategoryFilterChange: (String?) -> Unit,
  onDeleteEntry: (String) -> Unit,
  onOpenAddEntry: () -> Unit,
  onOpenCreateRoom: () -> Unit,
  onOpenJoinRoom: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(modifier = modifier.fillMaxSize()) {
    Column(modifier = Modifier.fillMaxSize()) {
      if (activeRoom == null) {
        // Welcome Onboarding Card
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
          contentAlignment = Alignment.Center
        ) {
          Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(26.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Box(
                modifier = Modifier
                  .size(72.dp)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Home,
                  contentDescription = null,
                  tint = EmeraldPrimary,
                  modifier = Modifier.size(36.dp)
                )
              }

              Spacer(modifier = Modifier.height(18.dp))

              Text(
                text = "Welcome to RoomSplit!",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
              )

              Spacer(modifier = Modifier.height(8.dp))

              Text(
                text = "Shared flatmate expenses with live cloud sync. Create your flat or enter a 6-character invite code from your flatmates.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
              )

              Spacer(modifier = Modifier.height(24.dp))

              Button(
                onClick = onOpenCreateRoom,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .height(50.dp)
                  .testTag("onboarding_create_room_button")
              ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create a Flat Room", fontWeight = FontWeight.Bold)
              }

              Spacer(modifier = Modifier.height(12.dp))

              OutlinedButton(
                onClick = onOpenJoinRoom,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .height(50.dp)
                  .testTag("onboarding_join_room_button")
              ) {
                Icon(Icons.Default.GroupAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Join with 6-Char Code", fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      } else {
        // Quick Stats Ribbon: Flat Total Spend
        val totalSpend = entries.sumOf { it.entry.amount }
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "Total Feed Spend",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = String.format(Locale.US, "₹%.2f", totalSpend),
                style = MaterialTheme.typography.titleLarge.copy(
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary
                )
              )
            }

            Surface(
              color = MaterialTheme.colorScheme.primaryContainer,
              shape = RoundedCornerShape(10.dp)
            ) {
              Text(
                text = "${entries.size} entries",
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = EmeraldPrimary
                ),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
              )
            }
          }
        }

        // Search Bar with Clean Elevated Surface
        OutlinedTextField(
          value = searchQuery,
          onValueChange = onSearchChange,
          placeholder = { Text("Search tomatoes, milk, Wi-Fi...") },
          leadingIcon = {
            Icon(
              imageVector = Icons.Default.Search,
              contentDescription = "Search",
              tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
          },
          trailingIcon = {
            if (searchQuery.isNotEmpty()) {
              IconButton(onClick = { onSearchChange("") }) {
                Icon(Icons.Default.Clear, contentDescription = "Clear search")
              }
            }
          },
          singleLine = true,
          shape = RoundedCornerShape(16.dp),
          colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedContainerColor = MaterialTheme.colorScheme.surface
          ),
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .testTag("feed_search_input")
        )

        // Category Filter Chips Row (All + 5 fixed categories)
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 4.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          FilterChip(
            selected = selectedCategory == null,
            onClick = { onCategoryFilterChange(null) },
            label = { Text("All (${entries.size})") },
            shape = RoundedCornerShape(16.dp),
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = EmeraldPrimary,
              selectedLabelColor = Color.White
            ),
            modifier = Modifier.testTag("category_filter_all")
          )

          ExpenseCategory.entries.forEach { cat ->
            val isSelected = selectedCategory.equals(cat.id, ignoreCase = true)
            FilterChip(
              selected = isSelected,
              onClick = {
                if (isSelected) onCategoryFilterChange(null)
                else onCategoryFilterChange(cat.id)
              },
              label = {
                Text("${cat.emoji} ${cat.displayName}")
              },
              shape = RoundedCornerShape(16.dp),
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = cat.color,
                selectedLabelColor = Color.White
              ),
              modifier = Modifier.testTag("category_filter_${cat.id}")
            )
          }
        }

        Spacer(modifier = Modifier.height(4.dp))

        if (entries.isEmpty()) {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .padding(32.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Box(
                modifier = Modifier
                  .size(76.dp)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.ReceiptLong,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(38.dp)
                )
              }
              Spacer(modifier = Modifier.height(18.dp))
              Text(
                text = if (searchQuery.isNotBlank() || selectedCategory != null) "No matching expenses"
                else "No expenses in ${activeRoom.name} yet",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
              )
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = "Tap '+ Add Expense' below to log your real daily vegetables, milk, gas, or flat rent in 10 seconds!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
              )
            }
          }
        } else {
          LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            items(
              items = entries,
              key = { it.entry.id }
            ) { item ->
              val isOwn = currentUser?.id == item.member.id
              EntryCard(
                item = item,
                isOwnEntry = isOwn,
                onDelete = { onDeleteEntry(item.entry.id) }
              )
            }

            item {
              Spacer(modifier = Modifier.height(88.dp))
            }
          }
        }
      }
    }

    // 10-Second Add Entry FAB (only visible when in an active room)
    if (activeRoom != null) {
      ExtendedFloatingActionButton(
        onClick = onOpenAddEntry,
        icon = { Icon(Icons.Default.Add, contentDescription = null, tint = Color.White) },
        text = {
          Text(
            text = "Add Expense",
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
        },
        containerColor = EmeraldPrimary,
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
          .align(Alignment.BottomEnd)
          .padding(18.dp)
          .testTag("add_expense_fab")
      )
    }
  }
}
