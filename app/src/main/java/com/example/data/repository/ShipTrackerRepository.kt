package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.util.SecurityUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

sealed class AuthResult {
    data class Success(val user: User) : AuthResult()
    data class Deactivated(val user: User, val reason: String) : AuthResult()
    object InvalidCredentials : AuthResult()
    object UserNotFound : AuthResult()
}

class ShipTrackerRepository(private val db: AppDatabase) {

    val allUsers: Flow<List<User>> = db.userDao().getAllUsers()
    val allShipments: Flow<List<Shipment>> = db.shipmentDao().getAllShipments()
    val allBatches: Flow<List<DispatchBatch>> = db.dispatchBatchDao().getAllBatches()
    val allReturns: Flow<List<ReturnRecord>> = db.returnRecordDao().getAllReturns()

    suspend fun authenticate(username: String, pin: String): AuthResult = withContext(Dispatchers.IO) {
        val cleanUser = username.trim().lowercase()
        var user = db.userDao().getUserByUsername(cleanUser)
        if (user == null) {
            // Check known aliases
            val alias = when (cleanUser) {
                "nolan" -> "nolancaparros"
                "nolancaparros" -> "nolan"
                "gj" -> "gjcaparros"
                "gjcaparros" -> "gj"
                else -> null
            }
            if (alias != null) {
                user = db.userDao().getUserByUsername(alias)
            }
        }
        if (user == null) return@withContext AuthResult.UserNotFound
        val pinValid = SecurityUtil.verifyPin(pin, user.passwordHash, user.pin)
        if (!pinValid) {
            return@withContext AuthResult.InvalidCredentials
        }
        if (!user.isActive) {
            val reason = if (user.deactivationReason.isNotBlank()) {
                user.deactivationReason
            } else {
                "Account deactivated by Owner Nolan Caparros. Please consult the owner to reactivate access."
            }
            return@withContext AuthResult.Deactivated(user, reason)
        }
        AuthResult.Success(user)
    }

    suspend fun checkDuplicateTracking(code: String): Shipment? = withContext(Dispatchers.IO) {
        db.shipmentDao().getShipmentByTrackingNumber(code.trim())
    }

    suspend fun saveShipment(shipment: Shipment) = withContext(Dispatchers.IO) {
        db.shipmentDao().insertShipment(shipment)
    }

    suspend fun updateShipmentStatus(id: String, newStatus: ShipmentStatus, log: String) = withContext(Dispatchers.IO) {
        db.shipmentDao().updateStatus(id, newStatus, System.currentTimeMillis(), log)
    }

    suspend fun createDispatchBatch(batch: DispatchBatch, shipmentIds: List<String>) = withContext(Dispatchers.IO) {
        db.dispatchBatchDao().insertBatch(batch)
        val now = System.currentTimeMillis()
        for (shipmentId in shipmentIds) {
            val existing = db.shipmentDao().getShipmentById(shipmentId)
            if (existing != null) {
                val updated = existing.copy(
                    status = ShipmentStatus.DISPATCHED,
                    dispatchBatchId = batch.id,
                    lastUpdated = now,
                    timelineLog = "${existing.timelineLog} → Dispatched in ${batch.batchNumber} (${batch.courier.displayName})"
                )
                db.shipmentDao().updateShipment(updated)
            }
        }
    }

    suspend fun logReturn(returnRecord: ReturnRecord) = withContext(Dispatchers.IO) {
        db.returnRecordDao().insertReturn(returnRecord)
        val existing = db.shipmentDao().getShipmentById(returnRecord.shipmentId)
        if (existing != null) {
            val updated = existing.copy(
                status = ShipmentStatus.RETURNED,
                lastUpdated = System.currentTimeMillis(),
                timelineLog = "${existing.timelineLog} → RETURNED: ${returnRecord.reason.label} (${returnRecord.itemCondition.label})"
            )
            db.shipmentDao().updateShipment(updated)
        }
    }

    suspend fun updateRefundStatus(
        returnId: String,
        newStatus: RefundStatus,
        ownerUser: User
    ) = withContext(Dispatchers.IO) {
        // Find existing return
        val currentReturns = db.returnRecordDao()
    }

    suspend fun insertUser(user: User) = withContext(Dispatchers.IO) {
        db.userDao().insertUser(user)
    }

    suspend fun updateUser(user: User) = withContext(Dispatchers.IO) {
        db.userDao().updateUser(user)
    }

    suspend fun toggleUserActive(user: User, reason: String = "") = withContext(Dispatchers.IO) {
        val willBeActive = !user.isActive
        val updated = user.copy(
            isActive = willBeActive,
            deactivationReason = if (!willBeActive) (if (reason.isNotBlank()) reason else "Deactivated by Owner Nolan") else ""
        )
        db.userDao().updateUser(updated)
    }

    suspend fun deleteUser(user: User) = withContext(Dispatchers.IO) {
        db.userDao().deleteUser(user)
    }

    suspend fun syncAllPending(): Int = withContext(Dispatchers.IO) {
        var count = 0
        count
    }
}
