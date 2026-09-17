package com.adnlv.lynd.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal
import java.time.LocalDate

@Entity(tableName = "bonds")
data class BondEntity(
    @PrimaryKey
    @ColumnInfo(name = "isin")
    val isin: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "currency")
    val currency: String,

    @ColumnInfo(name = "nominal_value")
    val nominalValue: BigDecimal,

    @ColumnInfo(name = "coupon_rate")
    val couponRate: BigDecimal,

    @ColumnInfo(name = "maturity_date")
    val maturityDate: LocalDate
)
