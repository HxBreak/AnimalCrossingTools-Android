package com.hxbreak.animalcrossingtools.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.res.getResourceIdOrThrow
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.google.android.material.ripple.RippleDrawableCompat
import com.hxbreak.animalcrossingtools.R

class IconView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private var mIcon1: AnimatedVectorDrawableCompat? = null
    private var mIcon2: AnimatedVectorDrawableCompat? = null

    private var mListener = View.OnClickListener {
        morph()
        warpListener?.onClick(this)
    }

    private var warpListener: View.OnClickListener? = null

    init {
        val array = context.obtainStyledAttributes(attrs, R.styleable.IconView)
        mIcon1 = AnimatedVectorDrawableCompat.create(
            context,
            array.getResourceIdOrThrow(R.styleable.IconView_icon1)
        )
        mIcon2 = AnimatedVectorDrawableCompat.create(
            context,
            array.getResourceIdOrThrow(R.styleable.IconView_icon2)
        )
        mIcon1?.stop()
        mIcon2?.stop()
        isSelected = false
        setImageDrawable(mIcon2)
        array.recycle()
        super.setOnClickListener(mListener)
    }


    override fun setOnClickListener(l: OnClickListener?) {
        warpListener = l;
    }

    fun morph() {
        val drawable = if (this.isSelected) mIcon2 else mIcon1
        setImageDrawable(drawable)
        drawable?.start()
        this.isSelected = !this.isSelected
    }
}