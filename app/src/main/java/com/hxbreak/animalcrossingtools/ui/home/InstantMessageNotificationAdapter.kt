package com.hxbreak.animalcrossingtools.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.hxbreak.animalcrossingtools.R
import com.hxbreak.animalcrossingtools.services.handler.InstantMessageController
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_instant_message_card.*

class InstantMessageNotificationAdapter(
    private val controller: InstantMessageController,
    private val listener: View.OnClickListener
) : RecyclerView.Adapter<InstantMessageNotificationAdapter.ViewHolder>(){

    inner class ViewHolder(override val containerView: View): RecyclerView.ViewHolder(containerView),
        LayoutContainer{
        fun bind(){
            val isOnline = controller.authorized.value == true
            if (isOnline){
                containerView.setOnClickListener(listener)
            }else{
                containerView.setOnClickListener(null)
            }
            online_title.text = if (isOnline) "Online" else "Offline"
            val list = controller.lobbyList.value
            if (!list.isNullOrEmpty()){
                val text = list.joinToString(
                    prefix = "Current Online Player${if (list.size > 1) "s" else ""}: ",
                    separator = ", "
                ) { "${it.id}(${it.port})" }
                online_message.visibility = View.VISIBLE
                online_message.text = text
            }else{
                online_message.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.
            from(parent.context).
            inflate(R.layout.item_instant_message_card, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind()
    }

    override fun getItemCount(): Int {
        return 1
    }
}