package com.hxbreak.animalcrossingtools.ui.musicplay

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import androidx.transition.Explode
import com.bumptech.glide.Glide
import com.example.android.uamp.media.extensions.fullDescription
import com.google.android.material.transition.MaterialArcMotion
import com.google.android.material.transition.MaterialContainerTransform
import com.hxbreak.animalcrossingtools.R
import com.hxbreak.animalcrossingtools.di.DiViewModelFactory
import com.hxbreak.animalcrossingtools.ui.song.imageTransitionName
import com.hxbreak.animalcrossingtools.ui.song.titleTransitionName
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_music_play.*
import javax.inject.Inject


class MusicPlayFragment : DaggerFragment() {

    companion object {
        fun newInstance() = MusicPlayFragment()
    }

    val args: MusicPlayFragmentArgs by navArgs()

    @Inject
    lateinit var viewModelFactory: DiViewModelFactory

    private val viewModel by viewModels<MusicPlayViewModel> { viewModelFactory }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_music_play, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        args.song?.let {
            ViewCompat.setTransitionName(imageView, it.imageTransitionName())
            ViewCompat.setTransitionName(textView6, it.titleTransitionName())
        }
        viewModel.connection.nowPlaying.observe(viewLifecycleOwner, Observer {
            Glide.with(imageView).load(it.fullDescription.iconUri).into(imageView)
            textView6.text = it.fullDescription.title
            textView7.text = it.fullDescription.subtitle
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val transform = MaterialContainerTransform()
        transform.scrimColor = Color.TRANSPARENT
        transform.setPathMotion(MaterialArcMotion())
        sharedElementEnterTransition = transform
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}