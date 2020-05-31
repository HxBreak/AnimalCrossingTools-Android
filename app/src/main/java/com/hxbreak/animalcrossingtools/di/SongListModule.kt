package com.hxbreak.animalcrossingtools.di

import androidx.lifecycle.ViewModel
import com.hxbreak.animalcrossingtools.ui.song.SongFragment
import com.hxbreak.animalcrossingtools.ui.song.SongViewModel
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap


@Module
abstract class SongListModule {
    @ContributesAndroidInjector(
        modules = [ViewModelBuilder::class]
    )
    internal abstract fun songFragment(): SongFragment

    @Binds
    @IntoMap
    @ViewModelKey(SongViewModel::class)
    abstract fun bindViewModel(viewModel: SongViewModel): ViewModel
}