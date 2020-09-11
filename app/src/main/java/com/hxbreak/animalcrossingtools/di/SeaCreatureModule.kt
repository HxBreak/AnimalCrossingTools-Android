package com.hxbreak.animalcrossingtools.di

import androidx.lifecycle.ViewModel
import com.hxbreak.animalcrossingtools.ui.seacreature.SeaCreatureFragment
import com.hxbreak.animalcrossingtools.ui.seacreature.SeaCreatureViewModel
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Module
abstract class SeaCreatureModule{

    @ContributesAndroidInjector(
        modules = [ViewModelBuilder::class]
    )
    abstract fun seaCreatureFragment(): SeaCreatureFragment

    @Binds
    @IntoMap
    @ViewModelKey(SeaCreatureViewModel::class)
    abstract fun bindViewModel(viewModel: SeaCreatureViewModel): ViewModel
}