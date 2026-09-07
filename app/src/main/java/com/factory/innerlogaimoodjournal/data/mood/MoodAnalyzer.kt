package com.factory.innerlogaimoodjournal.data.mood

import com.factory.innerlogaimoodjournal.data.model.MoodLevel

object MoodAnalyzer {

    private val positiveWords = listOf(
        "happy", "great", "good", "grateful", "excited", "love", "joy", "calm",
        "relaxed", "proud", "accomplished", "peaceful", "hopeful", "energized",
        "thankful", "motivated", "confident", "blessed", "amazing", "wonderful"
    )

    private val negativeWords = listOf(
        "sad", "angry", "anxious", "stressed", "tired", "worried", "upset",
        "frustrated", "lonely", "overwhelmed", "depressed", "hopeless", "afraid",
        "scared", "exhausted", "hurt", "disappointed", "hate", "awful", "terrible"
    )

    fun suggestMood(text: String): MoodLevel {
        val words = text.lowercase().split(Regex("\\W+")).filter { it.isNotBlank() }
        if (words.isEmpty()) return MoodLevel.OKAY

        var score = 0
        words.forEach { word ->
            if (positiveWords.contains(word)) score++
            if (negativeWords.contains(word)) score--
        }

        return when {
            score >= 3 -> MoodLevel.GREAT
            score == 1 || score == 2 -> MoodLevel.GOOD
            score == 0 -> MoodLevel.OKAY
            score == -1 || score == -2 -> MoodLevel.LOW
            else -> MoodLevel.AWFUL
        }
    }

    fun generateInsight(text: String, mood: MoodLevel): String {
        val words = text.lowercase().split(Regex("\\W+")).filter { it.isNotBlank() }
        val matchedPositive = words.filter { positiveWords.contains(it) }.distinct()
        val matchedNegative = words.filter { negativeWords.contains(it) }.distinct()

        return when (mood) {
            MoodLevel.GREAT, MoodLevel.GOOD -> {
                val highlight = matchedPositive.firstOrNull()
                if (highlight != null) {
                    "It sounds like \"$highlight\" played a big part in your day. Keep noticing what lifts your mood."
                } else {
                    "Your entry carries a positive tone today. Take a moment to appreciate what went well."
                }
            }
            MoodLevel.OKAY -> "A balanced day. Consider what small thing could tip tomorrow in a brighter direction."
            MoodLevel.LOW, MoodLevel.AWFUL -> {
                val highlight = matchedNegative.firstOrNull()
                if (highlight != null) {
                    "Feeling \"$highlight\" is valid. Try a short walk, a few deep breaths, or reaching out to someone you trust."
                } else {
                    "It sounds like today was tough. Be gentle with yourself — this feeling will pass."
                }
            }
        }
    }

    fun weeklySummary(scores: List<Int>): String {
        if (scores.isEmpty()) return "Log a few more days to see your weekly mood trend."
        val average = scores.average()
        val trend = if (scores.size >= 2) scores.last() - scores.first() else 0
        val averageText = when {
            average >= 4.0 -> "You've been feeling great this week overall."
            average >= 3.0 -> "You've had a fairly balanced week."
            else -> "This week has leaned a bit heavier emotionally."
        }
        val trendText = when {
            trend > 0 -> "Your mood is trending upward — keep it going."
            trend < 0 -> "Your mood has dipped recently. Consider some self-care."
            else -> "Your mood has stayed steady."
        }
        return "$averageText $trendText"
    }
}
