package com.hxbreak.animalcrossingtools.ui.houseware.detail

import android.os.Bundle
import android.view.*
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.transition.MaterialSharedAxis
import com.hxbreak.animalcrossingtools.R
import com.hxbreak.animalcrossingtools.adapter.LightAdapter
import com.hxbreak.animalcrossingtools.adapter.Typer
import com.hxbreak.animalcrossingtools.i18n.toLocaleName
import com.hxbreak.animalcrossingtools.ui.BackAbleAppbarFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_houseware_detail.*
import javax.inject.Inject

@AndroidEntryPoint
class HousewareDetailFragment : BackAbleAppbarFragment(){

    override fun configSupportActionBar() = true

    private var adapter: LightAdapter? = null
    private fun requireAdapter() = adapter ?: error("adapter == null")

    private val args by navArgs<HousewareDetailFragmentArgs>()

    @Inject
    lateinit var viewModelFactory: HousewareDetailViewModel.AssistedFactory
    private val viewModel by viewModels<HousewareDetailViewModel>(factoryProducer = {
            HousewareDetailViewModel.provideFactory(viewModelFactory, args.filename, args.housewareId)
        })

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = LightAdapter()
        viewModel.item.observe(viewLifecycleOwner){
            requireToolbarTitle().setText("$it")
        }
        viewModel.items.observe(viewLifecycleOwner){
            it.firstOrNull()?.let {
                item_title.text = it.name.toLocaleName(viewModel.locale)
                return@observe
            }
            nav.navigateUp()
        }
    }

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
        return inflater.inflate(R.layout.fragment_houseware_detail, container, false)
    }
}
