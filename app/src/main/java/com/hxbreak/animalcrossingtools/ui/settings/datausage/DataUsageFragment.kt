package com.hxbreak.animalcrossingtools.ui.settings.datausage

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.navigation.fragment.findNavController
import com.google.android.material.transition.MaterialContainerTransform
import com.hxbreak.animalcrossingtools.R
import com.hxbreak.animalcrossingtools.data.prefs.DataUsageStorage
import com.hxbreak.animalcrossingtools.ui.BackAbleAppbarFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_datausage.*
import javax.inject.Inject

@AndroidEntryPoint
class DataUsageFragment : BackAbleAppbarFragment() {

    private val navigator by lazy {
        findNavController()
    }

    @Inject
    lateinit var preference: DataUsageStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = MaterialContainerTransform().apply {
            duration = 300
            scrimColor = Color.TRANSPARENT
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_datausage, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewCompat.setTransitionName(requireView(), "root")
        settings_choose_data_download_policy.setOnClickListener {
            FurnitureDataRefreshPolicyFragment.newInstance().show(childFragmentManager, null)
        }
        preference.selectStorableDataRefreshDurationLiveData.observe(viewLifecycleOwner){
            data_download_policy_value.text = it.toLocalizationString(resources)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}