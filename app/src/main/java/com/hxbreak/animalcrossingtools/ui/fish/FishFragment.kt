package com.hxbreak.animalcrossingtools.ui.fish

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialSharedAxis
import com.hxbreak.animalcrossingtools.R
import com.hxbreak.animalcrossingtools.data.source.entity.FishEntityMix
import com.hxbreak.animalcrossingtools.extensions.testChanged
import com.hxbreak.animalcrossingtools.ui.EditBackAbleAppbarFragment
import com.hxbreak.animalcrossingtools.ui.LazyMutableBooleanProperty
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.bottom_sheet_fish.*
import kotlinx.android.synthetic.main.fragment_fish.*

@AndroidEntryPoint
class FishFragment : EditBackAbleAppbarFragment() {

    private val viewModel by viewModels<FishViewModel>()
    private var bottomSheetView: View? = null
    private var adapter: FishAdapter? = null

    override val uiSelectModeMutableLiveData by lazy { viewModel.editMode }

    private fun requireAdapter() = adapter ?: throw IllegalStateException("adapter == null")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val forward = MaterialSharedAxis(MaterialSharedAxis.X, true)
        enterTransition = forward
        val backward = MaterialSharedAxis(MaterialSharedAxis.X, false)
        returnTransition = backward
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_fish, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireToolbar().setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)

        arrayOf(donate, found).forEach { it.alpha = 0f }
        val enableIndicator = viewModel.locale.language == "zh"
        requireToolbar().title = null
        requireToolbarTitle().setText(res.getString(R.string.fish_catalog))
        viewModel.data.observe(viewLifecycleOwner){
            requireAdapter().submitList(it)
            recycler_view.doOnPreDraw { _ ->
                recycler_view.run {
                    if (itemDecorationCount > 0) {
                        for (i in itemDecorationCount - 1 downTo 0) {
                            removeItemDecorationAt(i)
                        }
                    }
                    if (it != null) {
                        if (enableIndicator) {
                            addItemDecoration(
                                FishHeadDecoration(
                                    requireContext(),
                                    it.map { it.fish.fish },
                                    recycler_view.width
                                )
                            )
                        }
                    }
                }
            }
        }
        viewModel.loading.observe(viewLifecycleOwner) {
            refresh_layout.isRefreshing = it == true
        }
        refresh_layout.setOnRefreshListener {
            viewModel.refresh.value = true
        }
        viewModel.error.observe(viewLifecycleOwner){
            if (it == null){
                common_layout.clearState()
            }else{
                common_layout.setException(it.first, it.second)
            }
        }
        viewModel.selected.observe(viewLifecycleOwner) {
            val enable = !it.isNullOrEmpty()
            arrayOf(donate, found).forEach {
                it.isEnabled = enable
            }
        }
        viewModel.selected.testChanged().observe(viewLifecycleOwner){
            if (it.collection.isNullOrEmpty()){
                requireToolbarTitle().clearLastSelected()
                requireToolbarTitle().setText(getString(R.string.fish_catalog), it.inc)
            } else {
                requireToolbarTitle().setText(getString(R.string.numbers_of_fish_select, it.collection.size), it.inc)
            }
        }

        adapter = FishAdapter()
        requireAdapter().register(FishViewBinder(viewModel, viewLifecycleOwner))
        recycler_view.adapter = adapter
        recycler_view.mEnableAlphabet = enableIndicator

        edit_mode.setOnClickListener {
            uiSelectMode = !uiSelectMode
        }

        donate.setOnClickListener {
            viewModel.toggleDonate()
        }

        found.setOnClickListener {
            viewModel.toggleFounded()
        }

        viewModel.active.observe(viewLifecycleOwner, Observer {
            active_summary.text = it
        })

        viewModel.found.observe(viewLifecycleOwner, Observer {
            founded_summary.text = it
        })

        viewModel.donated.observe(viewLifecycleOwner, Observer {
            donated_summary.text = it
        })

        viewModel.donateAction.observe(viewLifecycleOwner, Observer {
            if (it == donate.isSelected)
                donate.morph()
        })

        viewModel.foundAction.observe(viewLifecycleOwner, Observer {
            if (it == found.isSelected)
                found.morph()
        })

        viewModel.editMode.observe(viewLifecycleOwner){
            requireAdapter().editMode = it
            if (edit_mode.isSelected != it) { edit_mode.morph() }
            if (it == false){ viewModel.clearSelected() }
        }

        viewModel.clickedFish.observe(viewLifecycleOwner){
//            effectBottomSheet(it.getContentIfNotHandled())
        }
    }

    var lastOffset = -1.0f

    private val listener = object : BottomSheetBehavior.BottomSheetCallback() {

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            val child = recycler_view
            if (child.startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL)) {
                val percent = slideOffset - lastOffset
                lastOffset = slideOffset
                child.nestedScrollBy(0, (percent * bottomSheet.height / 4).toInt())
            }
        }

        override fun onStateChanged(bottomSheet: View, newState: Int) {}
    }

    private fun effectBottomSheet(fish: FishEntityMix?) {
        bottomSheetView = bottomSheetView ?: bottom_sheet_viewstub.inflate()
        val sheetBehavior = BottomSheetBehavior.from(bottomSheetView!!)
        sheetBehavior.addBottomSheetCallback(listener)
        if (fish == null) {
            sheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        } else {
            sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            Glide.with(requireContext()).clear(bt_fish_image)
            Glide.with(requireContext())
                .load("https://acnhapi.com/v1/images/fish/${fish.fish.id}")
                .placeholder(R.drawable.ic_fish)
                .into(bt_fish_image)
            bt_fish_name.text = fish.fish.name.nameCNzh
            bt_item_stock.text = "${fish.saved?.quantity ?: 0}"
        }
    }

    override fun animateIconList() = listOf(donate, found)
}
