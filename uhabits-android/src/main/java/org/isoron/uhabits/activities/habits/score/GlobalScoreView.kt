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
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.LinearLayout
import org.isoron.platform.gui.toInt
import org.isoron.uhabits.R
import org.isoron.uhabits.core.models.PaletteColor
import org.isoron.uhabits.core.models.Score
import org.isoron.uhabits.databinding.GlobalScoreBinding
import org.isoron.uhabits.utils.StyledResources
import org.isoron.uhabits.utils.applyBottomInset
import org.isoron.uhabits.utils.applyRootViewInsets
import org.isoron.uhabits.utils.currentTheme
import org.isoron.uhabits.utils.setupToolbar
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

private data class MoverRowViews(
    val row: View,
    val name: android.widget.TextView,
    val change: android.widget.TextView,
    val fill: View,
    val spacer: View,
    val alignRight: Boolean
)

data class GlobalScoreState(
    val visibleHabits: Int,
    val scoreToday: Double,
    val selectedPeriod: TrendPeriod,
    val trend: List<Score>,
    val topGainers: List<HabitMover>,
    val topDecliners: List<HabitMover>
)

@SuppressLint("ViewConstructor")
class GlobalScoreView(context: Context) : FrameLayout(context) {
    private val binding = GlobalScoreBinding.inflate(LayoutInflater.from(context))
    private var listener: ((TrendPeriod) -> Unit)? = null
    private var isUpdatingPeriod = false

    init {
        addView(binding.root)
        setupToolbar(
            toolbar = binding.toolbar,
            color = PaletteColor(11),
            title = resources.getString(R.string.global_trends),
            theme = currentTheme()
        )
        val items = resources.getStringArray(R.array.strengthIntervalNames).take(3)
        val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinner.adapter = adapter
        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (isUpdatingPeriod) return
                listener?.invoke(
                    when (position) {
                        0 -> TrendPeriod.DAILY
                        1 -> TrendPeriod.WEEKLY
                        else -> TrendPeriod.MONTHLY
                    }
                )
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
        binding.outerLinearLayout.applyBottomInset()
        applyRootViewInsets()
    }

    fun setListener(listener: (TrendPeriod) -> Unit) {
        this.listener = listener
    }

    fun setState(state: GlobalScoreState) {
        setSelectedPeriod(state.selectedPeriod)

        if (state.visibleHabits == 0) {
            binding.totalScoreValue.text = "--"
            binding.visibleHabits.text = ""
            binding.emptyState.visibility = View.VISIBLE
            binding.scoreView.visibility = View.GONE
            binding.moversCard.visibility = View.GONE
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
        binding.moversCard.visibility = View.VISIBLE
        binding.scoreView.setBucketSize(state.selectedPeriod.bucketSize)
        binding.scoreView.setColor(currentTheme().color(PaletteColor(11)).toInt())
        binding.scoreView.setScores(state.trend)
        val maxChange = max(
            state.topGainers.maxOfOrNull { abs(it.change) } ?: 0.0,
            state.topDecliners.maxOfOrNull { abs(it.change) } ?: 0.0
        ).coerceAtLeast(0.01)
        setMoverRows(
            rows = listOf(
                MoverRowViews(binding.gainerRow1, binding.gainerName1, binding.gainerChange1, binding.gainerFill1, binding.gainerSpacer1, false),
                MoverRowViews(binding.gainerRow2, binding.gainerName2, binding.gainerChange2, binding.gainerFill2, binding.gainerSpacer2, false),
                MoverRowViews(binding.gainerRow3, binding.gainerName3, binding.gainerChange3, binding.gainerFill3, binding.gainerSpacer3, false)
            ),
            emptyView = binding.noGainers,
            movers = state.topGainers,
            maxChange = maxChange,
            fillColor = currentTheme().color(PaletteColor(11)).toInt()
        )
        setMoverRows(
            rows = listOf(
                MoverRowViews(binding.declinerRow1, binding.declinerName1, binding.declinerChange1, binding.declinerFill1, binding.declinerSpacer1, true),
                MoverRowViews(binding.declinerRow2, binding.declinerName2, binding.declinerChange2, binding.declinerFill2, binding.declinerSpacer2, true),
                MoverRowViews(binding.declinerRow3, binding.declinerName3, binding.declinerChange3, binding.declinerFill3, binding.declinerSpacer3, true)
            ),
            emptyView = binding.noDecliners,
            movers = state.topDecliners,
            maxChange = maxChange,
            fillColor = StyledResources(context).getColor(R.attr.contrast60)
        )
    }

    private fun setSelectedPeriod(period: TrendPeriod) {
        isUpdatingPeriod = true
        binding.spinner.setSelection(
            when (period) {
                TrendPeriod.DAILY -> 0
                TrendPeriod.WEEKLY -> 1
                TrendPeriod.MONTHLY -> 2
            }
        )
        isUpdatingPeriod = false
    }

    private fun setMoverRows(
        rows: List<MoverRowViews>,
        emptyView: android.widget.TextView,
        movers: List<HabitMover>,
        maxChange: Double,
        fillColor: Int
    ) {
        if (movers.isEmpty()) {
            rows.forEach { it.row.visibility = View.GONE }
            emptyView.visibility = View.VISIBLE
            return
        }

        emptyView.visibility = View.GONE
        rows.forEachIndexed { index, row ->
            if (index >= movers.size) {
                row.row.visibility = View.GONE
            } else {
                val mover = movers[index]
                row.row.visibility = View.VISIBLE
                row.name.text = mover.habitName
                row.change.text = resources.getString(
                    R.string.mover_change_value,
                    mover.change * 100
                )
                row.fill.setBackgroundColor(fillColor)
                setBarFraction(
                    fill = row.fill,
                    spacer = row.spacer,
                    fraction = (abs(mover.change) / maxChange).toFloat().coerceIn(0f, 1f),
                    alignRight = row.alignRight
                )
            }
        }
    }

    private fun setBarFraction(fill: View, spacer: View, fraction: Float, alignRight: Boolean) {
        if (alignRight) {
            spacer.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f - fraction)
            fill.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, fraction)
        } else {
            fill.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, fraction)
            spacer.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f - fraction)
        }
    }
}
