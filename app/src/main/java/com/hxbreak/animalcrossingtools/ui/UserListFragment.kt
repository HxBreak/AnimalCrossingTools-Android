package com.hxbreak.animalcrossingtools.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.map
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hxbreak.animalcrossingtools.R
import com.hxbreak.animalcrossingtools.SimpleNetUser
import com.hxbreak.animalcrossingtools.adapter.ItemViewDelegate
import com.hxbreak.animalcrossingtools.adapter.LightAdapter
import com.hxbreak.animalcrossingtools.databinding.FragmentUserListBinding
import com.hxbreak.animalcrossingtools.services.handler.InstantMessageController
import com.hxbreak.animalcrossingtools.toSimpleNetUser
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class UserListFragment : Fragment() {

    @Inject
    lateinit var controller: InstantMessageController

    private val viewModel by activityViewModels<UserListViewModel>()
    private var binding: FragmentUserListBinding? = null
    private var adapter: LightAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserListBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = binding ?: return
        binding.toolbar.title = ""
        binding.refreshLayout.isEnabled = false
        binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        adapter = LightAdapter()
        adapter!!.register(SimpleNetUserViewBinder{
            lifecycleScope.launch {
                controller.sendStunRequest(it.id)
            }
        })
        binding.recyclerView.adapter = adapter
        controller.lobbyList.map { it.map { it.toSimpleNetUser() } }.observe(viewLifecycleOwner){
            adapter!!.submitList(it)
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
