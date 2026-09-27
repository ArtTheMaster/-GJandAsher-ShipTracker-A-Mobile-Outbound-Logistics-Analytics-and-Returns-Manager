import 'package:flutter/material.dart';

enum UserRole {
  owner('Owner / Admin', 'Full access to analytics, staff accounts, reports, and return approvals'),
  staff('Warehouse Staff', 'Scanning, validation, packing, dispatch handover, and return logging');

  final String label;
  final String description;
  const UserRole(this.label, this.description);
}

class User {
  final String id;
  final String username;
  final String fullName;
  final UserRole role;
  final String pin;
  final String passwordHash;
  final bool isActive;
  final String deactivationReason;
  final bool canScanParcels;
  final bool canDispatch;
  final bool canHandleReturns;
  final bool canEditShipments;
  final int createdAt;

  User({
    required this.id,
    required this.username,
    required this.fullName,
    required this.role,
    required this.pin,
    this.passwordHash = '',
    this.isActive = true,
    this.deactivationReason = '',
    this.canScanParcels = true,
    this.canDispatch = true,
    this.canHandleReturns = true,
    this.canEditShipments = true,
    int? createdAt,
  }) : createdAt = createdAt ?? DateTime.now().millisecondsSinceEpoch;

  User copyWith({
    String? id,
    String? username,
    String? fullName,
    UserRole? role,
    String? pin,
    String? passwordHash,
    bool? isActive,
    String? deactivationReason,
    bool? canScanParcels,
    bool? canDispatch,
    bool? canHandleReturns,
    bool? canEditShipments,
    int? createdAt,
  }) {
    return User(
      id: id ?? this.id,
      username: username ?? this.username,
      fullName: fullName ?? this.fullName,
      role: role ?? this.role,
      pin: pin ?? this.pin,
      passwordHash: passwordHash ?? this.passwordHash,
      isActive: isActive ?? this.isActive,
      deactivationReason: deactivationReason ?? this.deactivationReason,
      canScanParcels: canScanParcels ?? this.canScanParcels,
      canDispatch: canDispatch ?? this.canDispatch,
      canHandleReturns: canHandleReturns ?? this.canHandleReturns,
      canEditShipments: canEditShipments ?? this.canEditShipments,
      createdAt: createdAt ?? this.createdAt,
    );
  }
}

enum PlatformType {
  shopee('Shopee', 'SPX, 630', Icons.shopping_bag_outlined, Color(0xFFEE4D2D)),
  lazada('Lazada', 'LZD, MP', Icons.storefront_outlined, Color(0xFF0F146D)),
  tiktok('TikTok Shop', 'TT, 990', Icons.video_collection_outlined, Color(0xFF010101)),
  fbMarketplace('FB Marketplace', 'FBM', Icons.facebook_outlined, Color(0xFF1877F2)),
  directOrder('Direct / IG', 'DIR', Icons.send_outlined, Color(0xFFE1306C));

  final String displayName;
  final String codePrefixHint;
  final IconData icon;
  final Color brandColor;
  const PlatformType(this.displayName, this.codePrefixHint, this.icon, this.brandColor);
}

enum CourierType {
  jtExpress('J&T Express', 'J&T', Color(0xFFDC2626)),
  spx('Shopee Xpress', 'SPX', Color(0xFFEA580C)),
  lbc('LBC Express', 'LBC', Color(0xFFD97706)),
  ninjaVan('Ninja Van', 'NinjaVan', Color(0xFFB91C1C)),
  flash('Flash Express', 'Flash', Color(0xFFCA8A04)),
  other('Other Courier', 'Other', Color(0xFF4B5563));

  final String displayName;
  final String shortName;
  final Color badgeColor;
  const CourierType(this.displayName, this.shortName, this.badgeColor);
}

enum ShipmentStatus {
  scanned('Scanned', Color(0xFF3B82F6)),
  prepared('Prepared', Color(0xFF8B5CF6)),
  dispatched('Dispatched', Color(0xFF10B981)),
  delivered('Delivered', Color(0xFF059669)),
  returned('Returned', Color(0xFFEF4444)),
  cancelled('Cancelled', Color(0xFF6B7280));

  final String label;
  final Color color;
  const ShipmentStatus(this.label, this.color);
}

enum ReturnReason {
  customerRejectedCod('Customer Rejected (COD)'),
  failedDelivery('Failed Delivery Attempts (RTS)'),
  damagedInTransit('Damaged in Transit'),
  wrongItem('Wrong Item Shipped'),
  buyerChangedMind('Buyer Changed Mind'),
  other('Other Reason');

  final String label;
  const ReturnReason(this.label);
}

enum ItemCondition {
  goodResalable('Good & Resalable', Color(0xFF10B981)),
  damagedPackaging('Damaged Box Only', Color(0xFFF59E0B)),
  damagedItem('Damaged Item (Requires Repair)', Color(0xFFEF4444)),
  totalLoss('Total Loss / Unusable', Color(0xFF7F1D1D));

  final String label;
  final Color color;
  const ItemCondition(this.label, this.color);
}

enum RefundStatus {
  pendingApproval('Pending Approval', Color(0xFFF59E0B)),
  processedRefunded('Refund Processed', Color(0xFF10B981)),
  deniedDisputed('Denied / Disputed', Color(0xFFEF4444));

  final String label;
  final Color color;
  const RefundStatus(this.label, this.color);
}

enum SyncStatus {
  synced('Synced', Color(0xFF10B981)),
  pendingSync('Pending Sync', Color(0xFFF59E0B));

  final String label;
  final Color color;
  const SyncStatus(this.label, this.color);
}

