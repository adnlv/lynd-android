package com.adnlv.lynd

import com.adnlv.lynd.data.db.Converters
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate

class ConvertersTest {

    private val converters = Converters()

    @Test
    fun localDateConversion_isCorrect() {
        val date = LocalDate.of(2026, 9, 17)
        val stringVal = converters.fromLocalDate(date)
        assertEquals("2026-09-17", stringVal)

        val parsedDate = converters.toLocalDate(stringVal)
        assertEquals(date, parsedDate)
    }

    @Test
    fun localDateNullConversion_returnsNull() {
        assertNull(converters.fromLocalDate(null))
        assertNull(converters.toLocalDate(null))
    }

    @Test
    fun bigDecimalConversion_isCorrect() {
        val amount = BigDecimal("1000.50")
        val stringVal = converters.fromBigDecimal(amount)
        assertEquals("1000.50", stringVal)

        val parsedAmount = converters.toBigDecimal(stringVal)
        assertEquals(amount, parsedAmount)
    }

    @Test
    fun bigDecimalNullConversion_returnsNull() {
        assertNull(converters.fromBigDecimal(null))
        assertNull(converters.toBigDecimal(null))
    }
}
