package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReturnsScreen(
    returns: List<ReturnRecord>,
    shipments: List<Shipment>,
    currentUser: User?,
    onLogReturn: (Shipment, ReturnReason, ItemCondition, String, (Boolean, String) -> Unit) -> Unit,
    onApproveRefund: (ReturnRecord, RefundStatus) -> Unit
) {
    var showLogDialog by remember { mutableStateOf(false) }
    var selectedShipmentForReturn by remember { mutableStateOf<Shipment?>(null) }
    var shipmentSearchQuery by remember { mutableStateOf("") }

    var selectedReason by remember { mutableStateOf(ReturnReason.FAILED_DELIVERY) }
    var reasonExpanded by remember { mutableStateOf(false) }

    var selectedCondition by remember { mutableStateOf(ItemCondition.GOOD_RESALABLE) }
    var conditionExpanded by remember { mutableStateOf(false) }

    var returnNotes by remember { mutableStateOf("") }
    var actionFeedback by remember { mutableStateOf<String?>(null) }

    val isOwner = currentUser?.role == UserRole.OWNER
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Summary & Action Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AssignmentReturn,
                                contentDescription = null,
                                tint = StatusRed,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Returns & Refunds Hub",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        val canLogReturns = currentUser?.role == UserRole.OWNER || (currentUser?.canHandleReturns == true)
                        Button(
                            onClick = {
                                if (canLogReturns) {
                                    showLogDialog = true
                                } else {
                                    actionFeedback = "Returns logging restricted by Owner Nolan."
                                }
                            },
                            enabled = canLogReturns,
                            colors = ButtonDefaults.buttonColors(containerColor = StatusRed),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("log_return_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Log Return", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Metrics row
                    val pendingCount = returns.count { it.refundStatus == RefundStatus.PENDING_APPROVAL }
                    val resalableCount = returns.count { it.itemCondition == ItemCondition.GOOD_RESALABLE }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val isDark = isSystemInDarkTheme()
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            color = if (isDark) StatusOrangeContainerDark else StatusOrangeLight
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Pending Refunds",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) StatusOrangeLight else Color(0xFFC2410C)
                                )
                                Text(
                                    text = "$pendingCount Parcels",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) MaterialTheme.colorScheme.onSurface else Slate900
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            color = if (isDark) StatusGreenContainerDark else StatusGreenLight
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Resalable Restocked",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) StatusGreenLight else Color(0xFF047857)
                                )
                                Text(
                                    text = "$resalableCount Parcels",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) MaterialTheme.colorScheme.onSurface else Slate900
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Recorded Returns (${returns.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (returns.isEmpty()) {
            item {
                Text(
                    text = "No parcel returns recorded yet. Click 'Log Return' to record returned or failed shipments.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(returns, key = { it.id }) { record ->
                val isDark = isSystemInDarkTheme()
                val refundBadgeColor = when (record.refundStatus) {
                    RefundStatus.PENDING_APPROVAL -> if (isDark) StatusOrangeLight to StatusOrangeContainerDark else Color(0xFFC2410C) to StatusOrangeLight
                    RefundStatus.PROCESSED_REFUNDED -> if (isDark) StatusGreenLight to StatusGreenContainerDark else Color(0xFF047857) to StatusGreenLight
                    RefundStatus.DENIED_DISPUTED -> if (isDark) StatusRedLight to StatusRedContainerDark else Color(0xFFB91C1C) to StatusRedLight
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = record.trackingNumber,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = refundBadgeColor.second
                            ) {
                                Text(
                                    text = record.refundStatus.label,
                                    color = refundBadgeColor.first,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Reason: ${record.reason.label}",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Condition: ${record.itemCondition.label} • Platform: ${record.platform.displayName}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (record.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Notes: ${record.notes}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Logged by ${record.loggedByName} • ${dateFormat.format(Date(record.loggedAt))}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Role-based refund approval actions (OWNER ONLY)
                        if (isOwner && record.refundStatus == RefundStatus.PENDING_APPROVAL) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        onApproveRefund(record, RefundStatus.PROCESSED_REFUNDED)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = StatusGreen),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Approve Refund", fontSize = 11.sp)
                                }

                                OutlinedButton(
                                    onClick = {
                                        onApproveRefund(record, RefundStatus.DENIED_DISPUTED)
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusRed),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Dispute / Deny", fontSize = 11.sp)
                                }
                            }
                        } else if (!isOwner && record.refundStatus == RefundStatus.PENDING_APPROVAL) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "🔒 Requires Owner approval to process refund",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }

    // Log Return Dialog
    if (showLogDialog) {
        val nonReturnedShipments = shipments.filter { it.status != ShipmentStatus.RETURNED }
        val matchingShipments = remember(nonReturnedShipments, shipmentSearchQuery) {
            if (shipmentSearchQuery.isBlank()) nonReturnedShipments.take(6)
            else nonReturnedShipments.filter {
                it.trackingNumber.contains(shipmentSearchQuery, ignoreCase = true) ||
                        it.recipientName.contains(shipmentSearchQuery, ignoreCase = true)
            }
        }

        AlertDialog(
            onDismissRequest = { showLogDialog = false },
            title = { Text("Log Parcel Return") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (selectedShipmentForReturn == null) {
                        Text(text = "Search & Select Original Shipment:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = shipmentSearchQuery,
                            onValueChange = { shipmentSearchQuery = it },
                            placeholder = { Text("Search tracking #...") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(matchingShipments) { s ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedShipmentForReturn = s },
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(
                                            text = s.trackingNumber,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${s.platform.displayName} • ${s.recipientName} (${s.status.label})",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Selected parcel preview
                        val s = selectedShipmentForReturn!!
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = s.trackingNumber,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "${s.platform.displayName} • ${s.courier.displayName}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                                    )
                                }
                                TextButton(onClick = { selectedShipmentForReturn = null }) {
                                    Text("Change", fontSize = 11.sp)
                                }
                            }
                        }

                        // Return Reason Dropdown
                        ExposedDropdownMenuBox(
                            expanded = reasonExpanded,
                            onExpandedChange = { reasonExpanded = !reasonExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedReason.label,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Reason for Return") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = reasonExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = reasonExpanded,
                                onDismissRequest = { reasonExpanded = false }
                            ) {
                                ReturnReason.values().forEach { reason ->
                                    DropdownMenuItem(
                                        text = { Text(reason.label) },
                                        onClick = {
                                            selectedReason = reason
                                            reasonExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Item Condition Dropdown
                        ExposedDropdownMenuBox(
                            expanded = conditionExpanded,
                            onExpandedChange = { conditionExpanded = !conditionExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedCondition.label,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Item Condition on Arrival") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = conditionExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = conditionExpanded,
                                onDismissRequest = { conditionExpanded = false }
                            ) {
                                ItemCondition.values().forEach { cond ->
                                    DropdownMenuItem(
                                        text = { Text(cond.label) },
                                        onClick = {
                                            selectedCondition = cond
                                            conditionExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = returnNotes,
                            onValueChange = { returnNotes = it },
                            label = { Text("Warehouse Inspection Notes") },
                            placeholder = { Text("e.g. Returned to stock shelf A2") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val s = selectedShipmentForReturn
                        if (s != null) {
                            onLogReturn(s, selectedReason, selectedCondition, returnNotes) { ok, msg ->
                                showLogDialog = false
                                selectedShipmentForReturn = null
                                returnNotes = ""
                            }
                        }
                    },
                    enabled = selectedShipmentForReturn != null,
                    colors = ButtonDefaults.buttonColors(containerColor = StatusRed)
                ) {
                    Text("Confirm Return Log")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showLogDialog = false
                    selectedShipmentForReturn = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}
