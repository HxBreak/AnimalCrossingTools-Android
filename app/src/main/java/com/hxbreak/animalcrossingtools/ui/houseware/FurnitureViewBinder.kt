package com.hxbreak.animalcrossingtools.ui.houseware

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.hxbreak.animalcrossingtools.GlideApp
import com.hxbreak.animalcrossingtools.R
import com.hxbreak.animalcrossingtools.adapter.ItemViewDelegate
import com.hxbreak.animalcrossingtools.adapter.LightAdapter
import com.hxbreak.animalcrossingtools.adapter.Typer
import com.hxbreak.animalcrossingtools.data.source.entity.HousewareEntity
import com.hxbreak.animalcrossingtools.extensions.littleCircleWaitAnimation
import com.hxbreak.animalcrossingtools.i18n.toLocaleName
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_houseware_item.*
import kotlinx.android.synthetic.main.item_housewares_variants.*


class HousewareItemViewBinder(
    val listener: (View, HousewareEntity) -> Unit
): ItemViewDelegate<HousewareEntity, HousewareItemViewBinder.ViewHolder> {

    inner class ViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView),
        LayoutContainer {
        fun bind(entity: HousewareEntity) {
            ViewCompat.setTransitionName(houseware_image, "${entity.fileName}-container")
            GlideApp.with(houseware_image)
                .load(entity.image_uri)
                .littleCircleWaitAnimation(containerView.context)
                .into(houseware_image)
            houseware_name.text = entity.variant
            containerView.setOnClickListener {
                listener(houseware_image, entity)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup) = ViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.item_houseware_item, parent, false)
    )

    override fun onBindViewHolder(data: HousewareEntity?, vh: ViewHolder) {
        data?.let { vh.bind(it) }
    }
}

class HousewaresViewBinder(
    val typed: Typer, val recycledViewPool: RecyclerView.RecycledViewPool,
    val viewModel: HousewaresViewModel
) : ItemViewDelegate<HousewareVariants, HousewaresViewBinder.ViewHolder> {

    inner class ViewHolder(override val containerView: View): RecyclerView.ViewHolder(containerView),
        LayoutContainer {

        val recyclerView: RecyclerView = item_recycler_view

        fun bind(housewares: HousewareVariants){
            if (housewares.variants.isNotEmpty()){
                val adapter = LightAdapter(typed)
                recyclerView.setRecycledViewPool(recycledViewPool)
                recyclerView.adapter = adapter
                captain.text = housewares.variants.first().name.toLocaleName(viewModel.locale)
                adapter.submitList(housewares.variants)
            }else{
                recyclerView.adapter = null
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup) = ViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.item_housewares_variants, parent, false)
    )

    override fun onBindViewHolder(data: HousewareVariants?, vh: ViewHolder) {
        data?.let { vh.bind(data) }
    }
}