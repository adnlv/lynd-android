package com.adnlv.lynd.domain

object ApproximateStringMatcher {

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
            val score = 100 - (target.length - trimmedQuery.length)
            return MatchResult(isMatch = true, score = score, matchedIndices = indices)
        }

        val substringIndex = target.indexOf(trimmedQuery, ignoreCase = true)
        if (substringIndex >= 0) {
            val indices = (substringIndex until substringIndex + trimmedQuery.length).toSet()
            val score = 80 - substringIndex
            return MatchResult(isMatch = true, score = score, matchedIndices = indices)
        }

        val subseqIndices = findSubsequenceIndices(trimmedQuery, target)
        if (subseqIndices != null) {
            val score = 60 + subseqIndices.size
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
        if (query.length < 3) return null

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

        val maxAllowedMismatches = if (query.length >= 5) 2 else 1
        if (mismatches <= maxAllowedMismatches) {
            val score = 40 - mismatches * 10
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
        if (trimmedQuery.isEmpty()) return candidates.take(50)

        return candidates
            .mapNotNull { isin ->
                val code = if (isin.startsWith(prefix, ignoreCase = true)) {
                    isin.removePrefix(prefix)
                } else {
                    isin.takeLast(6)
                }
                val result = match(trimmedQuery, code)
                if (result.isMatch) isin to result else null
            }
            .sortedWith(compareByDescending<Pair<String, MatchResult>> { it.second.score }.thenBy { it.first })
            .map { it.first }
            .take(50)
    }
}
