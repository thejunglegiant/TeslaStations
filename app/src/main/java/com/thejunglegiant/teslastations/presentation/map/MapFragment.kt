package com.thejunglegiant.teslastations.presentation.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.algo.NonHierarchicalViewBasedAlgorithm
import com.thejunglegiant.teslastations.R
import com.thejunglegiant.teslastations.databinding.FragmentMapBinding
import com.thejunglegiant.teslastations.domain.entity.BoundsItem
import com.thejunglegiant.teslastations.domain.entity.StationEntity
import com.thejunglegiant.teslastations.domain.mapper.toLatLngBounds
import com.thejunglegiant.teslastations.extensions.*
import com.thejunglegiant.teslastations.presentation.core.BaseBindingFragment
import com.thejunglegiant.teslastations.presentation.core.StatusBarMode
import com.thejunglegiant.teslastations.presentation.list.filter.RegionFilterBottomDialog
import com.thejunglegiant.teslastations.presentation.list.models.ListEvent
import com.thejunglegiant.teslastations.presentation.map.models.MapEvent
import com.thejunglegiant.teslastations.presentation.map.models.MapViewState
import com.thejunglegiant.teslastations.utils.ARG_STATION_LOCATION
import com.thejunglegiant.teslastations.utils.LocationUtil
import org.koin.androidx.viewmodel.ext.android.viewModel


