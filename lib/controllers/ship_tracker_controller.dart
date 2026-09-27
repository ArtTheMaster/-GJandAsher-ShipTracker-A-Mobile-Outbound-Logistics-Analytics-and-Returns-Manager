import 'package:flutter/material.dart';
import '../models/models.dart';
import '../utils/classification_helper.dart';

enum ScreenTab {
  parcels('Parcels', Icons.inventory_2_outlined),
  dispatch('Dispatch', Icons.local_shipping_outlined),
  scan('Scan & Validate', Icons.qr_code_scanner_outlined),
  returns('Returns', Icons.assignment_return_outlined),
  dashboard('Analytics', Icons.insights_outlined);

  final String title;
  final IconData icon;
  const ScreenTab(this.title, this.icon);
}

enum DateRangeFilter {
  all('All Dates'),
  today('Today'),
  last7Days('Last 7 Days'),
  older('Older');

  final String label;
  const DateRangeFilter(this.label);
}

class ShipTrackerController extends ChangeNotifier {
  // Current logged in user
  User? _currentUser;
  User? get currentUser => _currentUser;

  // Selected bottom navigation tab
  ScreenTab _currentTab = ScreenTab.parcels;
  ScreenTab get currentTab => _currentTab;

  // Splash screen state
  bool _isSplashVisible = true;
  bool get isSplashVisible => _isSplashVisible;

  void dismissSplash() {
    _isSplashVisible = false;
    notifyListeners();
  }

  // Users list (Canonical accounts strictly managed)
  final List<User> _users = [
    User(
      id: 'usr_owner_01',
      username: 'nolancaparros',
      fullName: 'Nolan Caparros',
      role: UserRole.owner,
      pin: '1234',
      isActive: true,
      canScanParcels: true,
      canDispatch: true,
      canHandleReturns: true,
      canEditShipments: true,
    ),
    User(
      id: 'usr_staff_01',
      username: 'gjcaparros',
      fullName: 'GJ Caparros',
      role: UserRole.staff,
      pin: '1111',
      isActive: true,
      canScanParcels: true,
      canDispatch: true,
      canHandleReturns: true,
      canEditShipments: true,
    ),
  ];
  List<User> get users => List.unmodifiable(_users);

  // Shipments list
  final List<Shipment> _shipments = [];
  List<Shipment> get shipments => List.unmodifiable(_shipments);

  // Dispatch batches
  final List<DispatchBatch> _batches = [];
  List<DispatchBatch> get batches => List.unmodifiable(_batches);

  // Return records
  final List<ReturnRecord> _returns = [];
  List<ReturnRecord> get returns => List.unmodifiable(_returns);

  // Filtering states
  String _searchQuery = '';
  String get searchQuery => _searchQuery;

  ShipmentStatus? _filterStatus;
  ShipmentStatus? get filterStatus => _filterStatus;

  PlatformType? _filterPlatform;
  PlatformType? get filterPlatform => _filterPlatform;

  CourierType? _filterCourier;
  CourierType? get filterCourier => _filterCourier;

  DateRangeFilter _filterDateRange = DateRangeFilter.all;
  DateRangeFilter get filterDateRange => _filterDateRange;

  // Scan UI State
  String _scanInputCode = '';
  String get scanInputCode => _scanInputCode;

  PlatformType _scanPlatform = PlatformType.shopee;
  PlatformType get scanPlatform => _scanPlatform;

  CourierType _scanCourier = CourierType.jtExpress;
  CourierType get scanCourier => _scanCourier;

  String _scanClassificationNote = '';
  String get scanClassificationNote => _scanClassificationNote;

  String _scanRecipientName = '';
  String get scanRecipientName => _scanRecipientName;

  String _scanRecipientPhone = '';
  String get scanRecipientPhone => _scanRecipientPhone;

  String _scanNotes = '';
  String get scanNotes => _scanNotes;

  bool _isOfflineScanMode = false;
  bool get isOfflineScanMode => _isOfflineScanMode;

  Shipment? _scanDuplicateWarning;
  Shipment? get scanDuplicateWarning => _scanDuplicateWarning;

