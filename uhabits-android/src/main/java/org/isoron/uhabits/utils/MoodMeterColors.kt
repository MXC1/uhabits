/*
 * Copyright (C) 2016-2025 Álinson Santos Xavier <git@axavier.org>
 *
 * This file is part of Loop Habit Tracker.
 *
 * Loop Habit Tracker is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Loop Habit Tracker is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.isoron.uhabits.utils

import android.graphics.Color

/**
 * Colors the valence x arousal "mood meter" grid: one color per corner (unpleasant/pleasant x
 * low/high arousal), bilinearly interpolated across the grid so nearby cells blend smoothly.
 */
object MoodMeterColors {
    private const val UNPLEASANT_HIGH_AROUSAL = 0xFFE57373.toInt() // red
    private const val PLEASANT_HIGH_AROUSAL = 0xFFFFF176.toInt() // yellow
    private const val UNPLEASANT_LOW_AROUSAL = 0xFF64B5F6.toInt() // blue
    private const val PLEASANT_LOW_AROUSAL = 0xFF81C784.toInt() // green

    /**
     * @param valenceFraction 0f (unpleasant) .. 1f (pleasant)
     * @param arousalFraction 0f (low arousal) .. 1f (high arousal)
     */
    fun interpolate(valenceFraction: Float, arousalFraction: Float): Int {
        val top = blend(UNPLEASANT_HIGH_AROUSAL, PLEASANT_HIGH_AROUSAL, valenceFraction)
        val bottom = blend(UNPLEASANT_LOW_AROUSAL, PLEASANT_LOW_AROUSAL, valenceFraction)
        return blend(bottom, top, arousalFraction)
    }

    private fun blend(colorA: Int, colorB: Int, fraction: Float): Int {
        val f = fraction.coerceIn(0f, 1f)
        fun lerp(a: Int, b: Int) = (a + f * (b - a)).toInt()
        return Color.argb(
            lerp(Color.alpha(colorA), Color.alpha(colorB)),
            lerp(Color.red(colorA), Color.red(colorB)),
            lerp(Color.green(colorA), Color.green(colorB)),
            lerp(Color.blue(colorA), Color.blue(colorB))
        )
    }
}
