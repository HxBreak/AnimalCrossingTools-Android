package com.hxbreak.animalcrossingtools.components.selections

import androidx.lifecycle.LiveData
import androidx.recyclerview.selection.SelectionTracker

class SelectionModeTrackerPredicate<T>(val mode: LiveData<Boolean>) : SelectionTracker.SelectionPredicate<T>() {

    override fun canSetStateForKey(key: T, nextState: Boolean): Boolean {
        return mode.value ?: false
    }

    override fun canSetStateAtPosition(position: Int, nextState: Boolean): Boolean {
        return mode.value ?: false
    }

    override fun canSelectMultiple() = true

}