  ShipTrackerController() {
    _seedInitialShipments();
  }

  void _seedInitialShipments() {
    final now = DateTime.now().millisecondsSinceEpoch;
    final hour = 3600 * 1000;

    _shipments.addAll([
      Shipment(
        id: 'shp_001',
        trackingNumber: 'SPXPH049281729',
        platform: PlatformType.shopee,
        courier: CourierType.spx,
        status: ShipmentStatus.scanned,
        recipientName: 'Samantha Cruz',
        recipientPhone: '0917-882-9102',
        recipientAddress: 'Bgy San Antonio, Pasig City',
        scannedAt: now - (hour * 2),
        scannedByUserId: 'usr_staff_01',
        scannedByName: 'GJ Caparros',
        notes: 'Priority parcel - 2 items clothing',
        timelineLog: 'Scanned into warehouse by GJ Caparros',
      ),
      Shipment(
        id: 'shp_002',
        trackingNumber: 'JT992817462PH',
        platform: PlatformType.shopee,
        courier: CourierType.jtExpress,
        status: ShipmentStatus.prepared,
        recipientName: 'Carlos Mendoza',
        recipientPhone: '0928-112-9843',
        recipientAddress: 'Commonwealth Ave, Quezon City',
        scannedAt: now - (hour * 5),
        scannedByUserId: 'usr_owner_01',
        scannedByName: 'Nolan Caparros',
        notes: 'Fragile sticker applied',
        timelineLog: 'Scanned & Packed by Nolan Caparros',
      ),
      Shipment(
        id: 'shp_003',
        trackingNumber: 'MPLZD019284712',
        platform: PlatformType.lazada,
        courier: CourierType.ninjaVan,
        status: ShipmentStatus.dispatched,
        recipientName: 'Elena Bautista',
        recipientPhone: '0939-554-1234',
        recipientAddress: 'Poblacion, Makati City',
        scannedAt: now - (hour * 28),
        scannedByUserId: 'usr_staff_01',
        scannedByName: 'GJ Caparros',
        dispatchBatchId: 'batch_001',
        notes: 'Batch handover confirmed',
        timelineLog: 'Dispatched in BATCH-99201 to Ninja Van rider Mark',
      ),
      Shipment(
        id: 'shp_004',
        trackingNumber: 'TT9902817412',
        platform: PlatformType.tiktok,
        courier: CourierType.flash,
        status: ShipmentStatus.returned,
        recipientName: 'Ramon Del Rosario',
        recipientPhone: '0915-443-8821',
        recipientAddress: 'Mandaluyong City',
        scannedAt: now - (hour * 48),
        scannedByUserId: 'usr_staff_01',
        scannedByName: 'GJ Caparros',
        notes: 'Buyer refused COD on delivery',
        timelineLog: 'Returned to hub. Logged by GJ Caparros',
      ),
    ]);

    _batches.add(
      DispatchBatch(
        id: 'batch_001',
        batchNumber: 'BATCH-99201',
        courier: CourierType.ninjaVan,
        handoverTimestamp: now - (hour * 26),
        dispatchedByUserId: 'usr_staff_01',
        dispatchedByName: 'GJ Caparros',
        courierRiderName: 'Mark Gonzales (Plate NA-4921)',
        totalParcels: 14,
        notes: 'Signed handover manifest #0492',
      ),
    );

    _returns.add(
      ReturnRecord(
        id: 'ret_001',
        shipmentId: 'shp_004',
        trackingNumber: 'TT9902817412',
        platform: PlatformType.tiktok,
        courier: CourierType.flash,
        reason: ReturnReason.customerRejectedCod,
        itemCondition: ItemCondition.goodResalable,
        refundStatus: RefundStatus.pendingApproval,
        notes: 'Item seal is intact. Safe to restock into warehouse inventory.',
        loggedAt: now - (hour * 12),
        loggedByUserId: 'usr_staff_01',
        loggedByName: 'GJ Caparros',
      ),
    );
  }

  // ========================================================
  // AUTHENTICATION & ROLE MANAGEMENT (STAFF vs OWNER RULES)
  // ========================================================

