package com.adnlv.lynd.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        BondEntity::class,
        BondPaymentEntity::class,
        HoldingEntity::class,
        SyncMetadataEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bondDao(): BondDao
    abstract fun holdingDao(): HoldingDao
    abstract fun payoutDao(): PayoutDao
    abstract fun syncMetadataDao(): SyncMetadataDao
}
