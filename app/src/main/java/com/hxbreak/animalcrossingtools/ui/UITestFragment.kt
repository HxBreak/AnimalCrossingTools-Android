package com.hxbreak.animalcrossingtools.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.paging.*
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.hxbreak.animalcrossingtools.GlideProgressCollector
import com.hxbreak.animalcrossingtools.data.prefs.DataUsageStorage
import com.hxbreak.animalcrossingtools.data.prefs.PreferenceStorage
import com.hxbreak.animalcrossingtools.data.source.DataRepository
import com.hxbreak.animalcrossingtools.data.source.entity.FishEntityMix
import com.hxbreak.animalcrossingtools.ui.fish.FishRemoteMediator
import com.hxbreak.animalcrossingtools.ui.user.UserListViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import timber.log.Timber

class TestViewModel @ViewModelInject constructor(
    val repository: DataRepository,
    val preferenceStorage: PreferenceStorage,
    private val dataUsageStorage: DataUsageStorage,
    val collector: GlideProgressCollector,
) : ViewModel() {

    val dao = repository.local().fishDao()

    val flow = flow<Int> {
        repeat(Int.MAX_VALUE){
            delay(1000L)
            emit(it)
        }
    }

    val paging = Pager(PagingConfig(Int.MAX_VALUE),
        0, FishRemoteMediator(repository)){
            repository.local().fishDao().paging()
        }.flow.mapLatest {
        val saved = dao.allFishSaved()
        it.map { entity ->
            FishEntityMix(entity, saved.firstOrNull { entity.id == it.id })
        }
    }
}

@AndroidEntryPoint
class UITestFragment : Fragment() {

    val viewModel2 by viewModels<UserListViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = SwipeRefreshLayout(requireContext())
        view.addView(RecyclerView(requireContext()))
        return view
    }

    inner class ViewHolder(val v: View) : RecyclerView.ViewHolder(v) {

    }

    val viewModel by viewModels<TestViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launchWhenStarted {
            val scope = CoroutineScope(SupervisorJob())
//            val conn = UdpConnection(scope, PeerConnectionTarget("192.168.0.104", 9999, 8999, "noid"))
//            val f = conn.connect()
//            f.addListener {
//                scope.launch {
//                    conn.loop()
//                }
//            }
            delay(1000L * 60 * 2)
            scope.cancel()
        }
//        val v = (requireView() as ViewGroup)[1] as RecyclerView
//        v.layoutManager = LinearLayoutManager(requireContext())
//        val adapter = CommonTypedAdapter(object : DiffUtil.ItemCallback<Any>() {
//            override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
//                return oldItem == newItem
//            }
//
//            override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
//                return oldItem == newItem
//            }
//        })
//        lifecycleScope.launch {
//            viewModel.paging.collectLatest {
//                Timber.e("Collected $it")
//                adapter.differ.submitData(it as PagingData<Any>)
//            }
//        }
//        v.adapter = adapter
//        adapter.register(object : ItemViewBinder<FishEntityMix, ViewHolder>() {
//            override fun onBindViewHolder(holder: ViewHolder, item: FishEntityMix) {
//                val tv = holder.v.findViewById<TextView>(android.R.id.text1)
//                tv.text = "${item.fish.name.nameCNzh}-${item.saved}"
//            }
//
//            override fun onCreateViewHolder(
//                inflater: LayoutInflater,
//                parent: ViewGroup
//            ): ViewHolder {
//                return ViewHolder(
//                    inflater.inflate(
//                        android.R.layout.simple_list_item_1,
//                        parent,
//                        false
//                    )
//                )
//            }
//        })
//        lifecycleScope.launchWhenCreated {
//            adapter.differ.loadStateFlow
//                // Only emit when REFRESH LoadState for RemoteMediator changes.
//                .distinctUntilChangedBy { it.refresh }
//                // Only react to cases where Remote REFRESH completes i.e., NotLoading.
//                .filter { it.refresh is LoadState.NotLoading }
//                .collect { v.scrollToPosition(0) }
//        }
//        lifecycleScope.launchWhenCreated {
//            adapter.differ.loadStateFlow.collectLatest { loadStates ->
//                (view as SwipeRefreshLayout).isRefreshing = loadStates.refresh is LoadState.Loading
//            }
//        }
    }
}