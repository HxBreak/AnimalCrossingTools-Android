package com.hxbreak.animalcrossingtools.ui.houseware.detail

import android.os.Bundle
import android.view.*
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.transition.MaterialSharedAxis
import com.hxbreak.animalcrossingtools.GlideApp
import com.hxbreak.animalcrossingtools.R
import com.hxbreak.animalcrossingtools.adapter.ItemComparable
import com.hxbreak.animalcrossingtools.adapter.ItemViewDelegate
import com.hxbreak.animalcrossingtools.adapter.LightAdapter
import com.hxbreak.animalcrossingtools.data.source.entity.HousewareEntity
import com.hxbreak.animalcrossingtools.extensions.littleCircleWaitAnimation
import com.hxbreak.animalcrossingtools.i18n.toLocaleName
import com.hxbreak.animalcrossingtools.ui.BackAbleAppbarFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.fragment_houseware_detail.*
import kotlinx.android.synthetic.main.page_simple_info_houseware.*
import javax.inject.Inject

@AndroidEntryPoint
class HousewareDetailFragment : BackAbleAppbarFragment(){

    private var adapter: LightAdapter? = null
    private var viewPagerAdapter: LightAdapter? = null
    private fun requireAdapter() = adapter ?: error("adapter == null")
    private var chipAdapter: LightAdapter? = null

    private val args by navArgs<HousewareDetailFragmentArgs>()

    @Inject
    lateinit var viewModelFactory: HousewareDetailViewModel.AssistedFactory
    private val viewModel by viewModels<HousewareDetailViewModel>(factoryProducer = {
            HousewareDetailViewModel.provideFactory(viewModelFactory, args.filename, args.housewareId)
        })

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = LightAdapter()
        chipAdapter = LightAdapter().also {
            it.register(ChipViewBinder())
        }
        viewPagerAdapter = LightAdapter().also {
            it.register(HousewareDetailHeaderViewBinder())
        }
        viewpager.adapter = viewPagerAdapter
        chip_group.layoutManager = FlexboxLayoutManager(requireContext(), FlexDirection.ROW, FlexWrap.WRAP)
        chip_group.adapter = chipAdapter
        viewModel.items.observe(viewLifecycleOwner){ entities ->
            entities.firstOrNull()?.let {
                viewPagerAdapter?.submitList(entities)
                val localName = it.name.toLocaleName(viewModel.locale)
                item_title.text = localName
                requireToolbarTitle().setText(localName)
                val list = mutableListOf<ChipData>(
                    ChipData("tag", it.tag),
                    ChipData("seriesId", it.seriesId),
                ).apply {
                    it.hhaConcept1?.let {
                        add(ChipData("hhaConcept1", it))
                    }
                    it.hhaConcept2?.let {
                        add(ChipData("hhaConcept2", it))
                    }
                }
                if (it.buyPrice > 0){
                    buyPrice.text = "\$${it.buyPrice}"
                }
                val line1 = res.getString(R.string.source, it.source)
                if (it.sourceDetail.isBlank()){
                    source.text = line1
                }else{
                    source.text = String.format("%s\n%s", line1, res.getString(R.string.source_detail, it.sourceDetail))
                }
                item_size.text = res.getString(R.string.houseware_size, it.size)
                chipAdapter?.submitList(list as List<ItemComparable<*>>)
                return@observe
            }

            nav.navigateUp()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        val forward = MaterialSharedAxis(MaterialSharedAxis.X, true)
//        enterTransition = forward
//        val backward = MaterialSharedAxis(MaterialSharedAxis.X, false)
//        returnTransition = backward
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapter = null
        chipAdapter = null
        viewPagerAdapter = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_houseware_detail, container, false)
    }
}

data class ChipData(
    val id: String,
    val text: String,
): ItemComparable<String>{
    override fun id() = id
}

class ChipViewBinder : ItemViewDelegate<ChipData, ChipViewBinder.ViewHolder>{

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        val chip = view as Chip
    }

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chip, parent, false))
    }

    override fun onBindViewHolder(data: ChipData?, vh: ViewHolder) {
        data?.let {
            vh.chip.text = it.text
        }
    }
}

class HousewareDetailHeaderViewBinder: ItemViewDelegate<HousewareEntity, HousewareDetailHeaderViewBinder.ViewHolder>{

    inner class ViewHolder(override val containerView: View): RecyclerView.ViewHolder(containerView), LayoutContainer{
        fun bind(entity: HousewareEntity) {
            GlideApp.with(image).load(entity.image_uri)
                .littleCircleWaitAnimation(containerView.context)
                .into(image)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.page_simple_info_houseware, parent, false)
        )
    }

    override fun onBindViewHolder(data: HousewareEntity?, vh: ViewHolder) {
        data?.let {
            vh.bind(it)
        }
    }
}