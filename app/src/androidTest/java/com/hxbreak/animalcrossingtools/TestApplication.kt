package com.hxbreak.animalcrossingtools

import androidx.fragment.app.Fragment
import com.hxbreak.animalcrossingtools.data.source.AnimalCrossingDatabase
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.DaggerApplication
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject

class TestApplication : DaggerApplication(), HasSupportFragmentInjector {

    @Inject
    lateinit var database: AnimalCrossingDatabase

    @Inject
    lateinit var fragmentInject: DispatchingAndroidInjector<Fragment>

    override fun supportFragmentInjector(): AndroidInjector<Fragment> = fragmentInject

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerTestApplicationComponent.factory().create(this)
    }

}