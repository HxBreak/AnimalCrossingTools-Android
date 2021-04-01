package com.hxbreak.animalcrossingtools.ui.fish

import FishViewBinder
import android.animation.AnimatorSet
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.animation.doOnEnd
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.ItemKeyProvider
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.transition.FadeThroughProvider
import com.google.android.material.transition.MaterialSharedAxis
import com.hxbreak.animalcrossingtools.R
import com.hxbreak.animalcrossingtools.adapter.CommonItemComparableDiffUtil
import com.hxbreak.animalcrossingtools.adapter.SelectableTypedAdapter
import com.hxbreak.animalcrossingtools.components.selections.SelectionModeTrackerPredicate
import com.hxbreak.animalcrossingtools.data.source.entity.FishEntityMix
import com.hxbreak.animalcrossingtools.databinding.FragmentFishBinding
import com.hxbreak.animalcrossingtools.ui.EditBackAbleAppbarFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.system.exitProcess


@AndroidEntryPoint
class FishFragment : EditBackAbleAppbarFragment() {

    private val viewModel by viewModels<FishViewModel>()
    private var bottomSheetView: View? = null

    override val uiSelectModeMutableLiveData by lazy { viewModel.editMode }

    private var _binding: FragmentFishBinding? = null
    private val binding: FragmentFishBinding
        get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val forward = MaterialSharedAxis(MaterialSharedAxis.X, true)
        enterTransition = forward

        val backward = MaterialSharedAxis(MaterialSharedAxis.X, false)
        returnTransition = backward
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFishBinding.inflate(inflater, container, false)
        return binding.root
    }

    var tracker: SelectionTracker<Long>? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireToolbar().setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        val enableIndicator = viewModel.locale.language == "zh"
        requireToolbar().title = null
        requireToolbarTitle().setText(res.getString(R.string.fish_catalog))
        binding.recyclerView.mEnableAlphabet = false
        val adapter = SelectableTypedAdapter(CommonItemComparableDiffUtil)
        val provider = object : ItemKeyProvider<Long>(SCOPE_CACHED) {
            override fun getKey(position: Int): Long? {
                val entity = adapter.differ.peek(position) as? FishEntityMix
                return entity?.fish?.id?.toLong()
            }

            override fun getPosition(key: Long): Int {
                val index = adapter.differ.snapshot().items.indexOfFirst {
                    if (it is FishEntityMix) {
                        it.fish.id.toLong() == key
                    } else {
                        false
                    }
                }
                return if (index >= 0) {
                    index
                } else {
                    RecyclerView.NO_POSITION
                }
            }
        }

        val lookup = object : ItemDetailsLookup<Long>() {
            override fun getItemDetails(e: MotionEvent): ItemDetails<Long>? {
                val v = binding.recyclerView.findChildViewUnder(e.x, e.y) ?: return null
                val vh = binding.recyclerView.getChildViewHolder(v)
                if (vh is FishViewBinder.ViewBinder) {
                    return vh.detail()
                }
                return null
            }
        }
        binding.recyclerView.adapter = adapter
        tracker = SelectionTracker.Builder(
            "selection",
            binding.recyclerView,
            provider,
            lookup,
            StorageStrategy.createLongStorage()
        ).withSelectionPredicate(SelectionModeTrackerPredicate(viewModel.editMode)).build()
        adapter.register(FishViewBinder(viewModel, tracker!!))
        tracker?.addObserver(object : SelectionTracker.SelectionObserver<Long>() {
            override fun onItemStateChanged(key: Long, selected: Boolean) {
                viewModel.selectFish(key, selected)
            }
        })
        binding.refreshLayout.setOnRefreshListener {
            adapter.differ.refresh()
        }
        lifecycleScope.launchWhenStarted {
            viewModel.selectedIds.collectLatest {
                val enable = !it.isNullOrEmpty()
                arrayOf(binding.donate, binding.found).forEach {
                    it.isEnabled = enable
                }
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.selectedIds.reduce { accumulator, value ->
                val direction = accumulator.size > value.size
                if (value.isEmpty()) {
                    requireToolbarTitle().setText(getString(R.string.fish_catalog), direction)
                } else {
                    requireToolbarTitle().setText(
                        getString(
                            R.string.numbers_of_fish_select,
                            value.size
                        ), direction
                    )
                }
                value
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.pagingFish.collectLatest {
                adapter.differ.submitData(it as PagingData<Any>)
            }
        }

        lifecycleScope.launchWhenStarted {
            adapter.differ.loadStateFlow.collectLatest { loadStates ->
                binding.refreshLayout.isRefreshing = loadStates.refresh is LoadState.Loading
            }
        }

        lifecycleScope.launchWhenStarted {
            adapter.differ.loadStateFlow
                .distinctUntilChanged()
                .collect {
                    val refresh = it.refresh
                    if (refresh is LoadState.Error) {
                        binding.commonLayout.setException(refresh.error) {
                            adapter.differ.retry()
                        }
                    } else if (refresh is LoadState.NotLoading
                        && ((it.mediator?.refresh ?: refresh) is LoadState.NotLoading)
                        && (it.source.refresh.endOfPaginationReached)
                        && adapter.differ.snapshot().items.isEmpty()) {
                        binding.commonLayout.setEmpty()
                    } else {
                        binding.commonLayout.clearState()
                    }
                }
        }
        binding.editMode.setOnClickListener {
            uiSelectMode = !uiSelectMode
        }

        binding.donate.setOnClickListener {
            tracker?.selection?.let {
                val ids = it.toList()
                if (ids.isNotEmpty()) {
                    viewModel.toggleDonate(ids)
                }
            }
        }

        binding.found.setOnClickListener {
            tracker?.selection?.let {
                val ids = it.toList()
                if (ids.isNotEmpty()) {
                    viewModel.toggleFounded(ids)
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.active.collectLatest {
                binding.activeSummary.text = it
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.donated.collectLatest {
                binding.donatedSummary.text = it
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.found.collectLatest {
                binding.foundedSummary.text = it
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.donateAction.collectLatest {
                if (it == binding.donate.isSelected) {
                    binding.donate.morph()
                }
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.foundAction.collectLatest {
                if (it == binding.found.isSelected) {
                    binding.found.morph()
                }
            }
        }

        viewModel.editMode.observe(viewLifecycleOwner) {
            fadeContentWithAnimation {
                adapter.mode = it
            }
            if (binding.editMode.isSelected != it) {
                binding.editMode.morph()
            }
            if (it == false) {
                tracker?.clearSelection()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        tracker?.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        tracker?.onRestoreInstanceState(savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun fadeContentWithAnimation(disappearBlock: () -> Unit) {
        val s = FadeThroughProvider()
        val disappear = s.createDisappear(binding.recyclerView, binding.recyclerView)
        val appear = s.createAppear(binding.recyclerView, binding.recyclerView)
        val set = AnimatorSet()
        set.duration = 200
        set.playSequentially(disappear, appear)
        disappear?.doOnEnd { _ ->
            disappearBlock()
        }
        set.start()
    }

    override fun animateIconList() = listOf(binding.donate, binding.found)
}
