package com.hxbreak.animalcrossingtools.ui.houseware

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.*
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import androidx.appcompat.widget.SearchView
import androidx.core.app.SharedElementCallback
import androidx.core.view.ViewCompat
import androidx.core.view.doOnNextLayout
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialElevationScale
import com.google.android.material.transition.MaterialSharedAxis
import com.hxbreak.animalcrossingtools.R
import com.hxbreak.animalcrossingtools.adapter.LightAdapter
import com.hxbreak.animalcrossingtools.adapter.Typer
import com.hxbreak.animalcrossingtools.fragment.Event
import com.hxbreak.animalcrossingtools.fragment.EventObserver
import com.hxbreak.animalcrossingtools.ui.BackAbleAppbarFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_houseware.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import timber.log.Timber
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class HousewaresFragment : BackAbleAppbarFragment(), SearchView.OnSuggestionListener {

    private val viewModel by viewModels<HousewaresViewModel>()

    private var adapter: LightAdapter? = null
    private fun requireAdapter() = adapter ?: throw Exception()
    private var suggestionsAdapter: SuggestionsAdapter? = null
    private var holder: RecyclerView.ViewHolder? = null

    companion object {
        const val KEY_DETAIL_SELECT = "onSelectFilenameVariant"
        const val ARGUMENT_FILENAME = "filename"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        suggestionsAdapter = SuggestionsAdapter(
            requireContext(),
            R.layout.abc_search_dropdown_item_icons_2line,
            viewModel.locale
        )
        adapter = LightAdapter()
        recycler_view.layoutManager = LinearLayoutManager(requireContext())
        recycler_view.adapter = adapter
        val typed = Typer()
        typed.register(HousewareItemViewBinder { root, entity ->
            val extras = FragmentNavigatorExtras(
                root to ViewCompat.getTransitionName(root)!!,
            )
            nav.navigate(
                HousewaresFragmentDirections.actionHousewaresFragmentToHousewareDetailFragment(
                    entity.fileName,
                    entity.internalId.toLong()
                ), extras
            )
        })
        recycler_view.post {
            if (returnElement?.hasBeenHandled != false){
                startPostponedEnterTransition()
            }
        }
        val recycledViewPool = RecyclerView.RecycledViewPool()
        requireAdapter().register(HousewaresViewBinder(typed, recycledViewPool, viewModel))
        viewModel.unpackedScreenData.observe(viewLifecycleOwner) {
            requireAdapter().submitList(it) {
                returnElement?.getContentIfNotHandled()?.let { element ->
                    recycler_view.doOnNextLayout { _ ->
                        val item = it.orEmpty().find {  it.variants.any { it.fileName == element.returnFilename }  }
                        val viewHolder = recycler_view.findViewHolderForAdapterPosition(
                            it.orEmpty().indexOf(item)
                        ) as? HousewaresViewBinder.ViewHolder
                        viewHolder?.let nest@{
                            if ((item?.variants?.size ?: 0) > 1){
                                val lm = it.recyclerView.layoutManager ?: return@nest
                                val position = item?.variants.orEmpty().indexOfFirst { it.fileName == element.returnFilename }
                                Timber.e("${element.returnFilename} $position ${item?.variants?.map { it.fileName }?.joinToString(separator = ", ")}")
                                val furnitureView = lm.findViewByPosition(position)
                                if (furnitureView == null || lm.isViewPartiallyVisible(furnitureView, false, true)){
                                    lm.scrollToPosition(position)
                                    it.recyclerView.post {
                                        startPostponedEnterTransition()
                                    }
                                }else{
                                    startPostponedEnterTransition()
                                }
                            }
                        }
                    }
                }
            }
        }
        viewModel.loading.observe(viewLifecycleOwner) {
            refresh_layout.isRefreshing = it == true
        }
        refresh_layout.setOnRefreshListener { viewModel.refresh.value = true }
        requireToolbarTitle().setText(getString(R.string.furniture_catalog))
        viewModel.screenStatus.observe(viewLifecycleOwner){
            when(it){
                is UiStatus.Error -> common_layout.setException(it.exception){ viewModel.refresh.value = true }
                is UiStatus.Empty -> common_layout.setEmpty()
                is UiStatus.Success -> common_layout.clearState()
            }
        }
        viewModel.database.observe(viewLifecycleOwner, EventObserver {
            Snackbar.make(requireView(), it, Snackbar.LENGTH_LONG).show()
        })
        viewModel.suggestionCursor.observe(viewLifecycleOwner) {
            suggestionsAdapter?.changeCursor(it)
        }
    }

    private var returnElement: Event<ReturnElement>? = null

    internal data class ReturnElement(
        val returnFilename: String,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFragmentResultListener(KEY_DETAIL_SELECT) { key, bundle ->
            returnElement = Event(
                ReturnElement(
                    bundle.getString(ARGUMENT_FILENAME)!!,
                )
            )
        }
        setHasOptionsMenu(true)
        setExitSharedElementCallback(object : SharedElementCallback() {
            override fun onMapSharedElements(
                names: MutableList<String>?,
                sharedElements: MutableMap<String, View>?
            ) {
                returnElement?.peekContent()?.let { element ->
                    val item = viewModel.unpackedScreenData.value.orEmpty().find { it.variants.any { it.fileName == element.returnFilename } }
                    val viewHolder = recycler_view.findViewHolderForAdapterPosition(
                        viewModel.unpackedScreenData.value.orEmpty().indexOf(item)
                    ) as? HousewaresViewBinder.ViewHolder
                    viewHolder?.let nest@{
                        if ((item?.variants?.size ?: 0) > 1){
                            val lm = it.recyclerView.layoutManager ?: return@nest
                            val position = item?.variants.orEmpty().indexOfFirst { it.fileName == element.returnFilename }
                            val furnitureView = lm.findViewByPosition(position)
                            furnitureView?.let {
                                val selectView = it.findViewById<View>(R.id.houseware_image)!!
                                sharedElements?.put(names!!.getOrElse(0){ ViewCompat.getTransitionName(selectView)!! }, selectView)
                                Timber.e("$names $sharedElements ${ViewCompat.getTransitionName(selectView)}")
                            }
                            returnElement = null
                        }
                    }
                }
            }
        })
        val forward = MaterialSharedAxis(MaterialSharedAxis.X, true)
        enterTransition = forward
        val backward = MaterialSharedAxis(MaterialSharedAxis.X, false)
        returnTransition = backward

        val exit = MaterialElevationScale(false).apply { duration = 300 }
        exitTransition = exit
        exit.excludeTarget(R.id.appbar, true)
        val reenter = MaterialElevationScale(true).apply { duration = 300 }
        reenter.excludeTarget(R.id.appbar, true)
        reenterTransition = reenter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        suggestionsAdapter?.changeCursor(null)
        suggestionsAdapter = null
        adapter = null
        holder = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_houseware, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.search_menu, menu)
        val searchMenu = menu.findItem(R.id.search)
        (searchMenu.actionView as? SearchView)?.let { searchView ->
            searchView.queryHint = getString(R.string.common_search_hint)
            searchView.suggestionsAdapter = suggestionsAdapter
            val completeTextView =
                searchView.findViewById<AppCompatAutoCompleteTextView>(R.id.search_src_text)
                    .also {
                        it.threshold = 1
                    }
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    viewModel.filter.value = query
                    completeTextView.dismissDropDown()
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    viewModel.searchSuggestionKeywords.value = Event(newText)
                    return true
                }
            })
            searchView.setOnSuggestionListener(this)
            searchMenu.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
                override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                    viewModel.filter.value = null
                    return true
                }

                override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                    viewModel.filter.value = null
                    return true
                }
            })
        }
    }

    override fun onSuggestionSelect(position: Int): Boolean {
        return true
    }

    override fun onSuggestionClick(position: Int): Boolean {
        suggestionsAdapter?.cursor?.let {
            val id = it.getString(it.getColumnIndexOrThrow("_id"))
            val filename = it.getString(it.getColumnIndexOrThrow("filename"))
            nav.navigate(
                HousewaresFragmentDirections.actionHousewaresFragmentToHousewareDetailFragment(
                    filename,
                    id.toLong()
                )
            )
        }
        return true
    }
}

class AvoidBottomPaddingMaterialToolbar : MaterialToolbar {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setPadding(paddingLeft, paddingTop, paddingRight, 0)//???
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }
}