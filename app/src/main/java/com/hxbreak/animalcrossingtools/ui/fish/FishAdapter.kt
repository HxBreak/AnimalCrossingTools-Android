package com.hxbreak.animalcrossingtools.ui.fish

import android.animation.ObjectAnimator
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hxbreak.animalcrossingtools.R
import com.hxbreak.animalcrossingtools.view.ViewUtils
import kotlinx.android.extensions.LayoutContainer

import kotlinx.android.synthetic.main.fish_item.*
import java.util.*

class FishAdapter(private val viewModel: FishViewModel) :
    ListAdapter<SelectableFishEntity, RecyclerView.ViewHolder>(FishDiff()) {
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

        fun bindData(fishEntity: SelectableFishEntity, index: Int, editMode: Boolean) {
            val width = ViewUtils.dp2px(view.context, 40f)
            val entity = fishEntity.fish.fish
            val to = if (editMode) 0f else -width.toFloat()
            checkBox.translationX = to
            desc_part.translationX = to + width
            checkBox.setOnCheckedChangeListener(null)
            checkBox.isChecked = fishEntity.selected
            checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
                viewModel.toggleFish(fishEntity.fish)
            }
            fish_item.isClickable = true
            fish_item.setOnClickListener {
                if (viewModel.editMode.value == true)
                    checkBox.performClick()
            }

            Glide.with(fish_image).load(fishEntity.fish.fish.icon_uri).into(fish_image)
            donated_icon.visibility =
                if (fishEntity.fish.saved?.donated == true) View.VISIBLE else View.GONE
            bookmark_icon.visibility =
                if (fishEntity.fish.saved?.owned == true) View.VISIBLE else View.GONE
            val currentMonth = calendar.get(Calendar.MONTH)
            val i = fishEntity.fish
//            val activeMonthes = booleanArrayOf(
//                i.jan, i.feb, i.mar, i.apr, i.may, i.jun, i.jul, i.aug, i.sep,
//                i.oct, i.nov, i.dec
//            )
//            val isActive = activeMonthes.getOrElse(currentMonth) { false }
//            fish_title.setTextColor(if (isActive) view.context.resources.getColor(R.color.colorAccent) else Color.BLACK)
            fish_title.setText("${fishEntity.fish.fish.name.nameCNzh}-\$${fishEntity.fish.fish.price}")
            fish_subtitle.setText(
                "${if (entity.availability.isIsAllDay) "All Day" else
                    fishEntity.fish.fish.availability.time}-${fishEntity.fish.fish.availability.location}"
            )
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


class FishDiff : DiffUtil.ItemCallback<SelectableFishEntity>() {
    override fun areItemsTheSame(
        oldItem: SelectableFishEntity,
        newItem: SelectableFishEntity
    ): Boolean {
        return oldItem.fish.fish.id == newItem.fish.fish.id
    }

    override fun areContentsTheSame(
        oldItem: SelectableFishEntity,
        newItem: SelectableFishEntity
    ): Boolean {
        return oldItem == newItem
    }
}