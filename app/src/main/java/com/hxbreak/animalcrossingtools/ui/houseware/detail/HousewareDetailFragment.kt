package com.hxbreak.animalcrossingtools.ui.houseware.detail

import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import androidx.core.app.SharedElementCallback
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.doOnPreDraw
import androidx.core.view.get
import androidx.core.view.isGone
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.ChangeBounds
import androidx.transition.ChangeClipBounds
import androidx.transition.ChangeTransform
import androidx.transition.TransitionSet
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.transition.MaterialArcMotion
import com.google.android.material.transition.MaterialContainerTransform
import com.google.android.material.transition.MaterialSharedAxis
import com.hxbreak.animalcrossingtools.GlideApp
import com.hxbreak.animalcrossingtools.R
import com.hxbreak.animalcrossingtools.adapter.ItemComparable
import com.hxbreak.animalcrossingtools.adapter.ItemViewDelegate
import com.hxbreak.animalcrossingtools.adapter.LightAdapter
import com.hxbreak.animalcrossingtools.data.source.entity.HousewareEntity
import com.hxbreak.animalcrossingtools.extensions.littleCircleWaitAnimation
import com.hxbreak.animalcrossingtools.fragment.EventObserver
import com.hxbreak.animalcrossingtools.i18n.toLocaleName
import com.hxbreak.animalcrossingtools.ui.BackAbleAppbarFragment
import com.hxbreak.animalcrossingtools.ui.houseware.HousewaresFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.fragment_houseware_detail.*
import kotlinx.android.synthetic.main.page_simple_info_houseware.*
import timber.log.Timber
import java.util.concurrent.TimeUnit
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
        viewModel.current.observe(viewLifecycleOwner, EventObserver {
            viewpager.setCurrentItem(it, false)
            viewpager.doOnPreDraw { view ->
                (viewpager[0] as RecyclerView).layoutManager?.findViewByPosition(it)?.let {
                    ViewCompat.setTransitionName(it.findViewById(R.id.image), "${args.filename}-container")
                }

                setFragmentResult("onSelectFilenameVariant",
                    bundleOf(
                        HousewaresFragment.ARGUMENT_FILENAME to args.filename,
                    )
                )
            }
        })
        TabLayoutMediator(tab_layout, viewpager){ tab, position ->
            tab.text = "${viewModel.items.value?.get(position)?.variant ?: "UNKNOW"}"
        }.attach()
        tab_layout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.position?.let {
                    val item = viewModel.items.value?.get(it)
                    (viewpager[0] as RecyclerView).layoutManager?.findViewByPosition(it)?.let {
                        ViewCompat.setTransitionName(it.findViewById(R.id.image), "${item?.fileName}-container")
                    }
                    setFragmentResult("onSelectFilenameVariant",
                        bundleOf(
                            HousewaresFragment.ARGUMENT_FILENAME to item?.fileName,
                        )
                    )
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
        viewModel.items.observe(viewLifecycleOwner){ entities ->
            entities.firstOrNull()?.let {
                tab_layout.isGone = entities.size <= 1

                viewPagerAdapter?.submitList(entities)

                val localName = it.name.toLocaleName(viewModel.locale)
                item_title.text = localName
                requireToolbarTitle().setText(localName)
                val list = mutableListOf(
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
        val transitionSet = TransitionSet()
        val transform = MaterialContainerTransform()
        transform.addTarget(R.id.coordinator)
        transform.scrimColor = Color.RED
        transform.setPathMotion(MaterialArcMotion())
        transitionSet.duration = 300
        transitionSet.addTransition(ChangeClipBounds())
        transitionSet.addTransition(ChangeTransform())
        transitionSet.addTransition(ChangeBounds())

        sharedElementEnterTransition = transitionSet
        val forward = MaterialSharedAxis(MaterialSharedAxis.X, true)
        forward.excludeTarget(R.id.appbar, true)
        enterTransition = forward
        val backward = MaterialSharedAxis(MaterialSharedAxis.X, false)
        backward.excludeTarget(R.id.appbar, true)
        returnTransition = backward
        setEnterSharedElementCallback(object : SharedElementCallback() {
            override fun onMapSharedElements(
                names: MutableList<String>?,
                sharedElements: MutableMap<String, View>?
            ) {
                val view = (viewpager[0] as RecyclerView).layoutManager
                    ?.findViewByPosition(viewpager.currentItem)?.findViewById<ImageView>(R.id.image)
                val item = viewModel.items.value.orEmpty()[viewpager.currentItem]
                val s = "${item.fileName}-container"
                ViewCompat.setTransitionName(view!!, s)
                sharedElements?.put(names!!.getOrElse(0){ s }, view)
                Timber.e("$names, $sharedElements")
            }
        })
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
        postponeEnterTransition(200, TimeUnit.MILLISECONDS)
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