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
import org.isoron.platform.time.getToday
import org.isoron.uhabits.HabitsApplication
import org.isoron.uhabits.activities.AndroidThemeSwitcher
import org.isoron.uhabits.core.models.HabitMatcher
import org.isoron.uhabits.core.models.Score
import org.isoron.uhabits.core.preferences.Preferences

class GlobalScoreActivity : AppCompatActivity() {
    private lateinit var preferences: Preferences
    private lateinit var view: GlobalScoreView
    private lateinit var app: HabitsApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = application as HabitsApplication
        preferences = app.component.preferences
        AndroidThemeSwitcher(this, preferences).apply()
        view = GlobalScoreView(this)
        setContentView(view)
    }

    override fun onResume() {
        super.onResume()
        view.setState(buildState())
    }

    private fun buildState(): GlobalScoreState {
        val matcher = if (preferences.areQuestionMarksEnabled) {
            HabitMatcher(
                isArchivedAllowed = preferences.showArchived,
                isEnteredAllowed = preferences.showCompleted
            )
        } else {
            HabitMatcher(
                isArchivedAllowed = preferences.showArchived,
                isCompletedAllowed = preferences.showCompleted
            )
        }

        val visibleHabits = app.component.habitList.getFiltered(matcher).toList()
        if (visibleHabits.isEmpty()) {
            return GlobalScoreState(visibleHabits = 0, scoreToday = 0.0, trend = emptyList())
        }

        val today = getToday()
        val trend = (0 until TREND_DAYS).map { offset ->
            val date = today.minus(offset)
            val value = visibleHabits.map { habit ->
                habit.scores[date].value
            }.average()
            Score(date = date, value = value)
        }

        val scoreToday = visibleHabits.map { habit ->
            habit.scores[today].value
        }.average()

        return GlobalScoreState(
            visibleHabits = visibleHabits.size,
            scoreToday = scoreToday,
            trend = trend
        )
    }

    companion object {
        private const val TREND_DAYS = 180
    }
}
