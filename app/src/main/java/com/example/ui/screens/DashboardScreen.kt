package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

@Composable
fun DashboardScreen(
    shipments: List<Shipment>,
    batches: List<DispatchBatch>,
    returns: List<ReturnRecord>,
    currentUser: User?,
    onOpenStaffDialog: (() -> Unit)? = null
) {
    val isOwner = currentUser?.role == UserRole.OWNER
    var reportOutputText by remember { mutableStateOf<String?>(null) }
    val clipboardManager = LocalClipboardManager.current
    var copiedNotice by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }

    if (!isOwner) {
        // Staff Restricted Access View
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Restricted",
                        tint = AccentAmber,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Owner / Admin Privileges Required",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Analytics, financial reports, and staff account management are restricted to the business owner per capstone role specifications.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "💡 Tip: Switch user role to 'Nolan (Owner)' in the top profile menu to explore full admin features!",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
        return
    }

    // Calculations
    val totalCount = shipments.size
    val preparedCount = shipments.count { it.status == ShipmentStatus.PREPARED }
    val dispatchedCount = shipments.count { it.status == ShipmentStatus.DISPATCHED }
    val deliveredCount = shipments.count { it.status == ShipmentStatus.DELIVERED }
    val returnedCount = shipments.count { it.status == ShipmentStatus.RETURNED }
    val returnRate = if (totalCount > 0) (returnedCount.toDouble() / totalCount * 100).toInt() else 0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Owner Header (Clean without redundant Staff button)
        item {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Executive Operations Dashboard",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Live outbound metrics & courier performance",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // KPI Summary Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                KpiCard(
                    modifier = Modifier.weight(1f),
                    title = "Total Volume",
                    value = "$totalCount",
                    icon = Icons.Default.AllInbox,
                    tint = LogisticsBlue,
                    bgColor = LogisticsBlueLight
                )
                KpiCard(
                    modifier = Modifier.weight(1f),
                    title = "Dispatched",
                    value = "$dispatchedCount",
                    icon = Icons.Default.LocalShipping,
                    tint = AccentAmber,
                    bgColor = AccentAmberLight
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                KpiCard(
                    modifier = Modifier.weight(1f),
                    title = "Delivered",
                    value = "$deliveredCount",
                    icon = Icons.Default.CheckCircle,
                    tint = StatusGreen,
                    bgColor = StatusGreenLight
                )
                KpiCard(
                    modifier = Modifier.weight(1f),
                    title = "Return Rate",
                    value = "$returnRate%",
                    icon = Icons.Default.AssignmentReturn,
                    tint = StatusRed,
                    bgColor = StatusRedLight
                )
            }
        }

        // Platform Breakdown
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Shipment Volume by Selling Platform",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    PlatformType.values().forEach { platform ->
                        val count = shipments.count { it.platform == platform }
                        val fraction = if (totalCount > 0) count.toFloat() / totalCount else 0f
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = platform.displayName, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Text(
                                    text = "$count parcels (${(fraction * 100).toInt()}%)",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { fraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = LogisticsBlue,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Courier Comparison
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Courier Dispatch & Delivery Efficiency",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    CourierType.values().forEach { courier ->
                        val courierShipments = shipments.filter { it.courier == courier }
                        val delivered = courierShipments.count { it.status == ShipmentStatus.DELIVERED }
                        val returnsForCourier = courierShipments.count { it.status == ShipmentStatus.RETURNED }

                        if (courierShipments.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = courier.displayName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(
                                        text = "${courierShipments.size} handled • $delivered delivered • $returnsForCourier returns",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                val successRate = (delivered.toFloat() / courierShipments.size * 100).toInt()
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (successRate >= 50) StatusGreenLight else AccentAmberLight
                                ) {
                                    Text(
                                        text = "$successRate% Success",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (successRate >= 50) Color(0xFF047857) else AccentAmberDark,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            Divider(color = MaterialTheme.colorScheme.outlineVariant)
                        }
                    }
                }
            }
        }

        // Export Reports (MODULE 10)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Assessment, contentDescription = null, tint = LogisticsBlue)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Generate Reports & Manifests",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val sb = StringBuilder()
                                sb.append("═══════════════════════════════════════════\n")
                                sb.append("GJANDASHER SHIPTRACKER - DISPATCH MANIFEST\n")
                                sb.append("Generated on: ${dateFormat.format(Date())}\n")
                                sb.append("Total Dispatched Batches: ${batches.size}\n")
                                sb.append("Owner / Supervisor: Nolan\n")
                                sb.append("═══════════════════════════════════════════\n\n")
                                batches.forEach { b ->
                                    sb.append("Batch: ${b.batchNumber} | Courier: ${b.courier.displayName}\n")
                                    sb.append("Rider: ${b.courierRiderName} | Parcels: ${b.totalParcels}\n")
                                    sb.append("Dispatched by: ${b.dispatchedByName}\n")
                                    sb.append("-------------------------------------------\n")
                                }
                                reportOutputText = sb.toString()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LogisticsBlue),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Dispatch Report", fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                val sb = StringBuilder()
                                sb.append("═══════════════════════════════════════════\n")
                                sb.append("GJANDASHER SHIPTRACKER - RETURNS & REFUNDS AUDIT\n")
                                sb.append("Generated on: ${dateFormat.format(Date())}\n")
                                sb.append("Total Recorded Returns: ${returns.size}\n")
                                sb.append("Owner / Supervisor: Nolan\n")
                                sb.append("═══════════════════════════════════════════\n\n")
                                returns.forEach { r ->
                                    sb.append("AWB: ${r.trackingNumber} [${r.platform.displayName}]\n")
                                    sb.append("Reason: ${r.reason.label} | Condition: ${r.itemCondition.label}\n")
                                    sb.append("Refund Status: ${r.refundStatus.label}\n")
                                    sb.append("Logged By: ${r.loggedByName}\n")
                                    sb.append("-------------------------------------------\n")
                                }
                                reportOutputText = sb.toString()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = StatusRed),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Returns Report", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            val sb = StringBuilder()
                            sb.append("TrackingNumber,Platform,Courier,Status,Recipient,ScannedAt,ScannedBy\n")
                            shipments.forEach { s ->
                                sb.append("${s.trackingNumber},${s.platform.name},${s.courier.name},${s.status.name},\"${s.recipientName}\",${s.scannedAt},\"${s.scannedByName}\"\n")
                            }
                            reportOutputText = sb.toString()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export CSV Format (Excel Compatible)", fontSize = 12.sp)
                    }
                }
            }
        }
    }

    // Report Output Preview Modal
    if (reportOutputText != null) {
        AlertDialog(
            onDismissRequest = {
                reportOutputText = null
                copiedNotice = false
            },
            title = { Text("Report Preview") },
            text = {
                Column {
                    Surface(
                        color = Slate900,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp)
                    ) {
                        Text(
                            text = reportOutputText ?: "",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = Slate100,
                            modifier = Modifier.padding(10.dp)
                        )
                    }

                    if (copiedNotice) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "✅ Copied to clipboard! Ready to paste into document or spreadsheet.",
                            color = StatusGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(reportOutputText ?: ""))
                        copiedNotice = true
                    }
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy Report")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    reportOutputText = null
                    copiedNotice = false
                }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun KpiCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    bgColor: Color
) {
    val isDark = isSystemInDarkTheme()
    val resolvedBgColor = if (isDark) tint.copy(alpha = 0.18f) else bgColor
    val titleColor = if (isDark) tint else Color(0xFF334155)
    val valueColor = if (isDark) MaterialTheme.colorScheme.onSurface else Color(0xFF0F172A)
    val iconBgColor = if (isDark) tint.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.8f)

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = resolvedBgColor
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = titleColor)
                Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = valueColor)
            }
        }
    }
}
