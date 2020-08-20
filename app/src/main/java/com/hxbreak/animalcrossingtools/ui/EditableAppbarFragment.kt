package com.hxbreak.animalcrossingtools.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.app.TaskStackBuilder
import androidx.navigation.NavDeepLinkBuilder
import androidx.navigation.fragment.findNavController
import com.google.android.material.appbar.AppBarLayout
import com.hxbreak.animalcrossingtools.R
import dagger.android.support.DaggerFragment
import timber.log.Timber

open class EditableAppbarFragment : AppbarFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
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