package com.thejunglegiant.teslastations.presentation.list

import android.view.LayoutInflater
import android.view.ViewGroup
import com.thejunglegiant.teslastations.R
import com.thejunglegiant.teslastations.databinding.ItemListStationBinding
import com.thejunglegiant.teslastations.domain.entity.StationEntity
import com.thejunglegiant.teslastations.presentation.core.adapters.BaseAdapter
import com.thejunglegiant.teslastations.presentation.core.adapters.BaseViewHolder

class StationsListAdapter : BaseAdapter<StationEntity>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder<StationEntity> {
        val binding = ItemListStationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    class ViewHolder(private val binding: ItemListStationBinding) : BaseViewHolder<StationEntity>(binding.root) {

        override fun bind(model: StationEntity) {
            binding.title.text = model.title
            binding.location.text = "${model.country}, ${model.city}"
            binding.stationHours.text = model.hours.ifEmpty {
                binding.root.context.getString(R.string.station_description_hours_placeholder)
            }
        }
    }
}