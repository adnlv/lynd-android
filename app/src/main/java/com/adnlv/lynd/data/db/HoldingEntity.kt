package com.adnlv.lynd.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.math.BigDecimal
import java.time.LocalDate

@Entity(
    tableName = "holdings",
    foreignKeys = [
        ForeignKey(
            entity = BondEntity::class,
            parentColumns = ["isin"],
            childColumns = ["isin"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["isin"])
    ]
)
data class HoldingEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "isin")
    val isin: String,

    @ColumnInfo(name = "quantity")
    val quantity: Int,

    @ColumnInfo(name = "price_per_bond")
    val pricePerBond: BigDecimal,

    @ColumnInfo(name = "total_paid_amount")
    val totalPaidAmount: BigDecimal,

    @ColumnInfo(name = "purchase_date")
    val purchaseDate: LocalDate
)
