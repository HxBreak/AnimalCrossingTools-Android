package com.hxbreak.animalcrossingtools.ui.chat

import android.os.Bundle
import android.view.*
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hxbreak.animalcrossingtools.adapter.CommonItemComparableDiffUtil
import com.hxbreak.animalcrossingtools.adapter.CommonTypedAdapter
import com.hxbreak.animalcrossingtools.databinding.FragmentChatBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@AndroidEntryPoint
class ChatFragment : Fragment() {

    companion object {
        fun newInstance() = ChatFragment()
    }

    private var _binding: FragmentChatBinding? = null
    private val binding: FragmentChatBinding
        get() = _binding!!

    @Inject
    lateinit var assistedFactory: ChatViewModel.AssistedFactory
    private val args by navArgs<ChatFragmentArgs>()
    private val viewModel by viewModels<ChatViewModel>(factoryProducer = {
        ChatViewModel.provideFactory(assistedFactory, args.chatToId)
    })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    private val chooseJpegOrPng = registerForActivityResult(ChooseJpegOrPng){
        it?.let {
            viewModel.sendUri(it)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = CommonTypedAdapter(CommonItemComparableDiffUtil)
        adapter.register(ChatMessageItemViewDelegate())
        lifecycleScope.launchWhenStarted {
            viewModel.paging.collect {
                adapter.differ.submitData(it as PagingData<Any>)
                adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver(){
                    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                        super.onItemRangeInserted(positionStart, itemCount)
                        if (positionStart == 0){
                            binding.recyclerView.smoothScrollToPosition(0)
                        }
                    }
                })
            }
        }
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, true)
        binding.test.setOnClickListener {
            chooseJpegOrPng.launch(Unit)
        }
        binding.send.setOnClickListener {
            val text = binding.chatInput.text.toString()
            if (text.isNotBlank()){
                viewModel.sendMessage(text)
            }
        }
        binding.chatContent.setOnApplyWindowInsetsListener { v, insets ->
            binding.test.isGone = !(insets.systemWindowInsetBottom > 0)
            v.setPadding(0, 0, 0, insets.systemWindowInsetBottom)
            insets.replaceSystemWindowInsets(
                insets.systemWindowInsetLeft,
                insets.systemWindowInsetTop,
                insets.systemWindowInsetRight,
                0
            )
            insets
        }
    }

    override fun onResume() {
        super.onResume()
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    override fun onPause() {
        super.onPause()
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_UNSPECIFIED)
    }
}
