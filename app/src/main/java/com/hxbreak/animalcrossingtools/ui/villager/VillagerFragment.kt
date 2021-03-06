package com.hxbreak.animalcrossingtools.ui.villager

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.transition.TransitionSet
import com.google.android.material.transition.MaterialArcMotion
import com.google.android.material.transition.MaterialContainerTransform
import com.google.android.material.transition.MaterialSharedAxis
import com.hxbreak.animalcrossingtools.R
import com.hxbreak.animalcrossingtools.adapter.CommonItemDecoration
import com.hxbreak.animalcrossingtools.adapter.SelectionAdapter
import com.hxbreak.animalcrossingtools.ui.BackAbleAppbarFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_villager.*
import java.lang.Exception

@AndroidEntryPoint
class VillagerFragment : BackAbleAppbarFragment(){

    private val viewModel by viewModels<VillagerViewModel>()

    private val transitionSet = TransitionSet()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val forward = MaterialSharedAxis(MaterialSharedAxis.X, true)
        enterTransition = forward

        val backward = MaterialSharedAxis(MaterialSharedAxis.X, false)
        returnTransition = backward
        val transform = MaterialContainerTransform()
        transform.scrimColor = Color.TRANSPARENT
        transform.setPathMotion(MaterialArcMotion())
        transitionSet.duration = 300
        transitionSet.addTransition(transform)
        sharedElementEnterTransition = transitionSet
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_villager, container, false)
    }

    var adapter: SelectionAdapter? = null

    fun requireAdapter() = adapter ?: throw IllegalStateException("adapter is null")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireToolbar().title = ""
        requireToolbarTitle().setText(getString(R.string.villager_catalog))
        adapter = SelectionAdapter()
        viewModel.loading.observe(viewLifecycleOwner){
            refresh_layout.isRefreshing = it == true
        }
        refresh_layout.setOnRefreshListener { viewModel.refresh.value = true }
        viewModel.error.observe(viewLifecycleOwner) {
            if (it is Exception){
                common_layout.setException(it){ viewModel.refresh.value = true }
            }else{
                common_layout.clearState()
            }
        }
        requireAdapter().register(VillagerViewBinder(viewModel, viewLifecycleOwner))
        viewModel.villagers.observe(viewLifecycleOwner) {
            requireAdapter().submitList(it)
        }

        recycler_view.layoutManager = StaggeredGridLayoutManager(3, GridLayoutManager.VERTICAL)
        recycler_view.adapter = adapter
        recycler_view.addItemDecoration(CommonItemDecoration(requireContext()))
    }

}