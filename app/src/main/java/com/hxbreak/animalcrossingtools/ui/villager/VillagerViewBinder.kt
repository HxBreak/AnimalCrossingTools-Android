package com.hxbreak.animalcrossingtools.ui.villager

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.hxbreak.animalcrossingtools.GlideApp
import com.hxbreak.animalcrossingtools.GlideProgress
import com.hxbreak.animalcrossingtools.R
import com.hxbreak.animalcrossingtools.adapter.SelectionItemViewDelegate
import com.hxbreak.animalcrossingtools.adapter.SelectionViewHolder
import com.hxbreak.animalcrossingtools.data.source.entity.VillagerEntity
import com.hxbreak.animalcrossingtools.i18n.toLocaleName
import com.hxbreak.animalcrossingtools.text
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_villager.*

class VillagerViewBinder(val viewModel: VillagerViewModel, val viewLifecycleOwner: LifecycleOwner) :
    SelectionItemViewDelegate<VillagerEntity, VillagerViewBinder.ViewHolder> {

    override fun onCreateViewHolder(parent: ViewGroup) = ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_villager, parent, false)
    )

    override fun onBindViewHolder(data: VillagerEntity?, vh: ViewHolder, editMode: Boolean) {
        data?.let {
            vh.bind(it)
        }
    }

    inner class ViewHolder(override val containerView: View) : SelectionViewHolder(containerView),
        LayoutContainer {

        var livedata : LiveData<GlideProgress.Loading>? = null

        fun bind(villager: VillagerEntity) {
            val drawable = CircularProgressDrawable(containerView.context).apply {
                strokeWidth = 5f
                centerRadius = 50f
                progressRotation = 0.75f
            }
            GlideApp.with(villager_avatar)
                .load(villager.image_uri)
                .placeholder(drawable)
                .into(villager_avatar)
            livedata?.removeObservers(viewLifecycleOwner)
            livedata = viewModel.collector[villager.image_uri]
            livedata?.observe(viewLifecycleOwner) {
                drawable.setStartEndTrim(0f, it.text().toFloat())
                drawable.invalidateSelf()
            }
            villager_name.text = villager.name.toLocaleName(viewModel.locale)
            villager_hobby_value.text = villager.hobby
            gender_value.text = villager.gender
            birthday_value.text = villager.birthday
            saying_value.text = "Saying: ${villager.saying}"
        }
    }

}