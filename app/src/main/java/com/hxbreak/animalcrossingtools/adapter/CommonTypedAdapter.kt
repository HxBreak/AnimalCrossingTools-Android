package com.hxbreak.animalcrossingtools.adapter

import androidx.paging.AsyncPagingDataDiffer
import androidx.recyclerview.widget.AdapterListUpdateCallback
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.drakeet.multitype.MultiTypeAdapter
import com.hxbreak.animalcrossingtools.components.selections.ActionModeListener

@Suppress("UNCHECKED_CAST")
open class CommonTypedAdapter(diffCallback: DiffUtil.ItemCallback<Any>): MultiTypeAdapter(){

    val differ: AsyncPagingDataDiffer<Any> = AsyncPagingDataDiffer(diffCallback, AdapterListUpdateCallback(this))

    override var items: List<Any> = ProxyList(differ) as List<Any>
        set(value) = error("THIS IS NOT ALLOWED $value on $field")

    override fun getItemCount(): Int {
        return differ.itemCount
    }

    private inner class ProxyList(val d: AsyncPagingDataDiffer<Any>): List<Any?>{
        override val size: Int
            get() = d.itemCount

        override fun contains(element: Any?): Boolean {
            throw NotImplementedError("contains")
        }

        override fun containsAll(elements: Collection<Any?>): Boolean {
            throw NotImplementedError("containsAll")
        }

        override fun get(index: Int): Any? {
            return d.getItem(index)
        }

        override fun indexOf(element: Any?): Int {
            throw NotImplementedError("indexOf")
        }

        override fun isEmpty(): Boolean {
            throw NotImplementedError("isEmpty")
        }

        override fun iterator(): Iterator<Any?> {
            throw NotImplementedError("iterator")
        }

        override fun lastIndexOf(element: Any?): Int {
            throw NotImplementedError("lastIndexOf")
        }

        override fun listIterator(): ListIterator<Any?> {
            throw NotImplementedError("listIterator")
        }

        override fun listIterator(index: Int): ListIterator<Any?> {
            throw NotImplementedError("listIterator")
        }

        override fun subList(fromIndex: Int, toIndex: Int): List<Any?> {
            throw NotImplementedError("subList")
        }
    }
}

class SelectableTypedAdapter(diffCallback: DiffUtil.ItemCallback<Any>): CommonTypedAdapter(diffCallback){

    var initialMode = true

    var mode: Boolean = true
        set(value) {
            field = value
            var changeTo = true
            if (!initialMode || differ.snapshot().items.isNotEmpty()){
                changeTo = false
                notifyDataSetChanged()
            }
            if (initialMode){
                initialMode = changeTo
            }
        }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: List<Any>
    ) {
        super.onBindViewHolder(holder, position, payloads)
        if (holder is ActionModeListener){
            holder.mode(mode)
        }
    }
}