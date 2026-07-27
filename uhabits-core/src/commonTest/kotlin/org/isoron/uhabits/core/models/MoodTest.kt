package org.isoron.uhabits.core.models

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MoodTest {
    @Test
    fun testToEntryValue() {
        assertEquals(1000, Mood.toEntryValue(1))
        assertEquals(5000, Mood.toEntryValue(5))
    }

    @Test
    fun testFromEntryValue() {
        assertEquals(1, Mood.fromEntryValue(1000))
        assertEquals(5, Mood.fromEntryValue(5000))
    }

    @Test
    fun testIsKnownValue() {
        assertTrue(Mood.isKnownValue(1000))
        assertTrue(Mood.isKnownValue(5000))
        assertTrue(Mood.isKnownValue(3000))
        assertFalse(Mood.isKnownValue(Entry.UNKNOWN))
        assertFalse(Mood.isKnownValue(Entry.SKIP))
        assertFalse(Mood.isKnownValue(Entry.NO))
        assertFalse(Mood.isKnownValue(Entry.YES_MANUAL))
        assertFalse(Mood.isKnownValue(Entry.YES_AUTO))
    }

    @Test
    fun testNormalize() {
        assertEquals(0.0, Mood.normalize(1.0))
        assertEquals(1.0, Mood.normalize(5.0))
        assertEquals(0.5, Mood.normalize(3.0))
    }

    @Test
    fun testDualToEntryValue() {
        assertEquals(10011, Mood.toEntryValue(1, 1))
        assertEquals(10055, Mood.toEntryValue(5, 5))
        assertEquals(10032, Mood.toEntryValue(3, 2))
    }

    @Test
    fun testDualIsKnownValue() {
        for (valence in Mood.MIN..Mood.MAX) {
            for (arousal in Mood.MIN..Mood.MAX) {
                assertTrue(Mood.isKnownValue(Mood.toEntryValue(valence, arousal)))
            }
        }
        assertFalse(Mood.isKnownValue(Entry.UNKNOWN))
        assertFalse(Mood.isKnownValue(Entry.SKIP))
        // Legacy-range values must not be misread as dual-encoded.
        assertTrue(Mood.isKnownValue(3000))
    }

    @Test
    fun testValenceOfAndArousalOf_dualEncoding() {
        for (valence in Mood.MIN..Mood.MAX) {
            for (arousal in Mood.MIN..Mood.MAX) {
                val value = Mood.toEntryValue(valence, arousal)
                assertEquals(valence, Mood.valenceOf(value))
                assertEquals(arousal, Mood.arousalOf(value))
            }
        }
    }

    @Test
    fun testValenceOfAndArousalOf_legacyEncoding() {
        // Entries logged before arousal tracking existed still decode correctly: valence comes
        // through as before, and arousal is unknown (null).
        for (level in Mood.MIN..Mood.MAX) {
            val value = Mood.toEntryValue(level)
            assertEquals(level, Mood.valenceOf(value))
            assertEquals(null, Mood.arousalOf(value))
        }
    }
}
