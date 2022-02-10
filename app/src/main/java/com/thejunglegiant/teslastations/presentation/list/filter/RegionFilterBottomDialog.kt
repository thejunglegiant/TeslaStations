package com.thejunglegiant.teslastations.presentation.list.filter

import android.os.Bundle
import android.view.View
import com.google.android.material.chip.Chip
import com.thejunglegiant.teslastations.databinding.DialogFilterStationsListBinding
import com.thejunglegiant.teslastations.domain.entity.ContinentEntity
import com.thejunglegiant.teslastations.extensions.*
import com.thejunglegiant.teslastations.presentation.core.BaseBindingBottomDialog
import com.thejunglegiant.teslastations.presentation.list.filter.models.FilterEvent
import com.thejunglegiant.teslastations.presentation.list.filter.models.FilterViewState
import org.koin.androidx.viewmodel.ext.android.viewModel

class RegionFilterBottomDialog :
    BaseBindingBottomDialog<DialogFilterStationsListBinding>(DialogFilterStationsListBinding::inflate) {

    private val viewModel: RegionFilterViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.viewState.observe(viewLifecycleOwner) {
            binding.loadingView.hide()

            when (it) {
                is FilterViewState.Display -> {
                    binding.fabFilter.show()
                    if (binding.listContinents.childCount != it.continents.size) {
                        binding.listContinents.removeAllViews()
                        it.continents.forEach { continent ->
                            val chip = Chip(context)
                            chip.tag = continent
                            chip.text = continent.name
                            binding.listContinents.addView(chip)
                        }
                    }

                    if (binding.listCountries.childCount != it.countries.size) {
                        binding.listCountries.removeAllViews()

                        it.countries.forEach { country ->
                            val chip = Chip(context)
                            chip.tag = country
                            chip.text = country.name
                            chip.setOnCheckedChangeListener { _, isChecked ->
                                if (isChecked) {
//                                    setArgsLiveData(ARG_FILTER_COUNTRY, country)
//                                    findNavController().popBackStack()
//                                    viewModel.obtainEvent(FilterEvent.CountryClicked(country.iso))
                                }
                            }
                            binding.listCountries.addView(chip)
                        }
                    }
                }
                is FilterViewState.Error -> {
                    binding.fabFilter.show()
                }
                FilterViewState.Loading -> {
                    binding.fabFilter.hide()
                    binding.loadingView.show()
                }
            }
        }

        binding.listContinents.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == -1) {
                viewModel.obtainEvent(FilterEvent.ContinentDisabled)
            } else {
                val continent = group.findViewById<Chip>(checkedId).tag as ContinentEntity
                viewModel.obtainEvent(FilterEvent.ContinentClicked(continent.id))
            }
        }

        viewModel.obtainEvent(FilterEvent.EnterScreen)
    }
}