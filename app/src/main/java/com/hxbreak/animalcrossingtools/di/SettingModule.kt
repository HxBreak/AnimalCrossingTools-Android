package com.hxbreak.animalcrossingtools.di

import androidx.lifecycle.ViewModel
import com.hxbreak.animalcrossingtools.theme.ThemeSettingDialogFragment
import com.hxbreak.animalcrossingtools.ui.settings.SettingsFragment
import com.hxbreak.animalcrossingtools.ui.settings.SettingsViewModel
import com.hxbreak.animalcrossingtools.ui.song.SongFragment
import com.hxbreak.animalcrossingtools.ui.song.SongViewModel
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap


@Module
abstract class SettingModule {
    @ContributesAndroidInjector(
        modules = [ViewModelBuilder::class]
    )
    internal abstract fun settingsFragment(): SettingsFragment

    @ContributesAndroidInjector(
        modules = [ViewModelBuilder::class]
    )
    internal abstract fun settingsDialogFragment(): ThemeSettingDialogFragment

    @Binds
    @IntoMap
    @ViewModelKey(SettingsViewModel::class)
    abstract fun bindViewModel(viewModel: SettingsViewModel): ViewModel
}