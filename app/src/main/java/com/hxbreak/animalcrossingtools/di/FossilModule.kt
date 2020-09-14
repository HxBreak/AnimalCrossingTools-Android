package com.hxbreak.animalcrossingtools.di

import androidx.lifecycle.ViewModel
import com.hxbreak.animalcrossingtools.ui.fossil.FossilFragment
import com.hxbreak.animalcrossingtools.ui.fossil.FossilViewModel
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Module
abstract class FossilModule{

    @ContributesAndroidInjector(
        modules = [ViewModelBuilder::class]
    )
    abstract fun fossilFragment(): FossilFragment

    @Binds
    @IntoMap
    @ViewModelKey(FossilViewModel::class)
    abstract fun bindViewModel(viewModel: FossilViewModel): ViewModel
}