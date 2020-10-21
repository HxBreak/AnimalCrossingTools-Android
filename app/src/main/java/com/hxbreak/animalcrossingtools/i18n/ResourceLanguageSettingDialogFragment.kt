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

package com.hxbreak.animalcrossingtools.i18n

import android.app.Dialog
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hxbreak.animalcrossingtools.ui.settings.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class ResourceLanguageSettingDialogFragment : AppCompatDialogFragment() {

    private lateinit var listAdapter: ArrayAdapter<LanguageHolder>

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        listAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_single_choice
        )

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle("选择资源语言")
            .setSingleChoiceItems(listAdapter, 0) { dialog, position ->
                dialog.dismiss()
                listAdapter.getItem(position)?.local?.let {
                    viewModel.setLanguage(it)
                }
            }
            .create()
    }

    private val viewModel by viewModels<SettingsViewModel>()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

//        listAdapter.addAll(viewModel.supportedLanguageRange.map { LanguageHolder(it, getTitleForLocale(it)) })
//        updateSelectedItem(viewModel.preferenceStorage.selectedLocale)
        viewModel.availableResourceLanguage.observe(this, Observer { languages ->
            listAdapter.clear()
            listAdapter.addAll(languages.map { language ->
                LanguageHolder(
                    language,
                    getTitleForLocale(language)
                )
            })
            updateSelectedItem(viewModel.preferenceStorage.selectedLocale)
        })
    }

    private fun updateSelectedItem(selected: Locale?) {
        val selectedPosition = (0 until listAdapter.count).indexOfFirst { index ->
            listAdapter.getItem(index)?.local == selected
        }
        (dialog as AlertDialog).listView.setItemChecked(selectedPosition, true)
    }

    private fun getTitleForLocale(locale: Locale) = locale.getDisplayName(locale)

    companion object {
        fun newInstance() = ResourceLanguageSettingDialogFragment()
    }

    private data class LanguageHolder(val local: Locale, val title: String) {
        override fun toString(): String = title
    }
}
