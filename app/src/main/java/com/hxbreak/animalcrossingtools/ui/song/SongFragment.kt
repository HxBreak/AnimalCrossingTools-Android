package com.hxbreak.animalcrossingtools.ui.song

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.addListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager

import com.hxbreak.animalcrossingtools.R
import com.hxbreak.animalcrossingtools.di.DiViewModelFactory
import com.hxbreak.animalcrossingtools.fragment.EventObserver
import com.hxbreak.animalcrossingtools.ui.EditBackAbleAppbarFragment
import kotlinx.android.synthetic.main.fragment_song.*
import javax.inject.Inject

class SongFragment : EditBackAbleAppbarFragment() {

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

    var adapter: SongAdapter? = null

    override fun onUiSelectChanged(value: Boolean) {
        super.onUiSelectChanged(value)
        viewModel.editMode.value = value
        animateToolbarIcons(viewModel.editMode.value!!)
        if (edit_mode.isSelected != value){ edit_mode.morph() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapter = null
    }

    override fun configSupportActionBar() = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        adapter = SongAdapter(viewModel)
        postponeEnterTransition()
        requireToolbar().title = ""
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
        edit_mode.setOnClickListener {
            viewModel.editMode.value = !viewModel.editMode.value!!
            uiSelectMode = viewModel.editMode.value!!
            if (viewModel.editMode.value == false) {
                viewModel.clearSelected()
            }
        }
        viewModel.editMode.observe(viewLifecycleOwner, Observer {
            adapter?.editMode = it
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
            toolbarTitle.let { title ->
                val toolbarHeight = requireToolbar().measuredHeight.toFloat()
                val titleWidth = requireToolbarTitle().measuredWidth.toFloat()
                val newTitle = if (it.isNullOrEmpty()) "CD" else "${it.size} 张被选中"
                if (requireToolbarTitle().text == newTitle) {
                    return@Observer
                }
                ObjectAnimator.ofFloat(title, "translationY", 0F, toolbarHeight).apply {
                    duration = 200
                    addListener(
                        onEnd = {
                            requireToolbarTitle().text = newTitle
                            requireToolbarTitle().translationY = 0F
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
