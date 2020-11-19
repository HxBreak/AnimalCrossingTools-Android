package com.hxbreak.animalcrossingtools.ui.art

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.transition.MaterialSharedAxis
import com.hxbreak.animalcrossingtools.R
import com.hxbreak.animalcrossingtools.adapter.SelectionAdapter
import com.hxbreak.animalcrossingtools.extensions.testChanged
import com.hxbreak.animalcrossingtools.ui.EditBackAbleAppbarFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_art.*
import java.lang.Exception

@AndroidEntryPoint
class ArtFragment : EditBackAbleAppbarFragment(){

    private val viewModel by viewModels<ArtViewModel>()

    var adapter: SelectionAdapter? = null

    private fun requireAdapter() = adapter ?: throw IllegalStateException("adapter is null")

    override val uiSelectModeMutableLiveData by lazy { viewModel.editMode }

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
        requireToolbarTitle().setText(res.getString(R.string.art_catalog))
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
                title.setText(res.getString(R.string.arts_catalog), it.inc)
            }else{
                title.setText(getString(R.string.numbers_of_art_select, it.collection.size), it.inc)
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
        viewModel.editMode.observe(viewLifecycleOwner){
            if (edit_mode.isSelected != it){ edit_mode.morph() }
            requireAdapter().editMode = it
            if (!it){ viewModel.clearSelected() }
        }
        viewModel.error.observe(viewLifecycleOwner){
            if (it is Exception){
                common_layout.setException(it){ viewModel.refresh.value = true }
            }else{
                common_layout.clearState()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapter = null
    }

    override fun animateIconList() = listOf(donate)
}