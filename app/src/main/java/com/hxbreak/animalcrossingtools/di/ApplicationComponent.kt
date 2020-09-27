package com.hxbreak.animalcrossingtools.di

import android.content.Context
import com.hxbreak.animalcrossingtools.App
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        ApplicationModule::class,
        AndroidInjectionModule::class,
        FlutterModule::class,
        FishListModule::class,
        SongListModule::class,
        TrackerModule::class,
        SettingModule::class,
        MainActivityModule::class,
        NowPlayingModule::class,
        NavigationMenuModule::class,
        VillagerModule::class,
        ArtModule::class,
        BugsModule::class,
        SeaCreatureModule::class,
        FossilModule::class,
        HousewaresModule::class,
        DiGlideModule::class,
    ]
)
interface ApplicationComponent : AndroidInjector<App> {

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance applicationContext: Context): ApplicationComponent
    }
}