package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.repository.AuthResult
import com.example.data.repository.ShipTrackerRepository
import com.example.util.ClassificationHelper
import com.example.util.SecurityUtil
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

enum class ScreenTab(val title: String) {
    PARCELS("Parcels"),
    DISPATCH("Dispatch"),
    SCAN("Scan & Validate"),
    RETURNS("Returns"),
    DASHBOARD("Analytics")
}

enum class DateRangeFilter(val label: String) {
    ALL("All Dates"),
    TODAY("Today"),
    LAST_7_DAYS("Last 7 Days"),
    OLDER("Older")
}

data class ScanUiState(
    val inputCode: String = "",
    val detectedPlatform: PlatformType = PlatformType.SHOPEE,
    val detectedCourier: CourierType = CourierType.JT_EXPRESS,
    val classificationNote: String = "",
    val recipientName: String = "",
    val recipientPhone: String = "",
    val notes: String = "",
    val duplicateWarning: Shipment? = null,
    val feedbackMessage: String? = null,
    val isSuccess: Boolean = false,
    val isOfflineMode: Boolean = false
)

class ShipTrackerViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application, viewModelScope)
    val repository = ShipTrackerRepository(db)

    // Splash Screen State
    private val _isSplashVisible = MutableStateFlow(true)
    val isSplashVisible: StateFlow<Boolean> = _isSplashVisible.asStateFlow()

    fun dismissSplash() {
        _isSplashVisible.value = false
    }

    // Current Session (null = logged out / at Auth screen)
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _currentTab = MutableStateFlow(ScreenTab.PARCELS)
    val currentTab: StateFlow<ScreenTab> = _currentTab.asStateFlow()

    // Data streams
    val allShipments: StateFlow<List<Shipment>> = repository.allShipments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBatches: StateFlow<List<DispatchBatch>> = repository.allBatches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allReturns: StateFlow<List<ReturnRecord>> = repository.allReturns
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsers: StateFlow<List<User>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Scan State
    private val _scanState = MutableStateFlow(ScanUiState())
    val scanState: StateFlow<ScanUiState> = _scanState.asStateFlow()

    // Shipments Filter State (MODULE 2)
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filterStatus = MutableStateFlow<ShipmentStatus?>(null)
    val filterStatus: StateFlow<ShipmentStatus?> = _filterStatus.asStateFlow()

    private val _filterPlatform = MutableStateFlow<PlatformType?>(null)
    val filterPlatform: StateFlow<PlatformType?> = _filterPlatform.asStateFlow()

    private val _filterCourier = MutableStateFlow<CourierType?>(null)
    val filterCourier: StateFlow<CourierType?> = _filterCourier.asStateFlow()

    private val _filterDateRange = MutableStateFlow(DateRangeFilter.ALL)
    val filterDateRange: StateFlow<DateRangeFilter> = _filterDateRange.asStateFlow()

    // Filtered Shipments Stream
    val filteredShipments: StateFlow<List<Shipment>> = combine(
        allShipments,
        _searchQuery,
        _filterStatus,
        _filterPlatform,
        combine(_filterCourier, _filterDateRange) { c, d -> Pair(c, d) }
    ) { list, query, status, platform, (courier, dateRange) ->
        val now = System.currentTimeMillis()
        val oneDayMillis = 24 * 3600 * 1000L
        val sevenDaysMillis = 7 * oneDayMillis

        list.filter { item ->
            val matchesQuery = query.isBlank() ||
                    item.trackingNumber.contains(query, ignoreCase = true) ||
                    item.recipientName.contains(query, ignoreCase = true) ||
                    item.notes.contains(query, ignoreCase = true)

            val matchesStatus = status == null || item.status == status
            val matchesPlatform = platform == null || item.platform == platform
            val matchesCourier = courier == null || item.courier == courier

            val age = now - item.scannedAt
            val matchesDate = when (dateRange) {
                DateRangeFilter.ALL -> true
                DateRangeFilter.TODAY -> age <= oneDayMillis
                DateRangeFilter.LAST_7_DAYS -> age <= sevenDaysMillis
                DateRangeFilter.OLDER -> age > sevenDaysMillis
            }

            matchesQuery && matchesStatus && matchesPlatform && matchesCourier && matchesDate
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Pending Sync count
    val pendingSyncCount: StateFlow<Int> = allShipments.map { list ->
        list.count { it.syncStatus == SyncStatus.PENDING_SYNC }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        // Observe users to sync active session, eliminate duplicates, and enforce single owner
        viewModelScope.launch {
            allUsers.collect { users ->
                if (users.isEmpty()) return@collect

                // 1. Remove legacy or duplicate alias accounts
                val aliasesToRemove = users.filter {
                    val u = it.username.trim().lowercase()
                    u in listOf("maria", "juan", "asher", "nolan", "gj") ||
                    (u != "nolancaparros" && it.fullName.equals("Nolan Caparros", ignoreCase = true)) ||
                    (u != "gjcaparros" && it.fullName.equals("GJ Caparros", ignoreCase = true))
                }
                for (old in aliasesToRemove) {
                    repository.deleteUser(old)
                }

                // 2. DEDUPLICATE: Remove any duplicate users having the same username or same full name
                val seenUsernames = mutableSetOf<String>()
                val seenFullNames = mutableSetOf<String>()
                for (u in users) {
                    if (aliasesToRemove.contains(u)) continue
                    val normUser = u.username.trim().lowercase()
                    val normName = u.fullName.trim().lowercase()
                    if (seenUsernames.contains(normUser) || seenFullNames.contains(normName)) {
                        repository.deleteUser(u)
                    } else {
                        seenUsernames.add(normUser)
                        seenFullNames.add(normName)
                    }
                }

                // 3. Ensure canonical Nolan Caparros (Owner) exists without duplication
                val nolan = users.firstOrNull { it.username.equals("nolancaparros", ignoreCase = true) }
                if (nolan == null) {
                    repository.insertUser(
                        User(
                            id = "usr_owner_01",
                            username = "nolancaparros",
                            fullName = "Nolan Caparros",
                            role = UserRole.OWNER,
                            pin = "1234",
                            passwordHash = SecurityUtil.hashPin("1234"),
                            isActive = true,
                            canScanParcels = true,
                            canDispatch = true,
                            canHandleReturns = true,
                            canEditShipments = true
                        )
                    )
                } else if (nolan.fullName != "Nolan Caparros" || nolan.role != UserRole.OWNER || nolan.pin != "1234") {
                    repository.updateUser(
                        nolan.copy(
                            username = "nolancaparros",
                            fullName = "Nolan Caparros",
                            pin = "1234",
                            passwordHash = SecurityUtil.hashPin("1234"),
                            role = UserRole.OWNER,
                            isActive = true
                        )
                    )
                }

                // 4. Ensure canonical GJ Caparros (Staff) exists without duplication
                val gj = users.firstOrNull { it.username.equals("gjcaparros", ignoreCase = true) }
                if (gj == null) {
                    repository.insertUser(
                        User(
                            id = "usr_staff_01",
                            username = "gjcaparros",
                            fullName = "GJ Caparros",
                            role = UserRole.STAFF,
                            pin = "1111",
                            passwordHash = SecurityUtil.hashPin("1111"),
                            isActive = true,
                            canScanParcels = true,
                            canDispatch = true,
                            canHandleReturns = true,
                            canEditShipments = true
                        )
                    )
                } else if (gj.fullName != "GJ Caparros" || gj.role != UserRole.STAFF || gj.pin != "1111") {
                    repository.updateUser(
                        gj.copy(
                            username = "gjcaparros",
                            fullName = "GJ Caparros",
                            pin = "1111",
                            passwordHash = SecurityUtil.hashPin("1111"),
                            role = UserRole.STAFF,
                            isActive = true
                        )
                    )
                }

                // 5. Enforce that only Nolan Caparros is Owner; all other users are staff accounts
                for (u in users) {
                    if (u.role == UserRole.OWNER && !u.username.equals("nolancaparros", ignoreCase = true)) {
                        repository.updateUser(u.copy(role = UserRole.STAFF))
                    }
                }

                // If current logged in user was deactivated in the database, log out immediately
                val current = _currentUser.value
                if (current != null) {
                    val freshRecord = users.firstOrNull { it.id == current.id }
                    if (freshRecord == null || !freshRecord.isActive) {
                        _currentUser.value = null
                    } else {
                        _currentUser.value = freshRecord
                    }
                }
            }
        }
    }

    // ==========================================
    // MODULE 1: AUTHENTICATION & ROLE MANAGEMENT
    // ==========================================

    fun login(username: String, pin: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            when (val auth = repository.authenticate(username, pin)) {
                is AuthResult.Success -> {
                    _currentUser.value = auth.user
                    _currentTab.value = ScreenTab.PARCELS
                    onResult(true, "Welcome back, ${auth.user.fullName} (${auth.user.role.label})")
                }
                is AuthResult.Deactivated -> {
                    onResult(
                        false,
                        "ACCOUNT DEACTIVATED: ${auth.reason}\n\nPlease consult Owner Nolan to reactivate your access."
                    )
                }
                is AuthResult.InvalidCredentials -> {
                    onResult(false, "Incorrect Access PIN. Please try again.")
                }
                is AuthResult.UserNotFound -> {
                    onResult(false, "User @$username not found. Check spelling or create an account.")
                }
            }
        }
    }

    fun quickSignIn(user: User, onResult: (Boolean, String) -> Unit) {
        if (!user.isActive) {
            val reason = if (user.deactivationReason.isNotBlank()) user.deactivationReason else "Account is deactivated by Nolan (Owner)."
            onResult(false, "ACCOUNT DEACTIVATED: $reason\n\nPlease contact Nolan to reactivate this account.")
            return
        }
        _currentUser.value = user
        _currentTab.value = ScreenTab.PARCELS
        onResult(true, "Signed in as ${user.fullName} (${user.role.label})")
    }

    fun registerAccount(
        fullName: String,
        username: String,
        pin: String,
        role: UserRole = UserRole.STAFF,
        onResult: (Boolean, String) -> Unit
    ) {
        val trimmedName = fullName.trim()
        val trimmedUser = username.trim().lowercase()
        val trimmedPin = pin.trim()

        if (trimmedName.isBlank()) {
            onResult(false, "Please enter your full name.")
            return
        }
        if (trimmedUser.length < 3) {
            onResult(false, "Username must be at least 3 characters.")
            return
        }
        if (trimmedPin.length < 4) {
            onResult(false, "Security PIN must be at least 4 digits.")
            return
        }
        if (trimmedUser in listOf("nolan", "nolancaparros", "gj", "gjcaparros")) {
            onResult(false, "This username is reserved. Please choose another username.")
            return
        }

        viewModelScope.launch {
            val existing = allUsers.value.firstOrNull {
                it.username.equals(trimmedUser, ignoreCase = true) ||
                it.fullName.equals(trimmedName, ignoreCase = true)
            }
            if (existing != null) {
                onResult(false, "An account with this username or name already exists.")
                return@launch
            }

            val newUser = User(
                id = "usr_${System.currentTimeMillis()}",
                username = trimmedUser,
                fullName = trimmedName,
                role = UserRole.STAFF, // Only Nolan is Owner; all self-registrations are Staff accounts
                pin = trimmedPin,
                passwordHash = SecurityUtil.hashPin(trimmedPin),
                isActive = true,
                canScanParcels = true,
                canDispatch = true,
                canHandleReturns = true,
                canEditShipments = true,
                createdAt = System.currentTimeMillis()
            )

            repository.insertUser(newUser)
            _currentUser.value = newUser
            _currentTab.value = ScreenTab.PARCELS
            onResult(true, "Staff account created successfully! Logged in as $trimmedName.")
        }
    }

    fun logout() {
        _currentUser.value = null
        _currentTab.value = ScreenTab.PARCELS
    }

    fun switchUser(user: User) {
        if (!user.isActive) return
        _currentUser.value = user
        _currentTab.value = ScreenTab.PARCELS
    }

    fun selectTab(tab: ScreenTab) {
        // Enforce Owner-only restriction for Dashboard/Analytics
        val user = _currentUser.value
        if (tab == ScreenTab.DASHBOARD && user?.role != UserRole.OWNER) {
            return
        }
        _currentTab.value = tab
    }

    // Owner Staff Management
    fun addStaffMember(
        username: String,
        fullName: String,
        pin: String,
        role: UserRole = UserRole.STAFF,
        canScan: Boolean = true,
        canDispatch: Boolean = true,
        canReturns: Boolean = true,
        canEdit: Boolean = true
    ) {
        val cleanUsername = username.trim().lowercase()
        val cleanFullName = fullName.trim()
        val cleanPin = pin.trim()
        if (cleanUsername.isBlank() || cleanFullName.isBlank() || cleanPin.isBlank()) return

        viewModelScope.launch {
            // Check if user already exists
            val existing = allUsers.value.firstOrNull {
                it.username.trim().lowercase() == cleanUsername ||
                it.fullName.trim().lowercase() == cleanFullName.lowercase()
            }
            if (existing != null) {
                // Update existing user instead of creating duplicate
                repository.updateUser(
                    existing.copy(
                        username = cleanUsername,
                        fullName = cleanFullName,
                        pin = cleanPin,
                        passwordHash = SecurityUtil.hashPin(cleanPin),
                        role = UserRole.STAFF, // Only Nolan is Owner; other accounts are Staff
                        canScanParcels = canScan,
                        canDispatch = canDispatch,
                        canHandleReturns = canReturns,
                        canEditShipments = canEdit,
                        isActive = true
                    )
                )
            } else {
                val newUser = User(
                    id = "usr_${System.currentTimeMillis()}",
                    username = cleanUsername,
                    fullName = cleanFullName,
                    role = UserRole.STAFF, // Only Nolan is Owner; other accounts are Staff
                    pin = cleanPin,
                    passwordHash = SecurityUtil.hashPin(cleanPin),
                    isActive = true,
                    canScanParcels = canScan,
                    canDispatch = canDispatch,
                    canHandleReturns = canReturns,
                    canEditShipments = canEdit
                )
                repository.insertUser(newUser)
            }
        }
    }

    fun toggleStaffStatus(user: User, reason: String = "") {
        viewModelScope.launch {
            repository.toggleUserActive(user, reason)
        }
    }

    fun updateStaffPermissions(
        user: User,
        canScan: Boolean,
        canDispatch: Boolean,
        canReturns: Boolean,
        canEdit: Boolean
    ) {
        viewModelScope.launch {
            val updated = user.copy(
                canScanParcels = canScan,
                canDispatch = canDispatch,
                canHandleReturns = canReturns,
                canEditShipments = canEdit
            )
            repository.updateUser(updated)
        }
    }

    // ==========================================
    // MODULE 2: PARCEL / SHIPMENT MANAGEMENT
    // ==========================================

    fun setSearchQuery(q: String) {
        _searchQuery.value = q
    }

    fun setFilterStatus(s: ShipmentStatus?) {
        _filterStatus.value = s
    }

    fun setFilterPlatform(p: PlatformType?) {
        _filterPlatform.value = p
    }

    fun setFilterCourier(c: CourierType?) {
        _filterCourier.value = c
    }

    fun setFilterDateRange(d: DateRangeFilter) {
        _filterDateRange.value = d
    }

    fun createManualShipment(
        trackingNumber: String,
        platform: PlatformType,
        courier: CourierType,
        status: ShipmentStatus,
        recipientName: String,
        recipientPhone: String,
        recipientAddress: String,
        notes: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val user = _currentUser.value
        if (user == null) {
            onResult(false, "User not authenticated.")
            return
        }

        if (user.role == UserRole.STAFF && !user.canScanParcels) {
            onResult(false, "Permission Denied: Your account does not have permission to log shipments. Consult Nolan (Owner).")
            return
        }

        val cleanTracking = trackingNumber.trim().uppercase()
        if (cleanTracking.length < 5) {
            onResult(false, "Tracking number must have at least 5 characters.")
            return
        }

        viewModelScope.launch {
            val duplicate = repository.checkDuplicateTracking(cleanTracking)
            if (duplicate != null) {
                onResult(false, "Duplicate parcel detected! Tracking number $cleanTracking already exists.")
                return@launch
            }

            val shipment = Shipment(
                id = "shp_${System.currentTimeMillis()}",
                trackingNumber = cleanTracking,
                platform = platform,
                courier = courier,
                status = status,
                recipientName = recipientName.trim().ifBlank { "Direct Buyer" },
                recipientPhone = recipientPhone.trim(),
                recipientAddress = recipientAddress.trim().ifBlank { "Standard outbound address" },
                scannedAt = System.currentTimeMillis(),
                scannedByUserId = user.id,
                scannedByName = user.fullName,
                notes = notes.trim(),
                syncStatus = SyncStatus.SYNCED,
                timelineLog = "Manually created by ${user.fullName} on ${platform.displayName} ($status)"
            )
            repository.saveShipment(shipment)
            onResult(true, "Parcel $cleanTracking added successfully!")
        }
    }

    fun updateShipmentNotes(shipment: Shipment, newNotes: String) {
        val user = _currentUser.value ?: return
        if (user.role == UserRole.STAFF && !user.canEditShipments) return
        viewModelScope.launch {
            val updated = shipment.copy(
                notes = newNotes.trim(),
                lastUpdated = System.currentTimeMillis(),
                timelineLog = "${shipment.timelineLog} → Notes updated by ${user.fullName}"
            )
            repository.saveShipment(updated)
        }
    }

    fun advanceShipmentStatus(shipment: Shipment, nextStatus: ShipmentStatus) {
        val user = _currentUser.value ?: return
        if (user.role == UserRole.STAFF && !user.canEditShipments) return
        viewModelScope.launch {
            val log = "${shipment.timelineLog} → Status updated to ${nextStatus.label} by ${user.fullName}"
            repository.updateShipmentStatus(shipment.id, nextStatus, log)
        }
    }

    // ==========================================
    // MODULE 3 & 4: SCANNER & CLASSIFICATION
    // ==========================================

    fun onScanCodeEntered(code: String) {
        val trimmed = code.trim().uppercase()
        val classification = ClassificationHelper.classifyBarcode(trimmed)
        _scanState.update {
            it.copy(
                inputCode = trimmed,
                detectedPlatform = classification.platform,
                detectedCourier = classification.courier,
                classificationNote = classification.confidence,
                duplicateWarning = null,
                feedbackMessage = null
            )
        }

        if (trimmed.isNotBlank()) {
            viewModelScope.launch {
                val existing = repository.checkDuplicateTracking(trimmed)
                if (existing != null) {
                    _scanState.update {
                        it.copy(
                            duplicateWarning = existing,
                            feedbackMessage = "⚠️ DUPLICATE DETECTED: Already recorded as ${existing.status.label} on ${existing.platform.displayName}!"
                        )
                    }
                }
            }
        }
    }

    fun updateScanPlatform(platform: PlatformType) {
        _scanState.update { it.copy(detectedPlatform = platform) }
    }

    fun updateScanCourier(courier: CourierType) {
        _scanState.update { it.copy(detectedCourier = courier) }
    }

    fun updateScanRecipient(name: String, phone: String, notes: String) {
        _scanState.update {
            it.copy(
                recipientName = name,
                recipientPhone = phone,
                notes = notes
            )
        }
    }

    fun toggleOfflineSimulation() {
        _scanState.update { it.copy(isOfflineMode = !it.isOfflineMode) }
    }

    fun submitScannedParcel(onComplete: (Boolean, String) -> Unit) {
        val state = _scanState.value
        val user = _currentUser.value
        if (user == null) {
            onComplete(false, "User not authenticated.")
            return
        }

        if (user.role == UserRole.STAFF && !user.canScanParcels) {
            onComplete(false, "Permission Denied: Your scanning privileges have been restricted. Please consult Nolan (Owner).")
            return
        }

        if (!ClassificationHelper.isValidTrackingCode(state.inputCode)) {
            onComplete(false, "Invalid barcode or tracking format (min 6 alphanumeric characters)")
            return
        }

        viewModelScope.launch {
            val duplicate = repository.checkDuplicateTracking(state.inputCode)
            if (duplicate != null) {
                onComplete(false, "Blocked! Parcel ${state.inputCode} already exists in database.")
                return@launch
            }

            val syncStatus = if (state.isOfflineMode) SyncStatus.PENDING_SYNC else SyncStatus.SYNCED
            val newShipment = Shipment(
                id = "shp_${System.currentTimeMillis()}",
                trackingNumber = state.inputCode,
                platform = state.detectedPlatform,
                courier = state.detectedCourier,
                status = ShipmentStatus.PREPARED,
                recipientName = state.recipientName.ifBlank { "Buyer (${state.detectedPlatform.displayName})" },
                recipientPhone = state.recipientPhone,
                recipientAddress = "Standard Metro delivery",
                scannedAt = System.currentTimeMillis(),
                scannedByUserId = user.id,
                scannedByName = user.fullName,
                notes = state.notes,
                syncStatus = syncStatus,
                timelineLog = "Scanned & Verified by ${user.fullName} [${state.detectedPlatform.displayName} / ${state.detectedCourier.displayName}]"
            )

            repository.saveShipment(newShipment)
            _scanState.value = ScanUiState(isOfflineMode = state.isOfflineMode)
            val msg = if (syncStatus == SyncStatus.PENDING_SYNC) {
                "✅ Scanned & saved OFFLINE (pending sync)"
            } else {
                "✅ Parcel logged & prepared for dispatch!"
            }
            onComplete(true, msg)
        }
    }

    // ==========================================
    // MODULE 5: DISPATCH HANDOVER
    // ==========================================

    fun createDispatchHandover(
        courier: CourierType,
        riderName: String,
        selectedShipmentIds: List<String>,
        notes: String,
        onComplete: (Boolean, String) -> Unit
    ) {
        val user = _currentUser.value
        if (user == null) {
            onComplete(false, "User not authenticated.")
            return
        }

        if (user.role == UserRole.STAFF && !user.canDispatch) {
            onComplete(false, "Permission Denied: Your account does not have dispatch privileges. Please consult Nolan (Owner).")
            return
        }

        if (selectedShipmentIds.isEmpty()) {
            onComplete(false, "Please select at least 1 parcel to dispatch")
            return
        }
        if (riderName.isBlank()) {
            onComplete(false, "Please enter courier rider name or plate number")
            return
        }

        viewModelScope.launch {
            val batchNumber = "BATCH-${System.currentTimeMillis().toString().takeLast(6)}"
            val batch = DispatchBatch(
                id = "batch_${System.currentTimeMillis()}",
                batchNumber = batchNumber,
                courier = courier,
                handoverTimestamp = System.currentTimeMillis(),
                dispatchedByUserId = user.id,
                dispatchedByName = user.fullName,
                courierRiderName = riderName.trim(),
                totalParcels = selectedShipmentIds.size,
                notes = notes,
                syncStatus = SyncStatus.SYNCED
            )

            repository.createDispatchBatch(batch, selectedShipmentIds)
            onComplete(true, "Handover complete! $batchNumber created for ${selectedShipmentIds.size} parcels.")
        }
    }

    // ==========================================
    // MODULE 7: RETURNS MANAGEMENT
    // ==========================================

    fun logParcelReturn(
        shipment: Shipment,
        reason: ReturnReason,
        condition: ItemCondition,
        notes: String,
        onComplete: (Boolean, String) -> Unit
    ) {
        val user = _currentUser.value
        if (user == null) {
            onComplete(false, "User not authenticated.")
            return
        }

        if (user.role == UserRole.STAFF && !user.canHandleReturns) {
            onComplete(false, "Permission Denied: Your account does not have permission to log returns. Please consult Nolan (Owner).")
            return
        }

        viewModelScope.launch {
            val returnRecord = ReturnRecord(
                id = "ret_${System.currentTimeMillis()}",
                shipmentId = shipment.id,
                trackingNumber = shipment.trackingNumber,
                platform = shipment.platform,
                courier = shipment.courier,
                reason = reason,
                itemCondition = condition,
                refundStatus = RefundStatus.PENDING_APPROVAL,
                notes = notes,
                loggedAt = System.currentTimeMillis(),
                loggedByUserId = user.id,
                loggedByName = user.fullName,
                syncStatus = SyncStatus.SYNCED
            )

            repository.logReturn(returnRecord)
            onComplete(true, "Return recorded for ${shipment.trackingNumber}. Pending Owner refund approval.")
        }
    }

    fun approveRefundStatus(returnRecord: ReturnRecord, newStatus: RefundStatus) {
        val user = _currentUser.value ?: return
        if (user.role != UserRole.OWNER) return

        viewModelScope.launch {
            val updated = returnRecord.copy(
                refundStatus = newStatus,
                approvedByUserId = user.id,
                approvedByName = user.fullName
            )
            db.returnRecordDao().updateReturn(updated)
        }
    }

    // ==========================================
    // MODULE 8: OFFLINE SYNC
    // ==========================================

    fun syncPendingRecords(onComplete: (Int) -> Unit) {
        viewModelScope.launch {
            val pending = allShipments.value.filter { it.syncStatus == SyncStatus.PENDING_SYNC }
            for (s in pending) {
                db.shipmentDao().updateSyncStatus(s.id, SyncStatus.SYNCED)
            }
            onComplete(pending.size)
        }
    }
}
