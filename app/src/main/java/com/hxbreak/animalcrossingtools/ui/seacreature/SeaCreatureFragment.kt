package com.hxbreak.animalcrossingtools.ui.seacreature

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
import kotlinx.android.synthetic.main.fragment_seacreature.*
import java.lang.Exception
import java.lang.IllegalStateException

@AndroidEntryPoint
class SeaCreatureFragment : EditBackAbleAppbarFragment(){

    private val viewModel by viewModels<SeaCreatureViewModel>()

    override val uiSelectModeMutableLiveData: MutableLiveData<Boolean> by lazy { viewModel.editMode }

    private var adapter: SelectionAdapter? = null

    private fun requireAdapter() = adapter ?: throw IllegalStateException("adapter == null")

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
        return inflater.inflate(R.layout.fragment_seacreature, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arrayOf(donate, found).forEach { it.alpha = 0f }
        edit_mode.setOnClickListener {
            uiSelectMode = !viewModel.editMode.value!!
        }
        viewModel.loading.observe(viewLifecycleOwner){
            refresh_layout.isRefreshing = it == true
        }
        refresh_layout.setOnRefreshListener { viewModel.refresh.postValue(false) }
        adapter = SelectionAdapter()
        requireAdapter().register(SeaCreatureViewBinder(viewModel))
        viewModel.seacreatures.observe(viewLifecycleOwner){
            requireAdapter().submitList(it)
        }
        recycler_view.adapter = adapter
        recycler_view.layoutManager = LinearLayoutManager(requireContext())
        viewModel.selected.testChanged().observe(viewLifecycleOwner){
            val title = requireToolbarTitle()
            if (it.collection.isNullOrEmpty()){
                title.setText(getString(R.string.sea_creature_catalog), it.inc)
                title.clearLastSelected()
            }else{
                title.setText(getString(R.string.numbers_of_seacreature_select, it.collection.size), it.inc)
            }
        }
        viewModel.selected.observe(viewLifecycleOwner){ selected ->
            animateIconList().forEach { it.isEnabled = !selected.isNullOrEmpty() }
        }
        viewModel.donateAction.observe(viewLifecycleOwner){
            if (donate.isSelected == it) donate.morph()
        }
        viewModel.ownAction.observe(viewLifecycleOwner){
            if (found.isSelected == it) found.morph()
        }
        found.setOnClickListener { viewModel.toggleOwn() }
        donate.setOnClickListener { viewModel.toggleDonate() }
        viewModel.donate.observe(viewLifecycleOwner){ donated_summary.text = it }
        viewModel.found.observe(viewLifecycleOwner){ founded_summary.text = it }
        viewModel.activies.observe(viewLifecycleOwner){ active_summary.text = it }
        viewModel.editMode.observe(viewLifecycleOwner){
            if (edit_mode.isSelected != it){ edit_mode.morph() }
            if (!it) viewModel.clearSelected()
            requireAdapter().editMode = it
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

    override fun animateIconList() = listOf(donate, found)
}