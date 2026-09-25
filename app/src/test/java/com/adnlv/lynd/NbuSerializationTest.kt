package com.adnlv.lynd

import com.adnlv.lynd.data.network.NbuPaymentDto
import com.adnlv.lynd.data.network.NbuSecurityDto
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class NbuSerializationTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Test
    fun deserializeSecurityDto_withFullFields_parsesCorrectly() {
        val jsonString = """
            {
                "cpcode": "UA4000187348",
                "nominal": 1000.0,
                "auk_proc": 15.5,
                "pgs_date": "2026-09-15",
                "val_code": "UAH",
                "emit_name": "Ministry of Finance",
                "payments": [
                    {
                        "pay_date": "2025-09-15",
                        "pay_type": "1",
                        "pay_val": 77.5
                    }
                ]
            }
        """.trimIndent()

        val dto = json.decodeFromString<NbuSecurityDto>(jsonString)

        assertEquals("UA4000187348", dto.cpcode)
        assertEquals(1000.0, dto.nominal ?: 0.0, 0.001)
        assertEquals(15.5, dto.aukProc ?: 0.0, 0.001)
        assertEquals("2026-09-15", dto.pgsDate)
        assertEquals("UAH", dto.valCode)
        assertEquals("Ministry of Finance", dto.emitName)
        assertNotNull(dto.payments)
        assertEquals(1, dto.payments?.size)

        val payment = dto.payments?.first()
        assertEquals("2025-09-15", payment?.payDate)
        assertEquals("1", payment?.payType)
        assertEquals(77.5, payment?.payVal ?: 0.0, 0.001)
    }

    @Test
    fun deserializeSecurityDto_withMissingAndNullFields_handlesDefaultsSafely() {
        val jsonString = """
            {
                "cpcode": "UA4000187348",
                "nominal": null,
                "auk_proc": null
            }
        """.trimIndent()

        val dto = json.decodeFromString<NbuSecurityDto>(jsonString)

        assertEquals("UA4000187348", dto.cpcode)
        assertNull(dto.nominal)
        assertNull(dto.aukProc)
        assertNull(dto.pgsDate)
        assertNull(dto.valCode)
        assertNull(dto.emitName)
        assertNull(dto.payments)
    }

    @Test
    fun deserializePaymentDto_handlesMissingFields() {
        val jsonString = "{}"
        val payment = json.decodeFromString<NbuPaymentDto>(jsonString)

        assertNull(payment.payDate)
        assertNull(payment.payType)
        assertNull(payment.payVal)
    }
}
