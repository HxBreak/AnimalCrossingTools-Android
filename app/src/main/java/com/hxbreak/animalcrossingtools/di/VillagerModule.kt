package com.hxbreak.animalcrossingtools.di

import androidx.lifecycle.ViewModel
import com.hxbreak.animalcrossingtools.ui.villager.VillagerFragment
import com.hxbreak.animalcrossingtools.ui.villager.VillagerViewModel
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Module
abstract class VillagerModule{

    @ContributesAndroidInjector(
        modules = [ViewModelBuilder::class]
    )
    abstract fun villagerFragment(): VillagerFragment

    @Binds
    @IntoMap
    @ViewModelKey(VillagerViewModel::class)
    abstract fun bindViewModel(viewModel: VillagerViewModel): ViewModel
}