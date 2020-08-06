package com.hxbreak.animalcrossingtools.ui

import android.animation.ObjectAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hxbreak.animalcrossingtools.R
import com.hxbreak.animalcrossingtools.utils.ViewUtils
import com.example.tracker_proto.NetworkUtils
import com.hxbreak.tracker_proto.data.ConnectedClient
import kotlinx.android.extensions.LayoutContainer

import kotlinx.android.synthetic.main.item_fish.*
import java.text.SimpleDateFormat
import java.util.*

class TrackerAdapter(private val viewModel: TrackerViewModel) :
    ListAdapter<ConnectedClient, RecyclerView.ViewHolder>(FishDiff()) {
    var editMode = false
        set(value) {
            if (field != value) {
                field = value
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder.from(parent, viewModel)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolder) {
            holder.bindData(getItem(position), position, editMode)
        }
    }

    class ViewHolder private constructor(
        val viewModel: TrackerViewModel,
        val view: View
    ) : RecyclerView.ViewHolder(view), LayoutContainer {

        val calendar = Calendar.getInstance()

        override val containerView: View?
            get() = view

        fun animateChange(editMode: Boolean) {
            val width = ViewUtils.dp2px(view.context, 40f)
            val to = if (editMode) 0f else -width.toFloat()
            if (checkBox.translationX != to) {
                ObjectAnimator.ofFloat(checkBox, "translationX", to)
                    .apply {
                        duration = 200
                        start()
                    }
                ObjectAnimator.ofFloat(desc_part, "translationX", to + width)
                    .apply {
                        duration = 200
                        start()
                    }
            }
        }

        fun bindData(client: ConnectedClient, index: Int, editMode: Boolean) {
            val width = ViewUtils.dp2px(view.context, 40f)
            val to = if (editMode) 0f else -width.toFloat()
            checkBox.translationX = to
            desc_part.translationX = to + width
            checkBox.setOnCheckedChangeListener(null)
//            checkBox.isChecked = fish.selected
//            checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
//                viewModel.toggleFish(fish.fish)
//            }
//            fish_item.isClickable = true
            fish_item.setOnClickListener {
                viewModel.chatWith(client)
            }
            donated_icon.visibility = View.GONE
            bookmark_icon.visibility = View.GONE
            fish_title.text =
                "${client.id} - ${NetworkUtils.ipToString(client.addr)}:${client.port}"
//            fish_title.setTextColor(if (isActive) view.context.resources.getColor(R.color.colorAccent) else Color.BLACK)
//            fish_title.setText("${fish.fish.name}-\$${fish.fish.price}")

            val date = Date(client.lastActiveTime + TimeZone.getDefault().rawOffset)
            val offset =
                System.currentTimeMillis() - (client.lastActiveTime + TimeZone.getDefault().rawOffset)
            val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            fmt.timeZone = TimeZone.getDefault()
            val text = when (offset) {
                in 0..700 -> "online-ok"
                in 700..1500 -> "online-slow"
                else -> "offline"
            }
            fish_subtitle.setText("$text")
        }

        companion object {
            fun from(parent: ViewGroup, viewModel: TrackerViewModel): ViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_fish, parent, false)
                return ViewHolder(viewModel, view)
            }
        }
    }
}


class FishDiff : DiffUtil.ItemCallback<ConnectedClient>() {
    override fun areItemsTheSame(oldItem: ConnectedClient, newItem: ConnectedClient): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: ConnectedClient, newItem: ConnectedClient): Boolean {
        return oldItem == newItem
    }
}