package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.*
import com.example.util.SecurityUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

@Database(
    entities = [User::class, Shipment::class, DispatchBatch::class, ReturnRecord::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun shipmentDao(): ShipmentDao
    abstract fun dispatchBatchDao(): DispatchBatchDao
    abstract fun returnRecordDao(): ReturnRecordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "shiptracker_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database)
                }
            }
        }

        suspend fun populateInitialData(db: AppDatabase) {
            val now = System.currentTimeMillis()
            val dayMillis = 86_400_000L

            // 1. Initial Users (Owner: Nolan Caparros, Staff: GJ Caparros)
            val owner = User(
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
            val staffGJ = User(
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
            val staffInactive = User(
                id = "usr_staff_03",
                username = "pedro",
                fullName = "Pedro Penduko",
                role = UserRole.STAFF,
                pin = "3333",
                passwordHash = SecurityUtil.hashPin("3333"),
                isActive = false,
                deactivationReason = "Account deactivated by Owner Nolan Caparros (Auditing)",
                canScanParcels = false,
                canDispatch = false,
                canHandleReturns = false,
                canEditShipments = false
            )
            db.userDao().insertUsers(listOf(owner, staffGJ, staffInactive))

            // 2. Initial Sample Shipments representing each stage
            val s1 = Shipment(
                id = "shp_001",
                trackingNumber = "SPXPH049281729",
                platform = PlatformType.SHOPEE,
                courier = CourierType.SPX,
                status = ShipmentStatus.SCANNED,
                recipientName = "Camille Rivera",
                recipientPhone = "09171234567",
                recipientAddress = "Quezon City, Metro Manila",
                scannedAt = now - 15 * 60 * 1000,
                scannedByUserId = staffGJ.id,
                scannedByName = staffGJ.fullName,
                notes = "Fragile glass cup x2",
                syncStatus = SyncStatus.SYNCED,
                timelineLog = "Scanned by GJ Caparros"
            )

            val s2 = Shipment(
                id = "shp_002",
                trackingNumber = "JZ99201948210",
                platform = PlatformType.TIKTOK,
                courier = CourierType.JT_EXPRESS,
                status = ShipmentStatus.PREPARED,
                recipientName = "Mark Anthony Tan",
                recipientPhone = "09289876543",
                recipientAddress = "Cebu City, Cebu",
                scannedAt = now - 2 * 3600 * 1000,
                scannedByUserId = staffGJ.id,
                scannedByName = staffGJ.fullName,
                notes = "Wireless earbuds black (x1)",
                syncStatus = SyncStatus.SYNCED,
                timelineLog = "Scanned → Packed & Barcode Label Verified"
            )

            val s3 = Shipment(
                id = "shp_003",
                trackingNumber = "LZD-9988214309",
                platform = PlatformType.LAZADA,
                courier = CourierType.NINJA_VAN,
                status = ShipmentStatus.PREPARED,
                recipientName = "Patricia Reyes",
                recipientPhone = "09087654321",
                recipientAddress = "Davao City, Davao del Sur",
                scannedAt = now - 3 * 3600 * 1000,
                scannedByUserId = staffGJ.id,
                scannedByName = staffGJ.fullName,
                notes = "Oversized hoodie grey (L)",
                syncStatus = SyncStatus.PENDING_SYNC,
                timelineLog = "Scanned → Weighed (0.65kg) → In Dispatch Bin"
            )

            val s4 = Shipment(
                id = "shp_004",
                trackingNumber = "JZ88204918231",
                platform = PlatformType.SHOPEE,
                courier = CourierType.JT_EXPRESS,
                status = ShipmentStatus.DISPATCHED,
                recipientName = "Bea Alcantara",
                recipientPhone = "09182233445",
                recipientAddress = "Pasig City, Metro Manila",
                scannedAt = now - dayMillis,
                scannedByUserId = staffGJ.id,
                scannedByName = staffGJ.fullName,
                dispatchBatchId = "BATCH-20260923-01",
                notes = "Ceramic plate set",
                syncStatus = SyncStatus.SYNCED,
                timelineLog = "Scanned → Prepared → Dispatched in BATCH-20260923-01"
            )

            val s5 = Shipment(
                id = "shp_005",
                trackingNumber = "FLASH-PH-7718290",
                platform = PlatformType.FB_MARKETPLACE,
                courier = CourierType.FLASH,
                status = ShipmentStatus.DELIVERED,
                recipientName = "Dave Mendoza",
                recipientPhone = "09391122334",
                recipientAddress = "Imus, Cavite",
                scannedAt = now - 3 * dayMillis,
                scannedByUserId = staffGJ.id,
                scannedByName = staffGJ.fullName,
                dispatchBatchId = "BATCH-20260921-01",
                notes = "Gaming mouse pad",
                syncStatus = SyncStatus.SYNCED,
                timelineLog = "Dispatched → Delivered successfully (COD collected)"
            )

            val s6 = Shipment(
                id = "shp_006",
                trackingNumber = "LBC-8891029384",
                platform = PlatformType.DIRECT_ORDER,
                courier = CourierType.LBC,
                status = ShipmentStatus.RETURNED,
                recipientName = "Kenneth Cruz",
                recipientPhone = "09459988776",
                recipientAddress = "Angeles City, Pampanga",
                scannedAt = now - 5 * dayMillis,
                scannedByUserId = staffGJ.id,
                scannedByName = staffGJ.fullName,
                dispatchBatchId = "BATCH-20260919-01",
                notes = "Smart watch strap",
                syncStatus = SyncStatus.SYNCED,
                timelineLog = "Dispatched → Delivery Failed (3 Attempts) → Returned to Warehouse"
            )

            db.shipmentDao().insertShipments(listOf(s1, s2, s3, s4, s5, s6))

            // 3. Initial Dispatch Batch
            val batch1 = DispatchBatch(
                id = "BATCH-20260923-01",
                batchNumber = "BATCH-20260923-01",
                courier = CourierType.JT_EXPRESS,
                handoverTimestamp = now - dayMillis,
                dispatchedByUserId = staffGJ.id,
                dispatchedByName = staffGJ.fullName,
                courierRiderName = "Kuya Arnel (Plate: 829-NCR)",
                totalParcels = 18,
                notes = "Afternoon 3:00 PM regular warehouse pickup",
                syncStatus = SyncStatus.SYNCED
            )
            db.dispatchBatchDao().insertBatch(batch1)

            // 4. Initial Return Record
            val return1 = ReturnRecord(
                id = "ret_001",
                shipmentId = s6.id,
                trackingNumber = s6.trackingNumber,
                platform = s6.platform,
                courier = s6.courier,
                reason = ReturnReason.FAILED_DELIVERY,
                itemCondition = ItemCondition.GOOD_RESALABLE,
                refundStatus = RefundStatus.PROCESSED_REFUNDED,
                notes = "Buyer unreachable during 3 attempts. Box intact, returned to shelf inventory.",
                loggedAt = now - 2 * dayMillis,
                loggedByUserId = staffGJ.id,
                loggedByName = staffGJ.fullName,
                approvedByUserId = owner.id,
                approvedByName = owner.fullName,
                syncStatus = SyncStatus.SYNCED
            )
            db.returnRecordDao().insertReturn(return1)
        }
    }
}
