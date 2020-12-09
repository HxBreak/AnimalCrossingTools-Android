package com.hxbreak.animalcrossingtools.ui.home

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.doOnLayout
import androidx.core.view.doOnNextLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeUtils
import com.hxbreak.animalcrossingtools.GlideApp
import com.hxbreak.animalcrossingtools.R
import com.hxbreak.animalcrossingtools.adapter.CommonItemDecoration
import com.hxbreak.animalcrossingtools.adapter.ItemViewDelegate
import com.hxbreak.animalcrossingtools.adapter.LightAdapter
import com.hxbreak.animalcrossingtools.data.source.entity.FishEntity
import com.hxbreak.animalcrossingtools.domain.home.LocalAvailabilityEntity
import com.hxbreak.animalcrossingtools.utils.ViewUtils
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_fish_circle_view.*
import kotlinx.android.synthetic.main.item_fish_notice.*

class CachedFishPreviewAdapter: RecyclerView.Adapter<CachedFishPreviewAdapter.ViewHolder>() {

    var list: List<LocalAvailabilityEntity<FishEntity>>? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    inner class ViewHolder(override val containerView: View): RecyclerView.ViewHolder(containerView), LayoutContainer {

        init {
            val decoration = CommonItemDecoration(ViewUtils.dp2px(containerView.context, 4f).toFloat(), false)
            recycler_view.addItemDecoration(decoration)
        }

        fun bind(it: List<LocalAvailabilityEntity<FishEntity>>) {
            val adapter = LightAdapter()
            recycler_view.adapter = adapter
            adapter.register(FishEntityCircleView())
            adapter.submitList(it.take(5).map { it.entity })
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

    @SuppressLint("UnsafeExperimentalUsageError")
    inner class ViewHolder(override val containerView: View): RecyclerView.ViewHolder(containerView),
        LayoutContainer {

        private val badgeDrawable: BadgeDrawable = BadgeDrawable.create(containerView.context)

        init {
            badgeDrawable.isVisible = true
            badgeDrawable.badgeGravity = BadgeDrawable.TOP_END
            image.doOnLayout {
                BadgeUtils.attachBadgeDrawable(badgeDrawable, image, root_container)
            }
        }

        fun bind(entity: FishEntity) {
            badgeDrawable.backgroundColor = Color.GREEN
            GlideApp.with(containerView)
                .load(entity.icon_uri)
                .into(image)
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