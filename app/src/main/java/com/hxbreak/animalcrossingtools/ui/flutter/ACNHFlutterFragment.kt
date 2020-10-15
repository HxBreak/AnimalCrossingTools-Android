package com.hxbreak.animalcrossingtools.ui.flutter

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.transition.MaterialSharedAxis
import io.flutter.embedding.android.FlutterFragment
import timber.log.Timber

/**
 * Flutter Module Enter Point
 */
class ACNHFlutterFragment : FlutterFragment(){

    val args by navArgs<ACNHFlutterFragmentArgs>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val forward = MaterialSharedAxis(MaterialSharedAxis.X, true)
        enterTransition = forward
        val backward = MaterialSharedAxis(MaterialSharedAxis.X, false)
        returnTransition = backward
        flutterEngine!!.navigationChannel.pushRoute(args.destination)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                this@ACNHFlutterFragment.onBackPressed()
            }
        })
        flutterEngine?.navigationChannel?.setMethodCallHandler { call, result ->
            when(call.method){
                "routeUpdated" -> {
                    val previousRouteName = call.argument<Any?>("previousRouteName")
                    val routeName = call.argument<Any?>("routeName")
                    if (previousRouteName is String && previousRouteName == args.destination){
                        findNavController().navigateUp()
                    }else if (routeName is String && routeName == "/"){
                        findNavController().navigateUp()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        flutterEngine?.navigationChannel?.setMethodCallHandler(null)
    }
}
