package com.adnlv.lynd.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface BondDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBond(bond: BondEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBonds(bonds: List<BondEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPayments(payments: List<BondPaymentEntity>)

    @Query("SELECT * FROM bonds WHERE isin = :isin LIMIT 1")
    suspend fun getBond(isin: String): BondEntity?
}
