package com.hxbreak.animalcrossingtools.ui.houseware

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialSharedAxis
import com.hxbreak.animalcrossingtools.GlideApp
import com.hxbreak.animalcrossingtools.R
import com.hxbreak.animalcrossingtools.adapter.ItemViewDelegate
import com.hxbreak.animalcrossingtools.adapter.LightAdapter
import com.hxbreak.animalcrossingtools.adapter.Typer
import com.hxbreak.animalcrossingtools.data.source.entity.HousewareEntity
import com.hxbreak.animalcrossingtools.extensions.littleCircleWaitAnimation
import com.hxbreak.animalcrossingtools.ui.EditBackAbleAppbarFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.fragment_houseware.*
import kotlinx.android.synthetic.main.item_houseware_item.*
import kotlinx.android.synthetic.main.item_housewares_variants.*
import timber.log.Timber

@AndroidEntryPoint
class HousewaresFragment : EditBackAbleAppbarFragment(){

    private val viewModel by viewModels<HousewaresViewModel>()

    override fun configSupportActionBar() = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = LightAdapter()
        recycler_view.layoutManager = LinearLayoutManager(requireContext())
        recycler_view.adapter = adapter
        val typer = Typer()
        typer.register(HousewareItemViewBinder())
        val recycledViewPool = RecyclerView.RecycledViewPool()
        requireAdapter().register(HousewaresViewBinder(typer, recycledViewPool))
        viewModel.housewares.observe(viewLifecycleOwner){
            requireAdapter().submitList(it)
        }
        viewModel.loading.observe(viewLifecycleOwner){
            refresh_layout.isRefreshing = it == true
        }
        refresh_layout.setOnRefreshListener { viewModel.refresh.value = true }
        requireToolbarTitle().setText("Housewares")
        viewModel.error.observe(viewLifecycleOwner){
            Timber.e(it)
            Snackbar.make(requireView(), "Error $it", Snackbar.LENGTH_LONG)
                .setAction("Retry"){
                    viewModel.refresh.value = true
                }
                .show()
        }
    }

    private var adapter: LightAdapter? = null
    private fun requireAdapter() = adapter ?: throw Exception()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val forward = MaterialSharedAxis(MaterialSharedAxis.X, true)
        enterTransition = forward
        val backward = MaterialSharedAxis(MaterialSharedAxis.X, false)
        returnTransition = backward
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapter = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_houseware, container, false)
    }
}

class HousewareItemViewBinder: ItemViewDelegate<HousewareEntity, HousewareItemViewBinder.ViewHolder>{

    inner class ViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer{
        fun bind(entity: HousewareEntity) {
            GlideApp.with(houseware_image)
                .load(entity.image_uri)
                .littleCircleWaitAnimation(containerView.context)
                .into(houseware_image)
            houseware_name.text = entity.variant
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup) = ViewHolder(LayoutInflater.from(parent.context)
        .inflate(R.layout.item_houseware_item, parent, false))

    override fun onBindViewHolder(data: HousewareEntity?, vh: ViewHolder) {
        data?.let { vh.bind(it) }
    }
}

class HousewaresViewBinder(val typer: Typer,val recycledViewPool: RecyclerView.RecycledViewPool) : ItemViewDelegate<HousewareVariants, HousewaresViewBinder.ViewHolder>{

    inner class ViewHolder(override val containerView: View): RecyclerView.ViewHolder(containerView), LayoutContainer {
        val adapter = LightAdapter(typer)

        fun bind(housewares: HousewareVariants){
            captain.text = housewares.variants.first().name.nameCNzh
            item_recycler_view.setRecycledViewPool(recycledViewPool)
            item_recycler_view.adapter = adapter
            adapter.submitList(housewares.variants)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup) = ViewHolder(LayoutInflater.from(parent.context)
        .inflate(R.layout.item_housewares_variants, parent, false))


    override fun onBindViewHolder(data: HousewareVariants?, vh: ViewHolder) {
        data?.let { vh.bind(data) }
    }
}