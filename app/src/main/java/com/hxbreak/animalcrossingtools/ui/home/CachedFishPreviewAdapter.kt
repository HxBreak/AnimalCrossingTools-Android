package com.hxbreak.animalcrossingtools.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.hxbreak.animalcrossingtools.GlideApp
import com.hxbreak.animalcrossingtools.R
import com.hxbreak.animalcrossingtools.adapter.ItemViewDelegate
import com.hxbreak.animalcrossingtools.adapter.LightAdapter
import com.hxbreak.animalcrossingtools.data.source.entity.FishEntity
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_fish_notice.*

class CachedFishPreviewAdapter: RecyclerView.Adapter<CachedFishPreviewAdapter.ViewHolder>() {

    var list: List<FishEntity>? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    inner class ViewHolder(override val containerView: View): RecyclerView.ViewHolder(containerView), LayoutContainer {
        fun bind(it: List<FishEntity>) {
            val adapter = LightAdapter()
            recycler_view.adapter = adapter
            adapter.register(FishEntityCircleView())
            adapter.submitList(it)
            notice_text.text = "5 Active..."
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_fish_notice, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        list?.let {
            holder.bind(it)
        }
    }

    override fun getItemCount() = if (list.isNullOrEmpty()) 0 else 1
}

class FishEntityCircleView: ItemViewDelegate<FishEntity, FishEntityCircleView.ViewHolder>{

    inner class ViewHolder(override val containerView: View): RecyclerView.ViewHolder(containerView),
        LayoutContainer {

        fun bind(entity: FishEntity) {
            GlideApp.with(containerView)
                .load(entity.icon_uri)
                .into(containerView as ImageView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup): FishEntityCircleView.ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_fish_circle_view, parent, false))
    }

    override fun onBindViewHolder(data: FishEntity?, vh: FishEntityCircleView.ViewHolder) {
        data?.let {
            vh.bind(it)
        }
    }
}