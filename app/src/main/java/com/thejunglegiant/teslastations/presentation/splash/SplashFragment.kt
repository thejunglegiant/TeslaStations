package com.thejunglegiant.teslastations.presentation.splash

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.thejunglegiant.teslastations.R
import com.thejunglegiant.teslastations.databinding.FragmentSplashBinding
import com.thejunglegiant.teslastations.extensions.showSnackBar
import com.thejunglegiant.teslastations.extensions.toastSh
import com.thejunglegiant.teslastations.presentation.core.BaseBindingFragment
import com.thejunglegiant.teslastations.presentation.splash.models.SplashEvent
import com.thejunglegiant.teslastations.presentation.splash.models.SplashViewState
import org.koin.androidx.viewmodel.ext.android.viewModel

class SplashFragment : BaseBindingFragment<FragmentSplashBinding>(FragmentSplashBinding::inflate) {

    private val viewModel: SplashViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.obtainEvent(SplashEvent.EnterScreen)

        viewModel.viewState.observe(viewLifecycleOwner) { state ->
            when (state) {
                SplashViewState.Error -> {
                    toastSh(R.string.error_something_went_wrong)
                }
                SplashViewState.Success -> {}
            }

            findNavController().navigate(
                SplashFragmentDirections
                    .actionSplashFragmentToMapFragment()
            )
        }
    }
}