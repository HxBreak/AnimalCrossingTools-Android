package com.hxbreak.animalcrossingtools.ui.musicplay

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.core.view.ViewCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.transition.*
import com.example.android.uamp.media.extensions.duration
import com.example.android.uamp.media.extensions.fullDescription
import com.google.android.material.transition.MaterialArcMotion
import com.google.android.material.transition.MaterialContainerTransform
import com.hxbreak.animalcrossingtools.GlideApp
import com.hxbreak.animalcrossingtools.R
import com.hxbreak.animalcrossingtools.di.DiViewModelFactory
import com.hxbreak.animalcrossingtools.ui.song.imageTransitionName
import com.hxbreak.animalcrossingtools.ui.song.titleTransitionName
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_music_play.*
import timber.log.Timber
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewCompat.setTransitionName(view, "container")
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        args.song?.let {
            Timber.e("${it.titleTransitionName()}")
            ViewCompat.setTransitionName(imageView, it.imageTransitionName())
            ViewCompat.setTransitionName(textView6, it.titleTransitionName())
        }
        viewModel.connection.nowPlaying.observe(viewLifecycleOwner, Observer {
            GlideApp.with(imageView).load(it.fullDescription.iconUri).into(imageView)
            textView6.text = it.fullDescription.title

            startPostponedEnterTransition()
        })
        viewModel.current.observe(viewLifecycleOwner, Observer {
            seekBar.progress = it
        })
        viewModel.playerState.observe(viewLifecycleOwner, Observer {
            it.first?.let {
                seekBar.max = it.duration.toInt()
            }
        })
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser && seekBar?.max != 0) {
                    viewModel.connection.transportControls.seekTo(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private val transitionSet = TransitionSet()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val transform = MaterialContainerTransform()
        transform.scrimColor = Color.TRANSPARENT
        transform.setPathMotion(MaterialArcMotion())
        transitionSet.duration = 300
        transitionSet.addTransition(transform)
        sharedElementEnterTransition = transitionSet
//        postponeEnterTransition()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}