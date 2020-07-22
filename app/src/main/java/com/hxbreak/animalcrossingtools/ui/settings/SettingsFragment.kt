package com.hxbreak.animalcrossingtools.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.hxbreak.animalcrossingtools.R
import com.hxbreak.animalcrossingtools.di.DiViewModelFactory
import com.hxbreak.animalcrossingtools.theme.Theme
import com.hxbreak.animalcrossingtools.theme.ThemeSettingDialogFragment
import dagger.android.support.DaggerFragment

import kotlinx.android.synthetic.main.fragment_settings.*
import javax.inject.Inject

class SettingsFragment : DaggerFragment() {


    @Inject
    lateinit var viewModelFactory: DiViewModelFactory

    private val viewModel by viewModels<SettingsViewModel> { viewModelFactory }

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
        settings_time_zone.setOnClickListener {
            viewModel.preferenceStorage.selectedTheme = Theme.DARK.storageKey
        }
    }
}