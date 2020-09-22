package com.hxbreak.animalcrossingtools.ui.fossil

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hxbreak.animalcrossingtools.GlideApp
import com.hxbreak.animalcrossingtools.R
import com.hxbreak.animalcrossingtools.adapter.SelectionItemViewDelegate
import com.hxbreak.animalcrossingtools.adapter.SelectionViewHolder
import com.hxbreak.animalcrossingtools.extensions.littleCircleWaitAnimation
import com.hxbreak.animalcrossingtools.i18n.toLocaleName
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_bug.*
import kotlinx.android.synthetic.main.viewstub_checkbox.*

class FossilViewBinder(val viewModel: FossilViewModel) : SelectionItemViewDelegate<FossilEntityMixSelectable, FossilViewBinder.ViewHolder>{

    override fun onCreateViewHolder(parent: ViewGroup) = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_bug, parent, false))

    override fun onBindViewHolder(
        data: FossilEntityMixSelectable?,
        vh: ViewHolder,
        editMode: Boolean
    ) {
        data?.let { vh.bind(it) }
    }

    inner class ViewHolder(override val containerView: View): SelectionViewHolder(containerView), LayoutContainer{

        fun bind(fossil: FossilEntityMixSelectable) {
            checkBox.setOnCheckedChangeListener(null)
            checkBox.isChecked = fossil.selected
            checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
                viewModel.toggle(fossil.entity.fileName)
            }
            itemView.setOnClickListener {
                if (viewModel.editMode.value == true) {
                    checkBox.performClick()
                }
            }
            donated_icon.visibility = if (fossil.saved?.donated == true) View.VISIBLE else View.GONE
            found_icon.visibility = if (fossil.saved?.owned == true) View.VISIBLE else View.GONE
            GlideApp.with(image).load(fossil.entity.imageUri)
                .littleCircleWaitAnimation(containerView.context)
                .into(image)
            title.text =
                "${fossil.entity.name.toLocaleName(viewModel.locale)}-\$${fossil.entity.price}"
            subtitle.text = "part of ${fossil.entity.partOf}"
        }
    }
}