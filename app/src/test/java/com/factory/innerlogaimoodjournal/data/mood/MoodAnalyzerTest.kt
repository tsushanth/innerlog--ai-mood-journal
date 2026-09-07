package com.factory.innerlogaimoodjournal.data.mood

import com.factory.innerlogaimoodjournal.data.model.MoodLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MoodAnalyzerTest {

    @Test
    fun `suggestMood defaults to OKAY for blank text`() {
        assertEquals(MoodLevel.OKAY, MoodAnalyzer.suggestMood(""))
    }

    @Test
    fun `suggestMood detects strongly positive text as GREAT`() {
        val mood = MoodAnalyzer.suggestMood("I feel happy grateful and proud today")
        assertEquals(MoodLevel.GREAT, mood)
    }

    @Test
    fun `suggestMood detects mildly positive text as GOOD`() {
        val mood = MoodAnalyzer.suggestMood("Feeling happy today")
        assertEquals(MoodLevel.GOOD, mood)
    }

    @Test
    fun `suggestMood detects neutral text as OKAY`() {
        val mood = MoodAnalyzer.suggestMood("The weather was cloudy today")
        assertEquals(MoodLevel.OKAY, mood)
    }

    @Test
    fun `suggestMood detects mildly negative text as LOW`() {
        val mood = MoodAnalyzer.suggestMood("Feeling tired today")
        assertEquals(MoodLevel.LOW, mood)
    }

    @Test
    fun `suggestMood detects strongly negative text as AWFUL`() {
        val mood = MoodAnalyzer.suggestMood("I feel sad angry anxious and hopeless")
        assertEquals(MoodLevel.AWFUL, mood)
    }

    @Test
    fun `generateInsight references a matched positive word`() {
        val insight = MoodAnalyzer.generateInsight("I feel grateful today", MoodLevel.GOOD)
        assertTrue(insight.contains("grateful"))
    }

    @Test
    fun `generateInsight references a matched negative word`() {
        val insight = MoodAnalyzer.generateInsight("I feel anxious today", MoodLevel.LOW)
        assertTrue(insight.contains("anxious"))
    }

    @Test
    fun `weeklySummary prompts for more data when scores are empty`() {
        assertEquals("Log a few more days to see your weekly mood trend.", MoodAnalyzer.weeklySummary(emptyList()))
    }

    @Test
    fun `weeklySummary reports an upward trend`() {
        val summary = MoodAnalyzer.weeklySummary(listOf(2, 3, 5))
        assertTrue(summary.contains("trending upward"))
    }

    @Test
    fun `weeklySummary reports a downward trend`() {
        val summary = MoodAnalyzer.weeklySummary(listOf(5, 3, 1))
        assertTrue(summary.contains("dipped recently"))
    }
}
