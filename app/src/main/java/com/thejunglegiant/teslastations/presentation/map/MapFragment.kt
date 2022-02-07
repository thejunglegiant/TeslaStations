package com.thejunglegiant.teslastations.presentation.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.maps.android.PolyUtil
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.algo.NonHierarchicalViewBasedAlgorithm
import com.thejunglegiant.teslastations.R
import com.thejunglegiant.teslastations.databinding.FragmentMapBinding
import com.thejunglegiant.teslastations.domain.entity.StationEntity
import com.thejunglegiant.teslastations.extensions.dp
import com.thejunglegiant.teslastations.extensions.showSnackBar
import com.thejunglegiant.teslastations.presentation.core.StatusBarMode
import com.thejunglegiant.teslastations.utils.LocationUtil
import org.koin.androidx.viewmodel.ext.android.viewModel

@SuppressLint("MissingPermission")
class MapFragment : Fragment(R.layout.fragment_map), OnMapReadyCallback {

    private val viewModel: MapViewModel by viewModel()

    // General layout
    private lateinit var binding: FragmentMapBinding

    // Stations' details dialog
    private val infoDialog by lazy {
        binding.bottomSheetInfo
    }
    private val bottomSheetBehavior by lazy {
        BottomSheetBehavior.from(infoDialog.bottomSheetLayout)
    }

    // Google Map
    private lateinit var map: GoogleMap
    private lateinit var clusterManager: ClusterManager<StationEntity>

    // User location
    private var locationCallbacks: MutableList<LocationCallback> = mutableListOf()
    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(requireActivity())
    }
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                fetchUserLocation()
            } else {
                binding.root.showSnackBar(R.string.why_location_permission_needed)
            }
        }

    // Current route polyline
    var polyline: Polyline? = null

    override fun onResume() {
        super.onResume()
        StatusBarMode.Translucent.onFragmentResumed(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMapBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupMap()
        hideBottomDialog()
        setListeners()
    }

    private fun setupMap() {
        val mapFragment = binding.map.getFragment<SupportMapFragment>()
        mapFragment.getMapAsync(this)
    }

    private fun hideBottomDialog() {
        // set initial state to STATE_HIDDEN
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun openInfoDialog(station: StationEntity) {
        infoDialog.title.text = station.stationTitle
        infoDialog.location.text = "${station.country}, ${station.city}"
        infoDialog.descriptionText.text = station.description
        infoDialog.descriptionPhone.text = station.contacts.first().number
        infoDialog.descriptionHours.text = station.hours.ifEmpty {
            getString(R.string.station_description_hours_placeholder)
        }
        infoDialog.btnStationDirection.setOnClickListener {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                viewModel.getRoute(
                    LatLng(location.latitude, location.longitude),
                    LatLng(station.latitude, station.longitude)
                )
            }
        }
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    private fun setListeners() {
        binding.mapDefault.btnCurrentLocation.setOnClickListener {
            when {
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED -> {
                    fetchUserLocation()
                }
                shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                    binding.root.showSnackBar(
                        R.string.location_permission_required,
                        R.string.try_again
                    ) {
                        requestPermissionLauncher.launch(
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                    }
                }
                else -> {
                    requestPermissionLauncher.launch(
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                }
            }
        }
        binding.mapDefault.btnMapLayer.setOnClickListener {
            checkMapReadyThen {
                map.mapType = when (map.mapType) {
                    GoogleMap.MAP_TYPE_SATELLITE -> GoogleMap.MAP_TYPE_NORMAL
                    else -> GoogleMap.MAP_TYPE_SATELLITE
                }
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
    private fun fetchUserLocation() {
        stopLocationUpdates()

        val locationCallback: LocationCallback by lazy {
            object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    super.onLocationResult(locationResult)
                    stopLocationUpdates()

                    locationResult.lastLocation.let { location ->
                        val latLng = LatLng(location.latitude, location.longitude)
                        checkMapReadyThen {
                            map.isMyLocationEnabled = true
                            map.uiSettings.isMyLocationButtonEnabled = false
                            moveMap(latLng)
                        }
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

    override fun onMapReady(googleMap: GoogleMap) {
        // Setup map
        map = googleMap

        // Setup clusterization
        clusterManager = ClusterManager<StationEntity>(context, map).apply {
            setOnClusterItemClickListener {
                openInfoDialog(it)
                return@setOnClusterItemClickListener true
            }
            algorithm = NonHierarchicalViewBasedAlgorithm(binding.map.width, binding.map.height)
            renderer = StationsRender(binding.map.context, map, this)
            map.setOnCameraIdleListener(this)
        }

        // Load data
        viewModel.stationsList.observe(viewLifecycleOwner) {
            addItems(it)
        }

        viewModel.route.observe(viewLifecycleOwner) {
            hideBottomDialog()
            polyline?.remove()
            polyline = map.addPolyline(
                PolylineOptions()
                    .addAll(PolyUtil.decode(it))
                    .width(5f.dp)
                    .color(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.blue
                        )
                    )
            )
        }
    }

    private fun addItems(list: List<StationEntity>) {
        clusterManager.addItems(list)
    }

    private fun moveMap(
        latLng: LatLng,
        zoom: Float = map.maxZoomLevel * DEFAULT_MAX_MAP_ZOOM_MULTIPLIER,
        animate: Boolean = true
    ) {
        val cu = CameraUpdateFactory.newLatLngZoom(
            latLng,
            zoom
        )
        if (animate)
            map.animateCamera(cu, 500, null)
        else
            map.moveCamera(cu)
    }

    private fun <T> checkMapReadyThen(stuffToDo: () -> T): T? {
        return if (::map.isInitialized) {
            stuffToDo()
        } else {
            null
        }
    }

    companion object {
        private const val DEFAULT_MAX_MAP_ZOOM_MULTIPLIER = 0.75f
    }
}