package com.hxbreak.animalcrossingtools.di

import com.hxbreak.animalcrossingtools.GlideModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class DiGlideModule{

    @ContributesAndroidInjector
    abstract fun injectGlideApp(): GlideModule

}