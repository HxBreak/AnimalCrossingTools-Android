package com.hxbreak.animalcrossingtools.di

import androidx.lifecycle.ViewModel
import com.hxbreak.animalcrossingtools.ui.TrackerFragment
import com.hxbreak.animalcrossingtools.ui.TrackerViewModel
import com.hxbreak.animalcrossingtools.ui.chat.ChatFragment
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap


@Module
abstract class TrackerModule {
    @ContributesAndroidInjector(
        modules = [ViewModelBuilder::class]
    )
    internal abstract fun trackerFragment(): TrackerFragment

    @ContributesAndroidInjector(
        modules = [ViewModelBuilder::class]
    )
    internal abstract fun chatFragment(): ChatFragment

    @Binds
    @IntoMap
    @ViewModelKey(TrackerViewModel::class)
    abstract fun bindViewModel(viewModel: TrackerViewModel): ViewModel
}