class Shipment {
  final String id;
  final String trackingNumber;
  final PlatformType platform;
  final CourierType courier;
  final ShipmentStatus status;
  final String recipientName;
  final String recipientPhone;
  final String recipientAddress;
  final int scannedAt;
  final String scannedByUserId;
  final String scannedByName;
  final String? dispatchBatchId;
  final String notes;
  final SyncStatus syncStatus;
  final int lastUpdated;
  final String timelineLog;

  Shipment({
    required this.id,
    required this.trackingNumber,
    required this.platform,
    required this.courier,
    required this.status,
    this.recipientName = '',
    this.recipientPhone = '',
    this.recipientAddress = '',
    int? scannedAt,
    this.scannedByUserId = '',
    this.scannedByName = '',
    this.dispatchBatchId,
    this.notes = '',
    this.syncStatus = SyncStatus.synced,
    int? lastUpdated,
    this.timelineLog = 'Scanned into warehouse',
  })  : scannedAt = scannedAt ?? DateTime.now().millisecondsSinceEpoch,
        lastUpdated = lastUpdated ?? DateTime.now().millisecondsSinceEpoch;

  Shipment copyWith({
    String? id,
    String? trackingNumber,
    PlatformType? platform,
    CourierType? courier,
    ShipmentStatus? status,
    String? recipientName,
    String? recipientPhone,
    String? recipientAddress,
    int? scannedAt,
    String? scannedByUserId,
    String? scannedByName,
    String? dispatchBatchId,
    String? notes,
    SyncStatus? syncStatus,
    int? lastUpdated,
    String? timelineLog,
  }) {
    return Shipment(
      id: id ?? this.id,
      trackingNumber: trackingNumber ?? this.trackingNumber,
      platform: platform ?? this.platform,
      courier: courier ?? this.courier,
      status: status ?? this.status,
      recipientName: recipientName ?? this.recipientName,
      recipientPhone: recipientPhone ?? this.recipientPhone,
      recipientAddress: recipientAddress ?? this.recipientAddress,
      scannedAt: scannedAt ?? this.scannedAt,
      scannedByUserId: scannedByUserId ?? this.scannedByUserId,
      scannedByName: scannedByName ?? this.scannedByName,
      dispatchBatchId: dispatchBatchId ?? this.dispatchBatchId,
      notes: notes ?? this.notes,
      syncStatus: syncStatus ?? this.syncStatus,
      lastUpdated: lastUpdated ?? this.lastUpdated,
      timelineLog: timelineLog ?? this.timelineLog,
    );
  }
}

class DispatchBatch {
  final String id;
  final String batchNumber;
  final CourierType courier;
  final int handoverTimestamp;
  final String dispatchedByUserId;
  final String dispatchedByName;
  final String courierRiderName;
  final int totalParcels;
  final String notes;
  final SyncStatus syncStatus;

  DispatchBatch({
    required this.id,
    required this.batchNumber,
    required this.courier,
    int? handoverTimestamp,
    required this.dispatchedByUserId,
    required this.dispatchedByName,
    required this.courierRiderName,
    required this.totalParcels,
    this.notes = '',
    this.syncStatus = SyncStatus.synced,
  }) : handoverTimestamp = handoverTimestamp ?? DateTime.now().millisecondsSinceEpoch;
}

class ReturnRecord {
  final String id;
  final String shipmentId;
  final String trackingNumber;
  final PlatformType platform;
  final CourierType courier;
  final ReturnReason reason;
  final ItemCondition itemCondition;
  final RefundStatus refundStatus;
  final String notes;
  final int loggedAt;
  final String loggedByUserId;
  final String loggedByName;
  final String? approvedByUserId;
  final String? approvedByName;
  final SyncStatus syncStatus;

  ReturnRecord({
    required this.id,
    required this.shipmentId,
    required this.trackingNumber,
    required this.platform,
    required this.courier,
    required this.reason,
    required this.itemCondition,
    required this.refundStatus,
    this.notes = '',
    int? loggedAt,
    required this.loggedByUserId,
    required this.loggedByName,
    this.approvedByUserId,
    this.approvedByName,
    this.syncStatus = SyncStatus.synced,
  }) : loggedAt = loggedAt ?? DateTime.now().millisecondsSinceEpoch;

  ReturnRecord copyWith({
    String? id,
    String? shipmentId,
    String? trackingNumber,
    PlatformType? platform,
    CourierType? courier,
    ReturnReason? reason,
    ItemCondition? itemCondition,
    RefundStatus? refundStatus,
    String? notes,
    int? loggedAt,
    String? loggedByUserId,
    String? loggedByName,
    String? approvedByUserId,
    String? approvedByName,
    SyncStatus? syncStatus,
  }) {
    return ReturnRecord(
      id: id ?? this.id,
      shipmentId: shipmentId ?? this.shipmentId,
      trackingNumber: trackingNumber ?? this.trackingNumber,
      platform: platform ?? this.platform,
      courier: courier ?? this.courier,
      reason: reason ?? this.reason,
      itemCondition: itemCondition ?? this.itemCondition,
      refundStatus: refundStatus ?? this.refundStatus,
      notes: notes ?? this.notes,
      loggedAt: loggedAt ?? this.loggedAt,
      loggedByUserId: loggedByUserId ?? this.loggedByUserId,
      loggedByName: loggedByName ?? this.loggedByName,
      approvedByUserId: approvedByUserId ?? this.approvedByUserId,
      approvedByName: approvedByName ?? this.approvedByName,
      syncStatus: syncStatus ?? this.syncStatus,
    );
  }
}
