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
            textView7.text = it.fullDescription.subtitle
        })
        val changeImage = ChangeImageTransform()
        changeImage.addTarget(imageView)
        transitionSet.addTransition(changeImage)
        val changeBounds = ChangeBounds()
        changeBounds.addTarget(textView6)
        transitionSet.addTransition(changeBounds)
//        startPostponedEnterTransition()
    }

    private val transitionSet = TransitionSet()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val transform = MaterialContainerTransform()
        transform.scrimColor = Color.TRANSPARENT
        transform.duration = 300
//        transform.addTarget(R.id.transition_container)
        transform.setPathMotion(MaterialArcMotion())
//        transitionSet.addTransition(transform)
        val move =
            TransitionInflater.from(requireContext()).inflateTransition(android.R.transition.move)
        sharedElementEnterTransition = transform
//        postponeEnterTransition()
        setEnterSharedElementCallback(object : SharedElementCallback() {
            override fun onSharedElementStart(
                sharedElementNames: MutableList<String>?,
                sharedElements: MutableList<View>?,
                sharedElementSnapshots: MutableList<View>?
            ) {
                super.onSharedElementStart(
                    sharedElementNames,
                    sharedElements,
                    sharedElementSnapshots
                )
                Timber.e("onSharedElementStart")
            }

            override fun onSharedElementEnd(
                sharedElementNames: MutableList<String>?,
                sharedElements: MutableList<View>?,
                sharedElementSnapshots: MutableList<View>?
            ) {
                super.onSharedElementEnd(sharedElementNames, sharedElements, sharedElementSnapshots)
                Timber.e("onSharedElementEnd")
            }
        })

        setExitSharedElementCallback(object : SharedElementCallback() {
            override fun onRejectSharedElements(rejectedSharedElements: MutableList<View>?) {
                super.onRejectSharedElements(rejectedSharedElements)
                Timber.e("onRejectSharedElements")
            }

            override fun onSharedElementEnd(
                sharedElementNames: MutableList<String>?,
                sharedElements: MutableList<View>?,
                sharedElementSnapshots: MutableList<View>?
            ) {
                super.onSharedElementEnd(sharedElementNames, sharedElements, sharedElementSnapshots)
            }

            override fun onCaptureSharedElementSnapshot(
                sharedElement: View?,
                viewToGlobalMatrix: Matrix?,
                screenBounds: RectF?
            ): Parcelable {
                return super.onCaptureSharedElementSnapshot(
                    sharedElement,
                    viewToGlobalMatrix,
                    screenBounds
                )
            }

            override fun onSharedElementsArrived(
                sharedElementNames: MutableList<String>?,
                sharedElements: MutableList<View>?,
                listener: OnSharedElementsReadyListener?
            ) {
                super.onSharedElementsArrived(sharedElementNames, sharedElements, listener)
            }

            override fun onMapSharedElements(
                names: MutableList<String>?,
                sharedElements: MutableMap<String, View>?
            ) {
                super.onMapSharedElements(names, sharedElements)
            }

            override fun onCreateSnapshotView(context: Context?, snapshot: Parcelable?): View {
                return super.onCreateSnapshotView(context, snapshot)
            }

            override fun onSharedElementStart(
                sharedElementNames: MutableList<String>?,
                sharedElements: MutableList<View>?,
                sharedElementSnapshots: MutableList<View>?
            ) {
                super.onSharedElementStart(
                    sharedElementNames,
                    sharedElements,
                    sharedElementSnapshots
                )
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}