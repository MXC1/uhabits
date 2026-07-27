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

package org.isoron.uhabits.activities.habits.list.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.text.TextPaint
import android.view.View
import android.view.View.OnClickListener
import android.view.View.OnLongClickListener
import me.tatarka.inject.annotations.Inject
import org.isoron.uhabits.R
import org.isoron.uhabits.core.models.Entry
import org.isoron.uhabits.core.models.Mood
import org.isoron.uhabits.core.preferences.Preferences
import org.isoron.uhabits.inject.ActivityContext
import org.isoron.uhabits.utils.InterfaceUtils.getDimension
import org.isoron.uhabits.utils.MoodMeterColors
import org.isoron.uhabits.utils.dim
import org.isoron.uhabits.utils.drawNotesIndicator
import org.isoron.uhabits.utils.getFontAwesome
import org.isoron.uhabits.utils.sres

@Inject
class MoodButtonViewFactory(
    @ActivityContext val context: Context,
    val preferences: Preferences
) {
    fun create() = MoodButtonView(context, preferences)
}

class MoodButtonView(
    @ActivityContext context: Context,
    val preferences: Preferences
) : View(context),
    OnClickListener,
    OnLongClickListener {

    var color = 0
        set(value) {
            field = value
            invalidate()
        }

    /** The raw stored Entry.value: UNKNOWN, SKIP, or a mood level scaled x1000 (1000..5000). */
    var value = Entry.UNKNOWN
        set(value) {
            field = value
            invalidate()
        }

    var notes = ""
        set(value) {
            field = value
            invalidate()
        }

    var onEdit: () -> Unit = { }

    private var drawer: Drawer = Drawer(context)

    init {
        setOnClickListener(this)
        setOnLongClickListener(this)
    }

    override fun onClick(v: View) {
        onEdit()
    }

    override fun onLongClick(v: View): Boolean {
        onEdit()
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawer.draw(canvas)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = getDimension(context, R.dimen.checkmarkWidth).toInt()
        val height = getDimension(context, R.dimen.checkmarkHeight).toInt()
        setMeasuredDimension(width, height)
    }

    private inner class Drawer(context: Context) {
        private val rect: RectF = RectF()
        private val lowContrast: Int = sres.getColor(R.attr.contrast40)
        private val pNotesIndicator = Paint()

        private val paint = TextPaint().apply {
            typeface = getFontAwesome()
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

        private val swatchPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }

        fun draw(canvas: Canvas) {
            if (Mood.isKnownValue(value)) {
                drawSwatch(canvas)
            } else {
                val id = when {
                    value == Entry.SKIP -> R.string.fa_skipped
                    preferences.areQuestionMarksEnabled -> R.string.fa_question
                    else -> R.string.fa_times
                }
                drawGlyph(canvas, id, if (value == Entry.SKIP) color else lowContrast)
            }

            paint.textSize = dim(R.dimen.smallTextSize)
            drawNotesIndicator(
                pNotesIndicator = pNotesIndicator,
                canvas = canvas,
                color = color,
                size = paint.measureText("m"),
                notes = notes
            )
        }

        /**
         * Known mood entries are shown as a filled swatch of the same color the mood meter grid
         * used to pick them, rather than a glyph -- so the row reads as an at-a-glance color
         * timeline of how the day felt.
         */
        private fun drawSwatch(canvas: Canvas) {
            val valenceFraction = (Mood.valenceOf(value) - Mood.MIN).toFloat() / (Mood.MAX - Mood.MIN)
            val arousal = Mood.arousalOf(value)
            // Legacy single-axis entries have no arousal: draw at neutral (mid) energy.
            val arousalFraction = if (arousal != null) {
                (arousal - Mood.MIN).toFloat() / (Mood.MAX - Mood.MIN)
            } else {
                0.5f
            }
            swatchPaint.color = MoodMeterColors.interpolate(valenceFraction, arousalFraction)
            val margin = 0.3f * width
            canvas.drawCircle(width / 2f, height / 2f, width / 2f - margin, swatchPaint)
        }

        private fun drawGlyph(canvas: Canvas, id: Int, color: Int) {
            paint.color = color
            paint.textSize = dim(R.dimen.smallTextSize)

            val label = resources.getString(id)
            val em = paint.measureText("m")

            rect.set(0f, 0f, width.toFloat(), height.toFloat())
            rect.offset(0f, 0.4f * em)
            canvas.drawText(label, rect.centerX(), rect.centerY(), paint)
        }
    }
}
