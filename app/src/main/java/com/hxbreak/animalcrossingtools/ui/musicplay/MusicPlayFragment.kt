package com.hxbreak.animalcrossingtools.ui.musicplay

import android.content.Context
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.RectF
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.SharedElementCallback
import androidx.core.view.ViewCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.transition.*
import com.bumptech.glide.Glide
import com.example.android.uamp.media.extensions.currentPlayBackPosition
import com.example.android.uamp.media.extensions.fullDescription
import com.google.android.material.transition.Hold
import com.google.android.material.transition.MaterialArcMotion
import com.google.android.material.transition.MaterialContainerTransform
import com.google.android.material.transition.MaterialSharedAxis
import com.hxbreak.animalcrossingtools.GlideApp
import com.hxbreak.animalcrossingtools.R
import com.hxbreak.animalcrossingtools.di.DiViewModelFactory
import com.hxbreak.animalcrossingtools.ui.song.imageTransitionName
import com.hxbreak.animalcrossingtools.ui.song.titleTransitionName
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_fish.*
import kotlinx.android.synthetic.main.fragment_music_play.*
import kotlinx.android.synthetic.main.fragment_music_play.toolbar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.floor


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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewCompat.setTransitionName(view, "container")
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        args.song?.let {
            ViewCompat.setTransitionName(imageView, it.imageTransitionName())
            ViewCompat.setTransitionName(textView6, it.titleTransitionName())
        }
        viewModel.connection.nowPlaying.observe(viewLifecycleOwner, Observer {
            GlideApp.with(imageView).load(it.fullDescription.iconUri).into(imageView)
            textView6.text = it.fullDescription.title

            startPostponedEnterTransition()
        })
        viewModel.connection.playbackState.observe(viewLifecycleOwner, Observer {
            Timber.e("$it")
            seekBar.max = floor(it.position / 1E3).toInt()
            seekBar.progress = floor(it.currentPlayBackPosition / 1E3).toInt()
            seekBar.secondaryProgress = floor(it.bufferedPosition / 1E3).toInt()

        })
        val changeImage = ChangeImageTransform()
        changeImage.addTarget(imageView)
        transitionSet.addTransition(changeImage)
        val changeBounds = ChangeBounds()
        changeBounds.addTarget(textView6)
        transitionSet.addTransition(changeBounds)
    }

    private val transitionSet = TransitionSet()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val transform = MaterialContainerTransform()
        transform.scrimColor = Color.TRANSPARENT
//        transform.duration = 300
//        transform.addTarget(R.id.transition_container)
        transform.setPathMotion(MaterialArcMotion())
        val move =
            TransitionInflater.from(requireContext()).inflateTransition(android.R.transition.move)
        transitionSet.duration = 300
        transitionSet.addTransition(transform)
        sharedElementEnterTransition = transitionSet
        postponeEnterTransition()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}