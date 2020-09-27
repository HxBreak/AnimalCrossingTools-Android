package com.hxbreak.animalcrossingtools

import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.hxbreak.animalcrossingtools.di.DiViewModelFactory
import com.hxbreak.animalcrossingtools.extensions.updateForTheme
import dagger.android.support.DaggerAppCompatActivity
import io.flutter.embedding.android.FlutterEngineConfigurator
import io.flutter.embedding.android.FlutterFragment
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.systemchannels.PlatformChannel
import io.flutter.plugins.GeneratedPluginRegistrant
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.reflect.Proxy
import javax.inject.Inject

class MainActivity : DaggerAppCompatActivity(), FlutterEngineConfigurator {

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
        viewModel.connection.nowPlaying.observe(this, {})
        setContentView(R.layout.activity_main)
        volumeControlStream = AudioManager.STREAM_MUSIC
    }

    override fun onSupportNavigateUp(): Boolean {
        return navigator.navigateUp() || super.onSupportNavigateUp()
    }

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        GeneratedPluginRegistrant.registerWith(flutterEngine)
        val field = flutterEngine.platformChannel.javaClass.getDeclaredField("platformMessageHandler")
        .apply { isAccessible = true }
        val handler = field.get(flutterEngine.platformChannel) as PlatformChannel.PlatformMessageHandler
        val proxy = Proxy.newProxyInstance(flutterEngine.platformChannel.javaClass.classLoader,
            handler.javaClass.interfaces
        ) { _, method, args ->
            if (method.name == "popSystemNavigator"){
                navigator.navigateUp()
            }else{
                method.invoke(handler, *(args ?: emptyArray()))
            }
        }
        field.set(flutterEngine.platformChannel, proxy)
    }

    override fun cleanUpFlutterEngine(flutterEngine: FlutterEngine) {
        flutterEngine.keyEventChannel.setEventResponseHandler(null)
    }

    private inline fun callIfFlutterFragment(block: FlutterFragment.() -> Unit) :Boolean {
        val fragment = supportFragmentManager.primaryNavigationFragment
        if (fragment is FlutterFragment){
            fragment.block()
            return true
        }
        return false
    }

    override fun onPostResume() {
        super.onPostResume()
        callIfFlutterFragment { onPostResume() }
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        callIfFlutterFragment { onTrimMemory(level) }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        callIfFlutterFragment { intent?.let { onNewIntent(it) } }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        callIfFlutterFragment { this.onRequestPermissionsResult(requestCode, permissions, grantResults) }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        callIfFlutterFragment { onUserLeaveHint() }
    }
}
