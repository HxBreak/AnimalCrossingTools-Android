package com.hxbreak.animalcrossingtools

import android.media.AudioManager
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.hxbreak.animalcrossingtools.di.DiViewModelFactory
import com.hxbreak.animalcrossingtools.extensions.updateForTheme
import dagger.android.AndroidInjection
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class MainActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: DiViewModelFactory

    private val viewModel by viewModels<MainActivityViewModel> { viewModelFactory }

    private val navigator by lazy {
        nav_host_fragment.findNavController()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        updateForTheme(viewModel.currentTheme)
        viewModel.theme.observe(this, Observer(::updateForTheme))
        setContentView(R.layout.activity_main)
        AndroidInjection.inject(this)
        volumeControlStream = AudioManager.STREAM_MUSIC
    }

    override fun onSupportNavigateUp(): Boolean {
        return navigator.navigateUp() || super.onSupportNavigateUp()
    }

}
