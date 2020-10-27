package com.hxbreak.animalcrossingtools.ui.settings.prefs.ui

import android.app.Dialog
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hxbreak.animalcrossingtools.data.prefs.Hemisphere
import com.hxbreak.animalcrossingtools.data.prefs.PreferenceStorage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class HemisphereChooseDialogFragment : AppCompatDialogFragment() {

    private lateinit var listAdapter: ArrayAdapter<Hemisphere>

    @Inject
    lateinit var preferenceStorage: PreferenceStorage

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        listAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_single_choice
        )

        listAdapter.clear()
        listAdapter.addAll(Hemisphere.values().toList())
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle("选择半球")
            .setSingleChoiceItems(listAdapter,
                listAdapter.getPosition(preferenceStorage.selectedHemisphere)
            ) { dialog, position ->
                dialog.dismiss()
                listAdapter.getItem(position)?.let {
                    preferenceStorage.selectedHemisphere = it
                }
            }
            .create()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

    }

    companion object {
        fun newInstance() = HemisphereChooseDialogFragment()
    }

}
