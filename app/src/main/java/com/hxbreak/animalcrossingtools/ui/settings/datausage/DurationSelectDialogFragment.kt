package com.hxbreak.animalcrossingtools.ui.settings.datausage

import android.app.Dialog
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*

fun Duration.toReadableString(): String {
    val days = seconds / 3600 / 24
    val hours = (seconds % (3600 * 24)) / 3600
    val dayStr = if (days > 0) ("$days Day" + if (days > 1) "s" else "") else null
    val hourStr = if (hours > 0) ("$hours Hour" + if (hours > 1) "s" else "") else null
    val str = mutableListOf<String>()
    dayStr?.let { str.add(it) }
    hourStr?.let { str.add(it) }
    return str.joinToString(separator = ", ")
//    return "%2d${ChronoUnit.DAYS.name} %2d${ChronoUnit.HOURS.name}".format(
//        seconds / 3600 / 24, (seconds % (3600 * 24)) / 3600
//    )
}

class DurationSelectDialogFragment : AppCompatDialogFragment(){

    data class DurationWithTitle(val duration: Duration){
        override fun toString(): String {
            return duration.toReadableString()
        }
    }
    lateinit var listAdapter: ArrayAdapter<DurationWithTitle>

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        listAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1)
        return MaterialAlertDialogBuilder(requireContext())
            .setSingleChoiceItems(listAdapter, 0){ dialog, position ->
                setFragmentResult(REQUEST_DURATION_SELECTION, bundleOf(
                    RESULT_KEY_DURATION to listAdapter.getItem(position)!!.duration.seconds
                ))
                dismiss()
            }.create()

    }

    private val prebuiltDuration = arrayOf(
        Duration.ofHours(12),
        Duration.ofDays(1),
        Duration.ofDays(3),
        Duration.ofDays(7)
    ).map { DurationWithTitle(it) }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        listAdapter.addAll(prebuiltDuration)
    }

    companion object {
        fun newInstance() = DurationSelectDialogFragment()

        const val REQUEST_DURATION_SELECTION = "DURATION_SELECTION"
        const val RESULT_KEY_DURATION = "DURATION"
    }
}