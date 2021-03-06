package com.hxbreak.animalcrossingtools.ui.seacreature

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hxbreak.animalcrossingtools.GlideApp
import com.hxbreak.animalcrossingtools.R
import com.hxbreak.animalcrossingtools.adapter.SelectionItemViewDelegate
import com.hxbreak.animalcrossingtools.adapter.SelectionViewHolder
import com.hxbreak.animalcrossingtools.data.source.entity.monthArray
import com.hxbreak.animalcrossingtools.extensions.littleCircleWaitAnimation
import com.hxbreak.animalcrossingtools.i18n.toLocaleName
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_bug.*
import kotlinx.android.synthetic.main.item_seacreature.*
import kotlinx.android.synthetic.main.item_seacreature.availability
import kotlinx.android.synthetic.main.item_seacreature.donated_icon
import kotlinx.android.synthetic.main.item_seacreature.found_icon
import kotlinx.android.synthetic.main.item_seacreature.image
import kotlinx.android.synthetic.main.item_seacreature.subtitle
import kotlinx.android.synthetic.main.item_seacreature.title
import kotlinx.android.synthetic.main.viewstub_checkbox.*

class SeaCreatureViewBinder(val viewModel: SeaCreatureViewModel) : SelectionItemViewDelegate<SeaCreatureEntityMixSelectable, SeaCreatureViewBinder.ViewHolder>{

    override fun onCreateViewHolder(parent: ViewGroup) = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_seacreature, parent, false))

    override fun onBindViewHolder(
        data: SeaCreatureEntityMixSelectable?,
        vh: ViewHolder,
        editMode: Boolean
    ) {
        data?.let { vh.bind(it) }
    }

    inner class ViewHolder(override val containerView: View): SelectionViewHolder(containerView), LayoutContainer{

        fun bind(seaCreature: SeaCreatureEntityMixSelectable) {
            checkBox.setOnCheckedChangeListener(null)
            checkBox.isChecked = seaCreature.selected
            checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
                viewModel.toggle(seaCreature.entity.id)
            }
            itemView.setOnClickListener {
                if (viewModel.editMode.value == true) {
                    checkBox.performClick()
                }
            }
            donated_icon.visibility =
                if (seaCreature.saved?.donated == true) View.VISIBLE else View.GONE
            found_icon.visibility =
                if (seaCreature.saved?.owned == true) View.VISIBLE else View.GONE
            GlideApp.with(image).load(seaCreature.entity.imageUri)
                .littleCircleWaitAnimation(containerView.context)
                .into(image)
            val itemAvailability = seaCreature.entity.availability
            availability.background = null
            val now = viewModel.preferenceStorage.timeInNow
            val month = now.monthValue
            val hour = now.hour
            availability.background = null
            if (itemAvailability.monthArray(viewModel.hemisphere).contains(month.toShort())){
                if (itemAvailability.timeArray.orEmpty().contains(hour.toShort())){
                    availability.background = ColorDrawable(Color.GREEN)
                }else{
                    availability.background = ColorDrawable(Color.BLUE)
                }
            }
            title.text =
                "${seaCreature.entity.name.toLocaleName(viewModel.locale)}-\$${seaCreature.entity.price}"
            subtitle.text =
                "${if (seaCreature.entity.availability.isAllDay) "All Day" else seaCreature.entity
                    .availability.time}(${itemAvailability.rarity})"
        }
    }
}