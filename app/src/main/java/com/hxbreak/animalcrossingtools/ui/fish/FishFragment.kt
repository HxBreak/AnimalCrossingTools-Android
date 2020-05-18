package com.hxbreak.animalcrossingtools.ui.fish

import android.animation.ObjectAnimator
import android.graphics.drawable.TransitionDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.animation.addListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hxbreak.animalcrossingtools.R
import com.hxbreak.animalcrossingtools.fragment.getViewModelFactory
import kotlinx.android.synthetic.main.fish_fragment.*


class FishFragment : Fragment() {

    private val viewModel by viewModels<FishViewModel> { getViewModelFactory() }
    private lateinit var adapter: FishAdapter

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
        viewModel.combinedLiveData.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
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
}
