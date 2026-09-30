package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.ScanUiState
import com.example.util.ClassificationHelper
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    scanState: ScanUiState,
    currentUser: User?,
    onCodeChange: (String) -> Unit,
    onPlatformChange: (PlatformType) -> Unit,
    onCourierChange: (CourierType) -> Unit,
    onRecipientChange: (String, String, String) -> Unit,
    onToggleOffline: () -> Unit,
    onSubmitParcel: ((Boolean, String) -> Unit) -> Unit
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    var isSubmitting by remember { mutableStateOf(false) }
    var feedbackText by remember { mutableStateOf<String?>(null) }
    var isSuccessFeedback by remember { mutableStateOf(false) }
    var isTorchOn by remember { mutableStateOf(false) }

    val cameraManager = remember {
        try {
            context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
        } catch (e: Exception) {
            null
        }
    }

    fun toggleFlashlight() {
        val nextState = !isTorchOn
        isTorchOn = nextState
        try {
            if (cameraManager != null) {
                val cameraId = cameraManager.cameraIdList.firstOrNull { id ->
                    val chars = cameraManager.getCameraCharacteristics(id)
                    chars.get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                } ?: cameraManager.cameraIdList.firstOrNull()
                if (cameraId != null) {
                    cameraManager.setTorchMode(cameraId, nextState)
                }
            }
        } catch (_: Exception) {
            // Software illumination fallback active
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            if (isTorchOn) {
                try {
                    val cameraId = cameraManager?.cameraIdList?.firstOrNull()
                    if (cameraId != null) cameraManager?.setTorchMode(cameraId, false)
                } catch (_: Exception) {}
            }
        }
    }

    // Dropdown expansion states for manual correction
    var platformExpanded by remember { mutableStateOf(false) }
    var courierExpanded by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()) }

    // Laser scanning animation
    val infiniteTransition = rememberInfiniteTransition(label = "laser")
    val laserOffsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 90f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laserOffset"
    )

    val validationResult = remember(scanState.inputCode) {
        ClassificationHelper.validateTrackingFormat(scanState.inputCode)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // MODULE 3: Barcode / QR Camera Scanner Viewfinder Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate700)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = null,
                                tint = AccentAmber,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Camera Barcode & QR Scanner",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        // Offline Mode Switch
                        FilterChip(
                            selected = scanState.isOfflineMode,
                            onClick = onToggleOffline,
                            label = {
                                Text(
                                    text = if (scanState.isOfflineMode) "Offline Mode" else "Online Mode",
                                    fontSize = 11.sp,
                                    color = if (scanState.isOfflineMode) StatusOrange else StatusGreen
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (scanState.isOfflineMode) Icons.Default.CloudOff else Icons.Default.CloudDone,
                                    contentDescription = null,
                                    tint = if (scanState.isOfflineMode) StatusOrange else StatusGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Scanner Viewfinder Area
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0F172A))
                            .border(1.dp, Slate700, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!hasCameraPermission) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = Slate400,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Camera Permission Required for Live Scanning",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                                    colors = ButtonDefaults.buttonColors(containerColor = LogisticsBlue),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text("Grant Camera Access", fontSize = 12.sp)
                                }
                            }
                        } else {
                            // Flashlight active illumination background effect for dark places
                            if (isTorchOn) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.radialGradient(
                                                colors = listOf(
                                                    Color(0xFFFFFAEB).copy(alpha = 0.38f),
                                                    Color(0xFFFEF3C7).copy(alpha = 0.18f),
                                                    Color.Transparent
                                                )
                                            )
                                        )
                                )
                            }

                            // Active Scanner Viewfinder reticle
                            Box(
                                modifier = Modifier
                                    .size(width = 250.dp, height = 100.dp)
                                    .border(2.dp, if (isTorchOn) Color(0xFFFBBF24) else LogisticsBlue, RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.TopCenter
                            ) {
                                // Animated Laser scanning beam
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(2.dp)
                                        .offset(y = laserOffsetY.dp)
                                        .background(if (isTorchOn) Color(0xFFF59E0B) else AccentAmber)
                                )
                            }

                            // Flashlight / Torch Controls & Status
                            Row(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isTorchOn) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFFFEF3C7)
                                    ) {
                                        Text(
                                            text = "Torch ON",
                                            color = Color(0xFF92400E),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { toggleFlashlight() },
                                    modifier = Modifier
                                        .size(34.dp)
                                        .background(
                                            if (isTorchOn) Color(0xFFFBBF24) else Slate800.copy(alpha = 0.85f),
                                            CircleShape
                                        )
                                ) {
                                    Icon(
                                        imageVector = if (isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                                        contentDescription = "Torch / Flashlight",
                                        tint = if (isTorchOn) Color(0xFF78350F) else Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Text(
                                text = if (isTorchOn) "🔦 Flashlight active • Scanner ready" else "Position barcode or QR inside reticle frame",
                                color = if (isTorchOn) Color(0xFFFEF3C7) else Color(0xFFCBD5E1),
                                fontSize = 11.sp,
                                fontWeight = if (isTorchOn) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick Camera Simulation Barcode Triggers (Test Samples)
                    Text(
                        text = "Or Tap Sample Barcodes to Test Auto-Detection:",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SampleBarcodeButton(
                                label = "Shopee SPX",
                                dotColor = Color(0xFFEE4D2D),
                                testTag = "sample_barcode_shopee",
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    val randomSuffix = (1000..9999).random()
                                    onCodeChange("SPXPH049$randomSuffix")
                                }
                            )
                            SampleBarcodeButton(
                                label = "J&T Express",
                                dotColor = Color(0xFFE11D48),
                                testTag = "sample_barcode_jnt",
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    val randomSuffix = (10000..99999).random()
                                    onCodeChange("JZ992$randomSuffix")
                                }
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SampleBarcodeButton(
                                label = "Lazada LEX",
                                dotColor = Color(0xFF0060FF),
                                testTag = "sample_barcode_lazada",
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    val randomSuffix = (1000..9999).random()
                                    onCodeChange("LZD-882$randomSuffix")
                                }
                            )
                            SampleBarcodeButton(
                                label = "TikTok Shop",
                                dotColor = Color(0xFF06B6D4),
                                testTag = "sample_barcode_tiktok",
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    val randomSuffix = (1000..9999).random()
                                    onCodeChange("TTS990$randomSuffix")
                                }
                            )
                        }
                    }
                }
            }
        }

        // MODULE 3 & 4: Manual Fallback, Validation, & Classification
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Scanned Tracking Code",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Format Validation Indicator Badge
                        if (scanState.inputCode.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (validationResult.isValid) StatusGreenLight else StatusRedLight
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (validationResult.isValid) Icons.Default.Check else Icons.Default.Close,
                                        contentDescription = null,
                                        tint = if (validationResult.isValid) StatusGreenDark else StatusRed,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (validationResult.isValid) "Valid Format" else "Invalid Format",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (validationResult.isValid) StatusGreenDark else StatusRed
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Manual Entry Fallback Input
                    OutlinedTextField(
                        value = scanState.inputCode,
                        onValueChange = onCodeChange,
                        placeholder = { Text("Manual entry fallback: e.g. SPXPH049281729") },
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        leadingIcon = {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = LogisticsBlue)
                        },
                        trailingIcon = {
                            if (scanState.inputCode.isNotBlank()) {
                                IconButton(onClick = { onCodeChange("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear input")
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tracking_code_input")
                    )

                    // DUPLICATE SCAN DETECTION & BLOCKING BANNER
                    AnimatedVisibility(visible = scanState.duplicateWarning != null) {
                        val dup = scanState.duplicateWarning
                        if (dup != null) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp),
                                shape = RoundedCornerShape(10.dp),
                                color = StatusRedLight,
                                border = androidx.compose.foundation.BorderStroke(1.dp, StatusRed)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Duplicate Alert",
                                        tint = StatusRed,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "⚠️ DUPLICATE PARCEL DETECTED & BLOCKED!",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = StatusRedDark
                                        )
                                        Text(
                                            text = "Previously recorded on: ${dateFormat.format(Date(dup.scannedAt))}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF7F1D1D)
                                        )
                                        Text(
                                            text = "Status: ${dup.status.label} • Handled by: ${dup.scannedByName}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF7F1D1D)
                                        )
                                        Text(
                                            text = "Double scan blocked to prevent duplicate warehouse inventory.",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF991B1B)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // MODULE 4: Platform & Courier Classification
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Platform & Courier Classification",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (scanState.classificationNote.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = LogisticsBlueLight
                            ) {
                                Text(
                                    text = scanState.classificationNote,
                                    fontSize = 10.sp,
                                    color = LogisticsBlueDark,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = "Auto-detected based on barcode pattern. Tap dropdown to manually correct if needed:",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                    )

                    // Platform Dropdown Selector (Manual Correction Option)
                    ExposedDropdownMenuBox(
                        expanded = platformExpanded,
                        onExpandedChange = { platformExpanded = !platformExpanded }
                    ) {
                        OutlinedTextField(
                            value = scanState.detectedPlatform.displayName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Selling Platform (Auto-Detected)") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = platformExpanded)
                            },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = platformExpanded,
                            onDismissRequest = { platformExpanded = false }
                        ) {
                            PlatformType.values().forEach { platform ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "${platform.displayName} (${platform.codePrefixHint})",
                                            fontWeight = if (platform == scanState.detectedPlatform) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        onPlatformChange(platform)
                                        platformExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Courier Dropdown Selector (Manual Correction Option)
                    ExposedDropdownMenuBox(
                        expanded = courierExpanded,
                        onExpandedChange = { courierExpanded = !courierExpanded }
                    ) {
                        OutlinedTextField(
                            value = scanState.detectedCourier.displayName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Assigned Courier (Auto-Detected)") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = courierExpanded)
                            },
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
                                    text = {
                                        Text(
                                            text = courier.displayName,
                                            fontWeight = if (courier == scanState.detectedCourier) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        onCourierChange(courier)
                                        courierExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Optional details (Recipient & Notes)
                    OutlinedTextField(
                        value = scanState.recipientName,
                        onValueChange = {
                            onRecipientChange(it, scanState.recipientPhone, scanState.notes)
                        },
                        label = { Text("Recipient Name (Optional)") },
                        placeholder = { Text("e.g. Maria Santos") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = scanState.notes,
                        onValueChange = {
                            onRecipientChange(scanState.recipientName, scanState.recipientPhone, it)
                        },
                        label = { Text("Item Contents / Handling Notes (Optional)") },
                        placeholder = { Text("e.g. Fragile ceramic cup, size XL") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Feedback Banner
                    if (feedbackText != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSuccessFeedback) StatusGreenLight else StatusRedLight,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Text(
                                text = feedbackText ?: "",
                                color = if (isSuccessFeedback) Color(0xFF065F46) else Color(0xFF991B1B),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    // Staff Privilege Check
                    val isScanAllowed = currentUser?.role == UserRole.OWNER || (currentUser?.canScanParcels == true)
                    if (!isScanAllowed) {
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
                                    text = "Scanning Restricted: Your account does not have permission to log parcels. Please consult Nolan Caparros (Owner).",
                                    color = Color(0xFF991B1B),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // Main Action Button (Blocked if duplicate or invalid format or restricted)
                    val canSubmit = isScanAllowed &&
                            scanState.inputCode.isNotBlank() &&
                            scanState.duplicateWarning == null &&
                            validationResult.isValid

                    Button(
                        onClick = {
                            isSubmitting = true
                            onSubmitParcel { success, message ->
                                isSubmitting = false
                                feedbackText = message
                                isSuccessFeedback = success
                            }
                        },
                        enabled = canSubmit && !isSubmitting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("confirm_scan_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LogisticsBlue)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when {
                                    scanState.duplicateWarning != null -> "Blocked: Duplicate Parcel"
                                    scanState.isOfflineMode -> "Save Parcel (Offline Sync Mode)"
                                    else -> "Confirm & Prepare for Dispatch"
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SampleBarcodeButton(
    label: String,
    dotColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .testTag(testTag)
            .heightIn(min = 44.dp),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(dotColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
