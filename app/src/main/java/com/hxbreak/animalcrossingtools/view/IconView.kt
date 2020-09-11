package com.hxbreak.animalcrossingtools.view

import android.content.Context
import android.graphics.drawable.RippleDrawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.content.res.getResourceIdOrThrow
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.hxbreak.animalcrossingtools.R
import com.hxbreak.animalcrossingtools.utils.ViewUtils

class IconView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatImageButton(context, attrs, defStyleAttr) {

    private var mIcon1: AnimatedVectorDrawableCompat? = null
    private var mIcon2: AnimatedVectorDrawableCompat? = null

    private var mListener = OnClickListener {
        morph()
        warpListener?.onClick(this)
    }

    private var warpListener: OnClickListener? = null

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

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            val backgroundStyle = context.obtainStyledAttributes(intArrayOf(android.R.attr.selectableItemBackgroundBorderless))
            val borderlessBackground = backgroundStyle.getDrawable(0)
            if (borderlessBackground is RippleDrawable){
                val dp = ViewUtils.dp2px(context, 8f)
                val boxSize = minOf(mIcon2!!.intrinsicWidth, mIcon2!!.intrinsicHeight)
                val fitRippleWidth = boxSize + dp
                val fitRippleHeight = boxSize + dp
                val x = w / 2 - fitRippleWidth / 2
                val y = h / 2 - fitRippleHeight / 2
                borderlessBackground.setHotspotBounds(x, y, x + fitRippleWidth, y + fitRippleHeight)
            }
            background = borderlessBackground
            backgroundStyle.recycle()
        }
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