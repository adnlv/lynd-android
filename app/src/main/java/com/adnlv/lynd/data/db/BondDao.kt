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

    @Query("DELETE FROM bond_payments WHERE bond_isin = :isin")
    suspend fun deletePaymentsForBond(isin: String)

    @Query("DELETE FROM bond_payments")
    suspend fun deleteAllPayments()

    @Query("SELECT * FROM bonds WHERE isin = :isin LIMIT 1")
    suspend fun getBond(isin: String): BondEntity?

    @Query("SELECT isin FROM bonds WHERE isin LIKE '%' || :query || '%' ORDER BY isin LIMIT 50")
    suspend fun searchBondsByIsin(query: String): List<String>

    @Query("SELECT DISTINCT SUBSTR(isin, 1, 6) FROM bonds WHERE LENGTH(isin) = 12 ORDER BY 1")
    suspend fun getDistinctIsinPrefixes(): List<String>
}
