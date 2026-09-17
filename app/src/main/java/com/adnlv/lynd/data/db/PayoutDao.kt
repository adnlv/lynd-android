package com.adnlv.lynd.data.db

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal
import java.time.LocalDate

data class PayoutRow(
    val isin: String,
    val bondName: String,
    val payDate: LocalDate,
    val payType: String,
    val payVal: BigDecimal,
    val quantity: Int,
    val currency: String
)

@Dao
interface PayoutDao {
    @Query("""
        SELECT 
            bp.bond_isin AS isin,
            COALESCE(b.name, '') AS bondName,
            bp.pay_date AS payDate,
            bp.pay_type AS payType,
            bp.pay_val AS payVal,
            h.quantity AS quantity,
            COALESCE(b.currency, 'UAH') AS currency
        FROM holdings h
        INNER JOIN bond_payments bp ON h.isin = bp.bond_isin
        LEFT JOIN bonds b ON h.isin = b.isin
        ORDER BY bp.pay_date ASC
    """)
    fun getAllPayoutRows(): Flow<List<PayoutRow>>
}
