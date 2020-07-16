/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hxbreak.animalcrossingtools.theme

import android.app.Dialog
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hxbreak.animalcrossingtools.R
import com.hxbreak.animalcrossingtools.di.DiViewModelFactory
import com.hxbreak.animalcrossingtools.ui.settings.SettingsViewModel
import dagger.android.support.DaggerAppCompatDialogFragment
import timber.log.Timber
import javax.inject.Inject

class ThemeSettingDialogFragment : DaggerAppCompatDialogFragment() {

    @Inject
    lateinit var viewModelFactory: DiViewModelFactory

    private lateinit var listAdapter: ArrayAdapter<ThemeHolder>

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        Timber.e("create")
        listAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_single_choice
        )

        return MaterialAlertDialogBuilder(context)
            .setTitle("选择主题")
            .setSingleChoiceItems(listAdapter, 0) { dialog, position ->
                dialog.dismiss()
                listAdapter.getItem(position)?.theme?.let {
                    viewModel.setTheme(it)
                }
            }
            .create()
    }

    private val viewModel by viewModels<SettingsViewModel> { viewModelFactory }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.availableThemes.observe(this, Observer { themes ->
            listAdapter.clear()
            listAdapter.addAll(themes.map { theme -> ThemeHolder(theme, getTitleForTheme(theme)) })

            updateSelectedItem(viewModel.theme.value)
        })

        viewModel.theme.observe(this, Observer(::updateSelectedItem))
    }

    private fun updateSelectedItem(selected: Theme?) {
        val selectedPosition = (0 until listAdapter.count).indexOfFirst { index ->
            listAdapter.getItem(index)?.theme == selected
        }
        (dialog as AlertDialog).listView.setItemChecked(selectedPosition, true)
    }

    private fun getTitleForTheme(theme: Theme) = when (theme) {
        Theme.LIGHT -> getString(R.string.settings_theme_light)
        Theme.DARK -> getString(R.string.settings_theme_dark)
        Theme.SYSTEM -> getString(R.string.settings_theme_system)
        Theme.BATTERY_SAVER -> getString(R.string.settings_theme_battery)
    }

    companion object {
        fun newInstance() = ThemeSettingDialogFragment()
    }

    private data class ThemeHolder(val theme: Theme, val title: String) {
        override fun toString(): String = title
    }
}
