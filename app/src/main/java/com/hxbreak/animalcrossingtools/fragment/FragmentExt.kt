package com.hxbreak.animalcrossingtools.fragment

import androidx.fragment.app.FragmentFactory
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

open class Event<out T>(private val content: T) {

    var hasBeenHandled = false
        private set // Allow external read but not write

    /**
     * Returns the content and prevents its use again.
     */
    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    /**
     * Returns the content, even if it's already been handled.
     */
    fun peekContent(): T = content
}

class EventObserver<T>(private val onEventUnhandledContent: (T) -> Unit) : Observer<Event<T>> {
    override fun onChanged(event: Event<T>?) {
        event?.getContentIfNotHandled()?.let {
            onEventUnhandledContent(it)
        }
    }
}

@ExperimentalContracts
inline fun FragmentFactory.useOnce(fragmentManager: FragmentManager, block: () -> Unit){
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    val saved = fragmentManager.fragmentFactory
    fragmentManager.fragmentFactory = this
    block()
    fragmentManager.fragmentFactory = saved
}