package com.hxbreak.animalcrossingtools.ui.user

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hxbreak.animalcrossingtools.databinding.FragmentStunTestBinding
import com.hxbreak.animalcrossingtools.services.DiscoverManager
import com.hxbreak.stun.DiscoverInfo
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class StunTestDialogFragment: DialogFragment() {

    private val viewModel by viewModels<UserListViewModel>()

    var _binding: FragmentStunTestBinding? = null
    val binding: FragmentStunTestBinding
        get() = _binding!!
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = MaterialAlertDialogBuilder(requireContext())
        _binding = FragmentStunTestBinding.inflate(LayoutInflater.from(requireContext()), null, false)
        builder.setView(binding.root)
        onViewCreated(binding.root, savedInstanceState)
        return builder.create()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.progressBar.max = 4
        lifecycleScope.launchWhenStarted {
            while (DiscoverManager.queue.poll() != null){}
            var info: DiscoverInfo? = null
            viewModel.stunTest.collectIndexed { index, value ->
                binding.progressBar.progress = index + 1
                binding.publicIpValue.text = "${value.publicIP}"
                binding.natTypeValue.text = value.natType?.name ?: "UNKNOWN"
                binding.localPortInfo.text = "${value.publicPort} : ${value.localPort}"
                info = value
                Timber.e(value.toString())
                viewModel.stunResult.emit(value)
            }

            binding.progressBar.progress = 4
            viewModel.stunResult.value?.let {
                DiscoverManager.queue.put(it)
                binding.connectAction.isEnabled = true
            }
        }
        binding.cancelAction.setOnClickListener {
            dismissAllowingStateLoss()
        }
        binding.connectAction.setOnClickListener {
            lifecycleScope.launch {
                viewModel.stunResult.value?.let {
                    viewModel.controller.sendStunInfoReply(it, true)
                }
                dismissAllowingStateLoss()
            }
        }
    }
}