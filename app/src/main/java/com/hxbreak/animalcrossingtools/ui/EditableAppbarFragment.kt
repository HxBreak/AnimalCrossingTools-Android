package com.hxbreak.animalcrossingtools.ui

import android.graphics.drawable.TransitionDrawable
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.google.android.material.appbar.AppBarLayout
import com.hxbreak.animalcrossingtools.R
import dagger.android.support.DaggerFragment

open class EditBackAbleAppbarFragment : EditableAppbarFragment() {

    open fun navigationIcon() = R.drawable.ic_arrow_back_white_24dp

    lateinit var backPressedDispatcher : OnBackPressedDispatcher

    open val handleBackPressed : OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() { onBackPressed() }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar.setNavigationIcon(navigationIcon())
        toolbar.setNavigationOnClickListener { onBackPressed() }
        backPressedDispatcher = requireActivity().onBackPressedDispatcher
        backPressedDispatcher.addCallback(viewLifecycleOwner, handleBackPressed)
    }

    open fun onBackPressed(){
        if (uiSelectMode){
            uiSelectMode = !uiSelectMode
        } else {
            handleBackPressed.isEnabled = false
            backPressedDispatcher.onBackPressed()
        }
    }

    override fun onUiSelectChanged(value: Boolean) {
        super.onUiSelectChanged(value)
        handleBackPressed.isEnabled = value
    }
}

open class EditableAppbarFragment : AppbarFragment() {

    open var uiSelectMode = false
        set(value) {
            field = value
            onUiSelectChanged(value)
            animateToolbar()
        }

    lateinit var toolbarBackgroundTransition: TransitionDrawable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        toolbarBackgroundTransition = ContextCompat.getDrawable(requireContext(), R.drawable.toolbar_color_animation) as TransitionDrawable
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar.background = toolbarBackgroundTransition
    }

    open fun animateToolbar(){
        toolbarBackgroundTransition.run { if (uiSelectMode) startTransition(200) else reverseTransition(200)  }
    }

    open fun onUiSelectChanged(value : Boolean) {}//dummy function
}

open class AppbarFragment : DaggerFragment(){
    lateinit var appbar: AppBarLayout
    lateinit var toolbar: Toolbar
    lateinit var title: TextView

    val nav by lazy { findNavController() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appbar = view.findViewById(R.id.appbar)
        toolbar = view.findViewById(R.id.toolbar)
        title = view.findViewById(R.id.title)
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    protected fun canNavigateUp() : Boolean{
        var destId = nav.currentDestination?.id
        var parent = nav.currentDestination?.parent
        while (parent != null){
            if (parent.startDestination != destId) {
                return true
            }
            destId = parent.id
            parent = parent.parent
        }
        return false
    }
}