package com.adnlv.lynd.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SyncMetadataDao {
    @Query("SELECT last_synced_at FROM sync_metadata WHERE id = 1 LIMIT 1")
    suspend fun getLastSyncTime(): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSyncTime(metadata: SyncMetadataEntity)
}
