package com.thejunglegiant.teslastations.presentation.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.thejunglegiant.teslastations.R
import com.thejunglegiant.teslastations.databinding.ItemListStationBinding
import com.thejunglegiant.teslastations.databinding.ItemLoadingPlaceholderBinding
import com.thejunglegiant.teslastations.domain.entity.StationEntity
import com.thejunglegiant.teslastations.presentation.core.adapters.BaseAdapter
import com.thejunglegiant.teslastations.presentation.core.adapters.BaseViewHolder

class StationsListAdapter : BaseAdapter<StationEntity>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder<StationEntity> {
        return if (viewType == STATION_VIEW_TYPE) {
            val binding = ItemListStationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            StationViewHolder(binding)
        } else {
            val binding = ItemLoadingPlaceholderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            LoadingViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder<StationEntity>, position: Int) {
        if (getItemViewType(position) == STATION_VIEW_TYPE) {
            super.onBindViewHolder(holder, position)
        }
    }

    class StationViewHolder(private val binding: ItemListStationBinding) : BaseViewHolder<StationEntity>(binding.root) {

        override fun bind(model: StationEntity) {
            binding.title.text = position.toString() + " - " + model.title
            binding.location.text = "${model.country}, ${model.city}"
            binding.stationHours.text = model.hours.ifEmpty {
                binding.root.context.getString(R.string.station_description_hours_placeholder)
            }
        }
    }

    class LoadingViewHolder(binding: ItemLoadingPlaceholderBinding) : BaseViewHolder<StationEntity>(binding.root) {
        override fun bind(model: StationEntity) {}
    }

    override fun getItemCount(): Int {
        return super.getItemCount() + 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == itemCount - 1) {
            LOADING_VIEW_TYPE
        } else {
            STATION_VIEW_TYPE
        }
    }

    companion object {
        private const val STATION_VIEW_TYPE = 0
        private const val LOADING_VIEW_TYPE = 1
    }
}