  bool authenticate(String username, String pin, void Function(bool success, String message) callback) {
    final cleanUser = username.trim().toLowerCase();
    final cleanPin = pin.trim();

    final user = _users.firstOrNull((u) => u.username.toLowerCase() == cleanUser);

    if (user == null) {
      callback(false, 'User @$cleanUser not found. Check spelling or create an account.');
      return false;
    }

    if (!user.isActive) {
      final reason = user.deactivationReason.isNotEmpty ? user.deactivationReason : 'Account is deactivated by Nolan (Owner).';
      callback(false, 'ACCOUNT DEACTIVATED: $reason\n\nPlease contact Nolan (Owner) to reactivate.');
      return false;
    }

    if (user.pin != cleanPin) {
      callback(false, 'Incorrect Access PIN. Please try again.');
      return false;
    }

    _currentUser = user;
    _currentTab = ScreenTab.parcels;
    notifyListeners();
    callback(true, 'Welcome back, ${user.fullName} (${user.role.label})');
    return true;
  }

  void quickSignIn(User user, void Function(bool success, String message) callback) {
    if (!user.isActive) {
      final reason = user.deactivationReason.isNotEmpty ? user.deactivationReason : 'Account is deactivated by Nolan (Owner).';
      callback(false, 'ACCOUNT DEACTIVATED: $reason\n\nPlease contact Nolan to reactivate this account.');
      return;
    }
    _currentUser = user;
    _currentTab = ScreenTab.parcels;
    notifyListeners();
    callback(true, 'Signed in as ${user.fullName} (${user.role.label})');
  }

  /// SIGN UP FUNCTION:
  /// Strictly registers STAFF accounts. Role selection is completely omitted.
  /// Only the Owner (Nolan) can modify or manage accounts afterwards.
  void registerAccount({
    required String fullName,
    required String username,
    required String pin,
    required void Function(bool success, String message) callback,
  }) {
    final cleanName = fullName.trim();
    final cleanUser = username.trim().toLowerCase();
    final cleanPin = pin.trim();

    if (cleanName.isEmpty) {
      callback(false, 'Please enter your full name.');
      return;
    }
    if (cleanUser.length < 3) {
      callback(false, 'Username must be at least 3 characters.');
      return;
    }
    if (cleanPin.length < 4) {
      callback(false, 'Security PIN must be at least 4 digits.');
      return;
    }

    // Reserved usernames protection
    if (['nolan', 'nolancaparros', 'gj', 'gjcaparros'].contains(cleanUser)) {
      callback(false, 'This username is reserved. Please choose another username.');
      return;
    }

    // Strict deduplication check: no duplicate usernames or full names
    final exists = _users.any((u) => u.username.toLowerCase() == cleanUser || u.fullName.toLowerCase() == cleanName.toLowerCase());
    if (exists) {
      callback(false, 'An account with this username or name already exists.');
      return;
    }

    // ALWAYS defaults to STAFF role - operational role assignment removed for self-registration!
    final newUser = User(
      id: 'usr_${DateTime.now().millisecondsSinceEpoch}',
      username: cleanUser,
      fullName: cleanName,
      role: UserRole.staff, // Fixed to Staff role
      pin: cleanPin,
      isActive: true,
      canScanParcels: true,
      canDispatch: true,
      canHandleReturns: true,
      canEditShipments: true,
    );

    _users.add(newUser);
    _currentUser = newUser;
    _currentTab = ScreenTab.parcels;
    notifyListeners();
    callback(true, 'Staff account created successfully! Logged in as $cleanName.');
  }

  void logout() {
    _currentUser = null;
    _currentTab = ScreenTab.parcels;
    notifyListeners();
  }

  void selectTab(ScreenTab tab) {
    // Only Nolan (Owner) can open Analytics / Dashboard
    if (tab == ScreenTab.dashboard && _currentUser?.role != UserRole.owner) {
      return;
    }
    _currentTab = tab;
    notifyListeners();
  }

  // ========================================================
  // OWNER-EXCLUSIVE STAFF & ROLE MANAGEMENT
  // ========================================================

