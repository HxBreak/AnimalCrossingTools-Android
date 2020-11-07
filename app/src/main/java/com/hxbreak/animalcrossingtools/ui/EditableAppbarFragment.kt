package com.hxbreak.animalcrossingtools.ui

import android.animation.ObjectAnimator
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.TransitionDrawable
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.transition.TransitionManager
import com.google.android.material.appbar.AppBarLayout
import com.hxbreak.animalcrossingtools.R
import com.hxbreak.animalcrossingtools.extensions.previousValue
import com.hxbreak.animalcrossingtools.utils.ViewUtils
import com.hxbreak.animalcrossingtools.view.AnimatedTextView
import com.hxbreak.animalcrossingtools.view.drawable.AnimatedColorDrawable
import timber.log.Timber
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
        uiSelectModeMutableLiveData.observe(viewLifecycleOwner){
            handleBackPressed.isEnabled = it
        }
    }

    open fun onBackPressed(){
        if (uiSelectMode){
            uiSelectMode = !uiSelectMode
        } else {
            handleBackPressed.isEnabled = false
            backPressedDispatcher.onBackPressed()
        }
    }
}

open class BackAbleAppbarFragment : EditBackAbleAppbarFragment(){
    override val uiSelectModeMutableLiveData: MutableLiveData<Boolean>
        get() = MutableLiveData(false)
}

open class EditableAppbarFragment : AppbarFragment() {

    @Suppress("IMPLICIT_NOTHING_TYPE_ARGUMENT_IN_RETURN_POSITION")
    open val uiSelectModeMutableLiveData: MutableLiveData<Boolean> by lazy {
        error("just placeholder, expose from viewModel")
    }

    var uiSelectMode: Boolean by LazyMutableBooleanProperty(lazy { uiSelectModeMutableLiveData })

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val resources = requireContext().resources
        val drawable = AnimatedColorDrawable(
            resources.getColor(R.color.colorPrimary),
            resources.getColor(R.color.colorAccent), 200)
        requireToolbar().background = drawable

        uiSelectModeMutableLiveData.observe(viewLifecycleOwner){
            animateToolbarIcons(it)
            drawable.run {
                if (it == true){ forward() }else{ reverse() }
            }
        }
    }

    private var isFirstAnimateToolbar: Boolean = true

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
    protected val res by lazy { requireContext().resources }


    fun requireAppbar() = appbar ?: throw NullPointerException()

    fun requireToolbar() = toolbar ?: throw NullPointerException()

    fun requireToolbarTitle() = toolbarTitle ?: throw NullPointerException()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appbar = view.findViewById(R.id.appbar)
        toolbar = view.findViewById(R.id.toolbar)
        toolbarTitle = view.findViewById(R.id.title)
        (requireActivity() as? AppCompatActivity)?.let {
            it.setSupportActionBar(requireToolbar())
            it.supportActionBar?.title = ""
        }
        requireToolbar().title = ""
    }

    override fun onDestroyView() {
        super.onDestroyView()
        appbar = null
        toolbar = null
        toolbarTitle = null
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