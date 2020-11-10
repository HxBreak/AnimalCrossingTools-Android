package com.hxbreak.animalcrossingtools.ui.settings.prefs.ui

import android.app.Dialog
import android.content.res.Resources
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hxbreak.animalcrossingtools.R
import com.hxbreak.animalcrossingtools.data.prefs.Hemisphere
import com.hxbreak.animalcrossingtools.data.prefs.PreferenceStorage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

fun Hemisphere.toLocalizationString(res: Resources): String{
    return when(this){
        Hemisphere.Northern -> res.getString(R.string.northern)
        Hemisphere.Southern -> res.getString(R.string.southern)
    }
}

@AndroidEntryPoint
class HemisphereChooseDialogFragment : AppCompatDialogFragment() {

    inner class TranslatedHemisphere(
        val hemisphere: Hemisphere
    ){
        override fun toString() = hemisphere.toLocalizationString(resources)
    }

    private lateinit var listAdapter: ArrayAdapter<TranslatedHemisphere>

    @Inject
    lateinit var preferenceStorage: PreferenceStorage

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        listAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_single_choice
        )

        listAdapter.clear()
        listAdapter.addAll(Hemisphere.values().map { TranslatedHemisphere(it) }.toList())
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.choose_hemisphere))
            .setSingleChoiceItems(listAdapter,
                Hemisphere.values().indexOf(preferenceStorage.selectedHemisphere)
            ) { dialog, position ->
                dialog.dismiss()
                listAdapter.getItem(position)?.let {
                    preferenceStorage.selectedHemisphere = it.hemisphere
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
