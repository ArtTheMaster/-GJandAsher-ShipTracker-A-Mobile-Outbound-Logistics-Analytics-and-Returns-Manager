package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserRole(val label: String, val description: String) {
    OWNER("Owner / Admin", "Full access to analytics, staff accounts, reports, and return approvals"),
    STAFF("Warehouse Staff", "Scanning, validation, packing, dispatch handover, and return logging")
}

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String,
    val username: String,
    val fullName: String,
    val role: UserRole,
    val pin: String,
    val passwordHash: String = "",
    val isActive: Boolean = true,
    val deactivationReason: String = "",
    val canScanParcels: Boolean = true,
    val canDispatch: Boolean = true,
    val canHandleReturns: Boolean = true,
    val canEditShipments: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

enum class PlatformType(val displayName: String, val codePrefixHint: String) {
    SHOPEE("Shopee", "SPX, 630"),
    LAZADA("Lazada", "LZD, MP"),
    TIKTOK("TikTok Shop", "TT, 990"),
    FB_MARKETPLACE("FB Marketplace", "FBM"),
    DIRECT_ORDER("Direct / IG", "DIR")
}

enum class CourierType(val displayName: String, val shortName: String) {
    JT_EXPRESS("J&T Express", "J&T"),
    SPX("Shopee Xpress", "SPX"),
    LBC("LBC Express", "LBC"),
    NINJA_VAN("Ninja Van", "NinjaVan"),
    FLASH("Flash Express", "Flash"),
    OTHER("Other Courier", "Other")
}

enum class ShipmentStatus(val label: String) {
    SCANNED("Scanned"),
    PREPARED("Prepared"),
    DISPATCHED("Dispatched"),
    DELIVERED("Delivered"),
    RETURNED("Returned"),
    CANCELLED("Cancelled")
}

enum class ReturnReason(val label: String) {
    CUSTOMER_REJECTED_COD("Customer Rejected (COD)"),
    FAILED_DELIVERY("Failed Delivery Attempts (RTS)"),
    DAMAGED_IN_TRANSIT("Damaged in Transit"),
    WRONG_ITEM("Wrong Item Shipped"),
    BUYER_CHANGED_MIND("Buyer Changed Mind"),
    OTHER("Other Reason")
}

enum class ItemCondition(val label: String) {
    GOOD_RESALABLE("Good & Resalable"),
    DAMAGED_PACKAGING("Damaged Box Only"),
    DAMAGED_ITEM("Damaged Item (Requires Repair)"),
    TOTAL_LOSS("Total Loss / Unusable")
}

enum class RefundStatus(val label: String) {
    PENDING_APPROVAL("Pending Approval"),
    PROCESSED_REFUNDED("Refund Processed"),
    DENIED_DISPUTED("Denied / Disputed")
}

enum class SyncStatus {
    SYNCED,
    PENDING_SYNC
}

@Entity(tableName = "shipments")
data class Shipment(
    @PrimaryKey val id: String,
    val trackingNumber: String,
    val platform: PlatformType,
    val courier: CourierType,
    val status: ShipmentStatus,
    val recipientName: String = "",
    val recipientPhone: String = "",
    val recipientAddress: String = "",
    val scannedAt: Long = System.currentTimeMillis(),
    val scannedByUserId: String = "",
    val scannedByName: String = "",
    val dispatchBatchId: String? = null,
    val notes: String = "",
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
    val lastUpdated: Long = System.currentTimeMillis(),
    val timelineLog: String = "Scanned into warehouse"
)

@Entity(tableName = "dispatch_batches")
data class DispatchBatch(
    @PrimaryKey val id: String,
    val batchNumber: String,
    val courier: CourierType,
    val handoverTimestamp: Long = System.currentTimeMillis(),
    val dispatchedByUserId: String,
    val dispatchedByName: String,
    val courierRiderName: String,
    val totalParcels: Int,
    val notes: String = "",
    val syncStatus: SyncStatus = SyncStatus.SYNCED
)

@Entity(tableName = "return_records")
data class ReturnRecord(
    @PrimaryKey val id: String,
    val shipmentId: String,
    val trackingNumber: String,
    val platform: PlatformType,
    val courier: CourierType,
    val reason: ReturnReason,
    val itemCondition: ItemCondition,
    val refundStatus: RefundStatus,
    val notes: String = "",
    val loggedAt: Long = System.currentTimeMillis(),
    val loggedByUserId: String,
    val loggedByName: String,
    val approvedByUserId: String? = null,
    val approvedByName: String? = null,
    val syncStatus: SyncStatus = SyncStatus.SYNCED
)
