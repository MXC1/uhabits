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

import androidx.annotation.StringRes
import org.isoron.uhabits.R
import org.isoron.uhabits.core.models.Mood

/**
 * FontAwesome glyph representing a mood level (1..5), used consistently across the list-screen
 * buttons, the entry dialog, and widgets.
 */
@StringRes
fun moodGlyphRes(level: Int): Int = when (level) {
    Mood.MIN -> R.string.fa_thumbs_o_down
    2 -> R.string.fa_frown_o
    3 -> R.string.fa_meh_o
    4 -> R.string.fa_smile_o
    else -> R.string.fa_thumbs_o_up
}
