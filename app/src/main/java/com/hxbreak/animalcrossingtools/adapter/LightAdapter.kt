package com.hxbreak.animalcrossingtools.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import java.lang.IllegalStateException

interface ItemComparable<T>{
    fun id(): T
}

internal class PlaceHolderViewBinder : ItemViewDelegate<Any, PlaceHolderViewBinder.PlaceHolderViewHolder>{

    override fun onCreateViewHolder(parent: ViewGroup): PlaceHolderViewHolder {
        return PlaceHolderViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(android.R.layout.simple_list_item_1, parent, false))
    }

    override fun onBindViewHolder(data: Any?, vh: PlaceHolderViewHolder) {
        vh.view.findViewById<TextView>(android.R.id.text1).text = "PlaceHolder"
    }

    class PlaceHolderViewHolder(val view: View): RecyclerView.ViewHolder(view)
}

class PlaceHolder

data class ViewItemRelation(
    val clazz: Class<*>,
    val delegate: ItemViewDelegate<Any, RecyclerView.ViewHolder>
)

interface ItemViewDelegate<T, VH: RecyclerView.ViewHolder>{

    fun onCreateViewHolder(parent: ViewGroup): VH

    fun onBindViewHolder(data: T?, vh: VH)

}

internal object DefaultItemComparableDiffUtil : DiffUtil.ItemCallback<ItemComparable<*>>() {
    override fun areItemsTheSame(
        oldItem: ItemComparable<*>,
        newItem: ItemComparable<*>
    ): Boolean {
        return oldItem.id() == newItem.id()
    }

    override fun areContentsTheSame(
        oldItem: ItemComparable<*>,
        newItem: ItemComparable<*>
    ): Boolean {
        return oldItem.equals(newItem)
    }

}

object CommonItemComparableDiffUtil : DiffUtil.ItemCallback<Any>() {

    override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
        if(oldItem is ItemComparable<*> && newItem is ItemComparable<*>){
            return (oldItem.javaClass == newItem.javaClass && oldItem.id() == newItem.id())
        }
        return false
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
        return oldItem == newItem
    }

}

class Typer {

    private val indexer = mutableListOf<ViewItemRelation>()

    fun register(clazz: Class<*>, delegate: ItemViewDelegate<*, out RecyclerView.ViewHolder>){
        @Suppress("UNCHECKED_CAST")
        indexer.add(ViewItemRelation(clazz, delegate as ItemViewDelegate<Any, RecyclerView.ViewHolder>))
    }

    inline fun <reified T : ItemComparable<*>> register(delegate: ItemViewDelegate<T, out RecyclerView.ViewHolder>){
        register(T::class.java, delegate)
    }

    operator fun get(index: Int) = indexer[index]

    fun indexOfFirst(predicate: (ViewItemRelation) -> Boolean): Int {
        return indexer.indexOfFirst(predicate)
    }
}

class LightAdapter(val typer: Typer = Typer(), diffCallback: DiffUtil.ItemCallback<ItemComparable<*>> = DefaultItemComparableDiffUtil):
    ListAdapter<ItemComparable<*>, RecyclerView.ViewHolder>(diffCallback) {

    init {
        register(PlaceHolder::class.java, PlaceHolderViewBinder())
    }

    fun register(clazz: Class<*>, delegate: ItemViewDelegate<*, out RecyclerView.ViewHolder>){
        typer.register(clazz, delegate)
    }

    inline fun <reified T : ItemComparable<*>> register(delegate: ItemViewDelegate<T, out RecyclerView.ViewHolder>){
        typer.register(T::class.java, delegate)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        typer[getItemViewType(position)].delegate.onBindViewHolder(getItem(position) as Any?, holder)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return typer[viewType].delegate.onCreateViewHolder(parent)
    }

    override fun getItemViewType(position: Int): Int {
        return indexViewType(position)
    }

    private fun indexViewType(position: Int): Int {
        val clazz = getItem(position)?.javaClass ?: PlaceHolder::class.java
        val index = typer.indexOfFirst { it.clazz.isAssignableFrom(clazz) }
        if (index > -1){
            return index
        }
        throw IllegalStateException("viewType cannot locate class:$clazz")
    }
}

//class PagingLightAdapter: PagingDataAdapter<ItemComparable<*>, RecyclerView.ViewHolder> {
//
//    constructor(diffCallback: DiffUtil.ItemCallback<ItemComparable<*>> = DefaultItemComparableDiffUtil) : super(diffCallback) {}
//
//    private val indexer = mutableListOf<ViewItemRelation>()
//
//    init {
//        register(PlaceHolder::class.java, PlaceHolderViewBinder())
//    }
//
//    fun register(clazz: Class<*>, delegate: ItemViewDelegate<*, out RecyclerView.ViewHolder>){
//        @Suppress("UNCHECKED_CAST")
//        indexer.add(ViewItemRelation(clazz, delegate as ItemViewDelegate<Any, RecyclerView.ViewHolder>))
//    }
//
//    inline fun <reified T : ItemComparable<*>> register(delegate: ItemViewDelegate<T, out RecyclerView.ViewHolder>){
//        register(T::class.java, delegate)
//    }
//
//    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//        indexer[getItemViewType(position)].delegate.onBindViewHolder(getItem(position) as Any?, holder)
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
//        return indexer[viewType].delegate.onCreateViewHolder(parent)
//    }
//
//    override fun getItemViewType(position: Int): Int {
//        return indexViewType(position)
//    }
//
//    private fun indexViewType(position: Int): Int {
//        val clazz = getItem(position)?.javaClass ?: PlaceHolder::class.java
//        val index = indexer.indexOfFirst { it.clazz.isAssignableFrom(clazz) }
//        if (index > -1){
//            return index
//        }
//        throw IllegalStateException("viewType cannot locate class:$clazz")
//    }
//
//    fun all() = (0 until itemCount).map { getItem(it) }
//
//}