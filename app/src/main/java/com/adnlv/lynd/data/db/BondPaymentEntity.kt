package com.adnlv.lynd.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.math.BigDecimal
import java.time.LocalDate

@Entity(
    tableName = "bond_payments",
    foreignKeys = [
        ForeignKey(
            entity = BondEntity::class,
            parentColumns = ["isin"],
            childColumns = ["bond_isin"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["bond_isin"])
    ]
)
data class BondPaymentEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "bond_isin")
    val bondIsin: String,

    @ColumnInfo(name = "pay_date")
    val payDate: LocalDate,

    @ColumnInfo(name = "pay_type")
    val payType: String,

    @ColumnInfo(name = "pay_val")
    val payVal: BigDecimal
)
