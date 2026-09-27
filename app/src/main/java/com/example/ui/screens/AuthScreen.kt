package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.User
import com.example.data.model.UserRole
import com.example.ui.theme.*

@Composable
fun AuthScreen(
    users: List<User>,
    onLogin: (String, String, (Boolean, String) -> Unit) -> Unit,
    onQuickSignIn: (User, (Boolean, String) -> Unit) -> Unit,
    onRegister: (String, String, String, UserRole, (Boolean, String) -> Unit) -> Unit
) {
    var isSignUpMode by remember { mutableStateOf(false) }

    // Sign In inputs
    var loginUsername by remember { mutableStateOf("") }
    var loginPin by remember { mutableStateOf("") }

    // Sign Up inputs
    var regFullName by remember { mutableStateOf("") }
    var regUsername by remember { mutableStateOf("") }
    var regPin by remember { mutableStateOf("") }
    var regConfirmPin by remember { mutableStateOf("") }

    // Alerts & Dialogs
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isSuccessMessage by remember { mutableStateOf(false) }
    var deactivatedAlertUser by remember { mutableStateOf<Pair<String, String>?>(null) }
    var showOwnerContactDialog by remember { mutableStateOf(false) }

    // Sort and deduplicate users so Nolan Caparros and GJ Caparros are prominently displayed first
    val sortedUsers = remember(users) {
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            // App Branding Header
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(LogisticsBlue),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocalShipping,
                    contentDescription = "GJandAsher ShipTracker Logo",
                    tint = Color.White,
                    modifier = Modifier.size(46.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "GJandAsher ShipTracker",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "Outbound Logistics, Analytics & Returns Manager",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Dedicated Top Header (Clean single indicator as requested)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (!isSignUpMode) Icons.Default.Login else Icons.Default.PersonAdd,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (!isSignUpMode) "Log In" else "Create An Account / Sign-Up",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Feedback Message Banner
            if (statusMessage != null) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSuccessMessage) StatusGreenLight else StatusRedLight,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSuccessMessage) StatusGreen else StatusRed
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isSuccessMessage) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = if (isSuccessMessage) Color(0xFF065F46) else Color(0xFF991B1B),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = statusMessage ?: "",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSuccessMessage) Color(0xFF065F46) else Color(0xFF991B1B),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        if (!isSignUpMode) {
            // ==========================================
            // LOG IN MODE
            // ==========================================
            item {
                // 1-Tap Quick Sign-In for evaluators, owner, and staff
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FlashOn,
                                contentDescription = null,
                                tint = AccentAmber,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Quick Demo Sign-In (1-Tap)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "Tap any pre-made account below to instantly test roles & workflows:",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                        )

                        sortedUsers.forEach { user ->
                            val isOwner = user.role == UserRole.OWNER
                            val isDeactivated = !user.isActive

                            val roleBadgeColor = when {
                                isDeactivated -> StatusRed
                                isOwner -> LogisticsBlue
                                else -> AccentAmberDark
                            }
                            val roleBadgeBg = when {
                                isDeactivated -> StatusRedLight
                                isOwner -> LogisticsBlueLight
                                else -> AccentAmberLight
                            }

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(
                                        width = 1.dp,
                                        color = if (isDeactivated) StatusRed.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        if (isDeactivated) {
                                            deactivatedAlertUser = Pair(user.fullName, user.username)
                                        } else {
                                            onQuickSignIn(user) { success, msg ->
                                                isSuccessMessage = success
                                                statusMessage = msg
                                                if (!success && msg.contains("DEACTIVATED", ignoreCase = true)) {
                                                    deactivatedAlertUser = Pair(user.fullName, user.username)
                                                }
                                            }
                                        }
                                    },
                                color = if (isDeactivated) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(roleBadgeBg),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = when {
                                                    isDeactivated -> Icons.Default.Block
                                                    isOwner -> Icons.Default.AdminPanelSettings
                                                    else -> Icons.Default.Person
                                                },
                                                contentDescription = null,
                                                tint = roleBadgeColor,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = user.fullName,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    color = if (isDeactivated) StatusRed else MaterialTheme.colorScheme.onSurface
                                                )
                                                if (isDeactivated) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "DEACTIVATED",
                                                        fontSize = 10.sp,
                                                        color = StatusRed,
                                                        fontWeight = FontWeight.ExtraBold
                                                    )
                                                }
                                            }
                                            Text(
                                                text = "@${user.username} • PIN: ${user.pin}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = if (isDeactivated) StatusRed else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = roleBadgeBg
                                    ) {
                                        Text(
                                            text = when {
                                                isDeactivated -> "Restricted"
                                                isOwner -> "Owner / Admin"
                                                else -> "Warehouse Staff"
                                            },
                                            color = roleBadgeColor,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Manual Credentials Form
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Or Enter Username & Access PIN",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = loginUsername,
                            onValueChange = {
                                loginUsername = it
                                statusMessage = null
                            },
                            label = { Text("Username") },
                            placeholder = { Text("e.g. nolancaparros or gjcaparros") },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null, tint = LogisticsBlue)
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("username_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = loginPin,
                            onValueChange = {
                                loginPin = it
                                statusMessage = null
                            },
                            label = { Text("Access PIN (e.g. 1234 or 1111)") },
                            visualTransformation = PasswordVisualTransformation(),
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = LogisticsBlue)
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("pin_input")
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (loginUsername.isBlank() || loginPin.isBlank()) {
                                    statusMessage = "Please enter both username and PIN."
                                    isSuccessMessage = false
                                    return@Button
                                }
                                onLogin(loginUsername, loginPin) { success, msg ->
                                    isSuccessMessage = success
                                    statusMessage = msg
                                    if (!success && msg.contains("DEACTIVATED", ignoreCase = true)) {
                                        deactivatedAlertUser = Pair(loginUsername, loginUsername)
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("login_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = LogisticsBlue)
                        ) {
                            Icon(Icons.Default.Login, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Log In to Operations",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Switch to create account button
                        OutlinedButton(
                            onClick = {
                                isSignUpMode = true
                                statusMessage = null
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("create_account_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "New Staff? Create an Account / Sign-Up",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        } else {
            // ==========================================
            // SIGN-UP / CREATE ACCOUNT MODE
            // ==========================================
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Badge,
                                contentDescription = null,
                                tint = LogisticsBlue,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Register New Staff Account",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = "New staff accounts are registered with secure SHA-256 PIN hashing.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                        )

                        OutlinedTextField(
                            value = regFullName,
                            onValueChange = {
                                regFullName = it
                                statusMessage = null
                            },
                            label = { Text("Full Name") },
                            placeholder = { Text("e.g. Carlos Mendoza") },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null)
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("signup_fullname_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = regUsername,
                            onValueChange = {
                                regUsername = it
                                statusMessage = null
                            },
                            label = { Text("Username (Unique ID)") },
                            placeholder = { Text("e.g. carlos") },
                            leadingIcon = {
                                Icon(Icons.Default.AlternateEmail, contentDescription = null)
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("signup_username_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = regPin,
                            onValueChange = {
                                regPin = it
                                statusMessage = null
                            },
                            label = { Text("4-Digit Access PIN / Password") },
                            placeholder = { Text("e.g. 5566") },
                            visualTransformation = PasswordVisualTransformation(),
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null)
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("signup_pin_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = regConfirmPin,
                            onValueChange = {
                                regConfirmPin = it
                                statusMessage = null
                            },
                            label = { Text("Confirm 4-Digit Access PIN") },
                            visualTransformation = PasswordVisualTransformation(),
                            leadingIcon = {
                                Icon(Icons.Default.LockReset, contentDescription = null)
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Staff Role indicator notice (Sign up is strictly for staff users; owner manages accounts)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = AccentAmberLight,
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Inventory2,
                                            contentDescription = null,
                                            tint = AccentAmberDark,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Account Role: Warehouse Staff",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Sign-up is for staff users. Account permissions and roles are managed exclusively by Owner Nolan Caparros.",
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = {
                                if (regFullName.isBlank() || regUsername.isBlank() || regPin.isBlank()) {
                                    statusMessage = "Please fill in all registration fields."
                                    isSuccessMessage = false
                                    return@Button
                                }
                                if (regPin.trim() != regConfirmPin.trim()) {
                                    statusMessage = "PINs do not match! Please check entered PIN."
                                    isSuccessMessage = false
                                    return@Button
                                }
                                onRegister(regFullName, regUsername, regPin, UserRole.STAFF) { success, msg ->
                                    isSuccessMessage = success
                                    statusMessage = msg
                                    if (success) {
                                        regFullName = ""
                                        regUsername = ""
                                        regPin = ""
                                        regConfirmPin = ""
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("signup_submit_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = LogisticsBlue)
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Create Staff Account", fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        TextButton(
                            onClick = {
                                isSignUpMode = false
                                statusMessage = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Already registered? Back to Log In", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(18.dp))

            // Role Guidelines Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Role-Based Access Control (RBAC)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• Nolan Caparros (Owner): Manages staff accounts, permissions, dispatch batches, analytics dashboards, and return refunds.",
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "• GJ Caparros (Warehouse Staff): Handles barcode scanning, validation, courier dispatch, and logging returned packages.",
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // ==========================================
    // DEACTIVATED / RESTRICTED ACCOUNT ALERT DIALOG
    // ==========================================
    if (deactivatedAlertUser != null) {
        val (targetName, targetUsername) = deactivatedAlertUser!!
        AlertDialog(
            onDismissRequest = { deactivatedAlertUser = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Block,
                    contentDescription = null,
                    tint = StatusRed,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Account Deactivated / Restricted",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = StatusRed
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "The account for $targetName (@$targetUsername) has been deactivated by Owner Nolan Caparros.",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "As an internal security policy, deactivated accounts cannot log in, scan parcels, or perform warehouse operations.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Surface(
                        color = StatusRedLight,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = StatusRed, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Action Required: Contact Nolan Caparros (Owner) to review permissions or reactivate your account.",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF7F1D1D)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        deactivatedAlertUser = null
                        showOwnerContactDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LogisticsBlue)
                ) {
                    Icon(Icons.Default.ContactPhone, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Contact Owner (Nolan)")
                }
            },
            dismissButton = {
                TextButton(onClick = { deactivatedAlertUser = null }) {
                    Text("Dismiss")
                }
            }
        )
    }

    // Owner Contact Info Sheet
    if (showOwnerContactDialog) {
        AlertDialog(
            onDismissRequest = { showOwnerContactDialog = false },
            title = {
                Text("Contact Business Owner", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Owner: Nolan Caparros",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Role: Business Owner & System Administrator",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                    Text(
                        text = "📍 Warehouse Office: Bay 3, Central Hub\n📞 Hotline / Viber: 0917-888-NOLAN\n📧 Admin Email: nolan@gjandashershiptracker.local",
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showOwnerContactDialog = false }) {
                    Text("Got It")
                }
            }
        )
    }
}
