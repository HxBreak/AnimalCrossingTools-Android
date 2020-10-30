package com.hxbreak.animalcrossingtools.ui.song

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.transition.MaterialElevationScale
import com.google.android.material.transition.MaterialSharedAxis

import com.hxbreak.animalcrossingtools.R
import com.hxbreak.animalcrossingtools.adapter.SelectionAdapter
import com.hxbreak.animalcrossingtools.extensions.testChanged
import com.hxbreak.animalcrossingtools.fragment.EventObserver
import com.hxbreak.animalcrossingtools.fragment.useOnce
import com.hxbreak.animalcrossingtools.ui.EditBackAbleAppbarFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_song.*
import javax.inject.Inject

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

    override fun configSupportActionBar() = true

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

        viewModel.erro.observe(viewLifecycleOwner, EventObserver {
            Toast.makeText(requireContext(), "$it", Toast.LENGTH_SHORT).show()
        })

        viewModel.selected.testChanged().observe(viewLifecycleOwner){
            if (it.collection.isNullOrEmpty()){
                requireToolbarTitle().clearLastSelected()
                requireToolbarTitle().setText("CD", it.inc)
            }else{
                requireToolbarTitle().setText("${it.collection.size} 张被选中", it.inc)
            }
        }

        arrayOf(textView2, textView3, founded_summary, active_summary).forEach {
            it.visibility = View.INVISIBLE
        }

        textView.text = "已收集/全部"
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
                    object : FragmentFactory(){
                        override fun instantiate(
                            classLoader: ClassLoader,
                            className: String
                        ): Fragment {
                            return super.instantiate(classLoader, className)
                        }
                    }.useOnce(parentFragmentManager){
                        findNavController().navigate(
                            action.actionId, action.arguments, null, extras
                        )
                    }
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