package com.hxbreak.animalcrossingtools.ui.user

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hxbreak.animalcrossingtools.databinding.FragmentStunTestBinding
import com.hxbreak.stun.StunHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.flow.flowOn

class StunTestDialogFragment: AppCompatDialogFragment() {

    val viewModel by viewModels<UserListViewModel>()

    var binding: FragmentStunTestBinding? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = FragmentStunTestBinding.inflate(LayoutInflater.from(requireContext()), null, false)
        val builder = MaterialAlertDialogBuilder(requireContext())
            .setView(binding!!.root)
        initView()
        return builder.create()
    }

    fun initView(){
        val binding = binding ?: return
        binding.progressBar.max = 4
        lifecycleScope.launchWhenStarted {
            StunHelper.testNatType(0, "stun.sipgate.net", 10000)
                .flowOn(Dispatchers.IO)
                .collectIndexed { index, value ->
                    binding.progressBar.progress = index + 1
                    binding.publicIpValue.text = "${value.publicIP}"
                    binding.natTypeValue.text = "${value.natType?.name ?: "UNKNOWN"}"
                }
            binding.connectAction.isEnabled = true
            binding.progressBar.progress = 4
        }
    }
}