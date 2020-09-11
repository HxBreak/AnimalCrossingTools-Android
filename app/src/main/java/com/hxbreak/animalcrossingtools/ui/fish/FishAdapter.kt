package com.hxbreak.animalcrossingtools.ui.fish

import com.hxbreak.animalcrossingtools.adapter.ItemComparable
import com.hxbreak.animalcrossingtools.adapter.SelectionAdapter
import com.hxbreak.animalcrossingtools.character.CharUtil
import com.hxbreak.animalcrossingtools.data.source.entity.FishEntity
import com.hxbreak.animalcrossingtools.view.IndexedRecyclerView
import java.util.*

class FishAdapter: SelectionAdapter(),
    IndexedRecyclerView.IndexableAdpater {

    var fish: List<Pair<String, FishEntity>>? = null

    override fun onCurrentListChanged(
        previousList: MutableList<ItemComparable<*>>,
        currentList: MutableList<ItemComparable<*>>
    ) {
        super.onCurrentListChanged(previousList, currentList)

        fish = currentList.filterIsInstance<SelectableFishEntity>().map { it.fish.fish }
            .map { CharUtil.toCategory(CharUtil.headPinyin(it.name.nameCNzh))
                .toUpperCase(Locale.getDefault()) to it }
    }

    override fun findFirstChildIndex(s: String): Int {
        fish?.let {
            return it.indexOfFirst { it.first == s }
        }
        return -1
    }
}