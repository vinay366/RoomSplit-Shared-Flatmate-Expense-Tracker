package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.AddEntryBottomSheet
import com.example.ui.components.RoomHeader
import com.example.ui.screens.BalanceScreen
import com.example.ui.screens.CreateRoomDialog
import com.example.ui.screens.FeedScreen
import com.example.ui.screens.JoinRoomDialog
import com.example.ui.screens.ProfileDialog
import com.example.ui.screens.RoomDetailsDialog
import com.example.ui.screens.SignInScreen
import com.example.ui.screens.SummaryScreen
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.RoomSplitTheme
import com.example.ui.viewmodel.NavTab
import com.example.ui.viewmodel.RoomSplitViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {
  private val viewModel: RoomSplitViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      RoomSplitTheme {
        var currentAuthUser by remember { mutableStateOf(Firebase.auth.currentUser) }

        DisposableEffect(Unit) {
          val listener = FirebaseAuth.AuthStateListener { auth ->
            currentAuthUser = auth.currentUser
          }
          Firebase.auth.addAuthStateListener(listener)
          onDispose {
            Firebase.auth.removeAuthStateListener(listener)
          }
        }

        if (currentAuthUser == null) {
          SignInScreen(
            onAuthSuccess = {
              currentAuthUser = Firebase.auth.currentUser
            }
          )
        } else {
          RoomSplitApp(viewModel = viewModel)
        }
      }
    }
  }
}