  void addStaffMemberByOwner({
    required String username,
    required String fullName,
    required String pin,
    bool canScan = true,
    bool canDispatch = true,
    bool canReturns = true,
    bool canEdit = true,
    required void Function(bool success, String message) callback,
  }) {
    if (_currentUser?.role != UserRole.owner) {
      callback(false, 'Access Denied: Only Owner Nolan can manage staff.');
      return;
    }

    final cleanUser = username.trim().toLowerCase();
    final cleanName = fullName.trim();
    final cleanPin = pin.trim();

    if (cleanUser.isEmpty || cleanName.isEmpty || cleanPin.isEmpty) {
      callback(false, 'All fields are required.');
      return;
    }

    final existingIndex = _users.indexWhere(
      (u) => u.username.toLowerCase() == cleanUser || u.fullName.toLowerCase() == cleanName.toLowerCase(),
    );

    if (existingIndex >= 0) {
      // Update existing record
      final existing = _users[existingIndex];
      _users[existingIndex] = existing.copyWith(
        username: cleanUser,
        fullName: cleanName,
        pin: cleanPin,
        role: UserRole.staff,
        canScanParcels: canScan,
        canDispatch: canDispatch,
        canHandleReturns: canReturns,
        canEditShipments: canEdit,
        isActive: true,
      );
      notifyListeners();
      callback(true, 'Updated permissions for $cleanName.');
    } else {
      final newUser = User(
        id: 'usr_${DateTime.now().millisecondsSinceEpoch}',
        username: cleanUser,
        fullName: cleanName,
        role: UserRole.staff,
        pin: cleanPin,
        isActive: true,
        canScanParcels: canScan,
        canDispatch: canDispatch,
        canHandleReturns: canReturns,
        canEditShipments: canEdit,
      );
      _users.add(newUser);
      notifyListeners();
      callback(true, 'Staff member $cleanName added.');
    }
  }

  void toggleStaffStatus(User targetUser, String reason) {
    if (_currentUser?.role != UserRole.owner) return;
    if (targetUser.role == UserRole.owner) return; // Cannot deactivate Owner!

    final idx = _users.indexWhere((u) => u.id == targetUser.id);
    if (idx != -1) {
      final newStatus = !targetUser.isActive;
      _users[idx] = targetUser.copyWith(
        isActive: newStatus,
        deactivationReason: newStatus ? '' : (reason.isNotEmpty ? reason : 'Deactivated by Owner Nolan'),
      );

      // If deactivated user is currently logged in, force logout
      if (_currentUser?.id == targetUser.id && !newStatus) {
        _currentUser = null;
      }
      notifyListeners();
    }
  }

  void updateStaffPermissions(User targetUser, {
    required bool canScan,
    required bool canDispatch,
    required bool canReturns,
    required bool canEdit,
  }) {
    if (_currentUser?.role != UserRole.owner) return;
    final idx = _users.indexWhere((u) => u.id == targetUser.id);
    if (idx != -1) {
      _users[idx] = targetUser.copyWith(
        canScanParcels: canScan,
        canDispatch: canDispatch,
        canHandleReturns: canReturns,
        canEditShipments: canEdit,
      );
      notifyListeners();
    }
  }

  // ========================================================
  // PARCEL SEARCH & FILTERING
  // ========================================================

  void setSearchQuery(String query) {
    _searchQuery = query;
    notifyListeners();
  }

  void setFilterStatus(ShipmentStatus? status) {
    _filterStatus = status;
    notifyListeners();
  }

  void setFilterPlatform(PlatformType? platform) {
    _filterPlatform = platform;
    notifyListeners();
  }

  void setFilterCourier(CourierType? courier) {
    _filterCourier = courier;
    notifyListeners();
  }

  void setFilterDateRange(DateRangeFilter filter) {
    _filterDateRange = filter;
    notifyListeners();
  }

