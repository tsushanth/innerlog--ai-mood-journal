package com.factory.innerlogaimoodjournal.data.model

enum class MoodLevel(val score: Int, val emoji: String, val label: String) {
    AWFUL(1, "😞", "Awful"),
    LOW(2, "😕", "Low"),
    OKAY(3, "😐", "Okay"),
    GOOD(4, "🙂", "Good"),
    GREAT(5, "😄", "Great");

    companion object {
        fun fromScore(score: Int): MoodLevel = entries.firstOrNull { it.score == score } ?: OKAY
    }
}
