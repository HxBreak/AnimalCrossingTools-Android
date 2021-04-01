package com.hxbreak.animalcrossingtools.ui.home

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.lifecycleScope
import androidx.navigation.*
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.transition.MaterialSharedAxis
import com.hxbreak.animalcrossingtools.R
import com.hxbreak.animalcrossingtools.adapter.ItemComparable
import com.hxbreak.animalcrossingtools.adapter.ItemViewDelegate
import com.hxbreak.animalcrossingtools.adapter.LightAdapter
import com.hxbreak.animalcrossingtools.data.Result
import com.hxbreak.animalcrossingtools.domain.home.LoadCachedFishUseCase
import com.hxbreak.animalcrossingtools.services.handler.InstantMessageController
import com.hxbreak.animalcrossingtools.ui.AppbarFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.android.synthetic.main.item_navigation_menu.*
import javax.inject.Inject

@AndroidEntryPoint
class MainFragment : AppbarFragment() {

    private var adapter: LightAdapter? = null
    private var messageAdapter: InstantMessageNotificationAdapter? = null

    @Inject
    lateinit var controller: InstantMessageController

    @Inject
    lateinit var loadCachedFishUseCase: LoadCachedFishUseCase

    private fun requireAdapter() = adapter ?: throw IllegalStateException("adapter == null")

    private val navigator by lazy {
        findNavController()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val backward = MaterialSharedAxis(MaterialSharedAxis.X, false)
        reenterTransition = backward

        val forward = MaterialSharedAxis(MaterialSharedAxis.X, true)
        exitTransition = forward

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    private val onNavigateToLobbyList = View.OnClickListener {
        nav.navigate(R.id.action_mainFragment_to_userListFragment)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        postponeEnterTransition()
        recycler_view.layoutManager = LinearLayoutManager(requireContext())
        adapter = LightAdapter()
        val viewBinder = NavigationMenuViewBinder {
            if (it.resId != null){
                navigator.navigate(it.resId, it.arguments, it.options, FragmentNavigatorExtras())
            } else if (it.direction != null){
                navigator.navigate(it.direction)
            }
        }
        val fishAdapter = LightAdapter()
        fishAdapter.register(viewBinder)

        messageAdapter = InstantMessageNotificationAdapter(controller, onNavigateToLobbyList)
        controller.authorized.observe(viewLifecycleOwner){
            messageAdapter?.notifyItemChanged(0)
        }
        controller.lobbyList.observe(viewLifecycleOwner){
            messageAdapter?.notifyItemChanged(0)
        }
        val cachedFish = CachedFishPreviewAdapter()

        recycler_view.adapter = ConcatAdapter(messageAdapter, fishAdapter, cachedFish, adapter)
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            when(val result = loadCachedFishUseCase(Unit)){
                is Result.Success -> {
                    cachedFish.list = result.data
                }
            }
        }
        requireAdapter().register(viewBinder)
        fishAdapter.submitList(listOf(
            NavigationMenu(getString(R.string.fish_catalog), R.id.action_mainFragment_to_fishFragment),
        ))
        requireAdapter().submitList(listOf(
            NavigationMenu(getString(R.string.bug_catalog), R.id.action_mainFragment_to_bugsFragment),
            NavigationMenu(getString(R.string.sea_creature_catalog), R.id.action_mainFragment_to_seaCreatureFragment),
            NavigationMenu(getString(R.string.fossil_catalog), R.id.action_mainFragment_to_fossilFragment),
            NavigationMenu(getString(R.string.song_catalog), R.id.action_mainFragment_to_songFragment),
            NavigationMenu(getString(R.string.villager_catalog), R.id.action_mainFragment_to_villagerFragment),
            NavigationMenu(getString(R.string.art_catalog), R.id.action_mainFragment_to_artFragment),
            NavigationMenu(getString(R.string.furniture_catalog), R.id.action_mainFragment_to_housewaresFragment),
//            NavigationMenu("Test", R.id.action_mainFragment_to_UITestFragment),
//            NavigationMenu("Flutter",
//                direction = MainNavDirections.actionGlobalFlutterFragment("/hello", cachedEngineId = "only")
//            ),
        ))
        recycler_view.doOnPreDraw {
            startPostponedEnterTransition()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapter = null
        messageAdapter = null
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_open_setting) {
            navigator.navigate(R.id.action_mainFragment_to_settingsFragment)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.home_toolbar_menu, menu)
    }
}

data class NavigationMenu(
    val title: String,
    val resId: Int? = null,
    val arguments: Bundle? = null,
    val options: NavOptions? = null,
    val extra: Navigator.Extras? = null,
    val direction: NavDirections? = null
): ItemComparable<String>{
    override fun id() = title
}

class NavigationMenuViewBinder(val onClickListener: (NavigationMenu) -> Unit) : ItemViewDelegate<NavigationMenu, NavigationMenuViewBinder.ViewHolder>{

    override fun onCreateViewHolder(parent: ViewGroup) = ViewHolder(LayoutInflater.from(parent.context)
        .inflate(R.layout.item_navigation_menu, parent, false))

    override fun onBindViewHolder(data: NavigationMenu?, vh: ViewHolder) {
        data?.let { vh.bind(it) }
    }

    inner class ViewHolder(override val containerView: View): RecyclerView.ViewHolder(containerView), LayoutContainer{

        fun bind(data: NavigationMenu){
            item_title.text = data.title
            ViewCompat.setTransitionName(containerView, data.title)
            itemView.setOnClickListener {
                onClickListener(data)
            }
        }
    }
}
