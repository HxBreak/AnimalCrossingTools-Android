package com.hxbreak.animalcrossingtools.ui

import android.animation.ObjectAnimator
import android.content.SharedPreferences
import android.graphics.drawable.TransitionDrawable
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import com.google.android.material.appbar.AppBarLayout
import com.hxbreak.animalcrossingtools.R
import com.hxbreak.animalcrossingtools.view.AnimatedTextView
import java.lang.NullPointerException
import java.util.*
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty


class LazyMutableBooleanProperty(
    private val mutableLiveData: Lazy<MutableLiveData<Boolean>>
) : ReadWriteProperty<Any, Boolean> {
    override fun setValue(thisRef: Any, property: KProperty<*>, value: Boolean) {
        mutableLiveData.value.value = value
    }

    override fun getValue(thisRef: Any, property: KProperty<*>): Boolean {
        return mutableLiveData.value.value ?: error("MutableLiveData Shouldn't Have Null Value")
    }

}

open class EditBackAbleAppbarFragment : EditableAppbarFragment() {

    open fun navigationIcon() = R.drawable.ic_arrow_back_white_24dp

    lateinit var backPressedDispatcher : OnBackPressedDispatcher

    open val handleBackPressed : OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() { onBackPressed() }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireToolbar().setNavigationIcon(navigationIcon())
        requireToolbar().setNavigationOnClickListener {
            onBackPressed()
        }
        backPressedDispatcher = requireActivity().onBackPressedDispatcher
        backPressedDispatcher.addCallback(viewLifecycleOwner, handleBackPressed)
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    open fun onBackPressed(){
        if (uiSelectMode){
            uiSelectMode = !uiSelectMode
        } else {
            handleBackPressed.isEnabled = false
            backPressedDispatcher.onBackPressed()
        }
    }

    override fun onUiSelectChanged(value: Boolean) {
        super.onUiSelectChanged(value)
        handleBackPressed.isEnabled = value
    }

}

class ReadWritePropertyPlaceHolder<T, V>: ReadWriteProperty<T, V>{
    override fun setValue(thisRef: T, property: KProperty<*>, value: V) {
        error("only placeholder here")
    }

    override fun getValue(thisRef: T, property: KProperty<*>): V {
        error("only placeholder here")
    }
}

open class EditableAppbarFragment : AppbarFragment() {

    open var uiSelectMode: Boolean by ReadWritePropertyPlaceHolder()

    @Suppress("IMPLICIT_NOTHING_TYPE_ARGUMENT_IN_RETURN_POSITION")
    open val uiSelectModeMutableLiveData: MutableLiveData<Boolean> by lazy {
        error("just placeholder")
    }

    open lateinit var toolbarBackgroundTransition: TransitionDrawable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        toolbarBackgroundTransition = ContextCompat.getDrawable(requireContext(), R.drawable.toolbar_color_animation) as TransitionDrawable
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireToolbar().background = toolbarBackgroundTransition
        uiSelectModeMutableLiveData.observe(viewLifecycleOwner){
            onUiSelectChanged(it)
        }
    }

    private var isFirstAnimateToolbar: Boolean = true

    open fun animateToolbar(){
        toolbarBackgroundTransition.resetTransition()
        toolbarBackgroundTransition.run { if (uiSelectMode) startTransition(200) else reverseTransition(200)  }
    }

    open fun onUiSelectChanged(value : Boolean) {
        animateToolbar()
        animateToolbarIcons(value)
    }

    open fun animateIconList(): List<View> = emptyList() //dummy function

    private fun animateToolbarIcons(visible: Boolean) {
        val target = if (visible) 1f else 0f
        animateIconList().forEachIndexed { i, view ->
            view.visibility = View.VISIBLE
            ObjectAnimator.ofFloat(view, "alpha", target)
                .apply {
                    duration = 200
                    startDelay = i.toLong() * 150
                    start()
                }
        }
    }
}

open class AppbarFragment : Fragment(){
    var appbar: AppBarLayout? = null
    var toolbar: Toolbar? = null
    var toolbarTitle: AnimatedTextView? = null

    val nav by lazy { findNavController() }

    fun requireAppbar() = appbar ?: throw NullPointerException()

    fun requireToolbar() = toolbar ?: throw NullPointerException()

    fun requireToolbarTitle() = toolbarTitle ?: throw NullPointerException()

    open fun configSupportActionBar() = false

    private var supportActionBarSet = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appbar = view.findViewById(R.id.appbar)
        toolbar = view.findViewById(R.id.toolbar)
        toolbarTitle = view.findViewById(R.id.title)
        if (configSupportActionBar()){
            (requireActivity() as? AppCompatActivity)?.let {
                it.setSupportActionBar(requireToolbar())
                it.supportActionBar?.title = ""
                supportActionBarSet = true
            }
        }
        requireToolbar().title = ""
    }

    override fun onDestroyView() {
        super.onDestroyView()
        appbar = null
        toolbar = null
        toolbarTitle = null
        if (supportActionBarSet){
            (requireActivity() as AppCompatActivity).setSupportActionBar(null)
            supportActionBarSet = false
        }
    }

    protected fun canNavigateUp() : Boolean{
        var destId = nav.currentDestination?.id
        var parent = nav.currentDestination?.parent
        while (parent != null){
            if (parent.startDestination != destId) {
                return true
            }
            destId = parent.id
            parent = parent.parent
        }
        return false
    }
}