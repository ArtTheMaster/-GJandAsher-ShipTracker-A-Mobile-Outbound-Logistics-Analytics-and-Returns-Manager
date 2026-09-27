package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY fullName ASC")
    fun getAllUsers(): Flow<List<User>>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): User?

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<User>)

    @Update
    suspend fun updateUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)
}

@Dao
interface ShipmentDao {
    @Query("SELECT * FROM shipments ORDER BY scannedAt DESC")
    fun getAllShipments(): Flow<List<Shipment>>

    @Query("SELECT * FROM shipments WHERE id = :id LIMIT 1")
    suspend fun getShipmentById(id: String): Shipment?

    @Query("SELECT * FROM shipments WHERE trackingNumber = :trackingNumber LIMIT 1")
    suspend fun getShipmentByTrackingNumber(trackingNumber: String): Shipment?

    @Query("SELECT * FROM shipments WHERE status = :status ORDER BY scannedAt DESC")
    fun getShipmentsByStatus(status: ShipmentStatus): Flow<List<Shipment>>

    @Query("SELECT * FROM shipments WHERE syncStatus = :syncStatus")
    fun getShipmentsBySyncStatus(syncStatus: SyncStatus): Flow<List<Shipment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShipment(shipment: Shipment)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShipments(shipments: List<Shipment>)

    @Update
    suspend fun updateShipment(shipment: Shipment)

    @Query("UPDATE shipments SET status = :status, lastUpdated = :timestamp, timelineLog = :log WHERE id = :id")
    suspend fun updateStatus(id: String, status: ShipmentStatus, timestamp: Long, log: String)

    @Query("UPDATE shipments SET syncStatus = :syncStatus WHERE id = :id")
    suspend fun updateSyncStatus(id: String, syncStatus: SyncStatus)

    @Delete
    suspend fun deleteShipment(shipment: Shipment)
}

@Dao
interface DispatchBatchDao {
    @Query("SELECT * FROM dispatch_batches ORDER BY handoverTimestamp DESC")
    fun getAllBatches(): Flow<List<DispatchBatch>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(batch: DispatchBatch)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatches(batches: List<DispatchBatch>)
}

@Dao
interface ReturnRecordDao {
    @Query("SELECT * FROM return_records ORDER BY loggedAt DESC")
    fun getAllReturns(): Flow<List<ReturnRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReturn(returnRecord: ReturnRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReturns(returns: List<ReturnRecord>)

    @Update
    suspend fun updateReturn(returnRecord: ReturnRecord)
}
