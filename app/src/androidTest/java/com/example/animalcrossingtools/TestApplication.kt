package com.example.animalcrossingtools

import androidx.fragment.app.Fragment
import com.hxbreak.animalcrossingtools.App
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject

class TestApplication : App(), HasSupportFragmentInjector {

    @Inject
    lateinit var fragmentInject: DispatchingAndroidInjector<Fragment>

    override fun supportFragmentInjector(): AndroidInjector<Fragment> = fragmentInject

}