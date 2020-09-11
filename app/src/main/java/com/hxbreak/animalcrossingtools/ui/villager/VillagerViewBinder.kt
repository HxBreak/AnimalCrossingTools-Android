package com.hxbreak.animalcrossingtools.ui.villager

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hxbreak.animalcrossingtools.GlideApp
import com.hxbreak.animalcrossingtools.R
import com.hxbreak.animalcrossingtools.adapter.SelectionItemViewDelegate
import com.hxbreak.animalcrossingtools.adapter.SelectionViewHolder
import com.hxbreak.animalcrossingtools.data.source.entity.VillagerEntity
import com.hxbreak.animalcrossingtools.i18n.toLocaleName
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_villager.*

class VillagerViewBinder(val viewModel: VillagerViewModel) : SelectionItemViewDelegate<VillagerEntity, VillagerViewBinder.ViewHolder>{

    override fun onCreateViewHolder(parent: ViewGroup) = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_villager, parent, false))

    override fun onBindViewHolder(data: VillagerEntity?, vh: ViewHolder, editMode: Boolean) {
        data?.let {
            vh.bind(it)
        }
    }

    inner class ViewHolder(override val containerView: View): SelectionViewHolder(containerView), LayoutContainer{

        fun bind(villager: VillagerEntity) {
            GlideApp.with(villager_avatar).load(villager.image_uri).into(villager_avatar)
            villager_name.text = villager.name.toLocaleName(viewModel.locale)
            villager_hobby_value.text = villager.hobby
            gender_value.text = villager.gender
            birthday_value.text = villager.birthday
            saying_value.text = "Saying: ${villager.saying}"
//            saying_value.setTextColor(Color.parseColor(villager.textcolor))
//            saying_value.background = ColorDrawable(Color.parseColor(villager.bubblecolor))
        }
    }

}