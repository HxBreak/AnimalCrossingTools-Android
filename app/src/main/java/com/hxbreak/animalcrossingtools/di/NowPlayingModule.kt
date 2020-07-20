package com.hxbreak.animalcrossingtools.di

import androidx.lifecycle.ViewModel
import com.hxbreak.animalcrossingtools.ui.musicplay.MusicPlayFragment
import com.hxbreak.animalcrossingtools.ui.musicplay.MusicPlayViewModel
import com.hxbreak.animalcrossingtools.ui.song.SongFragment
import com.hxbreak.animalcrossingtools.ui.song.SongViewModel
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap


@Module
abstract class NowPlayingModule {
    @ContributesAndroidInjector(
        modules = [ViewModelBuilder::class]
    )
    internal abstract fun nowPlayingFragment(): MusicPlayFragment

    @Binds
    @IntoMap
    @ViewModelKey(MusicPlayViewModel::class)
    abstract fun bindViewModel(viewModel: MusicPlayViewModel): ViewModel
}