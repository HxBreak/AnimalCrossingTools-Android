package com.hxbreak.animalcrossingtools.ui.musicplay

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.hxbreak.animalcrossingtools.R
import com.hxbreak.animalcrossingtools.di.DiViewModelFactory
import com.hxbreak.animalcrossingtools.ui.fish.FishViewModel
import dagger.android.DaggerActivity
import dagger.android.support.DaggerFragment
import javax.inject.Inject


class MusicPlayFragment : DaggerFragment() {

    companion object {
        fun newInstance() = MusicPlayFragment()
    }

    @Inject
    lateinit var viewModelFactory: DiViewModelFactory

    private val viewModel by viewModels<MusicPlayViewModel> { viewModelFactory }

    private val args: MusicPlayFragmentArgs by navArgs()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_music_play, container, false)
    }

    val player by lazy { SimpleExoPlayer.Builder(requireContext()).build() }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(
            context,
            Util.getUserAgent(requireContext(), "")
        )
        val videoSource: MediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(Uri.parse(args.url))

//        player.prepare(videoSource)
//        player.playWhenReady = true
        viewModel
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }

}