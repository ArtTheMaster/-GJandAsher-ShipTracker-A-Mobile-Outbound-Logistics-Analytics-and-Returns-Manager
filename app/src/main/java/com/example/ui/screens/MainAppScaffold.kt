package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.ScreenTab
import com.example.ui.viewmodel.ShipTrackerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScaffold(
    viewModel: ShipTrackerViewModel,
    currentUser: User,
    onLogout: () -> Unit
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val scanState by viewModel.scanState.collectAsState()
    val allShipments by viewModel.allShipments.collectAsState()
    val filteredShipments by viewModel.filteredShipments.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filterStatus by viewModel.filterStatus.collectAsState()
    val filterPlatform by viewModel.filterPlatform.collectAsState()
    val filterCourier by viewModel.filterCourier.collectAsState()
    val filterDateRange by viewModel.filterDateRange.collectAsState()
    val allBatches by viewModel.allBatches.collectAsState()
    val allReturns by viewModel.allReturns.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val pendingSyncCount by viewModel.pendingSyncCount.collectAsState()

    var showUserSwitchDialog by remember { mutableStateOf(false) }
    var showStaffDialog by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    val isOwner = currentUser.role == UserRole.OWNER

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "GJandAsher",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (isOwner) LogisticsBlueLight else AccentAmberLight
                            ) {
                                Text(
                                    text = if (isOwner) "Owner" else "Staff",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isOwner) LogisticsBlueDark else AccentAmberDark,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = currentUser.fullName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    // Sync Status Indicator Button
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                if (pendingSyncCount > 0) {
                                    viewModel.syncPendingRecords { count ->
                                        snackbarMessage = "Synced $count offline records to cloud database!"
                                    }
                                } else {
                                    snackbarMessage = "All outbound parcel records are up to date."
                                }
                            },
                        color = if (pendingSyncCount > 0) StatusOrangeLight else StatusGreenLight,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (pendingSyncCount > 0) Icons.Default.SyncProblem else Icons.Default.CloudDone,
                                contentDescription = "Sync Status",
                                tint = if (pendingSyncCount > 0) StatusOrange else StatusGreenDark,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (pendingSyncCount > 0) "$pendingSyncCount Sync" else "Online",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (pendingSyncCount > 0) StatusOrange else StatusGreenDark
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Switch Account / Profile Button
                    IconButton(
                        onClick = { showUserSwitchDialog = true },
                        modifier = Modifier.testTag("switch_user_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwitchAccount,
                            contentDescription = "Switch Account",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            // Role-based Navigation Bar per requirements:
            // Middle: Barcode/QR Code Scanning
            // Staff: 4 buttons (Parcels, Scan, Dispatch, Returns)
            // Owner: 5 buttons (Parcels, Dispatch, Scan in middle, Returns, Analytics)
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                windowInsets = WindowInsets.navigationBars
            ) {
                if (isOwner) {
                    // OWNER: 5 BUTTONS (Parcels, Dispatch, Scan in middle, Returns, Analytics)
                    NavigationBarItem(
                        selected = currentTab == ScreenTab.PARCELS,
                        onClick = { viewModel.selectTab(ScreenTab.PARCELS) },
                        icon = {
                            BadgedBox(badge = {
                                if (allShipments.isNotEmpty()) {
                                    Badge { Text("${allShipments.size}") }
                                }
                            }) {
                                Icon(Icons.Default.Inventory2, contentDescription = "Parcels")
                            }
                        },
                        label = { Text("Parcels", fontSize = 11.sp) }
                    )

                    NavigationBarItem(
                        selected = currentTab == ScreenTab.DISPATCH,
                        onClick = { viewModel.selectTab(ScreenTab.DISPATCH) },
                        icon = { Icon(Icons.Default.LocalShipping, contentDescription = "Dispatch") },
                        label = { Text("Dispatch", fontSize = 11.sp) }
                    )

                    // MIDDLE BUTTON: Barcode/QR Code Scanning
                    NavigationBarItem(
                        selected = currentTab == ScreenTab.SCAN,
                        onClick = { viewModel.selectTab(ScreenTab.SCAN) },
                        icon = {
                            Surface(
                                shape = CircleShape,
                                color = if (currentTab == ScreenTab.SCAN) LogisticsBlue else LogisticsBlueLight,
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.QrCodeScanner,
                                        contentDescription = "Scan Barcode/QR",
                                        tint = if (currentTab == ScreenTab.SCAN) Color.White else LogisticsBlueDark,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        },
                        label = { Text("Scan", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )

                    NavigationBarItem(
                        selected = currentTab == ScreenTab.RETURNS,
                        onClick = { viewModel.selectTab(ScreenTab.RETURNS) },
                        icon = {
                            val pendingReturns = allReturns.count { it.refundStatus == RefundStatus.PENDING_APPROVAL }
                            BadgedBox(badge = {
                                if (pendingReturns > 0) {
                                    Badge(containerColor = StatusRed) { Text("$pendingReturns") }
                                }
                            }) {
                                Icon(Icons.Default.AssignmentReturn, contentDescription = "Returns")
                            }
                        },
                        label = { Text("Returns", fontSize = 11.sp) }
                    )

                    NavigationBarItem(
                        selected = currentTab == ScreenTab.DASHBOARD,
                        onClick = { viewModel.selectTab(ScreenTab.DASHBOARD) },
                        icon = {
                            Icon(Icons.Default.BarChart, contentDescription = "Analytics")
                        },
                        label = { Text("Analytics", fontSize = 11.sp) }
                    )
                } else {
                    // STAFF: EXACTLY 4 BUTTONS (Parcels, Scan, Dispatch, Returns)
                    // Barcode/QR Code Scanning is in the middle!
                    NavigationBarItem(
                        selected = currentTab == ScreenTab.PARCELS,
                        onClick = { viewModel.selectTab(ScreenTab.PARCELS) },
                        icon = {
                            BadgedBox(badge = {
                                if (allShipments.isNotEmpty()) {
                                    Badge { Text("${allShipments.size}") }
                                }
                            }) {
                                Icon(Icons.Default.Inventory2, contentDescription = "Parcels")
                            }
                        },
                        label = { Text("Parcels", fontSize = 11.sp) }
                    )

                    // MIDDLE: Barcode/QR Code Scanning
                    NavigationBarItem(
                        selected = currentTab == ScreenTab.SCAN,
                        onClick = { viewModel.selectTab(ScreenTab.SCAN) },
                        icon = {
                            Surface(
                                shape = CircleShape,
                                color = if (currentTab == ScreenTab.SCAN) LogisticsBlue else LogisticsBlueLight,
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.QrCodeScanner,
                                        contentDescription = "Scan Barcode/QR",
                                        tint = if (currentTab == ScreenTab.SCAN) Color.White else LogisticsBlueDark,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        },
                        label = { Text("Scan", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )

                    NavigationBarItem(
                        selected = currentTab == ScreenTab.DISPATCH,
                        onClick = { viewModel.selectTab(ScreenTab.DISPATCH) },
                        icon = { Icon(Icons.Default.LocalShipping, contentDescription = "Dispatch") },
                        label = { Text("Dispatch", fontSize = 11.sp) }
                    )

                    NavigationBarItem(
                        selected = currentTab == ScreenTab.RETURNS,
                        onClick = { viewModel.selectTab(ScreenTab.RETURNS) },
                        icon = {
                            Icon(Icons.Default.AssignmentReturn, contentDescription = "Returns")
                        },
                        label = { Text("Returns", fontSize = 11.sp) }
                    )
                }
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                ScreenTab.PARCELS -> {
                    ShipmentsScreen(
                        shipments = filteredShipments,
                        searchQuery = searchQuery,
                        filterStatus = filterStatus,
                        filterPlatform = filterPlatform,
                        filterCourier = filterCourier,
                        filterDateRange = filterDateRange,
                        currentUser = currentUser,
                        onSearchChange = viewModel::setSearchQuery,
                        onStatusFilterChange = viewModel::setFilterStatus,
                        onPlatformFilterChange = viewModel::setFilterPlatform,
                        onCourierFilterChange = viewModel::setFilterCourier,
                        onDateRangeFilterChange = viewModel::setFilterDateRange,
                        onAdvanceStatus = viewModel::advanceShipmentStatus,
                        onOpenReturnDialog = {
                            viewModel.selectTab(ScreenTab.RETURNS)
                        },
                        onCreateManualShipment = viewModel::createManualShipment,
                        onUpdateNotes = viewModel::updateShipmentNotes
                    )
                }
                ScreenTab.SCAN -> {
                    ScanScreen(
                        scanState = scanState,
                        currentUser = currentUser,
                        onCodeChange = viewModel::onScanCodeEntered,
                        onPlatformChange = viewModel::updateScanPlatform,
                        onCourierChange = viewModel::updateScanCourier,
                        onRecipientChange = viewModel::updateScanRecipient,
                        onToggleOffline = viewModel::toggleOfflineSimulation,
                        onSubmitParcel = viewModel::submitScannedParcel
                    )
                }
                ScreenTab.DISPATCH -> {
                    DispatchScreen(
                        shipments = allShipments,
                        batches = allBatches,
                        currentUser = currentUser,
                        onCreateBatch = viewModel::createDispatchHandover
                    )
                }
                ScreenTab.RETURNS -> {
                    ReturnsScreen(
                        returns = allReturns,
                        shipments = allShipments,
                        currentUser = currentUser,
                        onLogReturn = viewModel::logParcelReturn,
                        onApproveRefund = viewModel::approveRefundStatus
                    )
                }
                ScreenTab.DASHBOARD -> {
                    if (isOwner) {
                        DashboardScreen(
                            shipments = allShipments,
                            batches = allBatches,
                            returns = allReturns,
                            currentUser = currentUser,
                            onOpenStaffDialog = { showStaffDialog = true }
                        )
                    } else {
                        // Restricted fallback if non-owner lands here
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                "Analytics is restricted to Owner Nolan.",
                                color = Color(0xFF64748B),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Snackbar Notification Overlay
            if (snackbarMessage != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Slate900,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = snackbarMessage ?: "",
                            color = Color.White,
                            fontSize = 12.sp,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { snackbarMessage = null }) {
                            Text("OK", color = AccentAmber, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    // Quick Switch Account Dialog
    if (showUserSwitchDialog) {
        AlertDialog(
            onDismissRequest = { showUserSwitchDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 16.dp),
            title = {
                Text(
                    text = "Switch Active Account",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Current: ${currentUser.fullName} (${currentUser.role.label})",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    val uniqueUsers = remember(allUsers) {
                        allUsers
                            .filter {
                                val u = it.username.trim().lowercase()
                                u !in listOf("maria", "juan", "asher", "nolan", "gj")
                            }
                            .distinctBy {
                                when (it.username.trim().lowercase()) {
                                    "nolancaparros", "nolan" -> "nolancaparros"
                                    "gjcaparros", "gj" -> "gjcaparros"
                                    else -> it.username.trim().lowercase()
                                }
                            }
                            .distinctBy { it.fullName.trim().lowercase() }
                            .sortedWith(
                                compareBy(
                                    { it.role != UserRole.OWNER },
                                    { !it.fullName.contains("Caparros", ignoreCase = true) },
                                    { !it.isActive },
                                    { it.fullName }
                                )
                            )
                    }
                    uniqueUsers.forEach { user ->
                        val isCurrent = user.id == currentUser.id
                        val isDeactivated = !user.isActive

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isDeactivated) {
                                        snackbarMessage = "Cannot switch: @${user.username} is deactivated. Contact Owner Nolan Caparros."
                                    } else {
                                        viewModel.switchUser(user)
                                        showUserSwitchDialog = false
                                    }
                                },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isDeactivated) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                            else if (isCurrent) LogisticsBlueLight.copy(alpha = 0.5f)
                            else MaterialTheme.colorScheme.surfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isDeactivated) StatusRed.copy(alpha = 0.5f)
                                else if (isCurrent) LogisticsBlue
                                else MaterialTheme.colorScheme.outlineVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = user.fullName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = if (isDeactivated) StatusRed else if (isCurrent) LogisticsBlueDark else MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1
                                        )
                                        if (isDeactivated) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = StatusRedLight
                                            ) {
                                                Text(
                                                    text = "DEACTIVATED",
                                                    fontSize = 9.sp,
                                                    color = StatusRed,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${user.role.label} • @${user.username} • PIN: ${user.pin}",
                                        fontSize = 11.sp,
                                        color = if (isDeactivated) StatusRed.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (isCurrent) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Active account",
                                        tint = LogisticsBlue
                                    )
                                }
                            }
                        }
                    }

                    if (isOwner) {
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedButton(
                            onClick = {
                                showUserSwitchDialog = false
                                showStaffDialog = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.ManageAccounts, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Manage Staff Accounts & Roles", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showUserSwitchDialog = false
                        onLogout()
                    }
                ) {
                    Text("Log Out", color = StatusRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showUserSwitchDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Staff Management Dialog (Owner only)
    if (showStaffDialog) {
        StaffManagementDialog(
            users = allUsers,
            onDismiss = { showStaffDialog = false },
            onAddStaff = { u, fn, p, r, s, d, ret, e ->
                viewModel.addStaffMember(u, fn, p, r, s, d, ret, e)
            },
            onToggleActive = { u, reason ->
                viewModel.toggleStaffStatus(u, reason)
            },
            onUpdatePermissions = { u, s, d, ret, e ->
                viewModel.updateStaffPermissions(u, s, d, ret, e)
            }
        )
    }
}
