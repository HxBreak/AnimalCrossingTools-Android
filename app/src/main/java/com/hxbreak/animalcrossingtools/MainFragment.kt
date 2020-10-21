package com.hxbreak.animalcrossingtools

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.doOnPreDraw
import androidx.navigation.*
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.transition.MaterialSharedAxis
import com.hxbreak.animalcrossingtools.adapter.ItemComparable
import com.hxbreak.animalcrossingtools.adapter.ItemViewDelegate
import com.hxbreak.animalcrossingtools.adapter.LightAdapter
import com.hxbreak.animalcrossingtools.ui.EditableAppbarFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.android.synthetic.main.item_navigation_menu.*

class MainFragment : EditableAppbarFragment() {

    private var adapter: LightAdapter? = null

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        postponeEnterTransition()
        recycler_view.layoutManager = LinearLayoutManager(requireContext())
        adapter = LightAdapter()
        recycler_view.adapter = adapter
        requireAdapter().register(NavigationMenuViewBinder {
            if (it.resId != null){
                navigator.navigate(it.resId, it.arguments, it.options, FragmentNavigatorExtras())
            } else if (it.direction != null){
                navigator.navigate(it.direction)
            }
        })
        requireAdapter().submitList(listOf(
            NavigationMenu("Fish", R.id.action_mainFragment_to_fishFragment),
            NavigationMenu("Bug", R.id.action_mainFragment_to_bugsFragment),
            NavigationMenu("SeaCreature", R.id.action_mainFragment_to_seaCreatureFragment),
            NavigationMenu("Fossil", R.id.action_mainFragment_to_fossilFragment),
            NavigationMenu("Song", R.id.action_mainFragment_to_songFragment),
            NavigationMenu("Villager", R.id.action_mainFragment_to_villagerFragment),
            NavigationMenu("Art", R.id.action_mainFragment_to_artFragment),
            NavigationMenu("Housewares", R.id.action_mainFragment_to_housewaresFragment),
            NavigationMenu("Flutter",
                direction = MainNavDirections.actionGlobalFlutterFragment("/about", cachedEngineId = "only")),
        ))
        recycler_view.doOnPreDraw {
            startPostponedEnterTransition()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (requireActivity() as AppCompatActivity).setSupportActionBar(null)
        adapter = null
    }

    override fun onResume() {
        super.onResume()
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
