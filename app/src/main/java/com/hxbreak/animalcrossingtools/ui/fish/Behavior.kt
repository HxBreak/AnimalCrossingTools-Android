package com.hxbreak.animalcrossingtools.ui.fish

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.hxbreak.animalcrossingtools.view.ScrollViewGroup


class Behavior constructor(context: Context?, attributeSet: AttributeSet?) :
    CoordinatorLayout.Behavior<View> (context, attributeSet)
{
    override fun layoutDependsOn(
        parent: CoordinatorLayout,
        child: View,
        dependency: View
    ): Boolean {
        return dependency is ScrollViewGroup
    }

    override fun onDependentViewChanged(
        parent: CoordinatorLayout,
        child: View,
        dependency: View
    ): Boolean {
//        child.translationY = dependency.y
        child.translationY = dependency.y
        child.setOnClickListener {
            Toast.makeText(child.context, "1", Toast.LENGTH_SHORT).show()
        }
        Log.e("HxBreak", "${dependency.top} ${dependency.height} ${dependency.y}")
        return true
    }
}