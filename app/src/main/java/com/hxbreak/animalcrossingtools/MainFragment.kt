package com.hxbreak.animalcrossingtools

import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.Navigator
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.transition.MaterialSharedAxis
import com.hxbreak.animalcrossingtools.adapter.ItemComparable
import com.hxbreak.animalcrossingtools.adapter.ItemViewDelegate
import com.hxbreak.animalcrossingtools.adapter.LightAdapter
import com.hxbreak.animalcrossingtools.data.source.DataRepository
import com.hxbreak.animalcrossingtools.di.DiViewModelFactory
import com.hxbreak.animalcrossingtools.ui.EditableAppbarFragment
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.android.synthetic.main.item_navigation_menu.*
import javax.inject.Inject

class MainFragment : EditableAppbarFragment() {

    @Inject
    lateinit var viewModelFactory: DiViewModelFactory

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
        requireAdapter().register(NavigationMenuViewBinder(navigator))
        requireAdapter().submitList(listOf(
            NavigationMenu("Fish", R.id.action_mainFragment_to_fishFragment),
            NavigationMenu("Bugs", R.id.action_mainFragment_to_bugsFragment),
            NavigationMenu("Song", R.id.action_mainFragment_to_songFragment),
            NavigationMenu("Villager", R.id.action_mainFragment_to_villagerFragment),
            NavigationMenu("Art", R.id.action_mainFragment_to_artFragment),
        ))
        recycler_view.post {
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
    val resId: Int?,
    val arguments: Bundle? = null,
    val options: NavOptions? = null,
    val extra: Navigator.Extras? = null,
): ItemComparable<String>{
    override fun id() = title
}

class NavigationMenuViewBinder(val navigator: NavController) : ItemViewDelegate<NavigationMenu, NavigationMenuViewBinder.ViewHolder>{

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
                if (data.resId != null){
                    navigator.navigate(data.resId, data.arguments, data.options, FragmentNavigatorExtras(
                        containerView to "container"
                    ))
                }
            }
        }
    }
}
