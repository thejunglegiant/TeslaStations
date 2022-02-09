package com.thejunglegiant.teslastations.presentation.list

import android.content.Context
import android.graphics.drawable.ShapeDrawable
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.thejunglegiant.teslastations.R
import com.thejunglegiant.teslastations.databinding.FragmentStationsListBinding
import com.thejunglegiant.teslastations.domain.entity.StationEntity
import com.thejunglegiant.teslastations.extensions.setArgsLiveData
import com.thejunglegiant.teslastations.presentation.core.BaseBindingFragment
import com.thejunglegiant.teslastations.presentation.core.StatusBarMode
import com.thejunglegiant.teslastations.presentation.core.adapters.BaseAdapterCallback
import com.thejunglegiant.teslastations.presentation.list.models.ListEvent
import com.thejunglegiant.teslastations.presentation.list.models.ListViewState
import com.thejunglegiant.teslastations.utils.ARG_STATION_LOCATION
import org.koin.androidx.viewmodel.ext.android.viewModel

class ListStationsFragment :
    BaseBindingFragment<FragmentStationsListBinding>(FragmentStationsListBinding::inflate) {

    private val viewModel: ListStationsViewModel by viewModel()

    private val adapter = StationsListAdapter()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        StatusBarMode.Colored(R.color.maroon).onFragmentResumed(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupList()
        setListeners()

        // TODO add pagination, now freezing ui
        viewModel.obtainEvent(ListEvent.EnterScreen)

        viewModel.viewState.observe(viewLifecycleOwner) {
            when (it) {
                is ListViewState.Display -> {
                    adapter.setData(it.data)
                }
                is ListViewState.Error -> {

                }
                is ListViewState.Loading -> {

                }
            }
        }
    }

    private fun setListeners() {
        adapter.attachCallback(object : BaseAdapterCallback<StationEntity> {
            override fun onItemClick(model: StationEntity, view: View) {
                setArgsLiveData(ARG_STATION_LOCATION, model)
                findNavController().popBackStack()
            }
        })
        binding.appBar.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupList() {
        val divider = DividerItemDecoration(
            context,
            DividerItemDecoration.VERTICAL
        )
        divider.setDrawable(
            ShapeDrawable()
                .apply {
                    intrinsicHeight = 1
                    paint.color = ContextCompat.getColor(requireContext(), R.color.gull_gray)
                }
        )
        binding.listStations.addItemDecoration(divider)
        binding.listStations.layoutManager = LinearLayoutManager(context)
        binding.listStations.adapter = adapter
    }
}