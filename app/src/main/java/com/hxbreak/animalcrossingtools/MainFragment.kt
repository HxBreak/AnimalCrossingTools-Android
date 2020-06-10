package com.hxbreak.animalcrossingtools

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.hxbreak.animalcrossingtools.R
import com.hxbreak.animalcrossingtools.data.source.DataRepository
import com.hxbreak.animalcrossingtools.di.DiViewModelFactory
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_main.*
import javax.inject.Inject

class MainFragment : DaggerFragment() {

    @Inject
    lateinit var viewModelFactory: DiViewModelFactory

    @Inject
    lateinit var repo: DataRepository

    private val viewModel by viewModels<TempViewModel> { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Glide.with(this).load(R.drawable.ic_fish).into(fish_category_image)
        fish_category.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_fishFragment2)
        }
        button.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_songFragment)
        }
        button2.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_trackerFragment)
        }
        rating.setOnRatingListener { v, RatingScore ->
            Toast.makeText(requireContext(), "$RatingScore", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (requireActivity() as AppCompatActivity).setSupportActionBar(null)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)

    }
}
