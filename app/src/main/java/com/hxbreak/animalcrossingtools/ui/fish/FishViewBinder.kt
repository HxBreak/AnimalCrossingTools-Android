package com.hxbreak.animalcrossingtools.ui.fish

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hxbreak.animalcrossingtools.GlideApp
import com.hxbreak.animalcrossingtools.R
import com.hxbreak.animalcrossingtools.adapter.SelectionItemViewDelegate
import com.hxbreak.animalcrossingtools.adapter.SelectionViewHolder
import com.hxbreak.animalcrossingtools.view.SlideSection
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_fish.*
import kotlinx.android.synthetic.main.viewstub_checkbox.*

class FishViewBinder(val viewModel: FishViewModel) : SelectionItemViewDelegate<SelectableFishEntity, FishViewBinder.ViewHolder> {

    override fun onCreateViewHolder(parent: ViewGroup) = ViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.item_fish, parent, false))

    override fun onBindViewHolder(
        data: SelectableFishEntity?,
        vh: ViewHolder,
        editMode: Boolean
    ) {
        data?.let { vh.bindData(it) }
    }

    inner class ViewHolder(val view: View): SelectionViewHolder(view), LayoutContainer{
        override val containerView: View?
            get() = view

        fun bindData(fishEntity: SelectableFishEntity) {
            requireSection().setState(if (isSelected) SlideSection.EXPANDED else SlideSection.COLLAPSED)
            val entity = fishEntity.fish.fish
            checkBox.setOnCheckedChangeListener(null)
            checkBox.isChecked = fishEntity.selected
            checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
                viewModel.toggleFish(fishEntity.fish)
            }
            fish_item.isClickable = true
            fish_item.setOnClickListener {
                if (viewModel.editMode.value == true)
                    checkBox.performClick()
                else
                    viewModel.fishOnClick(fishEntity.fish.fish.id)
            }
            GlideApp.with(image).load(fishEntity.fish.fish.icon_uri).placeholder(R.drawable.ic_fish).into(image)
            donated_icon.visibility =
                if (fishEntity.fish.saved?.donated == true) View.VISIBLE else View.GONE
            found_icon.visibility =
                if (fishEntity.fish.saved?.owned == true) View.VISIBLE else View.GONE
            val i = fishEntity.fish
            title.setText("${fishEntity.fish.fish.localeName}-\$${fishEntity.fish.fish.price}")
            subtitle.setText(
                "${if (entity.availability.isAllDay) "All Day" else
                    fishEntity.fish.fish.availability.time}-${fishEntity.fish.fish.availability.location}"
            )
        }
    }
}
