package com.adnlv.lynd.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

data class HoldingWithBond(
    val id: Int,
    val isin: String,
    val bondName: String,
    val quantity: Int,
    val pricePerBond: java.math.BigDecimal,
    val totalPaidAmount: java.math.BigDecimal,
    val purchaseDate: java.time.LocalDate
)

@Dao
interface HoldingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHolding(holding: HoldingEntity)

    @Query("DELETE FROM holdings WHERE id = :id")
    suspend fun deleteHolding(id: Int)

    @Query("""
        SELECT 
            h.id AS id,
            h.isin AS isin,
            COALESCE(b.name, '') AS bondName,
            h.quantity AS quantity,
            h.price_per_bond AS pricePerBond,
            h.total_paid_amount AS totalPaidAmount,
            h.purchase_date AS purchaseDate
        FROM holdings h
        LEFT JOIN bonds b ON h.isin = b.isin
        ORDER BY h.purchase_date DESC
    """)
    fun getAllHoldings(): Flow<List<HoldingWithBond>>
}
