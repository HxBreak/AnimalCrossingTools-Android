package com.hxbreak.animalcrossingtools.ui.bugs

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
import com.hxbreak.animalcrossingtools.extensions.testChanged
import com.hxbreak.animalcrossingtools.ui.EditBackAbleAppbarFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_bugs.*
import java.lang.IllegalStateException
import javax.inject.Inject

@AndroidEntryPoint
class BugsFragment : EditBackAbleAppbarFragment(){

    private val viewModel by viewModels<BugsViewModel>()

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
        return inflater.inflate(R.layout.fragment_bugs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        edit_mode.setOnClickListener {
            uiSelectMode = !viewModel.editMode.value!!
        }
        viewModel.loading.observe(viewLifecycleOwner){
            refresh_layout.isRefreshing = it == true
        }
        refresh_layout.setOnRefreshListener { viewModel.refresh.postValue(false) }
        adapter = SelectionAdapter()
        requireAdapter().register(BugViewBinder(viewModel))
        viewModel.bugs.observe(viewLifecycleOwner){
            requireAdapter().submitList(it)
        }
        recycler_view.adapter = adapter
        recycler_view.layoutManager = LinearLayoutManager(requireContext())
        viewModel.selected.testChanged().observe(viewLifecycleOwner){
            val title = requireToolbarTitle()
            if (it.collection.isNullOrEmpty()){
                title.setText("Bugs", it.inc)
                title.clearLastSelected()
            }else{
                title.setText("${it.collection.size} Selected", it.inc)
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapter = null
    }

    override fun onUiSelectChanged(value: Boolean) {
        super.onUiSelectChanged(value)
        viewModel.editMode.value = value
        if (edit_mode.isSelected != value){ edit_mode.morph() }
        if (!value) viewModel.clearSelected()
        requireAdapter().editMode = value
    }

    override fun configSupportActionBar() = true

    override fun animateIconList() = listOf(donate, found)
}