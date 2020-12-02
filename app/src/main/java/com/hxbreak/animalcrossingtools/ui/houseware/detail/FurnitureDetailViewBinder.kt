package com.hxbreak.animalcrossingtools.ui.houseware.detail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.hxbreak.animalcrossingtools.GlideApp
import com.hxbreak.animalcrossingtools.R
import com.hxbreak.animalcrossingtools.adapter.ItemComparable
import com.hxbreak.animalcrossingtools.adapter.ItemViewDelegate
import com.hxbreak.animalcrossingtools.data.source.entity.FurnitureEntity
import com.hxbreak.animalcrossingtools.extensions.littleCircleWaitAnimation
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.page_simple_info_houseware.*


data class ChipData(
    val id: String,
    val text: String,
): ItemComparable<String> {
    override fun id() = id
}

class ChipViewBinder : ItemViewDelegate<ChipData, ChipViewBinder.ViewHolder> {

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        val chip = view as Chip
    }

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chip, parent, false))
    }

    override fun onBindViewHolder(data: ChipData?, vh: ViewHolder) {
        data?.let {
            vh.chip.text = it.text
        }
    }
}

class HousewareDetailHeaderViewBinder:
    ItemViewDelegate<FurnitureEntity, HousewareDetailHeaderViewBinder.ViewHolder> {

    inner class ViewHolder(override val containerView: View): RecyclerView.ViewHolder(containerView),
        LayoutContainer {
        fun bind(entity: FurnitureEntity) {
            GlideApp.with(image).load(entity.image_uri)
                .littleCircleWaitAnimation(containerView.context)
                .into(image)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
            .inflate(R.layout.page_simple_info_houseware, parent, false)
        )
    }

    override fun onBindViewHolder(data: FurnitureEntity?, vh: ViewHolder) {
        data?.let {
            vh.bind(it)
        }
    }
}