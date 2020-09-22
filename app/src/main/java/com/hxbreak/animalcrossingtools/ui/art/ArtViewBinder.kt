package com.hxbreak.animalcrossingtools.ui.art

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hxbreak.animalcrossingtools.GlideApp
import com.hxbreak.animalcrossingtools.R
import com.hxbreak.animalcrossingtools.adapter.SelectionItemViewDelegate
import com.hxbreak.animalcrossingtools.adapter.SelectionViewHolder
import com.hxbreak.animalcrossingtools.extensions.littleCircleWaitAnimation
import com.hxbreak.animalcrossingtools.i18n.toLocaleName
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_fish.*
import kotlinx.android.synthetic.main.viewstub_checkbox.*

class ArtViewBinder(val viewModel: ArtViewModel) : SelectionItemViewDelegate<ArtEntityMixSelectable, ArtViewBinder.ViewHolder>{

    override fun onCreateViewHolder(parent: ViewGroup) =
        ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_fish, parent, false))

    override fun onBindViewHolder(data: ArtEntityMixSelectable?, vh: ViewHolder, editMode: Boolean) {
        data?.let { vh.bind(it) }
    }

    inner class ViewHolder(override val containerView: View): SelectionViewHolder(containerView), LayoutContainer{
        fun bind(item: ArtEntityMixSelectable){
            checkBox.setOnCheckedChangeListener(null)
            checkBox.isChecked = item.selected
            checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
                viewModel.toggleArt(item.art.id)
            }
            donated_icon.visibility = View.GONE
            found_icon.visibility = if (item.saved?.owned == true) View.VISIBLE else View.GONE
            itemView.setOnClickListener {
                if (viewModel.editMode.value == true) {
                    checkBox.performClick()
                }
            }
            subtitle.text =
                "hasFake: ${if (item.art.hasFake) "yes" else "no"}, BuyPrice: ${item.art.buyPrice}"
            title.text = item.art.name.toLocaleName(viewModel.locale)
            GlideApp.with(image).load(item.art.imageUri)
                .littleCircleWaitAnimation(containerView.context)
                .into(image)
        }
    }

}