@SuppressLint("MissingPermission")
class MapFragment : BaseBindingFragment<FragmentMapBinding>(FragmentMapBinding::inflate),
    OnMapReadyCallback, GoogleMap.OnMapLoadedCallback {

    private val viewModel: MapViewModel by viewModel()

    // Stations' details dialog
    private var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>? = null

    // Google Map
    private lateinit var map: GoogleMap
    private lateinit var clusterManager: ClusterManager<StationEntity>

    // User location
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

    // Current route polyline
    private var polyline: Polyline? = null

    override fun onResume() {
        super.onResume()
        StatusBarMode.Translucent.onFragmentResumed(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupMap()
        hideBottomDialog()
        setListeners()
        initViewModel()
    }

    private fun setupMap() {
        val mapFragment = binding.map.getFragment<SupportMapFragment>()
        mapFragment.getMapAsync(this)
    }

    private fun hideBottomDialog() {
        // set initial state to STATE_HIDDEN
        bottomSheetBehavior = BottomSheetBehavior.from(binding.infoDialog.bottomSheetLayout)
        bottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun openInfoDialog(station: StationEntity) {
        checkMapReadyThen {
            moveMap(LatLng(station.latitude, station.longitude))
        }
        binding.infoDialog.title.text = station.stationTitle
        binding.infoDialog.location.text = "${station.country}, ${station.city}"
        binding.infoDialog.descriptionText.text = station.description
        binding.infoDialog.descriptionPhone.text = station.contactNumber.ifEmpty {
            getString(R.string.station_description_number_placeholder)
        }
        binding.infoDialog.descriptionHours.text = station.hours.ifEmpty {
            getString(R.string.station_description_hours_placeholder)
        }
        binding.infoDialog.btnDeleteStation.setOnClickListener {
            viewModel.obtainEvent(MapEvent.ItemDeleteClicked(station))
        }
        binding.infoDialog.btnStationDirection.setOnClickListener {
            checkLocationPermission {
                viewModel.obtainEvent(MapEvent.ItemDirectionClicked)
                fetchUserLocation(shouldMoveMap = false) { location ->
                    viewModel.obtainEvent(
                        MapEvent.ItemDirectionFound(
                            from = location,
                            to = LatLng(station.latitude, station.longitude)
                        )
                    )
                }
            }
        }
        bottomSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    private fun setListeners() {
        binding.mapDefault.btnList.setOnClickListener {
            findNavController().navigate(
                MapFragmentDirections
                    .actionMapFragmentToListStationsFragment()
            )
        }
        binding.mapDefault.btnCurrentLocation.setOnClickListener {
            checkLocationPermission { fetchUserLocation() }
        }
        binding.mapDefault.btnMapLayer.setOnClickListener {
            viewModel.obtainEvent(MapEvent.MapModeClicked)
        }
        binding.mapDefault.btnFilter.setOnClickListener {
            setFragmentResultListener(RegionFilterBottomDialog.REQUEST_KEY_FILTER_RESULT) { _, bundle ->
                val bounds =
                    bundle.getSerializable(RegionFilterBottomDialog.KEY_BOUNDS) as BoundsItem?

                bounds?.let {
                    moveMap(it.toLatLngBounds())
                }
            }

            findNavController().navigate(
                MapFragmentDirections
                    .actionMapFragmentToRegionFilterBottomDialog()
            )
        }
    }

    private fun initViewModel() {
        viewModel.viewState.observe(viewLifecycleOwner) {
            loge(
                TAG, when (it) {
                    is MapViewState.Direction -> "Direction"
                    is MapViewState.Display -> "Display"
                    is MapViewState.Error -> "Error"
                    is MapViewState.ItemDeleted -> "ItemDeleted"
                    is MapViewState.ItemDetails -> "ItemDetails"
                    MapViewState.Loading -> "Loading"
                }
            )
            binding.loading.root.isVisible = false
            when (it) {
                is MapViewState.Direction -> {
                    hideBottomDialog()
                    polyline?.remove()
                    polyline = map.addPolyline(
                        PolylineOptions()
                            .addAll(it.points)
                            .width(5f.dp)
                            .color(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.thunderbird
                                )
                            )
                    )
                    moveMap(it.bounds)
                }
                is MapViewState.Display -> {
                    polyline?.remove()
                    hideBottomDialog()

                    it.data
                        .filter { item -> item.status != StationEntity.Status.HIDDEN }
                        .also { visibleList ->
                            setItems(visibleList)
                        }

                    it.settings?.let { settings ->
                        map.mapType = if (settings.defaultMapLayer) {
                            GoogleMap.MAP_TYPE_NORMAL
                        } else {
                            GoogleMap.MAP_TYPE_SATELLITE
                        }
                    }
                }
                is MapViewState.Error -> {
                    when {
                        it.msgRes != null -> {
                            binding.root.showSnackBar(it.msgRes)
                        }
                        it.msg != null -> {
                            binding.root.showSnackBar(it.msg)
                        }
                    }
                }
                is MapViewState.ItemDetails -> {
                    polyline?.remove()
                    openInfoDialog(it.item)
                }
                is MapViewState.ItemDeleted -> {
                    when (it.item.status) {
                        StationEntity.Status.VISIBLE -> {
                            addItem(it.item)
                            openInfoDialog(it.item)
                        }
                        StationEntity.Status.HIDDEN -> {
                            polyline?.remove()
                            hideBottomDialog()

                            removeItem(it.item.copy(status = StationEntity.Status.VISIBLE))
                            binding.root.showSnackBar(
                                R.string.station_deleted,
                                R.string.undo,
                                Snackbar.LENGTH_LONG
                            ) {
                                viewModel.obtainEvent(MapEvent.ItemDeleteClicked(it.item))
                            }
                        }
                    }

                }
                MapViewState.Loading -> {
                    hideBottomDialog()
                    binding.loading.root.isVisible = true
                }
            }
        }
    }

    private fun checkLocationPermission(
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
    private fun fetchUserLocation(
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

                        checkMapReadyThen {
                            map.isMyLocationEnabled = true
                            map.uiSettings.isMyLocationButtonEnabled = false
                            if (shouldMoveMap) moveMap(latLng)
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
        map.setOnMapLoadedCallback(this)

        // Setup clusterization
        clusterManager = ClusterManager<StationEntity>(context, map).apply {
            setOnClusterItemClickListener { station ->
                viewModel.obtainEvent(MapEvent.ItemClicked(station))
                return@setOnClusterItemClickListener true
            }
            algorithm = NonHierarchicalViewBasedAlgorithm(binding.map.width, binding.map.height)
            renderer = StationsRender(binding.map.context, map, this)
            map.setOnCameraIdleListener(this)
        }
    }

    override fun onMapLoaded() {
        viewModel.obtainEvent(MapEvent.EnterScreen)

        getArgsLiveData<StationEntity>(ARG_STATION_LOCATION)?.observe(viewLifecycleOwner) { result ->
            viewModel.obtainEvent(MapEvent.ItemClicked(result))
        }

        removeArgsLiveData<StationEntity>(ARG_STATION_LOCATION)
    }

    private fun setItems(list: List<StationEntity>) {
        clusterManager.clearItems()
        clusterManager.addItems(list)
        clusterManager.cluster()
    }

    private fun addItem(item: StationEntity) {
        clusterManager.addItem(item)
        clusterManager.cluster()
    }

    private fun removeItem(item: StationEntity) {
        clusterManager.removeItem(item)
        clusterManager.cluster()
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

    private fun moveMap(
        bounds: LatLngBounds,
        padding: Int = 50,
        animate: Boolean = true
    ) {
        val cu = CameraUpdateFactory.newLatLngBounds(bounds, padding)

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
        val TAG: String = MapFragment::class.java.simpleName
        private const val DEFAULT_MAX_MAP_ZOOM_MULTIPLIER = .75f
    }
}