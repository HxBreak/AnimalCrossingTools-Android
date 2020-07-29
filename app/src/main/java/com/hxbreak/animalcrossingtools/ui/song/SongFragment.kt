package com.hxbreak.animalcrossingtools.ui.song

import android.animation.ObjectAnimator
import android.graphics.drawable.TransitionDrawable
import android.os.Build
import android.os.Bundle
import android.transition.TransitionInflater
import android.view.*
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.addListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDestination
import androidx.navigation.NavDestinationBuilder
import androidx.navigation.NavDirections
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.transition.Hold
import com.google.android.material.transition.MaterialSharedAxis
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback

import com.hxbreak.animalcrossingtools.R
import com.hxbreak.animalcrossingtools.di.DiViewModelFactory
import com.hxbreak.animalcrossingtools.fragment.EventObserver
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_song.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class SongFragment : DaggerFragment() {

    @Inject
    lateinit var viewModelFactory: DiViewModelFactory

    private val viewModel by viewModels<SongViewModel> { viewModelFactory }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_song, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    var adapter: SongAdapter? = null

    private val onBackPressedCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            isEnabled = false
            edit_mode.callOnClick()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapter = null
        (requireActivity() as AppCompatActivity).setSupportActionBar(null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            if (viewModel.editMode.value == true) {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            } else {
                findNavController().navigateUp()
            }
        }
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        adapter = SongAdapter(viewModel)
        postponeEnterTransition()
        toolbar.title = ""
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            onBackPressedCallback
        )
        recycler_view.adapter = adapter
        recycler_view.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        viewModel.items.observe(viewLifecycleOwner, Observer {
            adapter?.submitList(it)
        })
        viewModel.loading.observe(viewLifecycleOwner, Observer {
            refresh_layout.isRefreshing = it == true
        })
        refresh_layout.setOnRefreshListener {
            viewModel.refresh.value = true
        }
        recycler_view.post {
            startPostponedEnterTransition()
        }
        val background =
            context?.resources?.getDrawable(R.drawable.toolbar_color_animation) as TransitionDrawable
        toolbar.background = background
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
        viewModel.editMode.observe(viewLifecycleOwner, Observer {
            adapter?.editMode = it
            onBackPressedCallback.isEnabled = it
            /**
             * Animate All ViewHolder In Screen
             */
            val layoutManager = recycler_view.layoutManager
            if (layoutManager is LinearLayoutManager) {
                val start = layoutManager.findFirstVisibleItemPosition()
                val end = layoutManager.findLastVisibleItemPosition()
                for (i in 0..recycler_view.childCount) {
                    val child = recycler_view.getChildAt(i)
                    if (child?.parent == null) {
                        continue
                    } else {
                        val holder =
                            recycler_view.getChildViewHolder(child) as SongAdapter.ViewHolder?;
                        holder?.animateChange(viewModel.editMode.value!!)
                    }
                }
                adapter?.notifyItemRangeChanged(0, start - 0)
                adapter?.notifyItemRangeChanged(end, adapter!!.itemCount - end - 1)
            } else {
                adapter?.notifyDataSetChanged()
            }
        })
        viewModel.ownAction.observe(viewLifecycleOwner, Observer {
            if (bookmark.isSelected == it)
                bookmark.morph()
        })
        bookmark.setOnClickListener {
            viewModel.toggleOwnSong()
        }

        viewModel.selected.observe(viewLifecycleOwner, Observer {
            bookmark.isEnabled = !it.isNullOrEmpty()
        })

        viewModel.erro.observe(viewLifecycleOwner, EventObserver {
            Toast.makeText(requireContext(), "$it", Toast.LENGTH_SHORT).show()
        })

        viewModel.selected.observe(viewLifecycleOwner, Observer {
            title.let { title ->
                val toolbarHeight = toolbar.measuredHeight.toFloat()
                val titleWidth = title.measuredWidth.toFloat()
                val newTitle = if (it.isNullOrEmpty()) "CD" else "${it.size} 张被选中"
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

        arrayOf(textView2, textView3, founded_summary, active_summary).forEach {
            it.visibility = View.INVISIBLE
        }

        textView.text = "已收集/全部"
        viewModel.collectedText.observe(viewLifecycleOwner, Observer {
            collected_summary.text = it
        })

        viewModel.lunchNowPlayingEvent.observe(viewLifecycleOwner, EventObserver {
            it.let {
                it.get()?.let {
                    val extras = FragmentNavigatorExtras(
                        it.first.retrieve("root") to "container"
                        ,
                        it.first.retrieve("image") to it.second.imageTransitionName(),
                        it.first.retrieve("title") to it.second.titleTransitionName()
                    )

                    findNavController().navigate(
                        SongFragmentDirections.actionSongFragmentToMusicPlayFragment(
                            it.second
                        ), extras
                    )
                }
            }
        })
    }

    private fun animateToolbarIcons(visible: Boolean) {
        val target = if (visible) 1f else 0f
        arrayOf(bookmark).forEachIndexed { i, view ->
            view.visibility = View.VISIBLE
            ObjectAnimator.ofFloat(view, "alpha", target)
                .apply {
                    duration = 200
                    startDelay = i.toLong() * 150
                    start()
                }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_sort_type) {
            viewModel.sort = !viewModel.sort
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.song_fragment_menu, menu)
    }
}