  List<Shipment> get filteredShipments {
    final now = DateTime.now().millisecondsSinceEpoch;
    final oneDay = 24 * 3600 * 1000;
    final sevenDays = 7 * oneDay;

    return _shipments.where((item) {
      final matchesQuery = _searchQuery.isEmpty ||
          item.trackingNumber.toLowerCase().contains(_searchQuery.toLowerCase()) ||
          item.recipientName.toLowerCase().contains(_searchQuery.toLowerCase()) ||
          item.notes.toLowerCase().contains(_searchQuery.toLowerCase());

      final matchesStatus = _filterStatus == null || item.status == _filterStatus;
      final matchesPlatform = _filterPlatform == null || item.platform == _filterPlatform;
      final matchesCourier = _filterCourier == null || item.courier == _filterCourier;

      final age = now - item.scannedAt;
      final matchesDate = switch (_filterDateRange) {
        DateRangeFilter.all => true,
        DateRangeFilter.today => age <= oneDay,
        DateRangeFilter.last7Days => age <= sevenDays,
        DateRangeFilter.older => age > sevenDays,
      };

      return matchesQuery && matchesStatus && matchesPlatform && matchesCourier && matchesDate;
    }).toList();
  }

  int get pendingSyncCount => _shipments.where((s) => s.syncStatus == SyncStatus.pendingSync).length;

  // ========================================================
  // SCANNER & BARCODE CLASSIFICATION
  // ========================================================

  void onScanCodeChanged(String code) {
    final trimmed = code.trim().toUpperCase();
    _scanInputCode = trimmed;
    final result = ClassificationHelper.classifyBarcode(trimmed);
    _scanPlatform = result.platform;
    _scanCourier = result.courier;
    _scanClassificationNote = result.confidence;

    if (trimmed.isNotEmpty) {
      _scanDuplicateWarning = _shipments.firstOrNull((s) => s.trackingNumber.toUpperCase() == trimmed);
    } else {
      _scanDuplicateWarning = null;
    }
    notifyListeners();
  }

  void setScanPlatform(PlatformType platform) {
    _scanPlatform = platform;
    notifyListeners();
  }

  void setScanCourier(CourierType courier) {
    _scanCourier = courier;
    notifyListeners();
  }

  void setScanRecipient(String name, String phone, String notes) {
    _scanRecipientName = name;
    _scanRecipientPhone = phone;
    _scanNotes = notes;
    notifyListeners();
  }

  void toggleOfflineMode() {
    _isOfflineScanMode = !_isOfflineScanMode;
    notifyListeners();
  }

  void submitScannedParcel(void Function(bool success, String message) callback) {
    if (_currentUser == null) {
      callback(false, 'Please sign in first.');
      return;
    }
    if (_currentUser!.role == UserRole.staff && !_currentUser!.canScanParcels) {
      callback(false, 'Permission Denied: Parcel scanning privileges restricted by Owner.');
      return;
    }
    if (!ClassificationHelper.isValidTrackingCode(_scanInputCode)) {
      callback(false, 'Invalid tracking number format (min 5 alphanumeric characters).');
      return;
    }

    final duplicate = _shipments.firstOrNull((s) => s.trackingNumber.toUpperCase() == _scanInputCode);
    if (duplicate != null) {
      callback(false, 'Blocked! Parcel $_scanInputCode already logged as ${duplicate.status.label}.');
      return;
    }

    final newShipment = Shipment(
      id: 'shp_${DateTime.now().millisecondsSinceEpoch}',
      trackingNumber: _scanInputCode,
      platform: _scanPlatform,
      courier: _scanCourier,
      status: ShipmentStatus.prepared,
      recipientName: _scanRecipientName.isNotEmpty ? _scanRecipientName : 'Buyer (${_scanPlatform.displayName})',
      recipientPhone: _scanRecipientPhone,
      recipientAddress: 'Standard Metro Express Outbound',
      scannedAt: DateTime.now().millisecondsSinceEpoch,
      scannedByUserId: _currentUser!.id,
      scannedByName: _currentUser!.fullName,
      notes: _scanNotes,
      syncStatus: _isOfflineScanMode ? SyncStatus.pendingSync : SyncStatus.synced,
      timelineLog: 'Scanned & Verified by ${_currentUser!.fullName} [${_scanPlatform.displayName} / ${_scanCourier.displayName}]',
    );

    _shipments.insert(0, newShipment);
    _scanInputCode = '';
    _scanRecipientName = '';
    _scanRecipientPhone = '';
    _scanNotes = '';
    _scanDuplicateWarning = null;
    notifyListeners();

    callback(
      true,
      _isOfflineScanMode ? '✅ Scanned & saved OFFLINE (pending sync)' : '✅ Parcel logged & prepared for dispatch!',
    );
  }

