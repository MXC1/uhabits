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

package org.isoron.uhabits.core.ui.views

import org.isoron.platform.gui.Color

/**
 * Colors the valence x arousal "mood meter" grid: one color per corner (unpleasant/pleasant x
 * low/high arousal), bilinearly interpolated across the grid so nearby cells blend smoothly.
 *
 * Mirrors org.isoron.uhabits.utils.MoodMeterColors on the Android side (which operates on
 * android.graphics.Color instead of the KMP [Color] used by [HistoryChart]).
 */
object MoodMeterColors {
    private val UNPLEASANT_HIGH_AROUSAL = Color(0xE57373) // red
    private val PLEASANT_HIGH_AROUSAL = Color(0xFFF176) // yellow
    private val UNPLEASANT_LOW_AROUSAL = Color(0x64B5F6) // blue
    private val PLEASANT_LOW_AROUSAL = Color(0x81C784) // green

    /**
     * @param valenceFraction 0.0 (unpleasant) .. 1.0 (pleasant)
     * @param arousalFraction 0.0 (low arousal) .. 1.0 (high arousal)
     */
    fun interpolate(valenceFraction: Double, arousalFraction: Double): Color {
        val v = valenceFraction.coerceIn(0.0, 1.0)
        val a = arousalFraction.coerceIn(0.0, 1.0)
        val top = UNPLEASANT_HIGH_AROUSAL.blendWith(PLEASANT_HIGH_AROUSAL, v)
        val bottom = UNPLEASANT_LOW_AROUSAL.blendWith(PLEASANT_LOW_AROUSAL, v)
        return bottom.blendWith(top, a)
    }
}
