package com.hxbreak.animalcrossingtools.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.hxbreak.animalcrossingtools.R

class ChatFragment : Fragment() {

    companion object {
        fun newInstance() = ChatFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

//    override fun onActivityCreated(savedInstanceState: Bundle?) {
//        super.onActivityCreated(savedInstanceState)
//        refresh_layout.isEnabled = false
//        toolbar.setupWithNavController(
//            findNavController(),
//            AppBarConfiguration(findNavController().graph)
//        )
//        toolbar.title = "${args.oclient.id}"
//        viewModel.connectTo(args.oclient)
//        viewModel.peerLastData.observe(viewLifecycleOwner, Observer {
//            it?.getContentIfNotHandled()?.let {
//                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
//            }
//        })
//
//        viewModel.targetLastInteractive.observe(viewLifecycleOwner, Observer {
//            toolbar.title = Date(it).toString()
//        })
//
//        viewModel.mConnectMode.observe(viewLifecycleOwner, Observer {
//            send.isEnabled = it != 0
//            if (it == 2) {
//                edit_mode.visibility = View.GONE
//            } else if (it == 1) {
//                edit_mode.visibility = View.VISIBLE
//            }
//        })
//        send.setOnClickListener {
//            viewModel.sendTo(args.oclient, chat_input.text.toString())
//        }
//    }
}
