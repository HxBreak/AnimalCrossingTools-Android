package com.hxbreak.animalcrossingtools.ui.bugs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hxbreak.animalcrossingtools.GlideApp
import com.hxbreak.animalcrossingtools.R
import com.hxbreak.animalcrossingtools.adapter.SelectionItemViewDelegate
import com.hxbreak.animalcrossingtools.adapter.SelectionViewHolder
import com.hxbreak.animalcrossingtools.i18n.toLocaleName
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_bug.*
import kotlinx.android.synthetic.main.viewstub_checkbox.*

class BugViewBinder(val viewModel: BugsViewModel) : SelectionItemViewDelegate<BugEntityMixSelectable, BugViewBinder.ViewHolder>{

    override fun onCreateViewHolder(parent: ViewGroup) = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_bug, parent, false))

    override fun onBindViewHolder(
        data: BugEntityMixSelectable?,
        vh: ViewHolder,
        editMode: Boolean
    ) {
        data?.let { vh.bind(it) }
    }

    inner class ViewHolder(override val containerView: View): SelectionViewHolder(containerView), LayoutContainer{

        fun bind(bug: BugEntityMixSelectable) {
            checkBox.setOnCheckedChangeListener(null)
            checkBox.isChecked = bug.selected
            checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
                viewModel.toggle(bug.entity.id)
            }
            itemView.setOnClickListener {
            if (viewModel.editMode.value == true){
                    checkBox.performClick()
                }
            }
            donated_icon.visibility = if (bug.saved?.donated == true) View.VISIBLE else View.GONE
            found_icon.visibility = if (bug.saved?.owned == true) View.VISIBLE else View.GONE
            GlideApp.with(image).load(bug.entity.imageUri).into(image)
            title.text = bug.entity.name.toLocaleName(viewModel.locale)
        }
    }
}