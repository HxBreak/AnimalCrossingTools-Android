package com.hxbreak.animalcrossingtools.ui.song

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.transition.MaterialElevationScale
import com.google.android.material.transition.MaterialSharedAxis

import com.hxbreak.animalcrossingtools.R
import com.hxbreak.animalcrossingtools.adapter.SelectionAdapter
import com.hxbreak.animalcrossingtools.extensions.testChanged
import com.hxbreak.animalcrossingtools.fragment.EventObserver
import com.hxbreak.animalcrossingtools.ui.EditBackAbleAppbarFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_song.*
import java.lang.Exception

@AndroidEntryPoint
class SongFragment : EditBackAbleAppbarFragment() {

    private val viewModel by viewModels<SongViewModel>()

    override val uiSelectModeMutableLiveData: MutableLiveData<Boolean> by lazy { viewModel.editMode }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_song, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        val forward = MaterialSharedAxis(MaterialSharedAxis.X, true)
        enterTransition = forward
        val backward = MaterialSharedAxis(MaterialSharedAxis.X, false)
        returnTransition = backward
        val exit = MaterialElevationScale(false).apply {
            duration = 300
        }
        exitTransition = exit
        val reenter = MaterialElevationScale(true).apply {
            duration = 300
        }
        reenterTransition = reenter
    }

    var adapter: SelectionAdapter? = null

    private fun requireAdapter() = adapter ?: throw IllegalStateException("adapter == null")

    override fun onDestroyView() {
        super.onDestroyView()
        adapter = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        recycler_view.doOnPreDraw { startPostponedEnterTransition() }
        adapter = SelectionAdapter()
        requireAdapter().register(SongViewBinder(viewModel))
        requireToolbar().title = ""
        recycler_view.adapter = adapter
        recycler_view.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        viewModel.items.observe(viewLifecycleOwner) {
            requireAdapter().submitList(it)
        }
        viewModel.loading.observe(viewLifecycleOwner) {
            refresh_layout.isRefreshing = it == true
        }
        refresh_layout.setOnRefreshListener {
            viewModel.refresh.value = true
        }
        edit_mode.setOnClickListener {
            uiSelectMode = !uiSelectMode
        }
        viewModel.editMode.observe(viewLifecycleOwner) {
            adapter?.editMode = it
        }
        viewModel.ownAction.observe(viewLifecycleOwner, Observer {
            if (donate.isSelected == it)
                donate.morph()
        })
        donate.setOnClickListener {
            viewModel.toggleOwnSong()
        }

        viewModel.selected.observe(viewLifecycleOwner) {
            donate.isEnabled = !it.isNullOrEmpty()
        }

        viewModel.error.observe(viewLifecycleOwner){
            if(it is Exception){
                common_layout.setException(it){ viewModel.refresh.value = true }
            }else{
                common_layout.clearState()
            }
        }

        viewModel.selected.testChanged().observe(viewLifecycleOwner){
            if (it.collection.isNullOrEmpty()){
                requireToolbarTitle().clearLastSelected()
                requireToolbarTitle().setText(getString(R.string.song_catalog), it.inc)
            }else{
                requireToolbarTitle().setText(getString(R.string.numbers_of_song_select, it.collection.size), it.inc)
            }
        }

        arrayOf(textView2, textView3, founded_summary, active_summary).forEach {
            it.visibility = View.INVISIBLE
        }

        textView.text = getString(R.string.collected_slash_all)
        viewModel.collectedText.observe(viewLifecycleOwner) {
            donated_summary.text = it
        }

        viewModel.lunchNowPlayingEvent.observe(viewLifecycleOwner, EventObserver {
            it.let {
                it.get()?.let {
                    val extras = FragmentNavigatorExtras(
                        it.second.root to "container",
                        it.second.image to it.first.imageTransitionName(),
//                        it.second.title to it.first.titleTransitionName(),
                    )
                    val action = SongFragmentDirections.actionSongFragmentToMusicPlayFragment(it.first)
                    nav.navigate(action, extras)
                }
            }
        })
        viewModel.editMode.observe(viewLifecycleOwner){
            if (edit_mode.isSelected != it){ edit_mode.morph() }
            if (!it){ viewModel.clearSelected() }
        }
    }

    override fun animateIconList() = listOf(donate)

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

internal data class SongItemView(
    val root: View,
    val title: View,
    val image: View
)