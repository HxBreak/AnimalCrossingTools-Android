package com.hxbreak.animalcrossingtools.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView

class IndexedRecyclerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    override fun onDraw(c: Canvas?) {
        super.onDraw(c)
        val indexerWidth = 20
        val offsetX = width - indexerWidth
//        c?.withTranslation (x = offsetX.toFloat()){
//            c.drawRect(Rect(0, 0, indexerWidth, height), Paint().apply {
//                color = Color.GRAY
//            })
//        }
    }
}