  void createManualShipment({
    required String trackingNumber,
    required PlatformType platform,
    required CourierType courier,
    required ShipmentStatus status,
    required String recipientName,
    required String recipientPhone,
    required String recipientAddress,
    required String notes,
    required void Function(bool success, String message) callback,
  }) {
    if (_currentUser == null) {
      callback(false, 'Not authenticated.');
      return;
    }
    if (_currentUser!.role == UserRole.staff && !_currentUser!.canScanParcels) {
      callback(false, 'Permission Denied: You cannot create shipments.');
      return;
    }
    final cleanTracking = trackingNumber.trim().toUpperCase();
    if (cleanTracking.length < 5) {
      callback(false, 'Tracking number must be at least 5 characters.');
      return;
    }
    if (_shipments.any((s) => s.trackingNumber.toUpperCase() == cleanTracking)) {
      callback(false, 'Duplicate parcel detected! $cleanTracking already exists.');
      return;
    }

    final shipment = Shipment(
      id: 'shp_${DateTime.now().millisecondsSinceEpoch}',
      trackingNumber: cleanTracking,
      platform: platform,
      courier: courier,
      status: status,
      recipientName: recipientName.isNotEmpty ? recipientName : 'Direct Buyer',
      recipientPhone: recipientPhone,
      recipientAddress: recipientAddress.isNotEmpty ? recipientAddress : 'Standard delivery',
      scannedAt: DateTime.now().millisecondsSinceEpoch,
      scannedByUserId: _currentUser!.id,
      scannedByName: _currentUser!.fullName,
      notes: notes,
      syncStatus: SyncStatus.synced,
      timelineLog: 'Manually logged by ${_currentUser!.fullName} on ${platform.displayName} ($status)',
    );

    _shipments.insert(0, shipment);
    notifyListeners();
    callback(true, 'Parcel $cleanTracking added successfully!');
  }

  void updateShipmentNotes(Shipment shipment, String newNotes) {
    if (_currentUser == null) return;
    if (_currentUser!.role == UserRole.staff && !_currentUser!.canEditShipments) return;

    final idx = _shipments.indexWhere((s) => s.id == shipment.id);
    if (idx != -1) {
      _shipments[idx] = shipment.copyWith(
        notes: newNotes,
        lastUpdated: DateTime.now().millisecondsSinceEpoch,
        timelineLog: '${shipment.timelineLog} → Notes updated by ${_currentUser!.fullName}',
      );
      notifyListeners();
    }
  }

  void advanceShipmentStatus(Shipment shipment, ShipmentStatus nextStatus) {
    if (_currentUser == null) return;
    if (_currentUser!.role == UserRole.staff && !_currentUser!.canEditShipments) return;

    final idx = _shipments.indexWhere((s) => s.id == shipment.id);
    if (idx != -1) {
      _shipments[idx] = shipment.copyWith(
        status: nextStatus,
        lastUpdated: DateTime.now().millisecondsSinceEpoch,
        timelineLog: '${shipment.timelineLog} → Status set to ${nextStatus.label} by ${_currentUser!.fullName}',
      );
      notifyListeners();
    }
  }

  // ========================================================
  // DISPATCH HANDOVER
  // ========================================================

