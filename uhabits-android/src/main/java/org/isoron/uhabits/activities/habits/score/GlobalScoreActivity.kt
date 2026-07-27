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

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.isoron.platform.time.LocalDate
import org.isoron.platform.time.getToday
import org.isoron.uhabits.HabitsApplication
import org.isoron.uhabits.activities.AndroidThemeSwitcher
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.HabitMatcher
import org.isoron.uhabits.core.models.HabitType
import org.isoron.uhabits.core.models.Score
import org.isoron.uhabits.core.preferences.Preferences

enum class TrendPeriod(
    val bucketSize: Int,
    val lookbackDays: Int
) {
    DAILY(bucketSize = 1, lookbackDays = 180),
    WEEKLY(bucketSize = 7, lookbackDays = 365),
    MONTHLY(bucketSize = 31, lookbackDays = 730)
}

data class HabitMover(
    val habitName: String,
    val change: Double
)

class GlobalScoreActivity : AppCompatActivity() {
    private lateinit var preferences: Preferences
    private lateinit var view: GlobalScoreView
    private lateinit var app: HabitsApplication
    private var selectedPeriod = TrendPeriod.WEEKLY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = application as HabitsApplication
        preferences = app.component.preferences
        selectedPeriod = savedInstanceState?.getString(KEY_PERIOD)?.let {
            TrendPeriod.valueOf(it)
        } ?: TrendPeriod.WEEKLY
        AndroidThemeSwitcher(this, preferences).apply()
        view = GlobalScoreView(this)
        view.setListener { period ->
            selectedPeriod = period
            refresh()
        }
        setContentView(view)
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_PERIOD, selectedPeriod.name)
    }

    private fun refresh() {
        view.setState(buildState(selectedPeriod))
    }

    private fun buildState(period: TrendPeriod): GlobalScoreState {
        val matcher = if (preferences.areQuestionMarksEnabled) {
            HabitMatcher(
                isArchivedAllowed = preferences.showArchived,
                isEnteredAllowed = true
            )
        } else {
            HabitMatcher(
                isArchivedAllowed = preferences.showArchived,
                isCompletedAllowed = true
            )
        }

        // Mood habits have no score, so they'd distort the average (and their own moving parts
        // aren't otherwise comparable to progress on regular habits) -- leave them out of the
        // global trend entirely.
        val visibleHabits = app.component.habitList.getFiltered(matcher).toList()
            .filter { it.type != HabitType.MOOD }
        if (visibleHabits.isEmpty()) {
            return GlobalScoreState(
                visibleHabits = 0,
                scoreToday = 0.0,
                selectedPeriod = period,
                trend = emptyList(),
                topGainers = emptyList(),
                topDecliners = emptyList()
            )
        }

        val today = getToday()
        val dailyTrend = (0 until period.lookbackDays).map { offset ->
            val date = today.minus(offset)
            val value = visibleHabits.map { habit ->
                habit.scores[date].value
            }.average()
            Score(date = date, value = value)
        }
        val trend = bucketTrend(dailyTrend, period)

        val scoreToday = visibleHabits.map { habit ->
            habit.scores[today].value
        }.average()

        return GlobalScoreState(
            visibleHabits = visibleHabits.size,
            scoreToday = scoreToday,
            selectedPeriod = period,
            trend = trend,
            topGainers = buildTopGainers(visibleHabits, today),
            topDecliners = buildTopDecliners(visibleHabits, today)
        )
    }

    private fun bucketTrend(
        dailyTrend: List<Score>,
        period: TrendPeriod
    ): List<Score> {
        if (period == TrendPeriod.DAILY) return dailyTrend

        val firstWeekday = preferences.firstWeekday
        return dailyTrend.groupBy { score ->
            when (period) {
                TrendPeriod.DAILY -> score.date
                TrendPeriod.WEEKLY -> score.date.startOfWeek(firstWeekday)
                TrendPeriod.MONTHLY -> score.date.startOfMonth()
            }
        }.map { (date, scores) ->
            Score(date, scores.map { it.value }.average())
        }.sortedBy { it.date }.reversed()
    }

    private fun buildTopGainers(
        habits: List<Habit>,
        today: LocalDate
    ): List<HabitMover> {
        return buildMovers(habits, today)
            .filter { it.change > 0.0 }
            .sortedByDescending { it.change }
            .take(MOVERS_LIMIT)
    }

    private fun buildTopDecliners(
        habits: List<Habit>,
        today: LocalDate
    ): List<HabitMover> {
        return buildMovers(habits, today)
            .filter { it.change < 0.0 }
            .sortedBy { it.change }
            .take(MOVERS_LIMIT)
    }

    private fun buildMovers(
        habits: List<Habit>,
        today: LocalDate
    ): List<HabitMover> {
        val previous = today.minus(MOVERS_WINDOW_DAYS)
        return habits.map { habit ->
            HabitMover(
                habitName = habit.name,
                change = habit.scores[today].value - habit.scores[previous].value
            )
        }
    }

    companion object {
        private const val MOVERS_WINDOW_DAYS = 30
        private const val MOVERS_LIMIT = 3
        private const val KEY_PERIOD = "selectedPeriod"
    }
}
