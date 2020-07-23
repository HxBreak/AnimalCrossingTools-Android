package com.hxbreak.animalcrossingtools.adapter

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class LightAdapter : RecyclerView.Adapter<ViewHolder<*>>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<*> {
        TODO("Not yet implemented")
    }

    override fun getItemCount(): Int {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: ViewHolder<*>, position: Int) {
        TODO("Not yet implemented")
    }

    override fun getItemViewType(position: Int): Int {
        
        return super.getItemViewType(position)
    }
}

abstract class ViewHolder<T>(val view: View) : RecyclerView.ViewHolder(view) {
    abstract fun bind(data: T);
}