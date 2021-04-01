package com.hxbreak.animalcrossingtools.components.selections

import androidx.recyclerview.selection.ItemDetailsLookup

interface ViewHolderItemDetails<T> {
    fun detail(): ItemDetailsLookup.ItemDetails<T>?
}