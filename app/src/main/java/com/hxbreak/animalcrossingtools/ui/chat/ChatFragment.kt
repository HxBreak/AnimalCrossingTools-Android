package com.hxbreak.animalcrossingtools.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.hxbreak.animalcrossingtools.R
import com.hxbreak.animalcrossingtools.di.DiViewModelFactory
import com.hxbreak.animalcrossingtools.ui.TrackerViewModel
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.chat_fragment.*
import kotlinx.android.synthetic.main.chat_fragment.edit_mode
import kotlinx.android.synthetic.main.chat_fragment.refresh_layout
import kotlinx.android.synthetic.main.chat_fragment.toolbar
import java.util.*
import javax.inject.Inject

class ChatFragment : DaggerFragment() {

    companion object {
        fun newInstance() = ChatFragment()
    }

    @Inject
    lateinit var viewModelFactory: DiViewModelFactory
    private val viewModel by viewModels<TrackerViewModel>(ownerProducer = { requireActivity() }) { viewModelFactory }

    //    val args: ChatFragment.
    val args: ChatFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.chat_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        refresh_layout.isEnabled = false
        toolbar.setupWithNavController(
            findNavController(),
            AppBarConfiguration(findNavController().graph)
        )
        toolbar.title = "${args.oclient.id}"
        viewModel.connectTo(args.oclient)
        viewModel.peerLastData.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        })

        viewModel.targetLastInteractive.observe(viewLifecycleOwner, Observer {
            toolbar.title = Date(it).toString()
        })

        viewModel.mConnectMode.observe(viewLifecycleOwner, Observer {
            send.isEnabled = it != 0
            if (it == 2) {
                edit_mode.visibility = View.GONE
            } else if (it == 1) {
                edit_mode.visibility = View.VISIBLE
            }
        })
        send.setOnClickListener {
            viewModel.sendTo(args.oclient, chat_input.text.toString())
        }
    }
}