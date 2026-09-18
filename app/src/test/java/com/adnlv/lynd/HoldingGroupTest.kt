package com.adnlv.lynd

import com.adnlv.lynd.domain.HoldingGroup
import com.adnlv.lynd.domain.HoldingItem
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate

class HoldingGroupTest {

    @Test
    fun groupingByIsin_aggregatesQuantityCorrectly() {
        val item1 = HoldingItem(
            id = 1,
            isin = "UA4000238281",
            bondName = "Bond 1",
            quantity = 2,
            pricePerBond = BigDecimal("1000.00"),
            totalPaidAmount = BigDecimal("2000.00"),
            purchaseDate = LocalDate.of(2026, 7, 28)
        )
        val item2 = HoldingItem(
            id = 2,
            isin = "UA4000238281",
            bondName = "Bond 1",
            quantity = 3,
            pricePerBond = BigDecimal("1010.00"),
            totalPaidAmount = BigDecimal("3030.00"),
            purchaseDate = LocalDate.of(2026, 8, 15)
        )
        val item3 = HoldingItem(
            id = 3,
            isin = "UA4000187348",
            bondName = "Bond 2",
            quantity = 1,
            pricePerBond = BigDecimal("1000.00"),
            totalPaidAmount = BigDecimal("1000.00"),
            purchaseDate = LocalDate.of(2026, 9, 1)
        )

        val groups = listOf(item1, item2, item3)
            .groupBy { it.isin }
            .map { (isin, items) ->
                HoldingGroup(
                    isin = isin,
                    totalQuantity = items.sumOf { it.quantity },
                    items = items
                )
            }

        assertEquals(2, groups.size)
        val group1 = groups.first { it.isin == "UA4000238281" }
        assertEquals(5, group1.totalQuantity)
        assertEquals(2, group1.items.size)

        val group2 = groups.first { it.isin == "UA4000187348" }
        assertEquals(1, group2.totalQuantity)
        assertEquals(1, group2.items.size)
    }
}
