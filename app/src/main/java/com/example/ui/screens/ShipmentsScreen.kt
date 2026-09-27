package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.DateRangeFilter
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShipmentsScreen(
    shipments: List<Shipment>,
    searchQuery: String,
    filterStatus: ShipmentStatus?,
    filterPlatform: PlatformType?,
    filterCourier: CourierType?,
    filterDateRange: DateRangeFilter,
    currentUser: User?,
    onSearchChange: (String) -> Unit,
    onStatusFilterChange: (ShipmentStatus?) -> Unit,
    onPlatformFilterChange: (PlatformType?) -> Unit,
    onCourierFilterChange: (CourierType?) -> Unit,
    onDateRangeFilterChange: (DateRangeFilter) -> Unit,
    onAdvanceStatus: (Shipment, ShipmentStatus) -> Unit,
    onOpenReturnDialog: (Shipment) -> Unit,
    onCreateManualShipment: (String, PlatformType, CourierType, ShipmentStatus, String, String, String, String, (Boolean, String) -> Unit) -> Unit,
    onUpdateNotes: (Shipment, String) -> Unit
) {
    var selectedShipment by remember { mutableStateOf<Shipment?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var actionFeedback by remember { mutableStateOf<Pair<Boolean, String>?>(null) }

    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Search & Comprehensive Filter Header (MODULE 2)
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchChange,
                        placeholder = { Text("Search tracking #, buyer, notes...", fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = LogisticsBlue) },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { onSearchChange("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("shipment_search_field")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Date Range Filters
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Date:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(DateRangeFilter.values()) { dr ->
                                FilterChip(
                                    selected = filterDateRange == dr,
                                    onClick = { onDateRangeFilterChange(dr) },
                                    label = {
                                        Text(
                                            text = dr.label,
                                            fontSize = 11.sp,
                                            fontWeight = if (filterDateRange == dr) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Status Filters
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Status:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            item {
                                FilterChip(
                                    selected = filterStatus == null,
                                    onClick = { onStatusFilterChange(null) },
                                    label = {
                                        Text(
                                            text = "All",
                                            fontSize = 11.sp,
                                            fontWeight = if (filterStatus == null) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                )
                            }
                            items(ShipmentStatus.values()) { status ->
                                FilterChip(
                                    selected = filterStatus == status,
                                    onClick = {
                                        onStatusFilterChange(if (filterStatus == status) null else status)
                                    },
                                    label = {
                                        Text(
                                            text = status.label,
                                            fontSize = 11.sp,
                                            fontWeight = if (filterStatus == status) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Platform & Courier Filters Row
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        item {
                            FilterChip(
                                selected = filterPlatform == null,
                                onClick = { onPlatformFilterChange(null) },
                                label = {
                                    Text(
                                        text = "All Platforms",
                                        fontSize = 11.sp,
                                        fontWeight = if (filterPlatform == null) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            )
                        }
                        items(PlatformType.values()) { platform ->
                            FilterChip(
                                selected = filterPlatform == platform,
                                onClick = {
                                    onPlatformFilterChange(if (filterPlatform == platform) null else platform)
                                },
                                label = {
                                    Text(
                                        text = platform.displayName,
                                        fontSize = 11.sp,
                                        fontWeight = if (filterPlatform == platform) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // Results count & Quick Manual Add Action
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${shipments.size} Outbound Parcels Found",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                TextButton(
                    onClick = { showCreateDialog = true },
                    modifier = Modifier.testTag("add_parcel_text_button")
                ) {
                    Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Manual Entry", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }

            // Shipments List
            if (shipments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Shipments Found",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Scan new barcodes, log manual parcels, or adjust filters",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(shipments, key = { it.id }) { item ->
                        val isDark = isSystemInDarkTheme()
                        val (statusBg, statusFg) = when (item.status) {
                            ShipmentStatus.SCANNED -> if (isDark) Slate700 to Slate100 else Slate200 to Slate800
                            ShipmentStatus.PREPARED -> if (isDark) AccentAmberContainerDark to AccentAmberLight else AccentAmberLight to AccentAmberDark
                            ShipmentStatus.DISPATCHED -> if (isDark) LogisticsBlueContainerDark to LogisticsBlueLight else LogisticsBlueLight to LogisticsBlueDark
                            ShipmentStatus.DELIVERED -> if (isDark) StatusGreenContainerDark to StatusGreenLight else StatusGreenLight to Color(0xFF047857)
                            ShipmentStatus.RETURNED -> if (isDark) StatusRedContainerDark to StatusRedLight else StatusRedLight to Color(0xFFB91C1C)
                            ShipmentStatus.CANCELLED -> if (isDark) Slate700 to Slate300 else Slate200 to Slate700
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedShipment = item },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = item.trackingNumber,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = statusBg
                                    ) {
                                        Text(
                                            text = item.status.label,
                                            color = statusFg,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant
                                        ) {
                                            Text(
                                                text = item.platform.displayName,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant
                                        ) {
                                            Text(
                                                text = item.courier.displayName,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    if (item.syncStatus == SyncStatus.PENDING_SYNC) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.SyncProblem,
                                                contentDescription = "Pending Sync",
                                                tint = StatusOrange,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Offline Queue",
                                                fontSize = 11.sp,
                                                color = StatusOrange,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }

                                if (item.recipientName.isNotBlank() || item.notes.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = if (item.recipientName.isNotBlank()) "Buyer: ${item.recipientName}" else item.notes,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Scanned: ${dateFormat.format(Date(item.scannedAt))} by ${item.scannedByName}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Floating Action Button for Manual Entry
        FloatingActionButton(
            onClick = { showCreateDialog = true },
            containerColor = LogisticsBlue,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("create_shipment_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Parcel")
        }
    }

    // ==========================================
    // MANUAL PARCEL CREATION MODAL (MODULE 2)
    // ==========================================
    if (showCreateDialog) {
        var mTracking by remember { mutableStateOf("") }
        var mPlatform by remember { mutableStateOf(PlatformType.SHOPEE) }
        var mCourier by remember { mutableStateOf(CourierType.JT_EXPRESS) }
        var mStatus by remember { mutableStateOf(ShipmentStatus.PREPARED) }
        var mRecipient by remember { mutableStateOf("") }
        var mPhone by remember { mutableStateOf("") }
        var mAddress by remember { mutableStateOf("") }
        var mNotes by remember { mutableStateOf("") }

        var platformMenuOpen by remember { mutableStateOf(false) }
        var courierMenuOpen by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = {
                Text(
                    text = "Add Outbound Shipment Record",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = mTracking,
                        onValueChange = { mTracking = it.uppercase() },
                        label = { Text("Tracking / Reference # *") },
                        placeholder = { Text("e.g. SPXPH09928172") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Platform Selector
                    ExposedDropdownMenuBox(
                        expanded = platformMenuOpen,
                        onExpandedChange = { platformMenuOpen = !platformMenuOpen }
                    ) {
                        OutlinedTextField(
                            value = mPlatform.displayName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Selling Platform") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = platformMenuOpen) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = platformMenuOpen,
                            onDismissRequest = { platformMenuOpen = false }
                        ) {
                            PlatformType.values().forEach { p ->
                                DropdownMenuItem(
                                    text = { Text(p.displayName) },
                                    onClick = {
                                        mPlatform = p
                                        platformMenuOpen = false
                                    }
                                )
                            }
                        }
                    }

                    // Courier Selector
                    ExposedDropdownMenuBox(
                        expanded = courierMenuOpen,
                        onExpandedChange = { courierMenuOpen = !courierMenuOpen }
                    ) {
                        OutlinedTextField(
                            value = mCourier.displayName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Assigned Courier") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = courierMenuOpen) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = courierMenuOpen,
                            onDismissRequest = { courierMenuOpen = false }
                        ) {
                            CourierType.values().forEach { c ->
                                DropdownMenuItem(
                                    text = { Text(c.displayName) },
                                    onClick = {
                                        mCourier = c
                                        courierMenuOpen = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = mRecipient,
                        onValueChange = { mRecipient = it },
                        label = { Text("Recipient / Buyer Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = mNotes,
                        onValueChange = { mNotes = it },
                        label = { Text("Package Contents / Notes") },
                        placeholder = { Text("e.g. 2x Oversized T-Shirt, Black") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (mTracking.isNotBlank()) {
                            onCreateManualShipment(
                                mTracking,
                                mPlatform,
                                mCourier,
                                mStatus,
                                mRecipient,
                                mPhone,
                                mAddress,
                                mNotes
                            ) { success, msg ->
                                actionFeedback = Pair(success, msg)
                                if (success) {
                                    showCreateDialog = false
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LogisticsBlue)
                ) {
                    Text("Save Parcel")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // ==========================================
    // DETAIL MODAL WITH AUDIT TIMELINE (MODULE 2)
    // ==========================================
    if (selectedShipment != null) {
        val s = selectedShipment!!
        var isEditingNotes by remember { mutableStateOf(false) }
        var editableNotes by remember { mutableStateOf(s.notes) }

        AlertDialog(
            onDismissRequest = { selectedShipment = null },
            title = {
                Column {
                    Text(
                        text = s.trackingNumber,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${s.platform.displayName} • ${s.courier.displayName}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Status: ${s.status.label}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Handler: ${s.scannedByName}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (s.recipientName.isNotBlank()) {
                        Text(
                            text = "Recipient: ${s.recipientName}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Editable Notes section
                    if (isEditingNotes) {
                        OutlinedTextField(
                            value = editableNotes,
                            onValueChange = { editableNotes = it },
                            label = { Text("Edit Package Notes") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    onUpdateNotes(s, editableNotes)
                                    isEditingNotes = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = LogisticsBlue),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Save Notes", fontSize = 11.sp)
                            }
                            OutlinedButton(
                                onClick = { isEditingNotes = false },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Cancel", fontSize = 11.sp)
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (s.notes.isNotBlank()) "Notes: ${s.notes}" else "Notes: (None provided)",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { isEditingNotes = true }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Notes", tint = LogisticsBlue, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    Divider(
                        color = MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Text(
                        text = "End-to-End Operational Timeline:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = s.timelineLog,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(10.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Quick Status Actions:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (s.status == ShipmentStatus.DISPATCHED) {
                            Button(
                                onClick = {
                                    onAdvanceStatus(s, ShipmentStatus.DELIVERED)
                                    selectedShipment = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = StatusGreen),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Mark Delivered", fontSize = 12.sp)
                            }
                        }

                        if (s.status != ShipmentStatus.RETURNED) {
                            OutlinedButton(
                                onClick = {
                                    val target = s
                                    selectedShipment = null
                                    onOpenReturnDialog(target)
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusRed),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Record Return", fontSize = 12.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedShipment = null }) {
                    Text("Close")
                }
            }
        )
    }
}
