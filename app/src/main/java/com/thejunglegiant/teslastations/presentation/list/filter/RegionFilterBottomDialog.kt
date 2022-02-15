package com.thejunglegiant.teslastations.presentation.list.filter

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.chip.Chip
import com.thejunglegiant.teslastations.R
import com.thejunglegiant.teslastations.databinding.DialogFilterStationsListBinding
import com.thejunglegiant.teslastations.domain.entity.ContinentEntity
import com.thejunglegiant.teslastations.domain.entity.CountryEntity
import com.thejunglegiant.teslastations.extensions.flowCollectLatest
import com.thejunglegiant.teslastations.extensions.hide
import com.thejunglegiant.teslastations.extensions.show
import com.thejunglegiant.teslastations.extensions.toastSh
import com.thejunglegiant.teslastations.presentation.core.BaseBindingBottomDialog
import com.thejunglegiant.teslastations.presentation.list.filter.models.FilterEvent
import com.thejunglegiant.teslastations.presentation.list.filter.models.FilterViewState
import org.koin.androidx.viewmodel.ext.android.viewModel

class RegionFilterBottomDialog :
    BaseBindingBottomDialog<DialogFilterStationsListBinding>(DialogFilterStationsListBinding::inflate) {

    private val viewModel: RegionFilterViewModel by viewModel()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        dialog.setOnShowListener {
            bottomSheetBehavior.skipCollapsed = true
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        flowCollectLatest(viewModel.viewState) {
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
        binding.listCountries.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId != -1) binding.fabFilter.performClick()
        }
        binding.fabFilter.setOnClickListener {
            val continent = binding.listContinents
                .findViewById<Chip>(binding.listContinents.checkedChipId)?.tag as ContinentEntity?
            val country = binding.listCountries
                .findViewById<Chip>(binding.listCountries.checkedChipId)?.tag as CountryEntity?

            when {
                country != null -> {
                    setFragmentResult(
                        REQUEST_KEY_FILTER_RESULT,
                        bundleOf(KEY_BOUNDS to country.getBounds())
                    )
                    findNavController().popBackStack()
                }
                continent != null -> {
                    setFragmentResult(
                        REQUEST_KEY_FILTER_RESULT,
                        bundleOf(KEY_BOUNDS to continent.getBounds())
                    )
                    findNavController().popBackStack()
                }
                else -> {
                    toastSh(R.string.choose_continent_or_country)
                }
            }
        }

        viewModel.obtainEvent(FilterEvent.EnterScreen)
    }

    companion object {
        const val REQUEST_KEY_FILTER_RESULT = "FILTER_RESULT"
        const val KEY_BOUNDS = "KEY_BOUNDS"
    }
}