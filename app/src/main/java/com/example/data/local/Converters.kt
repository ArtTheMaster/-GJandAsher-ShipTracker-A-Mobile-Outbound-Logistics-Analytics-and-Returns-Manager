package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.*

class Converters {
    @TypeConverter
    fun fromUserRole(value: UserRole): String = value.name

    @TypeConverter
    fun toUserRole(value: String): UserRole = try {
        UserRole.valueOf(value)
    } catch (e: Exception) {
        UserRole.STAFF
    }

    @TypeConverter
    fun fromPlatformType(value: PlatformType): String = value.name

    @TypeConverter
    fun toPlatformType(value: String): PlatformType = try {
        PlatformType.valueOf(value)
    } catch (e: Exception) {
        PlatformType.SHOPEE
    }

    @TypeConverter
    fun fromCourierType(value: CourierType): String = value.name

    @TypeConverter
    fun toCourierType(value: String): CourierType = try {
        CourierType.valueOf(value)
    } catch (e: Exception) {
        CourierType.JT_EXPRESS
    }

    @TypeConverter
    fun fromShipmentStatus(value: ShipmentStatus): String = value.name

    @TypeConverter
    fun toShipmentStatus(value: String): ShipmentStatus = try {
        ShipmentStatus.valueOf(value)
    } catch (e: Exception) {
        ShipmentStatus.SCANNED
    }

    @TypeConverter
    fun fromReturnReason(value: ReturnReason): String = value.name

    @TypeConverter
    fun toReturnReason(value: String): ReturnReason = try {
        ReturnReason.valueOf(value)
    } catch (e: Exception) {
        ReturnReason.OTHER
    }

    @TypeConverter
    fun fromItemCondition(value: ItemCondition): String = value.name

    @TypeConverter
    fun toItemCondition(value: String): ItemCondition = try {
        ItemCondition.valueOf(value)
    } catch (e: Exception) {
        ItemCondition.GOOD_RESALABLE
    }

    @TypeConverter
    fun fromRefundStatus(value: RefundStatus): String = value.name

    @TypeConverter
    fun toRefundStatus(value: String): RefundStatus = try {
        RefundStatus.valueOf(value)
    } catch (e: Exception) {
        RefundStatus.PENDING_APPROVAL
    }

    @TypeConverter
    fun fromSyncStatus(value: SyncStatus): String = value.name

    @TypeConverter
    fun toSyncStatus(value: String): SyncStatus = try {
        SyncStatus.valueOf(value)
    } catch (e: Exception) {
        SyncStatus.SYNCED
    }
}
