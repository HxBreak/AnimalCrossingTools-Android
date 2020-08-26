package com.hxbreak.animalcrossingtools

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.hxbreak.animalcrossingtools.R
import com.hxbreak.animalcrossingtools.data.source.DataRepository
import com.hxbreak.animalcrossingtools.di.DiViewModelFactory
import com.hxbreak.animalcrossingtools.ui.EditableAppbarFragment
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_main.*
import javax.inject.Inject

class MainFragment : EditableAppbarFragment() {

    @Inject
    lateinit var viewModelFactory: DiViewModelFactory

    @Inject
    lateinit var repo: DataRepository

    private val viewModel by viewModels<TempViewModel> { viewModelFactory }

    private val navigator by lazy {
        findNavController()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        fish_category.setOnClickListener {
            navigator.navigate(R.id.action_mainFragment_to_fishFragment2)
        }
        button.setOnClickListener {
            navigator.navigate(R.id.action_mainFragment_to_songFragment)
        }
        button2.setOnClickListener {
            navigator.navigate(R.id.action_mainFragment_to_trackerFragment)
        }
        rating.setOnRatingListener { v, RatingScore ->
            Toast.makeText(requireContext(), "$RatingScore", Toast.LENGTH_SHORT).show()
        }
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (requireActivity() as AppCompatActivity).setSupportActionBar(null)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        Glide.with(this).load(R.drawable.ic_fish).into(fish_category_image)
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
