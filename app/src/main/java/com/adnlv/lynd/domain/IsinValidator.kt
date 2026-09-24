package com.adnlv.lynd.domain

object IsinValidator {

    private val isinRegex = Regex("^[A-Z]{2}[A-Z0-9]{9}[0-9]$")

    fun isValid(isin: String): Boolean {
        if (isin.length != 12) return false
        if (!isinRegex.matches(isin)) return false
        return validateLuhn(isin)
    }

    fun validateCodeInput(code: String, prefix: String): String? {
        if (code.isEmpty()) return null

        if (!code.all { it.isDigit() }) {
            return "Code must contain only digits"
        }

        if (code.length > 6) {
            return "Code must not exceed 6 digits"
        }

        if (code.length == 6) {
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
                    append(ch - 'A' + 10)
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
                val doubled = digit * 2
                sum += (doubled / 10) + (doubled % 10)
            } else {
                sum += digit
            }
            multiplyByTwo = !multiplyByTwo
        }

        val checkDigit = (10 - (sum % 10)) % 10
        val actualCheckDigit = isin.last() - '0'

        return checkDigit == actualCheckDigit
    }
}
