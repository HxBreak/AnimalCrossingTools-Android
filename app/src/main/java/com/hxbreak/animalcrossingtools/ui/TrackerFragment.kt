package com.hxbreak.animalcrossingtools.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.hxbreak.animalcrossingtools.R
import kotlinx.android.synthetic.main.fragment_tracker.*
import javax.inject.Inject

class TrackerFragment : Fragment() {

    companion object {
        fun newInstance() = TrackerFragment()
    }
    private val viewModel by activityViewModels<TrackerViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tracker, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        refresh_layout.isEnabled = false
        toolbar.title = viewModel.id
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

//    override fun onActivityCreated(savedInstanceState: Bundle?) {
//        super.onActivityCreated(savedInstanceState)
//        recycler_view.layoutManager =
//            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
//        val adapter = TrackerAdapter(viewModel)
//        recycler_view.adapter = adapter
//        viewModel.onlines.observe(viewLifecycleOwner, Observer {
//            adapter.submitList(it)
//        })
//        viewModel.peerLastData.observe(viewLifecycleOwner, Observer {
//            it?.getContentIfNotHandled()?.let {
//                val service =
//                    requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//                var channel: NotificationChannel? = null
//                if (Build.VERSION.SDK_INT >= 26) {
//                    channel =
//                        NotificationChannel("1", "Message", NotificationManager.IMPORTANCE_DEFAULT)
//                    service.createNotificationChannel(channel)
//                }
//                val notification = NotificationCompat.Builder(requireContext(), "1")
//                    .setSmallIcon(R.mipmap.ic_launcher_round)
//                    .setContentTitle("$it")
//                    .setSubText("Message Recv")
//                    .build()
//                service.notify(0, notification)
//            }
//        })
//        viewModel.oclient.observe(viewLifecycleOwner, Observer {
//            it?.getContentIfNotHandled()?.let {
//                findNavController().navigate(
//                    TrackerFragmentDirections.actionTrackerFragmentToChatFragment(
//                        it
//                    )
//                )
//            }
//        })
//    }
}
