package com.thejunglegiant.teslastations.presentation.list

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.LatLngBounds
import com.thejunglegiant.teslastations.R
import com.thejunglegiant.teslastations.databinding.FragmentStationsListBinding
import com.thejunglegiant.teslastations.domain.entity.BoundsItem
import com.thejunglegiant.teslastations.domain.entity.StationEntity
import com.thejunglegiant.teslastations.domain.mapper.toLatLngBounds
import com.thejunglegiant.teslastations.extensions.*
import com.thejunglegiant.teslastations.presentation.core.BaseBindingFragment
import com.thejunglegiant.teslastations.presentation.core.StatusBarMode
import com.thejunglegiant.teslastations.presentation.core.adapters.BaseAdapterCallback
import com.thejunglegiant.teslastations.presentation.list.filter.RegionFilterBottomDialog
import com.thejunglegiant.teslastations.presentation.list.models.ListEvent
import com.thejunglegiant.teslastations.presentation.list.models.ListViewState
import com.thejunglegiant.teslastations.utils.ARG_FILTER_BOUNDS
import com.thejunglegiant.teslastations.utils.ARG_STATION_LOCATION
import org.koin.androidx.viewmodel.ext.android.viewModel

class ListStationsFragment :
    BaseBindingFragment<FragmentStationsListBinding>(FragmentStationsListBinding::inflate) {

    private val viewModel: ListStationsViewModel by viewModel()

    private val adapter = StationsListAdapter()
    private val paginationListener = PaginationListener()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        StatusBarMode.Colored(R.color.maroon).onFragmentResumed(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupList()
        setListeners()

        viewModel.obtainEvent(ListEvent.EnterScreen)

        viewModel.viewState.observe(viewLifecycleOwner) {
            loge(TAG, "$it")
            logd(TAG, "===========")
            when (it) {
                is ListViewState.Display -> {
                    paginationListener.isLoading = false
                    paginationListener.isLastPage =
                        it.data.isEmpty() || it.data.size < ListStationsViewModel.PAGE_LIMIT

                    adapter.setData(it.data)
                }
                is ListViewState.DisplayMore -> {
                    paginationListener.isLoading = false
                    paginationListener.isLastPage =
                        it.data.isEmpty() || it.data.size < ListStationsViewModel.PAGE_LIMIT

                    adapter.addData(it.data)
                }
                is ListViewState.Error -> {
                    paginationListener.isLoading = false
                }
                is ListViewState.Loading -> {
                    paginationListener.isLoading = true
                }
            }
        }
    }

    private fun setListeners() {
        binding.appBar.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
        adapter.attachCallback(object : BaseAdapterCallback<StationEntity> {
            override fun onItemClick(model: StationEntity, view: View) {
                setArgsLiveData(ARG_STATION_LOCATION, model)
                findNavController().popBackStack()
            }
        })
        binding.fabFilter.setOnClickListener {
            setFragmentResultListener(RegionFilterBottomDialog.REQUEST_KEY_FILTER_RESULT) { _, bundle ->
                val bounds =
                    bundle.getSerializable(RegionFilterBottomDialog.KEY_BOUNDS) as BoundsItem?

                bounds?.let {
                    loge(TAG, "$it")
                    viewModel.obtainEvent(ListEvent.FilterList(it.toLatLngBounds()))
                }
            }

            findNavController().navigate(
                ListStationsFragmentDirections
                    .actionListStationsFragmentToRegionFilterBottomDialog()
            )
        }
    }

    private fun setupList() {
        val divider = object : DividerItemDecoration(context, VERTICAL) {
            init {
                ContextCompat.getDrawable(requireContext(), R.drawable.divider)?.let { drawable ->
                    setDrawable(drawable)
                }
            }

            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                val position = parent.getChildAdapterPosition(view)

                if (position == state.itemCount - 1 || position == state.itemCount - 2) {
                    outRect.setEmpty()
                } else {
                    super.getItemOffsets(outRect, view, parent, state);
                }
            }
        }
        binding.listStations.addItemDecoration(divider)
        binding.listStations.layoutManager = LinearLayoutManager(context)
        binding.listStations.adapter = adapter
        paginationListener.setOnPaginateCallback {
            viewModel.obtainEvent(ListEvent.LoadMoreStations)
        }
        binding.listStations.addOnScrollListener(paginationListener)
    }

    companion object {
        val TAG: String = ListStationsFragment::class.java.simpleName
    }
}