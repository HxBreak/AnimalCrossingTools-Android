package com.hxbreak.animalcrossingtools.ui.song

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import com.hxbreak.animalcrossingtools.GlideApp
import com.hxbreak.animalcrossingtools.R
import com.hxbreak.animalcrossingtools.adapter.SelectionItemViewDelegate
import com.hxbreak.animalcrossingtools.adapter.SelectionViewHolder
import com.hxbreak.animalcrossingtools.view.SlideSection
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_fish.*
import kotlinx.android.synthetic.main.viewstub_checkbox.*

class SongViewBinder(val viewModel: SongViewModel) : SelectionItemViewDelegate<SongMixSelectable, SongViewBinder.ViewHolder>{

    override fun onCreateViewHolder(parent: ViewGroup): SongViewBinder.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_fish, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(data: SongMixSelectable?, vh: SongViewBinder.ViewHolder, editMode: Boolean) {
        vh.bindData(data!!)
    }

    inner class ViewHolder constructor(
        val view: View
    ) : SelectionViewHolder(view), LayoutContainer {

        override val containerView: View?
            get() = view

        fun bindData(song: SongMixSelectable) {
            requireSection().setState(if (isSelected) SlideSection.EXPANDED else SlideSection.COLLAPSED)
            checkBox.setOnCheckedChangeListener(null)
            checkBox.isChecked = song.selected
            checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
                viewModel.toggleSong(song.song.id)
            }
            itemView.isClickable = true
            GlideApp.with(image).clear(image)
            GlideApp.with(image).load("${song.song.imageUrl}").into(image)
            found_icon.visibility = View.GONE
            donated_icon.visibility =
                if (song.songSaved?.owned == true) View.VISIBLE else View.GONE
//            val i = fish.fish
//            val activeMonthes = booleanArrayOf(i.jan, i.feb, i.mar, i.apr, i.may, i.jun, i.jul, i.aug, i.sep,
//                i.oct, i.nov, i.dec)
//            val isActive = activeMonthes.getOrElse(currentMonth){false}
//            fish_title.setTextColor(if (isActive) view.context.resources.getColor(R.color.colorAccent) else Color.BLACK)
            title.setText("${song.song.localName}")
            subtitle.setText(
                "${if (song.song.buyPrice != null) "$" else ""}${song.song.buyPrice ?: "非卖品"}"
            )
            ViewCompat.setTransitionName(image, song.song.imageTransitionName())
            ViewCompat.setTransitionName(title, song.song.titleTransitionName())
            ViewCompat.setTransitionName(itemView, song.song.fileName)
            itemView.setOnClickListener {
                if (viewModel.editMode.value!!) {
                    checkBox.performClick()
                } else {
                    viewModel.playSong(song.song, SongItemView(itemView, title, image))
                }
            }
        }

    }

}