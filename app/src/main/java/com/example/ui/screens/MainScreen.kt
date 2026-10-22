package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.UserRole
import com.example.ui.MainViewModel
import com.example.ui.NavigationDestination
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldSecondary
import com.example.ui.theme.IndigoLight
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.RoseDanger
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import com.example.ui.components.PulsingStatusDot
import com.example.ui.theme.BanglalinkOrange
import com.example.ui.theme.BanglalinkOrangeLight
import com.example.ui.theme.PrantejDeepEmerald
import com.example.ui.theme.PrantejEmeraldBright
import com.example.ui.theme.PrantejGold

data class NavItem(
    val destination: NavigationDestination,
    val title: String,
    val icon: ImageVector,
    val adminOnly: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val currentDest by viewModel.currentDestination.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val uiMessage by viewModel.uiMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var menuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(uiMessage) {
        uiMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    // Handle back button to return to Dashboard
    BackHandler(enabled = currentDest != NavigationDestination.Dashboard) {
        viewModel.navigateTo(NavigationDestination.Dashboard)
    }

    val isAdmin = currentUser?.role == UserRole.ADMIN

    val navItems = listOf(
        NavItem(NavigationDestination.Dashboard, "Dashboard", Icons.Default.Dashboard),
        NavItem(NavigationDestination.POS, "POS", Icons.Default.PointOfSale),
        NavItem(NavigationDestination.Products, "Products", Icons.Default.Inventory),
        NavItem(NavigationDestination.Transactions, "Sales", Icons.Default.Receipt),
        NavItem(NavigationDestination.Logs, "Audit", Icons.Default.History),
        NavItem(NavigationDestination.Staff, "Staff", Icons.Default.People),
        NavItem(NavigationDestination.Settings, "Settings", Icons.Default.Settings)
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.prantej_brand_symbol),
                            contentDescription = "PRANTEJ Official Logo",
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.dp, PrantejGold.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "PRANTEJ",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "CloudStock",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = BanglalinkOrange
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                PulsingStatusDot()
                            }
                            Text(
                                text = if (isAdmin) "Super Admin Terminal" else "Store Cashier Active",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isAdmin) BanglalinkOrange else PrantejEmeraldBright,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                },
                actions = {
                    // Role Badge with Banglalink-style vibrant glow
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isAdmin) BanglalinkOrange.copy(alpha = 0.15f)
                                else PrantejEmeraldBright.copy(alpha = 0.15f)
                            )
                            .border(
                                1.dp,
                                if (isAdmin) BanglalinkOrange.copy(alpha = 0.35f)
                                else PrantejEmeraldBright.copy(alpha = 0.35f),
                                RoundedCornerShape(10.dp)
                            )
                            .padding(horizontal = 9.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = currentUser?.role?.name ?: "USER",
                            color = if (isAdmin) BanglalinkOrange else PrantejEmeraldBright,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menu Options")
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(currentUser?.displayName ?: "User", fontWeight = FontWeight.Bold)
                                        Text(currentUser?.email ?: "", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    }
                                },
                                onClick = {}
                            )
                            DropdownMenuItem(
                                text = { Text("Sign Out", color = RoseDanger, fontWeight = FontWeight.Bold) },
                                leadingIcon = { Icon(Icons.Default.ExitToApp, contentDescription = null, tint = RoseDanger) },
                                onClick = {
                                    menuExpanded = false
                                    viewModel.logout()
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                navItems.forEach { item ->
                    val isSelected = currentDest == item.destination
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.navigateTo(item.destination) },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title
                            )
                        },
                        label = {
                            Text(
                                text = item.title,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BanglalinkOrange,
                            selectedTextColor = BanglalinkOrange,
                            indicatorColor = BanglalinkOrange.copy(alpha = 0.16f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentDest,
                transitionSpec = {
                    (slideInHorizontally(
                        initialOffsetX = { width -> (width * 0.15f).toInt() },
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioLowBouncy)
                    ) + fadeIn(animationSpec = tween(220))) togetherWith
                    (slideOutHorizontally(
                        targetOffsetX = { width -> -(width * 0.15f).toInt() },
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioLowBouncy)
                    ) + fadeOut(animationSpec = tween(180)))
                },
                label = "NavigationTransition"
            ) { dest ->
                when (dest) {
                    NavigationDestination.Dashboard -> DashboardScreen(viewModel)
                    NavigationDestination.Products -> ProductsScreen(viewModel)
                    NavigationDestination.POS -> POSScreen(viewModel)
                    NavigationDestination.Transactions -> TransactionsScreen(viewModel)
                    NavigationDestination.Logs -> InventoryLogsScreen(viewModel)
                    NavigationDestination.Staff -> StaffManagementScreen(viewModel)
                    NavigationDestination.Settings -> SettingsScreen(viewModel)
                }
            }
        }
    }
}

