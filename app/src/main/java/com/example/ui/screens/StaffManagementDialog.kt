package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.User
import com.example.data.model.UserRole
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffManagementDialog(
    users: List<User>,
    onDismiss: () -> Unit,
    onAddStaff: (String, String, String, UserRole, Boolean, Boolean, Boolean, Boolean) -> Unit,
    onToggleActive: (User, String) -> Unit,
    onUpdatePermissions: (User, Boolean, Boolean, Boolean, Boolean) -> Unit
) {
    var showAddForm by remember { mutableStateOf(false) }
    var selectedUserForPermissions by remember { mutableStateOf<User?>(null) }
    var deactivationTargetUser by remember { mutableStateOf<User?>(null) }
    var deactivationReasonInput by remember { mutableStateOf("Account deactivated by Owner Nolan Caparros") }

    // New Staff form inputs
    var newUsername by remember { mutableStateOf("") }
    var newFullName by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var newRole by remember { mutableStateOf(UserRole.STAFF) }
    var newCanScan by remember { mutableStateOf(true) }
    var newCanDispatch by remember { mutableStateOf(true) }
    var newCanReturns by remember { mutableStateOf(true) }
    var newCanEdit by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .padding(vertical = 12.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Staff & Roles Manager",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Owner Control: Configure access, permissions & deactivation",
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (!showAddForm) {
                    IconButton(
                        onClick = { showAddForm = true },
                        modifier = Modifier.testTag("add_staff_icon_button")
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Add Staff", tint = LogisticsBlue)
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Add New Staff Form
                if (showAddForm) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Create Warehouse Staff Account",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = newFullName,
                                onValueChange = { newFullName = it },
                                label = { Text("Full Name") },
                                placeholder = { Text("e.g. Carlos Mendoza") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            OutlinedTextField(
                                value = newUsername,
                                onValueChange = { newUsername = it },
                                label = { Text("Username") },
                                placeholder = { Text("e.g. carlos") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            OutlinedTextField(
                                value = newPin,
                                onValueChange = { newPin = it },
                                label = { Text("4-Digit Access PIN") },
                                placeholder = { Text("e.g. 4455") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Initial Permissions:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Can Scan Parcels", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                Switch(
                                    checked = newCanScan,
                                    onCheckedChange = { newCanScan = it },
                                    modifier = Modifier.height(24.dp)
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Can Dispatch Batches", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                Switch(
                                    checked = newCanDispatch,
                                    onCheckedChange = { newCanDispatch = it },
                                    modifier = Modifier.height(24.dp)
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Can Log Returns", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                Switch(
                                    checked = newCanReturns,
                                    onCheckedChange = { newCanReturns = it },
                                    modifier = Modifier.height(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        if (newUsername.isNotBlank() && newFullName.isNotBlank() && newPin.isNotBlank()) {
                                            onAddStaff(
                                                newUsername,
                                                newFullName,
                                                newPin,
                                                newRole,
                                                newCanScan,
                                                newCanDispatch,
                                                newCanReturns,
                                                newCanEdit
                                            )
                                            newUsername = ""
                                            newFullName = ""
                                            newPin = ""
                                            showAddForm = false
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = LogisticsBlue),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Save User", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }

                                OutlinedButton(
                                    onClick = { showAddForm = false },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Cancel", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                val uniqueUsers = remember(users) {
                    users
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

                Text(
                    text = "Registered Accounts (${uniqueUsers.size}):",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uniqueUsers, key = { it.id }) { user ->
                        val isOwner = user.role == UserRole.OWNER
                        val isDeactivated = !user.isActive

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isDeactivated) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isDeactivated) StatusRed.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                // Top Row: Avatar, Name, Role/Status Pill, and Quick Actions
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isDeactivated) StatusRedLight
                                                else if (isOwner) LogisticsBlueLight
                                                else AccentAmberLight
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isDeactivated) Icons.Default.PersonOff
                                            else if (isOwner) Icons.Default.AdminPanelSettings
                                            else Icons.Default.Person,
                                            contentDescription = null,
                                            tint = if (isDeactivated) StatusRed else if (isOwner) LogisticsBlue else AccentAmberDark,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = user.fullName,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = if (isDeactivated) StatusRed else MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f, fill = false)
                                            )

                                            if (isOwner) {
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = LogisticsBlueLight
                                                ) {
                                                    Text(
                                                        text = "Owner",
                                                        fontSize = 10.sp,
                                                        color = LogisticsBlueDark,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            } else if (isDeactivated) {
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = StatusRedLight
                                                ) {
                                                    Text(
                                                        text = "DEACTIVATED",
                                                        fontSize = 9.sp,
                                                        color = StatusRed,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            } else {
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = AccentAmberLight
                                                ) {
                                                    Text(
                                                        text = "Staff",
                                                        fontSize = 10.sp,
                                                        color = AccentAmberDark,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "@${user.username} • PIN: ${user.pin}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    // Quick action buttons for active staff
                                    if (!isOwner && user.isActive) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(
                                                onClick = { selectedUserForPermissions = user },
                                                modifier = Modifier.size(34.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Tune,
                                                    contentDescription = "Edit Permissions",
                                                    tint = LogisticsBlue,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            IconButton(
                                                onClick = { deactivationTargetUser = user },
                                                modifier = Modifier.size(34.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.PersonOff,
                                                    contentDescription = "Deactivate",
                                                    tint = StatusRed,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                // For Active Staff: Privileges Bar
                                if (!isOwner && user.isActive) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 0.5.dp)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "Privileges:",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            PermissionPill(label = "Scan", allowed = user.canScanParcels)
                                            PermissionPill(label = "Dispatch", allowed = user.canDispatch)
                                            PermissionPill(label = "Returns", allowed = user.canHandleReturns)
                                        }
                                    }
                                }

                                // For Deactivated Staff: Full-width deactivation info & Reactivate button
                                if (!isOwner && isDeactivated) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Divider(color = StatusRed.copy(alpha = 0.2f), thickness = 0.5.dp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                Icons.Default.Block,
                                                contentDescription = null,
                                                tint = StatusRed,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (user.deactivationReason.isNotBlank()) user.deactivationReason else "Account disabled by Owner",
                                                fontSize = 11.sp,
                                                color = StatusRed,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Button(
                                            onClick = { onToggleActive(user, "") },
                                            colors = ButtonDefaults.buttonColors(containerColor = StatusGreen),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.height(30.dp)
                                        ) {
                                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(13.dp), tint = Color.White)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Reactivate", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", fontWeight = FontWeight.Bold)
            }
        }
    )

    // ==========================================
    // PERMISSIONS MANAGEMENT MODAL FOR SELECTED STAFF
    // ==========================================
    if (selectedUserForPermissions != null) {
        val target = selectedUserForPermissions!!
        var permScan by remember(target) { mutableStateOf(target.canScanParcels) }
        var permDispatch by remember(target) { mutableStateOf(target.canDispatch) }
        var permReturns by remember(target) { mutableStateOf(target.canHandleReturns) }
        var permEdit by remember(target) { mutableStateOf(target.canEditShipments) }

        AlertDialog(
            onDismissRequest = { selectedUserForPermissions = null },
            title = {
                Text(
                    text = "Configure Privileges for ${target.fullName}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Enable or restrict specific features for @${target.username}:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Barcode / QR Scanning", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Text("Log parcels into warehouse", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Switch(checked = permScan, onCheckedChange = { permScan = it })
                            }
                            Divider(modifier = Modifier.padding(vertical = 6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Courier Dispatch Handover", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Text("Create batches & dispatch riders", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Switch(checked = permDispatch, onCheckedChange = { permDispatch = it })
                            }
                            Divider(modifier = Modifier.padding(vertical = 6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Record Returns", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Text("Log returned or RTS parcels", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Switch(checked = permReturns, onCheckedChange = { permReturns = it })
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdatePermissions(target, permScan, permDispatch, permReturns, permEdit)
                        selectedUserForPermissions = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LogisticsBlue)
                ) {
                    Text("Apply Privileges")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedUserForPermissions = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // ==========================================
    // DEACTIVATION CONFIRMATION DIALOG
    // ==========================================
    if (deactivationTargetUser != null) {
        val target = deactivationTargetUser!!
        AlertDialog(
            onDismissRequest = { deactivationTargetUser = null },
            icon = {
                Icon(Icons.Default.PersonOff, contentDescription = null, tint = StatusRed, modifier = Modifier.size(32.dp))
            },
            title = {
                Text(
                    text = "Deactivate ${target.fullName}?",
                    fontWeight = FontWeight.Bold,
                    color = StatusRed
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Once deactivated, @${target.username} will be immediately locked out. They will NOT be able to log in, scan parcels, or perform operations until you reactivate them.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = deactivationReasonInput,
                        onValueChange = { deactivationReasonInput = it },
                        label = { Text("Reason for Deactivation") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onToggleActive(target, deactivationReasonInput)
                        deactivationTargetUser = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusRed)
                ) {
                    Text("Confirm Deactivation", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deactivationTargetUser = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun PermissionPill(label: String, allowed: Boolean) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = if (allowed) StatusGreenLight else StatusRedLight
    ) {
        Text(
            text = if (allowed) "✓ $label" else "✗ $label",
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = if (allowed) StatusGreenDark else StatusRedDark,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )
    }
}
