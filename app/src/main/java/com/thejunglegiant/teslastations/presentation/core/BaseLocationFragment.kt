package com.thejunglegiant.teslastations.presentation.core

import android.Manifest
import android.content.pm.PackageManager
import android.os.Looper
import android.view.LayoutInflater
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.thejunglegiant.teslastations.R
import com.thejunglegiant.teslastations.extensions.showSnackBar
import com.thejunglegiant.teslastations.utils.LocationUtil

abstract class BaseLocationFragment<T : ViewBinding>(
    bindingInflater: (inflater: LayoutInflater) -> T
) : BaseBindingFragment<T>(bindingInflater) {

    private var locationCallbacks: MutableList<LocationCallback> = mutableListOf()
    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(requireActivity())
    }
    private var permissionGrantedCallback: (() -> Unit)? = null
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                permissionGrantedCallback?.invoke()
                permissionGrantedCallback = null
            } else {
                binding.root.showSnackBar(R.string.why_location_permission_needed)
            }
        }

    protected fun checkLocationPermission(
        doWithPermission: () -> Unit
    ) {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                doWithPermission.invoke()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                binding.root.showSnackBar(
                    R.string.location_permission_required,
                    R.string.try_again
                ) {
                    permissionGrantedCallback = doWithPermission
                    requestPermissionLauncher.launch(
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                }
            }
            else -> {
                permissionGrantedCallback = doWithPermission
                requestPermissionLauncher.launch(
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            }
        }
    }

    private fun stopLocationUpdates() {
        locationCallbacks.forEach {
            fusedLocationClient.removeLocationUpdates(it)
        }
        locationCallbacks.clear()
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
    protected fun fetchUserLocation(
        shouldMoveMap: Boolean = true,
        doWithLocation: (location: LatLng) -> Unit = {}
    ) {
        stopLocationUpdates()

        val locationCallback: LocationCallback by lazy {
            object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    super.onLocationResult(locationResult)
                    stopLocationUpdates()

                    locationResult.lastLocation.let { location ->
                        val latLng = LatLng(location.latitude, location.longitude)
                        doWithLocation.invoke(latLng)

                        if (shouldMoveMap) onLocationReady(location = latLng)
                    }
                }
            }
        }
        locationCallbacks.add(locationCallback)

        fusedLocationClient.requestLocationUpdates(
            LocationUtil.createLocationRequest(),
            locationCallback,
            Looper.getMainLooper()
        )
    }

    abstract fun onLocationReady(location: LatLng)
}