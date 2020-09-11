package com.hxbreak.animalcrossingtools.di

import androidx.lifecycle.ViewModel
import com.hxbreak.animalcrossingtools.ui.bugs.BugsFragment
import com.hxbreak.animalcrossingtools.ui.bugs.BugsViewModel
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Module
abstract class BugsModule{

    @ContributesAndroidInjector(
        modules = [ViewModelBuilder::class]
    )
    abstract fun bugsFragment(): BugsFragment

    @Binds
    @IntoMap
    @ViewModelKey(BugsViewModel::class)
    abstract fun bindViewModel(viewModel: BugsViewModel): ViewModel
}