package com.hxbreak.animalcrossingtools.di

import androidx.lifecycle.ViewModel
import com.hxbreak.animalcrossingtools.ui.art.ArtFragment
import com.hxbreak.animalcrossingtools.ui.art.ArtViewModel
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Module
abstract class ArtModule {


    @ContributesAndroidInjector(
        modules = [ViewModelBuilder::class]
    )
    abstract fun artFragment(): ArtFragment

    @Binds
    @IntoMap
    @ViewModelKey(ArtViewModel::class)
    abstract fun bindViewModel(viewModel: ArtViewModel): ViewModel
}