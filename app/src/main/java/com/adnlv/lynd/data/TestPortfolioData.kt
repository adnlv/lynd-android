package com.adnlv.lynd.data

import com.adnlv.lynd.data.db.BondDao
import com.adnlv.lynd.data.db.BondEntity
import com.adnlv.lynd.data.db.BondPaymentEntity
import com.adnlv.lynd.data.db.HoldingDao
import com.adnlv.lynd.data.db.HoldingEntity
import java.math.BigDecimal
import java.time.LocalDate

object TestPortfolioData {

    val bonds = listOf(
        BondEntity(
            isin = "UA4000187348",
            name = "Міністерство фінансів України",
            currency = "UAH",
            nominalValue = BigDecimal("1000.00"),
            couponRate = BigDecimal("12.50"),
            maturityDate = LocalDate.of(2029, 10, 12)
        ),
        BondEntity(
            isin = "UA4000190441",
            name = "Міністерство фінансів України",
            currency = "UAH",
            nominalValue = BigDecimal("1000.00"),
            couponRate = BigDecimal("11.83"),
            maturityDate = LocalDate.of(2026, 10, 14)
        ),
        BondEntity(
            isin = "UA4000238281",
            name = "Міністерство фінансів України",
            currency = "UAH",
            nominalValue = BigDecimal("1000.00"),
            couponRate = BigDecimal("16.35"),
            maturityDate = LocalDate.of(2026, 12, 16)
        ),
        BondEntity(
            isin = "UA4000236541",
            name = "Міністерство фінансів України",
            currency = "USD",
            nominalValue = BigDecimal("1000.00"),
            couponRate = BigDecimal("4.17"),
            maturityDate = LocalDate.of(2027, 2, 4)
        ),
        BondEntity(
            isin = "UA4000237242",
            name = "Міністерство фінансів України",
            currency = "USD",
            nominalValue = BigDecimal("1000.00"),
            couponRate = BigDecimal("4.12"),
            maturityDate = LocalDate.of(2027, 4, 15)
        ),
        BondEntity(
            isin = "UA4000237077",
            name = "Міністерство фінансів України",
            currency = "EUR",
            nominalValue = BigDecimal("1000.00"),
            couponRate = BigDecimal("3.19"),
            maturityDate = LocalDate.of(2027, 2, 25)
        ),
        BondEntity(
            isin = "UA4000238364",
            name = "Міністерство фінансів України",
            currency = "EUR",
            nominalValue = BigDecimal("1000.00"),
            couponRate = BigDecimal("3.24"),
            maturityDate = LocalDate.of(2027, 5, 6)
        )
    )

