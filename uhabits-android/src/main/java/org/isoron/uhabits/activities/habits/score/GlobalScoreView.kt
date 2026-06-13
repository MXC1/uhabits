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
package org.isoron.uhabits.activities.habits.score

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import org.isoron.platform.gui.toInt
import org.isoron.uhabits.R
import org.isoron.uhabits.core.models.PaletteColor
import org.isoron.uhabits.core.models.Score
import org.isoron.uhabits.databinding.GlobalScoreBinding
import org.isoron.uhabits.utils.applyBottomInset
import org.isoron.uhabits.utils.applyRootViewInsets
import org.isoron.uhabits.utils.currentTheme
import org.isoron.uhabits.utils.setupToolbar
import kotlin.math.roundToInt

data class GlobalScoreState(
    val visibleHabits: Int,
    val scoreToday: Double,
    val trend: List<Score>
)

@SuppressLint("ViewConstructor")
class GlobalScoreView(context: Context) : FrameLayout(context) {
    private val binding = GlobalScoreBinding.inflate(LayoutInflater.from(context))

    init {
        addView(binding.root)
        setupToolbar(
            toolbar = binding.toolbar,
            color = PaletteColor(11),
            title = resources.getString(R.string.global_score),
            theme = currentTheme()
        )
        binding.outerLinearLayout.applyBottomInset()
        applyRootViewInsets()
    }

    fun setState(state: GlobalScoreState) {
        if (state.visibleHabits == 0) {
            binding.totalScoreValue.text = "--"
            binding.visibleHabits.text = ""
            binding.emptyState.visibility = View.VISIBLE
            binding.scoreView.visibility = View.GONE
            return
        }

        val scorePercentage = (state.scoreToday * 100).roundToInt()
        binding.totalScoreValue.text =
            resources.getString(R.string.global_score_percent_value, scorePercentage)
        binding.visibleHabits.text = resources.getQuantityString(
            R.plurals.visible_habits_count,
            state.visibleHabits,
            state.visibleHabits
        )
        binding.emptyState.visibility = View.GONE
        binding.scoreView.visibility = View.VISIBLE
        binding.scoreView.setBucketSize(1)
        binding.scoreView.setColor(currentTheme().color(PaletteColor(11)).toInt())
        binding.scoreView.setScores(state.trend)
    }
}
