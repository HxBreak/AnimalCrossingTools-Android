package com.hxbreak.animalcrossingtools.adapter

import android.view.View
import android.view.ViewGroup
import androidx.core.view.get
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hxbreak.animalcrossingtools.view.SlideSection
import timber.log.Timber
import kotlin.IllegalStateException

data class SelectionViewItemRelation(
    val clazz: Class<*>,
    val delegate: SelectionItemViewDelegate<Any, SelectionViewHolder>
)

open class SelectionAdapter : ListAdapter<ItemComparable<*>, SelectionViewHolder> {

    constructor(diffCallback: DiffUtil.ItemCallback<ItemComparable<*>> = DefaultItemComparableDiffUtil) : super(diffCallback) {}

    private var mRecyclerView: RecyclerView? = null

    var editMode = false
        set(value) {
            if (field != value) {
                field = value
                mRecyclerView?.run{
                    if (childCount > 0){
                        for (i in 0 until childCount){
                            val viewHolder = mRecyclerView?.getChildViewHolder(this[i])
                            if (viewHolder is SelectionViewHolder){
                                viewHolder.isSelected = field
                            }
                        }
                        val start = getChildViewHolder(this[0]).bindingAdapterPosition
                        val end = getChildViewHolder(this[childCount - 1]).bindingAdapterPosition
                        notifyItemRangeChanged(0, start - 0)
                        notifyItemRangeChanged(end, adapter!!.itemCount - end - 1)
                    }
                }
            }
        }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        mRecyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        mRecyclerView = null
    }

    private val indexer = mutableListOf<SelectionViewItemRelation>()

    override fun onBindViewHolder(holder: SelectionViewHolder, position: Int) {
        holder.isSelected = editMode
        indexer[getItemViewType(position)].delegate.onBindViewHolder(getItem(position) as Any?, holder, editMode)
    }

    fun register(clazz: Class<*>, delegate: SelectionItemViewDelegate<*, out SelectionViewHolder>){
        @Suppress("UNCHECKED_CAST")
        indexer.add(SelectionViewItemRelation(clazz, delegate as SelectionItemViewDelegate<Any, SelectionViewHolder>))
    }

    inline fun <reified T : ItemComparable<*>> register(delegate: SelectionItemViewDelegate<T, out SelectionViewHolder>){
        register(T::class.java, delegate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectionViewHolder {
        return indexer[viewType].delegate.onCreateViewHolder(parent)
    }

    override fun getItemViewType(position: Int): Int {
        return indexViewType(position)
    }

    private fun indexViewType(position: Int): Int {
        val clazz = getItem(position)?.javaClass ?: PlaceHolder::class.java
        val index = indexer.indexOfFirst { it.clazz.isAssignableFrom(clazz) }
        if (index > -1){
            return index
        }
        throw IllegalStateException("viewType cannot locate class:$clazz")
    }
}

open class SelectionViewHolder(private val view: View): RecyclerView.ViewHolder(view){

    var section: SlideSection? = null

    fun requireSection() = section ?: throw IllegalStateException("section == null")

    var isSelected: Boolean = false
    set(value) {
        if (value != field){
            field = value
            section?.setState(if (value) SlideSection.EXPANDED else SlideSection.COLLAPSED)
        }
    }

    init {
        if (view is SlideSection){
            section = view
        }
    }
}

interface SelectionItemViewDelegate<T, VH: SelectionViewHolder> {

    fun onCreateViewHolder(parent: ViewGroup): VH

    fun onBindViewHolder(data: T?, vh: VH, editMode: Boolean)

}