    val payments = listOf(
        // UA4000187348 (UAH)
        BondPaymentEntity(bondIsin = "UA4000187348", payDate = LocalDate.of(2026, 4, 17), payType = "coupon", payVal = BigDecimal("62.50")),
        BondPaymentEntity(bondIsin = "UA4000187348", payDate = LocalDate.of(2026, 10, 16), payType = "coupon", payVal = BigDecimal("62.50")),
        BondPaymentEntity(bondIsin = "UA4000187348", payDate = LocalDate.of(2027, 4, 16), payType = "coupon", payVal = BigDecimal("62.50")),
        BondPaymentEntity(bondIsin = "UA4000187348", payDate = LocalDate.of(2027, 10, 15), payType = "coupon", payVal = BigDecimal("62.50")),
        BondPaymentEntity(bondIsin = "UA4000187348", payDate = LocalDate.of(2028, 4, 14), payType = "coupon", payVal = BigDecimal("62.50")),
        BondPaymentEntity(bondIsin = "UA4000187348", payDate = LocalDate.of(2028, 10, 13), payType = "coupon", payVal = BigDecimal("62.50")),
        BondPaymentEntity(bondIsin = "UA4000187348", payDate = LocalDate.of(2029, 4, 13), payType = "coupon", payVal = BigDecimal("62.50")),
        BondPaymentEntity(bondIsin = "UA4000187348", payDate = LocalDate.of(2029, 10, 12), payType = "coupon", payVal = BigDecimal("62.50")),
        BondPaymentEntity(bondIsin = "UA4000187348", payDate = LocalDate.of(2029, 10, 12), payType = "redemption", payVal = BigDecimal("1000.00")),

        // UA4000190441 (UAH)
        BondPaymentEntity(bondIsin = "UA4000190441", payDate = LocalDate.of(2026, 4, 15), payType = "coupon", payVal = BigDecimal("59.15")),
        BondPaymentEntity(bondIsin = "UA4000190441", payDate = LocalDate.of(2026, 10, 14), payType = "coupon", payVal = BigDecimal("59.15")),
        BondPaymentEntity(bondIsin = "UA4000190441", payDate = LocalDate.of(2026, 10, 14), payType = "redemption", payVal = BigDecimal("1000.00")),

        // UA4000238281 (UAH)
        BondPaymentEntity(bondIsin = "UA4000238281", payDate = LocalDate.of(2025, 12, 17), payType = "coupon", payVal = BigDecimal("81.75")),
        BondPaymentEntity(bondIsin = "UA4000238281", payDate = LocalDate.of(2026, 6, 17), payType = "coupon", payVal = BigDecimal("81.75")),
        BondPaymentEntity(bondIsin = "UA4000238281", payDate = LocalDate.of(2026, 12, 16), payType = "coupon", payVal = BigDecimal("81.75")),
        BondPaymentEntity(bondIsin = "UA4000238281", payDate = LocalDate.of(2026, 12, 16), payType = "redemption", payVal = BigDecimal("1000.00")),

        // UA4000236541 (USD)
        BondPaymentEntity(bondIsin = "UA4000236541", payDate = LocalDate.of(2025, 8, 7), payType = "coupon", payVal = BigDecimal("20.85")),
        BondPaymentEntity(bondIsin = "UA4000236541", payDate = LocalDate.of(2026, 2, 5), payType = "coupon", payVal = BigDecimal("20.85")),
        BondPaymentEntity(bondIsin = "UA4000236541", payDate = LocalDate.of(2026, 8, 6), payType = "coupon", payVal = BigDecimal("20.85")),
        BondPaymentEntity(bondIsin = "UA4000236541", payDate = LocalDate.of(2027, 2, 4), payType = "coupon", payVal = BigDecimal("20.85")),
        BondPaymentEntity(bondIsin = "UA4000236541", payDate = LocalDate.of(2027, 2, 4), payType = "redemption", payVal = BigDecimal("1000.00")),

        // UA4000237242 (USD)
        BondPaymentEntity(bondIsin = "UA4000237242", payDate = LocalDate.of(2025, 10, 16), payType = "coupon", payVal = BigDecimal("20.60")),
        BondPaymentEntity(bondIsin = "UA4000237242", payDate = LocalDate.of(2026, 4, 16), payType = "coupon", payVal = BigDecimal("20.60")),
        BondPaymentEntity(bondIsin = "UA4000237242", payDate = LocalDate.of(2026, 10, 15), payType = "coupon", payVal = BigDecimal("20.60")),
        BondPaymentEntity(bondIsin = "UA4000237242", payDate = LocalDate.of(2027, 4, 15), payType = "coupon", payVal = BigDecimal("20.60")),
        BondPaymentEntity(bondIsin = "UA4000237242", payDate = LocalDate.of(2027, 4, 15), payType = "redemption", payVal = BigDecimal("1000.00")),

        // UA4000237077 (EUR)
        BondPaymentEntity(bondIsin = "UA4000237077", payDate = LocalDate.of(2026, 2, 26), payType = "coupon", payVal = BigDecimal("15.95")),
        BondPaymentEntity(bondIsin = "UA4000237077", payDate = LocalDate.of(2026, 8, 27), payType = "coupon", payVal = BigDecimal("15.95")),
        BondPaymentEntity(bondIsin = "UA4000237077", payDate = LocalDate.of(2027, 2, 25), payType = "coupon", payVal = BigDecimal("15.95")),
        BondPaymentEntity(bondIsin = "UA4000237077", payDate = LocalDate.of(2027, 2, 25), payType = "redemption", payVal = BigDecimal("1000.00")),

        // UA4000238364 (EUR)
        BondPaymentEntity(bondIsin = "UA4000238364", payDate = LocalDate.of(2026, 5, 7), payType = "coupon", payVal = BigDecimal("16.20")),
        BondPaymentEntity(bondIsin = "UA4000238364", payDate = LocalDate.of(2026, 11, 5), payType = "coupon", payVal = BigDecimal("16.20")),
        BondPaymentEntity(bondIsin = "UA4000238364", payDate = LocalDate.of(2027, 5, 6), payType = "coupon", payVal = BigDecimal("16.20")),
        BondPaymentEntity(bondIsin = "UA4000238364", payDate = LocalDate.of(2027, 5, 6), payType = "redemption", payVal = BigDecimal("1000.00"))
    )

