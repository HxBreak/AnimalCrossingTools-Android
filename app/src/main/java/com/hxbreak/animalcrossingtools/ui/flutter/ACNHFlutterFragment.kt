package com.hxbreak.animalcrossingtools.ui.flutter

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.navArgs
import com.google.android.material.transition.MaterialSharedAxis
import io.flutter.embedding.android.FlutterFragment

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

        args.destination
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                this@ACNHFlutterFragment.onBackPressed()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}
