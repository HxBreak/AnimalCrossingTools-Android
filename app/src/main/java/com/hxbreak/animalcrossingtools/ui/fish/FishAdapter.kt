package com.hxbreak.animalcrossingtools.ui.fish

import android.animation.ObjectAnimator
import android.graphics.Color
import android.transition.TransitionManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.ScaleAnimation
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import android.widget.ViewAnimator
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.animation.addListener
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hxbreak.animalcrossingtools.R
import com.hxbreak.animalcrossingtools.data.Fish
import com.hxbreak.animalcrossingtools.view.ViewUtils
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.fish_fragment.*

import kotlinx.android.synthetic.main.fish_item.*
import java.util.*

class FishAdapter(private val viewModel: FishViewModel) :
    ListAdapter<SelectableFish, RecyclerView.ViewHolder>(FishDiff()) {
    var editMode = false
        set(value) {
            if (field != value) {
                field = value
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder.from(parent, viewModel)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolder) {
            holder.bindData(getItem(position), position, editMode)
        }
    }

    class ViewHolder private constructor(
        val viewModel: FishViewModel,
        val view: View
    ) : RecyclerView.ViewHolder(view), LayoutContainer {

        val calendar = Calendar.getInstance()

        override val containerView: View?
            get() = view

        fun animateChange(editMode: Boolean) {
            val width = ViewUtils.dp2px(view.context, 40f)
            val to = if (editMode) 0f else -width.toFloat()
            if (checkBox.translationX != to) {
                ObjectAnimator.ofFloat(checkBox, "translationX", to)
                    .apply {
                        duration = 200
                        start()
                    }
                ObjectAnimator.ofFloat(desc_part, "translationX", to + width)
                    .apply {
                        duration = 200
                        start()
                    }
            }
        }

        fun bindData(fish: SelectableFish, index: Int, editMode: Boolean) {
            val width = ViewUtils.dp2px(view.context, 40f)
            val to = if (editMode) 0f else -width.toFloat()
            checkBox.translationX = to
            desc_part.translationX = to + width
            checkBox.setOnCheckedChangeListener(null)
            checkBox.isChecked = fish.selected
            checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
                viewModel.toggleFish(fish.fish)
            }
            fish_item.isClickable = true
            fish_item.setOnClickListener {
                if (viewModel.editMode.value == true)
                    checkBox.performClick()
            }

            Glide.with(fish_image).load(fish.fish.imageLink).into(fish_image)
            donated_icon.visibility = if (fish.fish.donated) View.VISIBLE else View.GONE
            bookmark_icon.visibility = if (fish.fish.owned) View.VISIBLE else View.GONE
            val currentMonth = calendar.get(Calendar.MONTH)
            val i = fish.fish
            val activeMonthes = booleanArrayOf(
                i.jan, i.feb, i.mar, i.apr, i.may, i.jun, i.jul, i.aug, i.sep,
                i.oct, i.nov, i.dec
            )
            val isActive = activeMonthes.getOrElse(currentMonth) { false }
            fish_title.setTextColor(if (isActive) view.context.resources.getColor(R.color.colorAccent) else Color.BLACK)
            fish_title.setText("${fish.fish.name}-\$${fish.fish.price}")
            fish_subtitle.setText("${fish.fish.time}-${fish.fish.location}")
        }

        companion object {
            fun from(parent: ViewGroup, viewModel: FishViewModel): ViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.fish_item, parent, false)
                return ViewHolder(viewModel, view)
            }
        }
    }
}


class FishDiff : DiffUtil.ItemCallback<SelectableFish>() {
    override fun areItemsTheSame(oldItem: SelectableFish, newItem: SelectableFish): Boolean {
        return oldItem.fish.name.equals(newItem.fish.name)
    }

    override fun areContentsTheSame(oldItem: SelectableFish, newItem: SelectableFish): Boolean {
        return oldItem == newItem
    }
}