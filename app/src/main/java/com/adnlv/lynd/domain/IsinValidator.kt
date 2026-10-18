package com.adnlv.lynd.domain

import com.adnlv.lynd.core.AppConstants

object IsinValidator {

    private const val LUHN_RADIX = 10
    private const val LUHN_LETTER_OFFSET = 10
    private const val LUHN_MULTIPLIER = 2

    private val isinRegex = Regex("^[A-Z]{2}[A-Z0-9]{9}[0-9]$")

    fun isValid(isin: String): Boolean {
        if (isin.length != AppConstants.ISIN_TOTAL_LENGTH) return false
        if (!isinRegex.matches(isin)) return false
        return validateLuhn(isin)
    }

    fun extractCode(isin: String, prefix: String): String {
        return if (isin.startsWith(prefix, ignoreCase = true)) {
            isin.removePrefix(prefix)
        } else {
            isin.takeLast(AppConstants.ISIN_CODE_LENGTH)
        }
    }

    fun validateCodeInput(
        code: String,
        prefix: String,
        hasMatchingRecord: Boolean = true
    ): String? {
        if (code.isEmpty()) return null

        if (!code.all { it.isDigit() }) {
            return "Code must contain only digits"
        }

        if (code.length > AppConstants.ISIN_CODE_LENGTH) {
            return "Code must not exceed ${AppConstants.ISIN_CODE_LENGTH} digits"
        }

        if (!hasMatchingRecord) {
            return "Invalid code"
        }

        if (code.length == AppConstants.ISIN_CODE_LENGTH) {
            val fullIsin = "$prefix$code"
            if (!isValid(fullIsin)) {
                return "Invalid ISIN checksum"
            }
        }

        return null
    }

    private fun validateLuhn(isin: String): Boolean {
        val converted = buildString {
            for (ch in isin) {
                if (ch.isDigit()) {
                    append(ch)
                } else if (ch in 'A'..'Z') {
                    append(ch - 'A' + LUHN_LETTER_OFFSET)
                } else {
                    return false
                }
            }
        }

        var sum = 0
        var multiplyByTwo = true
        for (i in converted.length - 2 downTo 0) {
            val digit = converted[i] - '0'
            if (multiplyByTwo) {
                val doubled = digit * LUHN_MULTIPLIER
                sum += (doubled / LUHN_RADIX) + (doubled % LUHN_RADIX)
            } else {
                sum += digit
            }
            multiplyByTwo = !multiplyByTwo
        }

        val checkDigit = (LUHN_RADIX - (sum % LUHN_RADIX)) % LUHN_RADIX
        val actualCheckDigit = isin.last() - '0'

        return checkDigit == actualCheckDigit
    }
}
