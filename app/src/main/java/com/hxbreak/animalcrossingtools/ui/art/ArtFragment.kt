package com.hxbreak.animalcrossingtools.ui.art

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.transition.MaterialSharedAxis
import com.hxbreak.animalcrossingtools.R
import com.hxbreak.animalcrossingtools.adapter.SelectionAdapter
import com.hxbreak.animalcrossingtools.di.DiViewModelFactory
import com.hxbreak.animalcrossingtools.extensions.testChanged
import com.hxbreak.animalcrossingtools.ui.EditBackAbleAppbarFragment
import kotlinx.android.synthetic.main.fragment_art.*
import javax.inject.Inject

class ArtFragment : EditBackAbleAppbarFragment(){

    @Inject
    lateinit var viewModelFactory: DiViewModelFactory

    private val viewModel by viewModels<ArtViewModel> { viewModelFactory }

    var adapter: SelectionAdapter? = null

    private fun requireAdapter() = adapter ?: throw IllegalStateException("adapter is null")

    override fun configSupportActionBar() = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val forward = MaterialSharedAxis(MaterialSharedAxis.X, true)
        enterTransition = forward
        val backward = MaterialSharedAxis(MaterialSharedAxis.X, false)
        returnTransition = backward
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_art, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireToolbar().title = ""
        requireToolbarTitle().setText("Art")
        adapter = SelectionAdapter()
        requireAdapter().register(ArtViewBinder(viewModel))
        recycler_view.adapter = adapter
        recycler_view.layoutManager = LinearLayoutManager(requireContext())

        viewModel.loading.observe(viewLifecycleOwner){
            refresh_layout.isRefreshing = it == true
        }
        viewModel.arts.observe(viewLifecycleOwner){
            requireAdapter().submitList(it)
        }
        refresh_layout.setOnRefreshListener { viewModel.refresh.value = true }
        edit_mode.setOnClickListener {
            uiSelectMode = !viewModel.editMode.value!!
        }
        viewModel.selected.observe(viewLifecycleOwner){
            donate.isEnabled = !it.isNullOrEmpty()
        }
        viewModel.selected.testChanged().observe(viewLifecycleOwner){
            val title = requireToolbarTitle()
            if (it.collection.isNullOrEmpty()){
                title.clearLastSelected()
                title.setText("Arts", it.inc)
            }else{
                title.setText("${it.collection.size} Piece${if (it.collection.size > 1) "s" else ""} Of Art Selected", it.inc)
            }
        }
        viewModel.ownAction.observe(viewLifecycleOwner){
            if (it == donate.isSelected){
                donate.morph()
            }
        }
        donate.setOnClickListener { viewModel.toggleOwnArt() }
        viewModel.collectedText.observe(viewLifecycleOwner){
            donated_summary.text = it
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapter = null
    }

    override fun onUiSelectChanged(value: Boolean) {
        super.onUiSelectChanged(value)
        viewModel.editMode.value = value
        if (edit_mode.isSelected != value){ edit_mode.morph() }
        requireAdapter().editMode = value
        if (!value){ viewModel.clearSelected() }
    }

    override fun animateIconList() = listOf(donate)
}