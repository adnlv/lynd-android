package com.adnlv.lynd

import com.adnlv.lynd.domain.IsinValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class IsinValidatorTest {

    @Test
    fun validUkrainianBondIsins_passValidation() {
        assertTrue(IsinValidator.isValid("UA4000187348"))
        assertTrue(IsinValidator.isValid("UA4000238281"))
        assertTrue(IsinValidator.isValid("UA4000227185"))
        assertTrue(IsinValidator.isValid("US0378331005")) // Apple Inc
    }

    @Test
    fun invalidChecksum_failsValidation() {
        assertFalse(IsinValidator.isValid("UA4000187349"))
        assertFalse(IsinValidator.isValid("UA4000238280"))
        assertFalse(IsinValidator.isValid("US0378331004"))
    }

    @Test
    fun invalidFormatOrLength_failsValidation() {
        assertFalse(IsinValidator.isValid(""))
        assertFalse(IsinValidator.isValid("UA400018734"))
        assertFalse(IsinValidator.isValid("UA40001873489"))
        assertFalse(IsinValidator.isValid("1A4000187348"))
        assertFalse(IsinValidator.isValid("ua4000187348"))
        assertFalse(IsinValidator.isValid("UA400018734X"))
    }

    @Test
    fun validateCodeInput_handlesEmptyAndPartialDigits() {
        assertNull(IsinValidator.validateCodeInput("", "UA4000"))
        assertNull(IsinValidator.validateCodeInput("2", "UA4000"))
        assertNull(IsinValidator.validateCodeInput("23828", "UA4000"))
    }

    @Test
    fun validateCodeInput_detectsNonDigits() {
        val error = IsinValidator.validateCodeInput("12a", "UA4000")
        assertEquals("Code must contain only digits", error)

        val errorSpace = IsinValidator.validateCodeInput("12 45", "UA4000")
        assertEquals("Code must contain only digits", errorSpace)
    }

    @Test
    fun validateCodeInput_detectsExceedingLength() {
        val error = IsinValidator.validateCodeInput("1234567", "UA4000")
        assertEquals("Code must not exceed 6 digits", error)
    }

    @Test
    fun validateCodeInput_validatesSixDigitChecksum() {
        // UA4000238281 is valid
        assertNull(IsinValidator.validateCodeInput("238281", "UA4000"))

        // UA4000238282 has invalid checksum
        val error = IsinValidator.validateCodeInput("238282", "UA4000")
        assertEquals("Invalid ISIN checksum", error)
    }
}
