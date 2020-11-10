package com.hxbreak.animalcrossingtools.ui.settings.datausage

import android.app.Dialog
import android.content.res.Resources
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.setFragmentResultListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hxbreak.animalcrossingtools.R
import com.hxbreak.animalcrossingtools.data.prefs.DataUsageStorage
import com.hxbreak.animalcrossingtools.data.prefs.StorableDuration
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.time.Duration
import javax.inject.Inject

internal fun StorableDuration.toLocalilzationString(res: Resources): String {
    return when(this){
        is StorableDuration.DOWNLOAD_WHEN_EMPTY -> res.getString(R.string.download_when_empty)
        is StorableDuration.DOWNLOAD_ALWAYS -> res.getString(R.string.download_always)
        is StorableDuration.InTime -> res.getString(R.string.download_in_time, duration.toReadableString())
    }
}

@AndroidEntryPoint
class FurnitureDataRefreshPolicyFragment : AppCompatDialogFragment(){

    inner class StorableDurationWithDescription(val duration: StorableDuration){
        override fun toString(): String {
            return duration.toLocalilzationString(resources)
        }
    }

    @Inject
    lateinit var preference: DataUsageStorage

    lateinit var listAdapter: ArrayAdapter<StorableDurationWithDescription>

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val index = when(preference.selectStorableDataRefreshDuration){
            is StorableDuration.DOWNLOAD_WHEN_EMPTY -> 0
            is StorableDuration.DOWNLOAD_ALWAYS -> 1
            is StorableDuration.InTime -> 2
        }
        listAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_single_choice)
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle("选择数据刷新")
            .setSingleChoiceItems(listAdapter, index){ dialog, position ->
                when(val value = listAdapter.getItem(position)!!.duration){
                    is StorableDuration.InTime -> {
                        DurationSelectDialogFragment.newInstance().show(childFragmentManager, null)
                        childFragmentManager.setFragmentResultListener(DurationSelectDialogFragment.REQUEST_DURATION_SELECTION, this)
                        { _, bundle ->
                            bundle.getLong(DurationSelectDialogFragment.RESULT_KEY_DURATION).let {
                                preference.selectStorableDataRefreshDuration = StorableDuration.InTime(Duration.ofSeconds(it))
                                dialog.dismiss()
                            }
                        }
                    }
                    else -> {
                        preference.selectStorableDataRefreshDuration = value
                        dismiss()
                    }
                }
            }.create()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        listAdapter.clear()
        val current = preference.selectStorableDataRefreshDuration
        listAdapter.addAll(
            StorableDurationWithDescription(StorableDuration.DOWNLOAD_WHEN_EMPTY),
            StorableDurationWithDescription(StorableDuration.DOWNLOAD_ALWAYS)
        )
        if (current is StorableDuration.InTime){
            listAdapter.add(StorableDurationWithDescription(current))
        }else{
            listAdapter.add(StorableDurationWithDescription(StorableDuration.InTime(Duration.ZERO)))
        }
    }

    companion object{
        fun newInstance() = FurnitureDataRefreshPolicyFragment()
    }
}