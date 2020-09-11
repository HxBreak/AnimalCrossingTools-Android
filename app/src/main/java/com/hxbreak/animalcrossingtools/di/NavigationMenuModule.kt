package com.hxbreak.animalcrossingtools.di

import androidx.lifecycle.ViewModel
import com.hxbreak.animalcrossingtools.MainFragment
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap


@Module
abstract class NavigationMenuModule {

    @ContributesAndroidInjector(
        modules = [ViewModelBuilder::class]
    )
    internal abstract fun mainFragment(): MainFragment

}

