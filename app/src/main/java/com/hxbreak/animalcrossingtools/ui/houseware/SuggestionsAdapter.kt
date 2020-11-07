package com.hxbreak.animalcrossingtools.ui.houseware

import android.content.Context
import android.database.Cursor
import android.view.View
import android.widget.Filter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.cursoradapter.widget.ResourceCursorAdapter
import com.hxbreak.animalcrossingtools.GlideApp
import com.hxbreak.animalcrossingtools.i18n.toLocaleName
import java.util.*


class SuggestionsAdapter(context: Context?, layout: Int, val locale: Locale) : ResourceCursorAdapter(
    context, layout, null, false
) {

    override fun bindView(view: View, context: Context?, cursor: Cursor?) {
        val text1 = view.findViewById<TextView>(android.R.id.text1)
        val icon1 = view.findViewById<ImageView>(android.R.id.icon1)
        text1.let { it.text = cursor?.toLocaleName(locale) }
        val imageIndex = cursor?.getColumnIndex("image_uri")
        if (imageIndex != null){
            icon1.isVisible = true
            GlideApp.with(icon1).load(cursor.getString(imageIndex)).into(icon1)
        }else{
            icon1.isVisible = false
            GlideApp.with(icon1).clear(icon1)
        }
    }

    /**
     * Avoid Internal Filter Component, We Really Don't Need this
     */
    override fun getFilter(): Filter {
        return object : Filter(){
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                return FilterResults().also {
                    it.count = 0
                    it.values = null
                }
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {}
        }
    }
}