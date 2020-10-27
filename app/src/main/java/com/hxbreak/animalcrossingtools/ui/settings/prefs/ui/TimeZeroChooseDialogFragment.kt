package com.hxbreak.animalcrossingtools.ui.settings.prefs.ui

import android.app.Dialog
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hxbreak.animalcrossingtools.data.prefs.PreferenceStorage
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.util.*
import javax.inject.Inject


@AndroidEntryPoint
class TimeZeroChooseDialogFragment : AppCompatDialogFragment() {

    private lateinit var listAdapter: ArrayAdapter<String>

    @Inject
    lateinit var preferenceStorage: PreferenceStorage
    lateinit var zoneIds: List<TimeZone>

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        listAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_single_choice
        )

        listAdapter.clear()
        zoneIds = TimeZone.getAvailableIDs().map {
            TimeZone.getTimeZone(it)
        }
        val currentTimeZone = preferenceStorage.selectedTimeZone
        val defaultPosition = zoneIds.indexOfFirst { currentTimeZone.id == it.id }
        listAdapter.addAll(zoneIds.map { it.getDisplayName(preferenceStorage.selectedLocale) })
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle("选择时区")
            .setSingleChoiceItems(listAdapter, defaultPosition) { dialog, position ->
                dialog.dismiss()
                listAdapter.getItem(position)?.let {
                    preferenceStorage.selectedTimeZone = try {
                        zoneIds[position]
                    }catch (e: Exception){
                        Timber.e(e)
                        TimeZone.getDefault()
                    }
                }
            }
            .create()
    }

    companion object {
        fun newInstance() = TimeZeroChooseDialogFragment()
    }

}
