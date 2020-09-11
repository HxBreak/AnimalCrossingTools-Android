package com.hxbreak.animalcrossingtools.adapter

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.hxbreak.animalcrossingtools.utils.ViewUtils

class CommonItemDecoration(private val margin: Float, val includeOuter: Boolean): RecyclerView.ItemDecoration(){

    constructor(margin: Float): this(margin, true)

    constructor(context: Context): this(ViewUtils.dp2px(context, 10f).toFloat())

    private val intMargin = margin.toInt()
    private val halfMargin = (margin / 2f).toInt()

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        val layoutManager = parent.layoutManager ?: return
        when(layoutManager){
            is StaggeredGridLayoutManager -> buildStaggeredLayoutMargin(outRect, view, parent, state, layoutManager)
            is GridLayoutManager -> buildGridLayoutMargin(outRect, view, parent, state, layoutManager)
            is LinearLayoutManager -> buildLinearLayoutMargin(outRect, view, parent, state, layoutManager)
        }
    }

    private fun buildGridLayoutMargin(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State, layoutManager: GridLayoutManager) {
        val spanCount = layoutManager.spanCount
        val position = (view.layoutParams as GridLayoutManager.LayoutParams).spanIndex
        val outerMargin = if (includeOuter) intMargin else 0
        val halfOuterMargin = if (includeOuter) halfMargin else 0
        if (position == 0){
            if (includeOuter){
                outRect.left = intMargin
            }else{
                outRect.left = 0
            }
            outRect.right = halfMargin
        }else if (position == (spanCount - 1)){
            if (includeOuter){
                outRect.right = intMargin
            }else{
                outRect.right = 0
            }
            outRect.left = halfMargin
        }else{
            outRect.left = halfMargin
            outRect.right = halfMargin
        }
        if (parent.getChildAdapterPosition(view) in 0 until spanCount){
            outRect.bottom = halfMargin
            outRect.top = outerMargin
        }else{
            outRect.bottom = halfMargin
            outRect.top = halfMargin
        }
    }

    private fun buildLinearLayoutMargin(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State, layoutManager: LinearLayoutManager){
        val index = layoutManager.getPosition(view)
        if (layoutManager.orientation == LinearLayoutManager.HORIZONTAL){
            when (index) {
                0 -> {
                    outRect.left = intMargin
                    outRect.right = halfMargin
                }
                (state.itemCount - 1) -> {
                    outRect.left = halfMargin
                    outRect.right = intMargin
                }
                else -> {
                    outRect.run {
                        left = halfMargin
                        right = halfMargin
                    }
                }
            }
            outRect.top = intMargin
            outRect.bottom = intMargin
        }else{
            when (index) {
                0 -> {
                    outRect.run {
                        top = intMargin
                        bottom = halfMargin
                    }
                }
                (state.itemCount - 1) -> {
                    outRect.run {
                        top = halfMargin
                        bottom = intMargin
                    }
                }
                else -> {
                    outRect.run {
                        top = halfMargin
                        bottom = halfMargin
                    }
                }
            }
            outRect.left = intMargin
            outRect.right = intMargin
        }
    }

    private fun buildStaggeredLayoutMargin(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State, layoutManager: StaggeredGridLayoutManager){
        val spanCount = layoutManager.spanCount
        val position = (view.layoutParams as StaggeredGridLayoutManager.LayoutParams).spanIndex
        if (position == 0){
            outRect.left = intMargin
            outRect.right = halfMargin
        }else if (position == (spanCount - 1)){
            outRect.right = intMargin
            outRect.left = halfMargin
        }else{
            outRect.left = halfMargin
            outRect.right = halfMargin
        }
        if (parent.getChildAdapterPosition(view) in 0 until spanCount){
            outRect.bottom = halfMargin
            outRect.top = intMargin
        }else{
            outRect.bottom = halfMargin
            outRect.top = halfMargin
        }
    }
}
