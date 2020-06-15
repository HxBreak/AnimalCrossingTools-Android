package com.hxbreak.animalcrossingtools.ui.fish

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.appbar.AppBarLayout
import com.hxbreak.animalcrossingtools.R

class FishBehavior(context: Context?, attrs: AttributeSet?) :
    AppBarLayout.ScrollingViewBehavior(context, attrs) {

    override fun layoutDependsOn(
        parent: CoordinatorLayout,
        child: View,
        dependency: View
    ): Boolean {
        return super.layoutDependsOn(
            parent,
            child,
            dependency
        ) || dependency.id == R.id.bottom_sheet_viewstub
    }

    override fun onDependentViewChanged(
        parent: CoordinatorLayout,
        child: View,
        dependency: View
    ): Boolean {
        super.onDependentViewChanged(parent, child, dependency)
        if (dependency.id == R.id.bottom_sheet_viewstub) {
            Log.e("HxBreak", "${dependency.top}")
        }
        return false
    }
}