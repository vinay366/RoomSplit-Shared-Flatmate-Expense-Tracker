package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.ui.screens.BalanceScreen
import com.example.ui.screens.CreateRoomDialog
import com.example.ui.screens.FeedScreen
import com.example.ui.screens.JoinRoomDialog
import com.example.ui.screens.ProfileDialog
import com.example.ui.screens.RoomDetailsDialog
import com.example.ui.screens.SignInScreen
import com.example.ui.screens.SummaryScreen
import com.example.ui.theme.RoomSplitTheme
import com.example.ui.viewmodel.RoomSplitViewModel
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    private val viewModel: RoomSplitViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            RoomSplitTheme {
                MainContent(viewModel = viewModel)
            }
        }
    }
}

enum class NavTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    FEED("Feed", Icons.Default.ReceiptLong),
    BALANCES("Balances", Icons.Default.AccountBalanceWallet),
    SUMMARY("Summary", Icons.Default.BarChart)
}

@Composable
fun MainContent(viewModel: RoomSplitViewModel) {
    val currentUserProfile by viewModel.currentUserProfile.collectAsState()
    val room by viewModel.currentRoom.collectAsState()
    val members by viewModel.members.collectAsState()
    val entries by viewModel.entries.collectAsState()
    val balances by viewModel.balances.collectAsState()
    val debtTransfers by viewModel.debtTransfers.collectAsState()
    val recentAlertMessage by viewModel.recentAlertMessage.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTab by remember { mutableStateOf(NavTab.FEED) }

    var showCreateRoomDialog by remember { mutableStateOf(false) }
    var showJoinRoomDialog by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }
    var showRoomDetailsDialog by remember { mutableStateOf(false) }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    if (currentUserProfile == null && FirebaseAuth.getInstance().currentUser == null) {
        SignInScreen(
            onSignInSuccess = {
                val user = FirebaseAuth.getInstance().currentUser
                if (user != null) {
                    viewModel.initUser(user.uid, user.displayName ?: "Flatmate", user.email ?: "")
                }
            }
        )
    } else {
        val user = currentUserProfile
        if (user == null) {
            Box(modifier = Modifier.fillMaxSize())
        } else {
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                bottomBar = {
                    if (room != null) {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ) {
                            NavTab.entries.forEach { tab ->
                                val isSelected = selectedTab == tab
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = { selectedTab = tab },
                                    icon = {
                                        Icon(
                                            imageVector = tab.icon,
                                            contentDescription = tab.title
                                        )
                                    },
                                    label = { Text(tab.title) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    ),
                                    modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    when (selectedTab) {
                        NavTab.FEED -> {
                            FeedScreen(
                                userProfile = user,
                                room = room,
                                members = members,
                                entries = entries,
                                recentAlertMessage = recentAlertMessage,
                                onAddEntry = { viewModel.addEntry(it) },
                                onDeleteEntry = { viewModel.deleteEntry(it) },
                                onCreateRoomClick = { showCreateRoomDialog = true },
                                onJoinRoomClick = { showJoinRoomDialog = true },
                                onOpenProfile = { showProfileDialog = true },
                                onOpenRoomDetails = { showRoomDetailsDialog = true }
                            )
                        }
                        NavTab.BALANCES -> {
                            BalanceScreen(
                                userProfile = user,
                                members = members,
                                entries = entries,
                                balances = balances,
                                debts = debtTransfers,
                                onRecordSettlement = { viewModel.recordSettlement(it) }
                            )
                        }
                        NavTab.SUMMARY -> {
                            SummaryScreen(
                                room = room,
                                members = members,
                                entries = entries,
                                balances = balances
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCreateRoomDialog) {
        CreateRoomDialog(
            onDismiss = { showCreateRoomDialog = false },
            onCreateRoom = { name ->
                viewModel.createRoom(name)
                showCreateRoomDialog = false
            }
        )
    }

    if (showJoinRoomDialog) {
        JoinRoomDialog(
            onDismiss = { showJoinRoomDialog = false },
            onJoinRoom = { code ->
                viewModel.joinRoom(code)
                showJoinRoomDialog = false
            }
        )
    }

    if (showProfileDialog && currentUserProfile != null) {
        ProfileDialog(
            userProfile = currentUserProfile!!,
            onDismiss = { showProfileDialog = false },
            onSaveUpiId = { upi -> viewModel.updateUpiId(upi) },
            onSignOut = {
                viewModel.signOut()
                showProfileDialog = false
            }
        )
    }

    if (showRoomDetailsDialog && room != null && currentUserProfile != null) {
        RoomDetailsDialog(
            room = room!!,
            members = members,
            currentUserId = currentUserProfile!!.id,
            onDismiss = { showRoomDetailsDialog = false },
            onSwitchOrLeaveRoom = {
                showRoomDetailsDialog = false
                viewModel.clearActiveRoom()
            }
        )
    }
}
