package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
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
fun DispatchScreen(
    shipments: List<Shipment>,
    batches: List<DispatchBatch>,
    currentUser: User?,
    onCreateBatch: (CourierType, String, List<String>, String, (Boolean, String) -> Unit) -> Unit
) {
    var selectedCourier by remember { mutableStateOf(CourierType.JT_EXPRESS) }
    var courierExpanded by remember { mutableStateOf(false) }
    var riderName by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var feedbackMessage by remember { mutableStateOf<String?>(null) }
    var viewManifestBatch by remember { mutableStateOf<DispatchBatch?>(null) }
    var generatedManifestBatch by remember { mutableStateOf<DispatchBatch?>(null) }

    val clipboardManager = LocalClipboardManager.current
    var copiedToClipboard by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    // Eligible parcels for dispatch are SCANNED or PREPARED for the selected courier
    val eligibleParcels = remember(shipments, selectedCourier) {
        shipments.filter {
            (it.status == ShipmentStatus.PREPARED || it.status == ShipmentStatus.SCANNED) &&
                    it.courier == selectedCourier
        }
    }

    val selectedParcelIds = remember { mutableStateListOf<String>() }

    // Auto-select all eligible when courier changes or list updates
    LaunchedEffect(eligibleParcels) {
        selectedParcelIds.clear()
        selectedParcelIds.addAll(eligibleParcels.map { it.id })
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // MODULE 5: Handover Creation & Batch Grouping Form
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocalShipping,
                            contentDescription = null,
                            tint = LogisticsBlue,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Courier Dispatch & Handover",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = "Group prepared parcels and log official handover records to courier riders.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                    )

                    // Operational Handover Context: Staff & Date/Time
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Badge,
                                    contentDescription = null,
                                    tint = LogisticsBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Dispatcher: ${currentUser?.fullName ?: "Staff"}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = dateFormat.format(Date()),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 1. Courier Selection Dropdown
                    ExposedDropdownMenuBox(
                        expanded = courierExpanded,
                        onExpandedChange = { courierExpanded = !courierExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedCourier.displayName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("1. Select Courier for Pickup") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = courierExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = courierExpanded,
                            onDismissRequest = { courierExpanded = false }
                        ) {
                            CourierType.values().forEach { courier ->
                                DropdownMenuItem(
                                    text = { Text(courier.displayName) },
                                    onClick = {
                                        selectedCourier = courier
                                        courierExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 2. Rider & Vehicle info
                    OutlinedTextField(
                        value = riderName,
                        onValueChange = { riderName = it },
                        label = { Text("2. Courier Rider Name & Plate Number") },
                        placeholder = { Text("e.g. Kuya Rey (Plate: 492-NCR)") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = LogisticsBlue)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("rider_name_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 3. Handover Notes
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("3. Handover Notes (Optional)") },
                        placeholder = { Text("e.g. Batch #1 afternoon pickup, cage verified") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Eligible Parcels Checklist Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "4. Select Prepared Parcels (${selectedParcelIds.size}/${eligibleParcels.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        TextButton(
                            onClick = {
                                if (selectedParcelIds.size == eligibleParcels.size) {
                                    selectedParcelIds.clear()
                                } else {
                                    selectedParcelIds.clear()
                                    selectedParcelIds.addAll(eligibleParcels.map { it.id })
                                }
                            }
                        ) {
                            Text(
                                text = if (selectedParcelIds.size == eligibleParcels.size) "Deselect All" else "Select All",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    if (eligibleParcels.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "No prepared parcels currently queued for ${selectedCourier.displayName}. Scan barcodes in the Scan tab to assign them to this courier.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            eligibleParcels.forEach { parcel ->
                                val isChecked = selectedParcelIds.contains(parcel.id)
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (isChecked) selectedParcelIds.remove(parcel.id)
                                            else selectedParcelIds.add(parcel.id)
                                        },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isChecked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surfaceVariant,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isChecked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = parcel.trackingNumber,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "${parcel.platform.displayName} • ${parcel.recipientName.ifBlank { "Direct Buyer" }}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Checkbox(
                                            checked = isChecked,
                                            onCheckedChange = {
                                                if (it) selectedParcelIds.add(parcel.id)
                                                else selectedParcelIds.remove(parcel.id)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    val isDispatchAllowed = currentUser?.role == UserRole.OWNER || (currentUser?.canDispatch == true)
                    if (!isDispatchAllowed) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = StatusRedLight,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = StatusRed, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Dispatch Restricted: Your account does not have permission to dispatch parcels. Please consult Nolan Caparros (Owner).",
                                    color = Color(0xFF991B1B),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    if (feedbackMessage != null) {
                        Text(
                            text = feedbackMessage ?: "",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    Button(
                        enabled = isDispatchAllowed && selectedParcelIds.isNotEmpty() && riderName.isNotBlank() && !isSubmitting,
                        onClick = {
                            isSubmitting = true
                            val count = selectedParcelIds.size
                            val currentRider = riderName
                            val currentNotes = notes
                            onCreateBatch(
                                selectedCourier,
                                currentRider,
                                selectedParcelIds.toList(),
                                currentNotes
                            ) { success, msg ->
                                isSubmitting = false
                                feedbackMessage = msg
                                if (success) {
                                    // Generate and show manifest summary
                                    val newBatch = DispatchBatch(
                                        id = "batch_${System.currentTimeMillis()}",
                                        batchNumber = "MANIFEST-${selectedCourier.shortName.uppercase()}-${System.currentTimeMillis().toString().takeLast(6)}",
                                        courier = selectedCourier,
                                        handoverTimestamp = System.currentTimeMillis(),
                                        dispatchedByUserId = currentUser?.id ?: "",
                                        dispatchedByName = currentUser?.fullName ?: "Staff",
                                        courierRiderName = currentRider,
                                        totalParcels = count,
                                        notes = currentNotes,
                                        syncStatus = SyncStatus.SYNCED
                                    )
                                    generatedManifestBatch = newBatch
                                    riderName = ""
                                    notes = ""
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("confirm_handover_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LogisticsBlue)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Confirm Handover & Generate Manifest (${selectedParcelIds.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // Recent Dispatches List (Handover Records)
        item {
            Text(
                text = "Recent Handover Manifests",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (batches.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No recorded dispatch manifests yet. Complete a handover above to generate your first manifest.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        } else {
            items(batches, key = { it.id }) { batch ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewManifestBatch = batch },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = batch.batchNumber,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "${batch.totalParcels} Parcels",
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Courier: ${batch.courier.displayName} • Rider: ${batch.courierRiderName}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Dispatched by: ${batch.dispatchedByName} • ${dateFormat.format(Date(batch.handoverTimestamp))}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    // ==========================================
    // DISPATCH / HANDOVER SUMMARY MANIFEST DIALOG
    // ==========================================
    val activeManifest = generatedManifestBatch ?: viewManifestBatch
    if (activeManifest != null) {
        val b = activeManifest
        val parcelsInBatch = shipments.filter { it.dispatchBatchId == b.id || (generatedManifestBatch != null && selectedParcelIds.contains(it.id)) }
        val displayCount = if (parcelsInBatch.isNotEmpty()) parcelsInBatch.size else b.totalParcels

        AlertDialog(
            onDismissRequest = {
                generatedManifestBatch = null
                viewManifestBatch = null
                copiedToClipboard = false
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = null,
                        tint = LogisticsBlue,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Official Handover Manifest",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        color = LogisticsBlueLight,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Manifest Reference: ${b.batchNumber}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = LogisticsBlueDark,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Status: Signed Handover Completed",
                                fontSize = 11.sp,
                                color = LogisticsBlueDark,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Text(
                        text = "• Courier: ${b.courier.displayName}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "• Receiving Rider: ${b.courierRiderName}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "• Dispatched By: ${b.dispatchedByName}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "• Date & Time: ${dateFormat.format(Date(b.handoverTimestamp))}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (b.notes.isNotBlank()) {
                        Text(
                            text = "• Notes: ${b.notes}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Divider(modifier = Modifier.padding(vertical = 4.dp))

                    Text(
                        text = "Itemized Manifest Parcels ($displayCount):",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 160.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (parcelsInBatch.isNotEmpty()) {
                            items(parcelsInBatch) { p ->
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = p.trackingNumber,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = p.platform.displayName,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        } else {
                            item {
                                Text(
                                    text = "$displayCount parcels officially handed over in this batch.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    if (copiedToClipboard) {
                        Text(
                            text = "✓ Manifest text copied to clipboard!",
                            color = StatusGreenDark,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val summaryText = buildString {
                            appendLine("--- GJandAsher Dispatch Manifest ---")
                            appendLine("Manifest: ${b.batchNumber}")
                            appendLine("Courier: ${b.courier.displayName}")
                            appendLine("Rider: ${b.courierRiderName}")
                            appendLine("Staff: ${b.dispatchedByName}")
                            appendLine("Timestamp: ${dateFormat.format(Date(b.handoverTimestamp))}")
                            appendLine("Total Parcels: $displayCount")
                            parcelsInBatch.forEach { p ->
                                appendLine("• ${p.trackingNumber} (${p.platform.displayName})")
                            }
                        }
                        clipboardManager.setText(AnnotatedString(summaryText))
                        copiedToClipboard = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LogisticsBlue)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (copiedToClipboard) "Copied!" else "Copy Manifest")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        generatedManifestBatch = null
                        viewManifestBatch = null
                        copiedToClipboard = false
                    }
                ) {
                    Text("Close")
                }
            }
        )
    }
}
