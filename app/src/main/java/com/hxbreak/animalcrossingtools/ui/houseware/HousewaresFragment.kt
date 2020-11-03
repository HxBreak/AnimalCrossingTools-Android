package com.hxbreak.animalcrossingtools.ui.houseware

import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.util.AttributeSet
import android.view.*
import android.widget.Filter
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.cursoradapter.widget.ResourceCursorAdapter
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialSharedAxis
import com.hxbreak.animalcrossingtools.GlideApp
import com.hxbreak.animalcrossingtools.R
import com.hxbreak.animalcrossingtools.adapter.ItemViewDelegate
import com.hxbreak.animalcrossingtools.adapter.LightAdapter
import com.hxbreak.animalcrossingtools.adapter.Typer
import com.hxbreak.animalcrossingtools.data.source.entity.HousewareEntity
import com.hxbreak.animalcrossingtools.extensions.littleCircleWaitAnimation
import com.hxbreak.animalcrossingtools.fragment.Event
import com.hxbreak.animalcrossingtools.fragment.EventObserver
import com.hxbreak.animalcrossingtools.i18n.toLocaleName
import com.hxbreak.animalcrossingtools.ui.BackAbleAppbarFragment
import com.hxbreak.animalcrossingtools.ui.houseware.detail.HousewareDetailFragmentDirections
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.fragment_houseware.*
import kotlinx.android.synthetic.main.item_houseware_item.*
import kotlinx.android.synthetic.main.item_housewares_variants.*
import java.util.*

@AndroidEntryPoint
class HousewaresFragment : BackAbleAppbarFragment(){

    private val viewModel by viewModels<HousewaresViewModel>()

    override fun configSupportActionBar() = true

    private var adapter: LightAdapter? = null
    private fun requireAdapter() = adapter ?: throw Exception()
    var suggestionsAdapter : SuggestionsAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        suggestionsAdapter = SuggestionsAdapter(
            requireContext(),
            R.layout.abc_search_dropdown_item_icons_2line,
            viewModel.locale
        )
        adapter = LightAdapter()
        recycler_view.layoutManager = LinearLayoutManager(requireContext())
        recycler_view.adapter = adapter
        val typer = Typer()
        typer.register(HousewareItemViewBinder(){
            nav.navigate(
                HousewaresFragmentDirections.actionHousewaresFragmentToHousewareDetailFragment(it.internalId.toLong())
            )
        })
        val recycledViewPool = RecyclerView.RecycledViewPool()
        requireAdapter().register(HousewaresViewBinder(typer, recycledViewPool, viewModel))
        viewModel.screenData.observe(viewLifecycleOwner){
            requireAdapter().submitList(it)
        }
        viewModel.loading.observe(viewLifecycleOwner){
            refresh_layout.isRefreshing = it == true
        }
        refresh_layout.setOnRefreshListener { viewModel.refresh.value = true }
        requireToolbarTitle().setText("Housewares")
        viewModel.error.observe(viewLifecycleOwner){
            Snackbar.make(requireView(), "Error $it", Snackbar.LENGTH_LONG)
                .setAction(requireContext().resources.getText(R.string.retry)){
                    viewModel.refresh.value = true
                }.show()
        }
        viewModel.database.observe(viewLifecycleOwner, EventObserver {
            Snackbar.make(requireView(), it, Snackbar.LENGTH_LONG).show()
        })
        viewModel.suggestionCursor.observe(viewLifecycleOwner){
            suggestionsAdapter?.changeCursor(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        val forward = MaterialSharedAxis(MaterialSharedAxis.X, true)
        enterTransition = forward
        val backward = MaterialSharedAxis(MaterialSharedAxis.X, false)
        returnTransition = backward
    }

    override fun onDestroyView() {
        super.onDestroyView()
        suggestionsAdapter?.changeCursor(null)
        suggestionsAdapter = null
        adapter = null
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
            searchView.queryHint = "Search You Want"
            searchView.suggestionsAdapter = suggestionsAdapter
            val completeTextView = searchView.findViewById<AppCompatAutoCompleteTextView>(R.id.search_src_text)
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
            searchView.setOnSuggestionListener(object : SearchView.OnSuggestionListener {
                override fun onSuggestionSelect(position: Int): Boolean {
                    return true
                }

                override fun onSuggestionClick(position: Int): Boolean {
                    return true
                }
            })
            searchMenu.setOnActionExpandListener(object : MenuItem.OnActionExpandListener{
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
    }
}

class SuggestionsAdapter(context: Context?, layout: Int, val locale: Locale) : ResourceCursorAdapter(
    context, layout, null, false
) {

    override fun bindView(view: View, context: Context?, cursor: Cursor?) {
        val text1 = view.findViewById<TextView>(android.R.id.text1)
        val icon1 = view.findViewById<ImageView>(android.R.id.icon1)
        text1.let { it.text = cursor?.toLocaleName(locale) }
        val imageIndex = cursor?.getColumnIndex("image_uri")
        if (imageIndex != null){
            icon1.isVisible = true
            GlideApp.with(icon1).load(cursor.getString(imageIndex)).into(icon1)
        }else{
            icon1.isVisible = false
            GlideApp.with(icon1).clear(icon1)
        }
    }

    /**
     * Avoid Internal Filter Component, We Really Don't Need this
     */
    override fun getFilter(): Filter {
        return object : Filter(){
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                return FilterResults().also {
                    it.count = 0
                    it.values = null
                }
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {}
        }
    }
}

class HousewareItemViewBinder(
    val listener: (HousewareEntity) -> Unit
): ItemViewDelegate<HousewareEntity, HousewareItemViewBinder.ViewHolder>{

    inner class ViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer{
        fun bind(entity: HousewareEntity) {
            GlideApp.with(houseware_image)
                .load(entity.image_uri)
                .littleCircleWaitAnimation(containerView.context)
                .into(houseware_image)
            houseware_name.text = entity.variant
            containerView.setOnClickListener { listener(entity) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup) = ViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.item_houseware_item, parent, false)
    )

    override fun onBindViewHolder(data: HousewareEntity?, vh: ViewHolder) {
        data?.let { vh.bind(it) }
    }
}

class HousewaresViewBinder(
    val typed: Typer, val recycledViewPool: RecyclerView.RecycledViewPool,
    val viewModel: HousewaresViewModel
) : ItemViewDelegate<HousewareVariants, HousewaresViewBinder.ViewHolder>{

    inner class ViewHolder(override val containerView: View): RecyclerView.ViewHolder(containerView), LayoutContainer {

        fun bind(housewares: HousewareVariants){
            if (housewares.variants.isNotEmpty()){
                val adapter = LightAdapter(typed)
                item_recycler_view.setRecycledViewPool(recycledViewPool)
                item_recycler_view.adapter = adapter
                captain.text = housewares.variants.first().name.toLocaleName(viewModel.locale)
                adapter.submitList(housewares.variants)
            }else{
                item_recycler_view.adapter = null
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup) = ViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.item_housewares_variants, parent, false)
    )

    override fun onBindViewHolder(data: HousewareVariants?, vh: ViewHolder) {
        data?.let { vh.bind(data) }
    }
}

class AvoidBottomPaddingMaterialToolbar: MaterialToolbar {
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