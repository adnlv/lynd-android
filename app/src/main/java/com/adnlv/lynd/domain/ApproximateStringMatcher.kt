package com.adnlv.lynd.domain

import com.adnlv.lynd.core.AppConstants

object ApproximateStringMatcher {

    private const val PREFIX_MATCH_BASE_SCORE = 100
    private const val SUBSTRING_MATCH_BASE_SCORE = 80
    private const val SUBSEQUENCE_MATCH_BASE_SCORE = 60
    private const val APPROXIMATE_MATCH_BASE_SCORE = 40
    private const val MISMATCH_PENALTY_SCORE = 10
    private const val MIN_QUERY_LENGTH_FOR_APPROX = 3
    private const val LONG_QUERY_LENGTH_THRESHOLD = 5
    private const val MAX_MISMATCHES_FOR_SHORT_QUERY = 1
    private const val MAX_MISMATCHES_FOR_LONG_QUERY = 2

    data class MatchResult(
        val isMatch: Boolean,
        val score: Int = 0,
        val matchedIndices: Set<Int> = emptySet()
    )

    fun match(query: String, target: String): MatchResult {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isEmpty()) {
            return MatchResult(isMatch = true, score = 0, matchedIndices = emptySet())
        }
        if (target.isEmpty()) {
            return MatchResult(isMatch = false)
        }

        if (target.startsWith(trimmedQuery, ignoreCase = true)) {
            val indices = (0 until trimmedQuery.length).toSet()
            val score = PREFIX_MATCH_BASE_SCORE - (target.length - trimmedQuery.length)
            return MatchResult(isMatch = true, score = score, matchedIndices = indices)
        }

        val substringIndex = target.indexOf(trimmedQuery, ignoreCase = true)
        if (substringIndex >= 0) {
            val indices = (substringIndex until substringIndex + trimmedQuery.length).toSet()
            val score = SUBSTRING_MATCH_BASE_SCORE - substringIndex
            return MatchResult(isMatch = true, score = score, matchedIndices = indices)
        }

        val subseqIndices = findSubsequenceIndices(trimmedQuery, target)
        if (subseqIndices != null) {
            val score = SUBSEQUENCE_MATCH_BASE_SCORE + subseqIndices.size
            return MatchResult(isMatch = true, score = score, matchedIndices = subseqIndices)
        }

        val editResult = findApproximateMatch(trimmedQuery, target)
        if (editResult != null) {
            return editResult
        }

        return MatchResult(isMatch = false)
    }

    private fun findSubsequenceIndices(query: String, target: String): Set<Int>? {
        val indices = mutableSetOf<Int>()
        var targetIndex = 0
        for (qChar in query) {
            var found = false
            while (targetIndex < target.length) {
                if (target[targetIndex].equals(qChar, ignoreCase = true)) {
                    indices.add(targetIndex)
                    targetIndex++
                    found = true
                    break
                }
                targetIndex++
            }
            if (!found) return null
        }
        return indices
    }

    private fun findApproximateMatch(query: String, target: String): MatchResult? {
        if (query.length < MIN_QUERY_LENGTH_FOR_APPROX) return null

        val targetWindow = target.take(query.length)
        var mismatches = 0
        val matchedIndices = mutableSetOf<Int>()

        for (i in query.indices) {
            if (i < targetWindow.length) {
                if (query[i].equals(targetWindow[i], ignoreCase = true)) {
                    matchedIndices.add(i)
                } else {
                    mismatches++
                }
            } else {
                mismatches++
            }
        }

        val maxAllowedMismatches = if (query.length >= LONG_QUERY_LENGTH_THRESHOLD) {
            MAX_MISMATCHES_FOR_LONG_QUERY
        } else {
            MAX_MISMATCHES_FOR_SHORT_QUERY
        }
        if (mismatches <= maxAllowedMismatches) {
            val score = APPROXIMATE_MATCH_BASE_SCORE - mismatches * MISMATCH_PENALTY_SCORE
            return MatchResult(isMatch = true, score = score, matchedIndices = matchedIndices)
        }

        return null
    }

    fun filterAndSort(
        candidates: List<String>,
        prefix: String,
        query: String
    ): List<String> {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isEmpty()) return candidates.take(AppConstants.ISIN_SEARCH_LIMIT)

        return candidates
            .mapNotNull { isin ->
                val code = IsinValidator.extractCode(isin, prefix)
                val result = match(trimmedQuery, code)
                if (result.isMatch) isin to result else null
            }
            .sortedWith(compareByDescending<Pair<String, MatchResult>> { it.second.score }.thenBy { it.first })
            .map { it.first }
            .take(AppConstants.ISIN_SEARCH_LIMIT)
    }
}