@Composable
fun RoomSplitApp(viewModel: RoomSplitViewModel) {
  val state by viewModel.uiState.collectAsState()

  Scaffold(
    modifier = Modifier
      .fillMaxSize()
      .windowInsetsPadding(WindowInsets.safeDrawing),
    topBar = {
      RoomHeader(
        room = state.activeRoom,
        currentUser = state.currentUser,
        members = state.roomMembers,
        notification = state.activeNotification,
        onDismissNotification = { viewModel.dismissNotification() },
        onOpenRoomDetails = { viewModel.openRoomDetails() },
        onOpenProfile = { viewModel.openProfileDialog() }
      )
    },
    bottomBar = {
      NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        windowInsets = WindowInsets.navigationBars,
        modifier = Modifier.testTag("main_navigation_bar")
      ) {
        // Tab 1: Feed
        NavigationBarItem(
          selected = state.currentTab == NavTab.FEED,
          onClick = { viewModel.selectTab(NavTab.FEED) },
          icon = {
            Icon(
              imageVector = if (state.currentTab == NavTab.FEED) Icons.Default.ReceiptLong else Icons.Outlined.ReceiptLong,
              contentDescription = "Shared Feed"
            )
          },
          label = {
            Text(
              text = "Feed",
              fontWeight = if (state.currentTab == NavTab.FEED) FontWeight.Bold else FontWeight.Normal
            )
          },
          colors = NavigationBarItemDefaults.colors(
            selectedIconColor = EmeraldPrimary,
            selectedTextColor = EmeraldPrimary,
            indicatorColor = MaterialTheme.colorScheme.primaryContainer
          ),
          modifier = Modifier.testTag("nav_tab_feed")
        )

        // Tab 2: Balances
        NavigationBarItem(
          selected = state.currentTab == NavTab.BALANCES,
          onClick = { viewModel.selectTab(NavTab.BALANCES) },
          icon = {
            Icon(
              imageVector = if (state.currentTab == NavTab.BALANCES) Icons.Default.AccountBalanceWallet else Icons.Outlined.AccountBalanceWallet,
              contentDescription = "Balances & Settlement"
            )
          },
          label = {
            Text(
              text = "Balances",
              fontWeight = if (state.currentTab == NavTab.BALANCES) FontWeight.Bold else FontWeight.Normal
            )
          },
          colors = NavigationBarItemDefaults.colors(
            selectedIconColor = EmeraldPrimary,
            selectedTextColor = EmeraldPrimary,
            indicatorColor = MaterialTheme.colorScheme.primaryContainer
          ),
          modifier = Modifier.testTag("nav_tab_balances")
        )

        // Tab 3: Summary
        NavigationBarItem(
          selected = state.currentTab == NavTab.SUMMARY,
          onClick = { viewModel.selectTab(NavTab.SUMMARY) },
          icon = {
            Icon(
              imageVector = if (state.currentTab == NavTab.SUMMARY) Icons.Default.PieChart else Icons.Outlined.PieChart,
              contentDescription = "Monthly Summary"
            )
          },
          label = {
            Text(
              text = "Summary",
              fontWeight = if (state.currentTab == NavTab.SUMMARY) FontWeight.Bold else FontWeight.Normal
            )
          },
          colors = NavigationBarItemDefaults.colors(
            selectedIconColor = EmeraldPrimary,
            selectedTextColor = EmeraldPrimary,
            indicatorColor = MaterialTheme.colorScheme.primaryContainer
          ),
          modifier = Modifier.testTag("nav_tab_summary")
        )
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      when (state.currentTab) {
        NavTab.FEED -> {
          FeedScreen(
            entries = state.filteredEntries,
            currentUser = state.currentUser,
            activeRoom = state.activeRoom,
            searchQuery = state.searchQuery,
            selectedCategory = state.selectedCategoryFilter,
            onSearchChange = { viewModel.setSearchQuery(it) },
            onCategoryFilterChange = { viewModel.setCategoryFilter(it) },
            onDeleteEntry = { viewModel.deleteEntry(it) },
            onOpenAddEntry = { viewModel.openAddEntry() },
            onOpenCreateRoom = { viewModel.openCreateRoom() },
            onOpenJoinRoom = { viewModel.openJoinRoom() }
          )
        }

        NavTab.BALANCES -> {
          BalanceScreen(
            currentUser = state.currentUser,
            totalSpend = state.totalSpendForMonth,
            memberBalances = state.memberBalances,
            settlements = state.settlements,
            selectedMonthKey = state.selectedMonthKey,
            availableMonths = state.availableMonths,
            onMonthSelected = { key, name -> viewModel.selectMonth(key, name) }
          )
        }

        NavTab.SUMMARY -> {
          SummaryScreen(
            summary = state.monthlySummary,
            room = state.activeRoom,
            selectedMonthKey = state.selectedMonthKey,
            availableMonths = state.availableMonths,
            onMonthSelected = { key, name -> viewModel.selectMonth(key, name) }
          )
        }
      }
    }
  }

  // Add Entry Bottom Sheet
  if (state.isAddEntrySheetOpen) {
    AddEntryBottomSheet(
      currentUser = state.currentUser,
      members = state.roomMembers,
      onDismiss = { viewModel.closeAddEntry() },
      onAddEntry = { itemName, category, amount, date, note, paidBy ->
        viewModel.addEntry(
          itemName = itemName,
          category = category,
          amount = amount,
          date = date,
          note = note,
          paidBy = paidBy
        )
      }
    )
  }

  // Room Details Dialog
  if (state.isRoomDetailsOpen) {
    RoomDetailsDialog(
      room = state.activeRoom,
      currentUser = state.currentUser,
      members = state.roomMembers,
      onUpdateRoomName = { viewModel.updateRoomName(it) },
      onOpenCreateRoom = { viewModel.openCreateRoom() },
      onOpenJoinRoom = { viewModel.openJoinRoom() },
      onDismiss = { viewModel.closeRoomDetails() }
    )
  }

  // Create Room Dialog
  if (state.isCreateRoomOpen) {
    CreateRoomDialog(
      onDismiss = { viewModel.closeCreateRoom() },
      onCreateRoom = { name, emoji -> viewModel.createRoom(name, emoji) }
    )
  }

  // Join Room Dialog
  if (state.isJoinRoomOpen) {
    JoinRoomDialog(
      onDismiss = { viewModel.closeJoinRoom() },
      onJoinRoom = { code, callback -> viewModel.joinRoom(code, callback) }
    )
  }

  // Profile Dialog
  if (state.isProfileDialogOpen) {
    ProfileDialog(
      currentUser = state.currentUser,
      onSaveProfile = { name, phone, avatarHex, upiId ->
        viewModel.saveProfile(name, phone, avatarHex, upiId)
      },
      onSignOut = { viewModel.signOut() },
      onDismiss = { viewModel.closeProfileDialog() }
    )
  }
}
