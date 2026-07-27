package org.isoron.uhabits.core.models

/**
 * Helpers for converting between human-facing mood coordinates and the raw representation
 * stored in [Entry.value].
 *
 * Two encodings are supported:
 * - Legacy, single-axis (valence only): `level * 1000`, i.e. 1000..5000, following the same
 *   x1000 fixed-point convention used by numerical habits to avoid colliding with the shared
 *   boolean sentinels (UNKNOWN, NO, YES_AUTO, YES_MANUAL, SKIP).
 * - Dual-axis (valence + arousal, aka the circumplex/"mood meter" model): offset into a range
 *   that can't collide with either the sentinels or the legacy range, so both kinds of entries
 *   can coexist in the same habit's history without a data migration.
 *
 * [valenceOf] and [arousalOf] are the general-purpose accessors -- they transparently handle
 * both encodings, so callers don't need to know which one produced a given entry.
 */
object Mood {
    const val MIN = 1
    const val MAX = 5

    private const val DUAL_OFFSET = 10000

    fun toEntryValue(level: Int) = level * 1000

    fun toEntryValue(valence: Int, arousal: Int) = DUAL_OFFSET + valence * 10 + arousal

    /** Legacy-only decode. Prefer [valenceOf], which also handles dual-axis entries. */
    fun fromEntryValue(value: Int) = value / 1000

    private fun isLegacyValue(value: Int) = value in toEntryValue(MIN)..toEntryValue(MAX)

    private fun isDualValue(value: Int): Boolean {
        if (value < DUAL_OFFSET) return false
        val rest = value - DUAL_OFFSET
        return (rest / 10) in MIN..MAX && (rest % 10) in MIN..MAX
    }

    fun isKnownValue(value: Int): Boolean = isDualValue(value) || isLegacyValue(value)

    /** The valence (pleasantness) level, 1..5, regardless of which encoding produced [value]. */
    fun valenceOf(value: Int): Int = if (isDualValue(value)) (value - DUAL_OFFSET) / 10 else fromEntryValue(value)

    /** The arousal (energy) level, 1..5, or null if [value] predates arousal tracking. */
    fun arousalOf(value: Int): Int? = if (isDualValue(value)) (value - DUAL_OFFSET) % 10 else null

    /**
     * Maps a mood level (possibly fractional, e.g. a rolling average) to a 0.0..1.0 score.
     */
    fun normalize(level: Double): Double = (level - MIN) / (MAX - MIN)
}
