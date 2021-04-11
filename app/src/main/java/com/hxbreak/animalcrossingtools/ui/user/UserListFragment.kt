package com.hxbreak.animalcrossingtools.ui.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.map
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.hxbreak.animalcrossingtools.R
import com.hxbreak.animalcrossingtools.SimpleNetUser
import com.hxbreak.animalcrossingtools.adapter.ItemViewDelegate
import com.hxbreak.animalcrossingtools.adapter.LightAdapter
import com.hxbreak.animalcrossingtools.databinding.FragmentUserListBinding
import com.hxbreak.animalcrossingtools.toSimpleNetUser
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UserListFragment : Fragment() {

    private val viewModel by viewModels<UserListViewModel>()
    private var binding: FragmentUserListBinding? = null
    private var adapter: LightAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserListBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    val nav by lazy { findNavController() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = binding ?: return
        binding.toolbar.title = ""
        binding.refreshLayout.isEnabled = false
        binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        binding.toolbar.setNavigationOnClickListener {
            nav.navigateUp()
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        adapter = LightAdapter()
        adapter!!.register(SimpleNetUserViewBinder{
            if ( it.id != viewModel.id){
                nav.navigate(UserListFragmentDirections.actionUserListFragmentToChatFragment(it.id))
            }
//            lifecycleScope.launch {
//                viewModel.controller.sendStunRequest(it.id)
//            }
        })
        binding.recyclerView.adapter = adapter
        viewModel.controller.lobbyList.map { it.map { it.toSimpleNetUser() } }.observe(viewLifecycleOwner){
            adapter!!.submitList(it)
        }
        viewModel.controller.stunRequest.observe(viewLifecycleOwner){
            it.getContentIfNotHandled()?.let {
                val builder = MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.alert)
                    .setMessage(resources.getString(R.string.stun_request_message, it.fromId))
                    .setNegativeButton(R.string.reject){ dialog, _ ->

                    }
                    .setPositiveButton(R.string.allow){ dialog, _ ->
                        lifecycleScope.launch {
                            viewModel.controller.sendStunRequest(it.fromId)
                        }
                        Toast.makeText(requireContext(), "Allowed", Toast.LENGTH_SHORT).show()
                    }
                builder.show()
            }
        }
        viewModel.controller.stunResponse.observe(viewLifecycleOwner){
            StunTestDialogFragment().show(childFragmentManager, null)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapter = null
    }

    companion object {
        fun newInstance() = UserListFragment()
    }
}

class SimpleNetUserViewBinder(val listener: (SimpleNetUser) -> Unit): ItemViewDelegate<SimpleNetUser, SimpleNetUserViewBinder.ViewHolder>{
    inner class ViewHolder(val view: View): RecyclerView.ViewHolder(view){
        fun bind(user: SimpleNetUser) {
            val text1 = view.findViewById<TextView>(android.R.id.text1)
            text1.text = "${user.id}"
            text1.setOnClickListener { listener(user) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false)
        )
    }

    override fun onBindViewHolder(data: SimpleNetUser?, vh: ViewHolder) {
        data?.let { vh.bind(it) }
    }
}
