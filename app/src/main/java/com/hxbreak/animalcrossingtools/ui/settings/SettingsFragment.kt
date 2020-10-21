package com.hxbreak.animalcrossingtools.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.transition.MaterialSharedAxis
import com.hxbreak.animalcrossingtools.R
import com.hxbreak.animalcrossingtools.i18n.ResourceLanguageSettingDialogFragment
import com.hxbreak.animalcrossingtools.theme.ThemeSettingDialogFragment

import kotlinx.android.synthetic.main.fragment_settings.*
import javax.inject.Inject

class SettingsFragment : Fragment() {

    private val navigator by lazy {
        findNavController()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val forward = MaterialSharedAxis(MaterialSharedAxis.X, true)
        enterTransition = forward

        val backward = MaterialSharedAxis(MaterialSharedAxis.X, false)
        returnTransition = backward

        reenterTransition = backward

        exitTransition = forward
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        settings_choose_theme.setOnClickListener {
            ThemeSettingDialogFragment.newInstance()
                .show(childFragmentManager, null)
        }

        settings_choose_resource_language.setOnClickListener {
            ResourceLanguageSettingDialogFragment.newInstance()
                .show(childFragmentManager, null)
        }

        settings_about_software.setOnClickListener {
            navigator.navigate(SettingsFragmentDirections.actionGlobalFlutterFragment("about", cachedEngineId = "only"))
        }
    }
}