package com.hxbreak.animalcrossingtools.ui.houseware

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.google.android.material.transition.MaterialSharedAxis
import com.hxbreak.animalcrossingtools.R
import com.hxbreak.animalcrossingtools.di.DiViewModelFactory
import com.hxbreak.animalcrossingtools.ui.EditBackAbleAppbarFragment
import kotlinx.android.synthetic.main.fragment_houseware.*
import javax.inject.Inject

class HousewaresFragment : EditBackAbleAppbarFragment(){

    @Inject
    lateinit var viewModelFactory: DiViewModelFactory

    private val viewModel by viewModels<HousewaresViewModel> { viewModelFactory }

    override fun configSupportActionBar() = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        edit_mode.setOnClickListener { uiSelectMode = !viewModel.editMode.value!! }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val forward = MaterialSharedAxis(MaterialSharedAxis.X, true)
        enterTransition = forward
        val backward = MaterialSharedAxis(MaterialSharedAxis.X, false)
        returnTransition = backward
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onUiSelectChanged(value: Boolean) {
        super.onUiSelectChanged(value)
        viewModel.editMode.value = value
    }

    override fun animateIconList() = listOf(found)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_houseware, container, false)
    }
}