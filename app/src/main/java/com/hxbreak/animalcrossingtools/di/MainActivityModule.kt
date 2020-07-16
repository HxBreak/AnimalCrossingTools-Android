package com.hxbreak.animalcrossingtools.di

import androidx.lifecycle.ViewModel
import com.hxbreak.animalcrossingtools.MainActivity
import com.hxbreak.animalcrossingtools.MainActivityViewModel
import com.hxbreak.animalcrossingtools.ui.settings.SettingsFragment
import com.hxbreak.animalcrossingtools.ui.settings.SettingsViewModel
import com.hxbreak.animalcrossingtools.ui.song.SongFragment
import com.hxbreak.animalcrossingtools.ui.song.SongViewModel
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap


@Module
abstract class MainActivityModule {
    @ContributesAndroidInjector(
        modules = [ViewModelBuilder::class]
    )
    internal abstract fun mainActivity(): MainActivity

    @Binds
    @IntoMap
    @ViewModelKey(MainActivityViewModel::class)
    abstract fun bindViewModel(viewModel: MainActivityViewModel): ViewModel
}