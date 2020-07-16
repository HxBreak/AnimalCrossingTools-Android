package com.hxbreak.animalcrossingtools.ui.fish

import android.animation.ObjectAnimator
import android.graphics.drawable.TransitionDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.animation.addListener
import androidx.core.view.NestedScrollingChild3
import androidx.core.view.ViewCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import com.hxbreak.animalcrossingtools.R
import com.hxbreak.animalcrossingtools.data.source.entity.FishEntityMix
import com.hxbreak.animalcrossingtools.di.DiViewModelFactory
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fish_bottom_sheet.*
import kotlinx.android.synthetic.main.fish_fragment.*
import timber.log.Timber
import javax.inject.Inject


class FishFragment : DaggerFragment() {

    @Inject
    lateinit var viewModelFactory: DiViewModelFactory

    private val viewModel by viewModels<FishViewModel> { viewModelFactory }
    private lateinit var adapter: FishAdapter
    private var bottomSheetView: View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fish_fragment, container, false)
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            isEnabled = false
            edit_mode.callOnClick()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar.setNavigationOnClickListener {
            if (viewModel.editMode.value == true) {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            } else {
                findNavController().navigateUp()
            }
        }
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        arrayOf(bookmark, donated).forEach { it.alpha = 0f }
        toolbar.title = null
        title.text = "Fish"
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            onBackPressedCallback
        )
        viewModel.data.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
            recycler_view.run {
                if (itemDecorationCount > 0) {
                    for (i in itemDecorationCount - 1 downTo 0) {
                        removeItemDecorationAt(i)
                    }
                }
                if (it != null) {
                    addItemDecoration(
                        FishHeadDecoration(
                            requireContext(),
                            it.map { it.fish.fish },
                            recycler_view.width
                        )
                    )
                }
            }
        })
        viewModel.loading.observe(viewLifecycleOwner, Observer {
            refresh_layout.isRefreshing = it == true
        })
        refresh_layout.setOnRefreshListener {
            viewModel.refresh.value = true
        }
        viewModel.error.observe(viewLifecycleOwner, Observer {
            val bar = Snackbar.make(coordinator, it.first.toString(), Snackbar.LENGTH_LONG)
            bar.setAction("RETRY") { v ->
                it.second()
            }
            bar.show()
        })
        viewModel.selectedFish.observe(viewLifecycleOwner, Observer {
            title.let { title ->
                val toolbarHeight = toolbar.measuredHeight.toFloat()
                val titleWidth = title.measuredWidth.toFloat()
                val newTitle = if (it.isNullOrEmpty()) "Fish" else "${it.size} Selected"
                if (title.text == newTitle) {
                    return@Observer
                }
                ObjectAnimator.ofFloat(title, "translationY", 0F, toolbarHeight).apply {
                    duration = 200
                    addListener(
                        onEnd = {
                            title.text = newTitle
                            title.translationY = 0F
                            animation_title.visibility = View.GONE
                        }
                    )
                    start()
                }
                animation_title.text = newTitle
                ObjectAnimator.ofFloat(animation_title, "translationY", -toolbarHeight, 0F).apply {
                    duration = 200
                    addListener(
                        onEnd = {
                            animation_title.visibility = View.GONE
                        },
                        onStart = {
                            animation_title.translationX = -titleWidth
                            animation_title.visibility = View.VISIBLE
                            animation_title.text = newTitle
                        }
                    )
                    start()
                }
            }
        })
        val background =
            context?.resources?.getDrawable(R.drawable.toolbar_color_animation) as TransitionDrawable
        toolbar.background = background

        adapter = FishAdapter(viewModel)
        recycler_view.itemAnimator = DefaultItemAnimator()
        recycler_view.adapter = adapter
        val layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        recycler_view.layoutManager = layoutManager

        edit_mode.setOnClickListener {
            viewModel.editMode.value = !viewModel.editMode.value!!
            animateToolbarIcons(viewModel.editMode.value!!)
            if (viewModel.editMode.value == true) {
                background.startTransition(200)
            } else {
                background.reverseTransition(200)
                viewModel.clearSelected()
            }
        }

        donated.setOnClickListener {
            viewModel.toggleBookmark()
        }

        bookmark.setOnClickListener {
            viewModel.toggleFounded()
        }

        viewModel.active.observe(viewLifecycleOwner, Observer {
            active_summary.text = it
        })

        viewModel.found.observe(viewLifecycleOwner, Observer {
            founded_summary.text = it
        })

        viewModel.donated.observe(viewLifecycleOwner, Observer {
            collected_summary.text = it
        })

        viewModel.bookmarkAction.observe(viewLifecycleOwner, Observer {
            if (it == bookmark.isSelected)
                bookmark.morph()
        })

        viewModel.donateAction.observe(viewLifecycleOwner, Observer {
            if (it == donated.isSelected)
                donated.morph()
        })

        viewModel.editMode.observe(viewLifecycleOwner, Observer {

            adapter.editMode = it
            onBackPressedCallback.isEnabled = it
            /**
             * Animate All ViewHolder In Screen
             */
            val start = layoutManager.findFirstVisibleItemPosition()
            val end = layoutManager.findLastVisibleItemPosition()
            for (i in 0..recycler_view.childCount) {
                val child = recycler_view.getChildAt(i)
                if (child?.parent == null) {
                    continue
                } else {
                    val holder = recycler_view.getChildViewHolder(child) as FishAdapter.ViewHolder?;
                    holder?.animateChange(viewModel.editMode.value!!)
                }
            }
            adapter.notifyItemRangeChanged(0, start - 0)
            adapter.notifyItemRangeChanged(end, adapter.itemCount - end - 1)
        })

        viewModel.clickedFish.observe(viewLifecycleOwner, Observer { effectBottomSheet(it) })
    }

    /**
     * Animate The Toolbar 's Icon, Make Them Fade in and out
     */
    private fun animateToolbarIcons(visible: Boolean) {
        val target = if (visible) 1f else 0f
        arrayOf(bookmark, donated).forEachIndexed { i, view ->
            view.visibility = View.VISIBLE
            ObjectAnimator.ofFloat(view, "alpha", target)
                .apply {
                    duration = 200
                    startDelay = i.toLong() * 150
                    start()
                }
        }
    }

    var lastOffset = -1.0f

    private val listener = object : BottomSheetBehavior.BottomSheetCallback() {

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            val child = recycler_view
            if (child.startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL)) {
                val percent = slideOffset - lastOffset
                lastOffset = slideOffset
                Timber.e("$percent")
                child.nestedScrollBy(0, (percent * bottomSheet.height / 4).toInt())
            }
        }

        override fun onStateChanged(bottomSheet: View, newState: Int) {}
    }

    fun effectBottomSheet(fish: FishEntityMix?) {
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
}
