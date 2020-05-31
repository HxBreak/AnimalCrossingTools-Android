package com.hxbreak.animalcrossingtools.di

import androidx.lifecycle.ViewModel
import com.hxbreak.animalcrossingtools.ui.fish.FishFragment
import com.hxbreak.animalcrossingtools.ui.fish.FishViewModel
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Module
abstract class FishListModule {
    @ContributesAndroidInjector(
        modules = [ViewModelBuilder::class]
    )
    internal abstract fun fishFragment(): FishFragment

    @Binds
    @IntoMap
    @ViewModelKey(FishViewModel::class)
    abstract fun bindViewModel(viewModel: FishViewModel): ViewModel
}