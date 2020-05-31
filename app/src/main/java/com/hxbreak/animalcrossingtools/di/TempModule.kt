package com.hxbreak.animalcrossingtools.di

import androidx.lifecycle.ViewModel
import com.hxbreak.animalcrossingtools.MainFragment
import com.hxbreak.animalcrossingtools.TempViewModel
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap


@Module
abstract class TempModule {
    @ContributesAndroidInjector(
        modules = [ViewModelBuilder::class]
    )
    internal abstract fun mainFragment(): MainFragment

    @Binds
    @IntoMap
    @ViewModelKey(TempViewModel::class)
    abstract fun bindViewModel(viewModel: TempViewModel): ViewModel
}