    val holdings = listOf(
        // UA4000187348 (UAH) - 3 lots
        HoldingEntity(
            isin = "UA4000187348",
            quantity = 10,
            pricePerBond = BigDecimal("1000.00"),
            totalPaidAmount = BigDecimal("10000.00"),
            purchaseDate = LocalDate.of(2026, 1, 15)
        ),
        HoldingEntity(
            isin = "UA4000187348",
            quantity = 15,
            pricePerBond = BigDecimal("1010.00"),
            totalPaidAmount = BigDecimal("15150.00"),
            purchaseDate = LocalDate.of(2026, 3, 20)
        ),
        HoldingEntity(
            isin = "UA4000187348",
            quantity = 5,
            pricePerBond = BigDecimal("990.00"),
            totalPaidAmount = BigDecimal("4950.00"),
            purchaseDate = LocalDate.of(2026, 5, 18)
        ),

        // UA4000190441 (UAH)
        HoldingEntity(
            isin = "UA4000190441",
            quantity = 5,
            pricePerBond = BigDecimal("1000.00"),
            totalPaidAmount = BigDecimal("5000.00"),
            purchaseDate = LocalDate.of(2026, 2, 1)
        ),

        // UA4000238281 (UAH)
        HoldingEntity(
            isin = "UA4000238281",
            quantity = 25,
            pricePerBond = BigDecimal("1020.00"),
            totalPaidAmount = BigDecimal("25500.00"),
            purchaseDate = LocalDate.of(2026, 3, 10)
        ),

        // UA4000236541 (USD) - 2 lots
        HoldingEntity(
            isin = "UA4000236541",
            quantity = 15,
            pricePerBond = BigDecimal("1000.00"),
            totalPaidAmount = BigDecimal("15000.00"),
            purchaseDate = LocalDate.of(2026, 2, 15)
        ),
        HoldingEntity(
            isin = "UA4000236541",
            quantity = 10,
            pricePerBond = BigDecimal("995.00"),
            totalPaidAmount = BigDecimal("9950.00"),
            purchaseDate = LocalDate.of(2026, 4, 10)
        ),

        // UA4000237242 (USD)
        HoldingEntity(
            isin = "UA4000237242",
            quantity = 8,
            pricePerBond = BigDecimal("1000.00"),
            totalPaidAmount = BigDecimal("8000.00"),
            purchaseDate = LocalDate.of(2026, 4, 1)
        ),

        // UA4000237077 (EUR) - 2 lots
        HoldingEntity(
            isin = "UA4000237077",
            quantity = 20,
            pricePerBond = BigDecimal("1000.00"),
            totalPaidAmount = BigDecimal("20000.00"),
            purchaseDate = LocalDate.of(2026, 2, 20)
        ),
        HoldingEntity(
            isin = "UA4000237077",
            quantity = 8,
            pricePerBond = BigDecimal("1005.00"),
            totalPaidAmount = BigDecimal("8040.00"),
            purchaseDate = LocalDate.of(2026, 5, 12)
        ),

        // UA4000238364 (EUR)
        HoldingEntity(
            isin = "UA4000238364",
            quantity = 12,
            pricePerBond = BigDecimal("1000.00"),
            totalPaidAmount = BigDecimal("12000.00"),
            purchaseDate = LocalDate.of(2026, 5, 1)
        )
    )

    suspend fun seed(bondDao: BondDao, holdingDao: HoldingDao) {
        bondDao.upsertBonds(bonds)
        bondDao.upsertPayments(payments)
        for (holding in holdings) {
            holdingDao.insertHolding(holding)
        }
    }
}