  void createDispatchHandover({
    required CourierType courier,
    required String riderName,
    required List<String> selectedShipmentIds,
    required String notes,
    required void Function(bool success, String message) callback,
  }) {
    if (_currentUser == null) {
      callback(false, 'Not authenticated.');
      return;
    }
    if (_currentUser!.role == UserRole.staff && !_currentUser!.canDispatch) {
      callback(false, 'Permission Denied: Your account does not have dispatch privileges.');
      return;
    }
    if (selectedShipmentIds.isEmpty) {
      callback(false, 'Please select at least 1 parcel to dispatch.');
      return;
    }
    if (riderName.trim().isEmpty) {
      callback(false, 'Please enter courier rider name or plate number.');
      return;
    }

    final batchNum = 'BATCH-${DateTime.now().millisecondsSinceEpoch.toString().substring(7)}';
    final batch = DispatchBatch(
      id: 'batch_${DateTime.now().millisecondsSinceEpoch}',
      batchNumber: batchNum,
      courier: courier,
      handoverTimestamp: DateTime.now().millisecondsSinceEpoch,
      dispatchedByUserId: _currentUser!.id,
      dispatchedByName: _currentUser!.fullName,
      courierRiderName: riderName.trim(),
      totalParcels: selectedShipmentIds.sizeOrCount(selectedShipmentIds.length),
      notes: notes,
      syncStatus: SyncStatus.synced,
    );

    _batches.insert(0, batch);

    // Update status of all included shipments
    for (final id in selectedShipmentIds) {
      final idx = _shipments.indexWhere((s) => s.id == id);
      if (idx != -1) {
        _shipments[idx] = _shipments[idx].copyWith(
          status: ShipmentStatus.dispatched,
          dispatchBatchId: batch.id,
          timelineLog: '${_shipments[idx].timelineLog} → Handed over to ${courier.displayName} rider $riderName in $batchNum',
        );
      }
    }
    notifyListeners();
    callback(true, 'Handover complete! $batchNum created for ${selectedShipmentIds.length} parcels.');
  }

  // ========================================================
  // RETURNS LOGGING & OWNER APPROVAL
  // ========================================================

  void logParcelReturn({
    required Shipment shipment,
    required ReturnReason reason,
    required ItemCondition condition,
    required String notes,
    required void Function(bool success, String message) callback,
  }) {
    if (_currentUser == null) {
      callback(false, 'Not authenticated.');
      return;
    }
    if (_currentUser!.role == UserRole.staff && !_currentUser!.canHandleReturns) {
      callback(false, 'Permission Denied: Your account cannot log returns. Consult Nolan (Owner).');
      return;
    }

    final ret = ReturnRecord(
      id: 'ret_${DateTime.now().millisecondsSinceEpoch}',
      shipmentId: shipment.id,
      trackingNumber: shipment.trackingNumber,
      platform: shipment.platform,
      courier: shipment.courier,
      reason: reason,
      itemCondition: condition,
      refundStatus: RefundStatus.pendingApproval,
      notes: notes,
      loggedAt: DateTime.now().millisecondsSinceEpoch,
      loggedByUserId: _currentUser!.id,
      loggedByName: _currentUser!.fullName,
      syncStatus: SyncStatus.synced,
    );

    _returns.insert(0, ret);

    // Update shipment status to Returned
    final idx = _shipments.indexWhere((s) => s.id == shipment.id);
    if (idx != -1) {
      _shipments[idx] = _shipments[idx].copyWith(
        status: ShipmentStatus.returned,
        timelineLog: '${_shipments[idx].timelineLog} → Returned parcel logged by ${_currentUser!.fullName}',
      );
    }
    notifyListeners();
    callback(true, 'Return recorded for ${shipment.trackingNumber}. Pending Owner refund approval.');
  }

  void approveRefundStatus(ReturnRecord record, RefundStatus newStatus) {
    if (_currentUser?.role != UserRole.owner) return;
    final idx = _returns.indexWhere((r) => r.id == record.id);
    if (idx != -1) {
      _returns[idx] = record.copyWith(
        refundStatus: newStatus,
        approvedByUserId: _currentUser!.id,
        approvedByName: _currentUser!.fullName,
      );
      notifyListeners();
    }
  }

  // ========================================================
  // OFFLINE SYNC
  // ========================================================

  void syncPendingRecords(void Function(int count) onComplete) {
    int count = 0;
    for (int i = 0; i < _shipments.length; i++) {
      if (_shipments[i].syncStatus == SyncStatus.pendingSync) {
        _shipments[i] = _shipments[i].copyWith(syncStatus: SyncStatus.synced);
        count++;
      }
    }
    notifyListeners();
    onComplete(count);
  }
}

extension on List<String> {
  int sizeOrCount(int length) => length;
}
