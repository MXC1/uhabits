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

package org.isoron.uhabits.activities.common.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatDialogFragment
import org.isoron.uhabits.HabitsApplication
import org.isoron.uhabits.R
import org.isoron.uhabits.core.models.Entry
import org.isoron.uhabits.core.models.Mood
import org.isoron.uhabits.databinding.MoodPopupBinding
import org.isoron.uhabits.utils.InterfaceUtils.getFontAwesome
import org.isoron.uhabits.utils.MoodMeterColors
import org.isoron.uhabits.utils.dp
import org.isoron.uhabits.utils.moodWord
import org.isoron.uhabits.utils.sres

class MoodDialog : AppCompatDialogFragment() {
    var onToggle: (Int, String) -> Unit = { _, _ -> }
    var onDismiss: () -> Unit = {}

    private var dismissedViaSaveAction = false
    private var originalNotes: String = ""
    private var originalValue: Int = Entry.UNKNOWN
    private lateinit var view: MoodPopupBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val appComponent = (requireActivity().application as HabitsApplication).component
        val prefs = appComponent.preferences
        view = MoodPopupBinding.inflate(LayoutInflater.from(context))

        originalNotes = requireArguments().getString("notes")!!
        originalValue = requireArguments().getInt("value")
        view.notes.setText(originalNotes)

        fun onClick(v: Int) {
            dismissedViaSaveAction = true
            val notes = view.notes.text.toString().trim()
            onToggle(v, notes)
            requireDialog().dismiss()
        }

        buildGrid(::onClick)

        arrayOf(view.skipBtnMood, view.unknownBtnMood).forEach {
            it.setTextColor(view.root.sres.getColor(R.attr.contrast60))
            it.typeface = getFontAwesome(requireContext())
        }
        if (!prefs.isSkipEnabled) view.skipBtnMood.visibility = GONE
        if (!prefs.areQuestionMarksEnabled) view.unknownBtnMood.visibility = GONE
        view.skipBtnMood.setOnClickListener { onClick(Entry.SKIP) }
        view.unknownBtnMood.setOnClickListener { onClick(Entry.UNKNOWN) }
        view.notes.setOnEditorActionListener { _, _, _ ->
            onClick(originalValue)
            true
        }

        val dialog = Dialog(requireContext())
        dialog.setContentView(view.root)
        dialog.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
            val margin = view.root.dp(16f).toInt()
            setLayout(resources.displayMetrics.widthPixels - 2 * margin, WRAP_CONTENT)
        }
        return dialog
    }

    /**
     * Builds a 5x5 valence (horizontal) x arousal (vertical) grid. Each cell is colored via
     * [MoodMeterColors], and a single tap immediately picks that combination -- same one-tap
     * "just tell me and dismiss" interaction as the rest of the app's entry popups.
     */
    private fun buildGrid(onClick: (Int) -> Unit) {
        val selectedValence = if (Mood.isKnownValue(originalValue)) Mood.valenceOf(originalValue) else null
        val selectedArousal = Mood.arousalOf(originalValue)
        val margin = view.root.dp(1.5f).toInt()

        view.moodGrid.removeAllViews()
        for (arousal in Mood.MAX downTo Mood.MIN) {
            val row = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, 0).apply { weight = 1f }
            }
            for (valence in Mood.MIN..Mood.MAX) {
                val isSelected = valence == selectedValence && arousal == selectedArousal
                val word = moodWord(requireContext(), valence, arousal)
                val cell = TextView(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(0, MATCH_PARENT).apply {
                        weight = 1f
                        setMargins(margin, margin, margin, margin)
                    }
                    background = cellDrawable(valence, arousal, isSelected)
                    text = word
                    setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.moodCellTextSize))
                    setTextColor(0xDE000000.toInt())
                    typeface = Typeface.DEFAULT_BOLD
                    gravity = Gravity.CENTER
                    maxLines = 2
                    includeFontPadding = false
                    contentDescription = getString(R.string.mood_cell_description, word, valence, arousal)
                    setOnClickListener { onClick(Mood.toEntryValue(valence, arousal)) }
                }
                row.addView(cell)
            }
            view.moodGrid.addView(row)
        }
    }

    private fun cellDrawable(valence: Int, arousal: Int, selected: Boolean): GradientDrawable {
        val valenceFraction = (valence - Mood.MIN).toFloat() / (Mood.MAX - Mood.MIN)
        val arousalFraction = (arousal - Mood.MIN).toFloat() / (Mood.MAX - Mood.MIN)
        val color = MoodMeterColors.interpolate(valenceFraction, arousalFraction)
        return GradientDrawable().apply {
            setColor(color)
            if (selected) {
                setStroke(view.root.dp(2.5f).toInt(), Color.BLACK)
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        if (!dismissedViaSaveAction) {
            val currentNotes = view.notes.text.toString().trim()
            if (currentNotes != originalNotes) {
                onToggle(originalValue, currentNotes)
            }
        }
        onDismiss()
    }
}
