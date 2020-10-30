package com.hxbreak.animalcrossingtools.extensions

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData


data class CollectionChangeEvent<T>(
    val inc: Boolean,
    val collection: Collection<T>?
)

object ACTransformations{

    @JvmStatic
    @MainThread
    fun <X> testChanged(source: LiveData<out Collection<X>>): LiveData<CollectionChangeEvent<X>> {
        val outputLiveData = MediatorLiveData<CollectionChangeEvent<X>>()
        outputLiveData.addSource(source) { currentValue ->
            val previousValue = outputLiveData.value
            if (previousValue?.collection != currentValue) {
                val preSize = previousValue?.collection?.size ?: 0
                val currentSize = currentValue?.size ?: 0
                outputLiveData.value = CollectionChangeEvent(currentSize < preSize, currentValue)
            }
        }
        return outputLiveData
    }

    @JvmStatic
    @MainThread
    fun <X> previousValue(source: LiveData<out X>): LiveData<Pair<X?, X?>>{
        val outputLiveData = MediatorLiveData<Pair<X?, X?>>()
        outputLiveData.addSource(source){
            val previousValue = outputLiveData.value
            outputLiveData.value = previousValue?.second to it

        }
        return outputLiveData
    }
}


@Suppress("NOTHING_TO_INLINE")
inline fun <X> LiveData<out Collection<X>>.testChanged(): LiveData<CollectionChangeEvent<X>> =
    ACTransformations.testChanged(this)

@Suppress("NOTHING_TO_INLINE")
inline fun <X> LiveData<out X>.previousValue() = ACTransformations.previousValue(this)

