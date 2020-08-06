package com.hxbreak.animalcrossingtools.ui.song

import android.animation.ObjectAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hxbreak.animalcrossingtools.GlideApp
import com.hxbreak.animalcrossingtools.R
import com.hxbreak.animalcrossingtools.utils.ViewUtils
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_fish.*
import java.lang.RuntimeException

class SongAdapter(private val viewModel: SongViewModel) :
    ListAdapter<SongMixSelectable, RecyclerView.ViewHolder>(FishDiff()) {
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
        val viewModel: SongViewModel,
        val view: View
    ) : RecyclerView.ViewHolder(view), LayoutContainer {

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

        fun bindData(song: SongMixSelectable, index: Int, editMode: Boolean) {
            val width = ViewUtils.dp2px(view.context, 40f)
            val to = if (editMode) 0f else -width.toFloat()
            checkBox.translationX = to
            desc_part.translationX = to + width
            checkBox.setOnCheckedChangeListener(null)
            checkBox.isChecked = song.selected
            checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
                viewModel.toggleSong(song.song.id)
            }
            itemView.isClickable = true
            GlideApp.with(fish_image).clear(fish_image)
            GlideApp.with(fish_image).load("${song.song.imageUrl}").into(fish_image)
            donated_icon.visibility = View.GONE
            bookmark_icon.visibility =
                if (song.songSaved?.owned == true) View.VISIBLE else View.GONE
//            val i = fish.fish
//            val activeMonthes = booleanArrayOf(i.jan, i.feb, i.mar, i.apr, i.may, i.jun, i.jul, i.aug, i.sep,
//                i.oct, i.nov, i.dec)
//            val isActive = activeMonthes.getOrElse(currentMonth){false}
//            fish_title.setTextColor(if (isActive) view.context.resources.getColor(R.color.colorAccent) else Color.BLACK)
            fish_title.setText("${song.song.localName}")
            fish_subtitle.setText(
                "${if (song.song.buyPrice != null) "$" else ""}${song.song.buyPrice ?: "非卖品"}"
            )
            ViewCompat.setTransitionName(fish_image, song.song.imageTransitionName())
            ViewCompat.setTransitionName(fish_title, song.song.titleTransitionName())
            ViewCompat.setTransitionName(itemView, song.song.fileName)
            itemView.setOnClickListener {
                if (viewModel.editMode.value!!) {
                    checkBox.performClick()
                } else {
                    viewModel.playSong(song.song, object : TransitionView {
                        override fun retrieve(name: String) = when (name) {
                            "image" -> fish_image
                            "title" -> fish_title
                            "root" -> itemView
                            else -> throw RuntimeException()
                        }

                    })
                }
            }
        }

        companion object {
            fun from(parent: ViewGroup, viewModel: SongViewModel): ViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_fish, parent, false)
                return ViewHolder(viewModel, view)
            }
        }
    }
}

interface TransitionView {
    fun retrieve(name: String): View
}

class FishDiff : DiffUtil.ItemCallback<SongMixSelectable>() {
    override fun areItemsTheSame(oldItem: SongMixSelectable, newItem: SongMixSelectable): Boolean {
        return oldItem.song.id == newItem.song.id
    }

    override fun areContentsTheSame(
        oldItem: SongMixSelectable,
        newItem: SongMixSelectable
    ): Boolean {
        return oldItem == newItem
    }
}