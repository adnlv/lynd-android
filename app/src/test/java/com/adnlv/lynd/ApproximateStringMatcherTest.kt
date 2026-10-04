package com.adnlv.lynd

import com.adnlv.lynd.domain.ApproximateStringMatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ApproximateStringMatcherTest {

    @Test
    fun match_emptyQuery_returnsMatchWithZeroScore() {
        val result = ApproximateStringMatcher.match("", "187348")
        assertTrue(result.isMatch)
        assertEquals(0, result.score)
        assertTrue(result.matchedIndices.isEmpty())
    }

    @Test
    fun match_emptyTarget_returnsNoMatch() {
        val result = ApproximateStringMatcher.match("187", "")
        assertFalse(result.isMatch)
    }

    @Test
    fun match_exactPrefix_returnsMatchedIndicesAndHighScore() {
        val result = ApproximateStringMatcher.match("187", "187348")
        assertTrue(result.isMatch)
        assertEquals(setOf(0, 1, 2), result.matchedIndices)
        assertEquals(97, result.score) // 100 - (6 - 3)
    }

    @Test
    fun match_substring_returnsMatchedIndices() {
        val result = ApproximateStringMatcher.match("873", "187348")
        assertTrue(result.isMatch)
        assertEquals(setOf(1, 2, 3), result.matchedIndices)
        assertEquals(79, result.score) // 80 - 1
    }

    @Test
    fun match_subsequence_returnsMatchedIndices() {
        val result = ApproximateStringMatcher.match("178", "187348")
        assertTrue(result.isMatch)
        assertEquals(setOf(0, 2, 5), result.matchedIndices)
    }

    @Test
    fun match_oneTypo_returnsApproximateMatch() {
        // Query has length 4 with 1 mismatch at index 1: '9' instead of '8'
        val result = ApproximateStringMatcher.match("1973", "187348")
        assertTrue(result.isMatch)
        assertEquals(setOf(0, 2, 3), result.matchedIndices)
    }

    @Test
    fun match_twoTyposForLongQuery_returnsApproximateMatch() {
        // Query length 5 with 2 mismatches
        val result = ApproximateStringMatcher.match("19794", "187348")
        assertTrue(result.isMatch)
        assertEquals(setOf(0, 2, 4), result.matchedIndices)
    }

    @Test
    fun match_completelyUnrelated_returnsNoMatch() {
        val result = ApproximateStringMatcher.match("9999", "187348")
        assertFalse(result.isMatch)
    }

    @Test
    fun filterAndSort_ordersExactOverFuzzy() {
        val candidates = listOf(
            "UA4000999999",
            "UA4000187348",
            "UA4000187000",
            "UA4000108070"
        )
        val sorted = ApproximateStringMatcher.filterAndSort(candidates, "UA4000", "187")
        assertEquals("UA4000187000", sorted[0])
        assertEquals("UA4000187348", sorted[1])
        assertEquals("UA4000108070", sorted[2])
        assertFalse(sorted.contains("UA4000999999"))
    }
}
