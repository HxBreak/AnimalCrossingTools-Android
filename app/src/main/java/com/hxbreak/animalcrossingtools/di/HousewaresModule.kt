package com.hxbreak.animalcrossingtools.di

import androidx.lifecycle.ViewModel
import com.hxbreak.animalcrossingtools.ui.houseware.HousewaresFragment
import com.hxbreak.animalcrossingtools.ui.houseware.HousewaresViewModel
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Module
abstract class HousewaresModule{

    @ContributesAndroidInjector(
        modules = [ViewModelBuilder::class]
    )
    abstract fun housewaresFragment(): HousewaresFragment

    @Binds
    @IntoMap
    @ViewModelKey(HousewaresViewModel::class)
    abstract fun bindViewModel(viewModel: HousewaresViewModel): ViewModel
}