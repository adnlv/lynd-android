package com.adnlv.lynd.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

data class HoldingWithBond(
    val id: Int,
    val isin: String,
    val bondName: String,
    val quantity: Int,
    val pricePerBond: java.math.BigDecimal,
    val totalPaidAmount: java.math.BigDecimal,
    val purchaseDate: java.time.LocalDate,
    val currency: String,
    val couponRate: java.math.BigDecimal
)

@Dao
interface HoldingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHolding(holding: HoldingEntity)

    @Update
    suspend fun updateHolding(holding: HoldingEntity)

    @Query("DELETE FROM holdings WHERE id = :id")
    suspend fun deleteHolding(id: Int)

    @Query("SELECT * FROM holdings WHERE id = :id LIMIT 1")
    suspend fun getHoldingById(id: Int): HoldingEntity?

    @Query("""
        SELECT 
            h.id AS id,
            h.isin AS isin,
            COALESCE(b.name, '') AS bondName,
            h.quantity AS quantity,
            h.price_per_bond AS pricePerBond,
            h.total_paid_amount AS totalPaidAmount,
            h.purchase_date AS purchaseDate,
            COALESCE(b.currency, 'UAH') AS currency,
            COALESCE(b.coupon_rate, '0') AS couponRate
        FROM holdings h
        LEFT JOIN bonds b ON h.isin = b.isin
        ORDER BY h.purchase_date DESC
    """)
    fun getAllHoldings(): Flow<List<HoldingWithBond>>

    @Query("""
        SELECT * FROM bond_payments
        WHERE bond_isin IN (SELECT DISTINCT isin FROM holdings)
        ORDER BY pay_date ASC
    """)
    fun getPaymentsForHoldings(): Flow<List<BondPaymentEntity>>
}
