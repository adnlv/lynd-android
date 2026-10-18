package com.adnlv.lynd.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

const val DEFAULT_SYNC_METADATA_ID = 1

@Entity(tableName = "sync_metadata")
data class SyncMetadataEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int = DEFAULT_SYNC_METADATA_ID,

    @ColumnInfo(name = "last_synced_at")
    val lastSyncedAt: Long
)
