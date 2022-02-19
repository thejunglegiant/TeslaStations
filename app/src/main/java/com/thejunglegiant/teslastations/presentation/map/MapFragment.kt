package com.thejunglegiant.teslastations.presentation.map

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.datastore.preferences.core.edit
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
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
import com.thejunglegiant.teslastations.presentation.core.BaseLocationFragment
import com.thejunglegiant.teslastations.presentation.core.StatusBarMode
import com.thejunglegiant.teslastations.presentation.core.ViewStateHandler
import com.thejunglegiant.teslastations.presentation.list.filter.RegionFilterBottomDialog
import com.thejunglegiant.teslastations.presentation.map.models.MapEvent
import com.thejunglegiant.teslastations.presentation.map.models.MapViewState
import com.thejunglegiant.teslastations.utils.ARG_STATION_LOCATION
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel

@SuppressLint("MissingPermission")
class MapFragment : BaseLocationFragment<FragmentMapBinding>(FragmentMapBinding::inflate),
    ViewStateHandler<MapViewState>, OnMapReadyCallback, GoogleMap.OnMapLoadedCallback {

    private val viewModel: MapViewModel by viewModel()

    // Stations' details dialog
    private var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>? = null

    // Google Map
    private lateinit var map: GoogleMap
    private lateinit var clusterManager: ClusterManager<StationEntity>

    // Current route polyline
    private var polyline: Polyline? = null

    override fun onResume() {
        super.onResume()
        StatusBarMode.Translucent.onFragmentResumed(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupMap()
        setListeners()
        hideBottomDialog()

        flowCollectLatest(viewModel.viewState, ::render)
    }

    private fun setupMap() {
        val mapFragment = binding.map.getFragment<SupportMapFragment>()
        mapFragment.getMapAsync(this)
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
            lifecycleScope.launch {
                context?.dataStore?.edit { prefs ->
                    val value = prefs[mapModeDefaultKey] ?: false
                    prefs[mapModeDefaultKey] = !value
                }
            }
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
        binding.mapDefault.btnCloseDirection.setOnClickListener {
            viewModel.obtainEvent(MapEvent.ItemDirectionCloseClicked)
        }
    }

    override fun render(state: MapViewState) {
        binding.loading.root.isVisible = state == MapViewState.Loading
        if (state !is MapViewState.Direction) binding.mapDefault.directionInfo.hide()
        if (state !is MapViewState.Error) {
            polyline?.remove()
            hideBottomDialog()
        }

        when (state) {
            is MapViewState.Direction -> {
                binding.mapDefault.startingPoint.text = state.direction.start
                binding.mapDefault.endingPoint.text = state.direction.destination
                binding.mapDefault.directionInfo.show()

                polyline = map.addPolyline(
                    PolylineOptions()
                        .addAll(state.direction.points)
                        .width(5f.dp)
                        .color(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.thunderbird
                            )
                        )
                )
                moveMap(state.direction.bounds)
            }
            is MapViewState.Display -> {
                state.deletedItem?.let { station ->
                    removeItem(station)
                    binding.root.showSnackBar(
                        R.string.station_deleted,
                        R.string.undo,
                        Snackbar.LENGTH_LONG
                    ) {
                        viewModel.obtainEvent(MapEvent.UndoItemDeleteClicked(station))
                    }
                }

                state.data
                    .filter { item -> item.status != StationEntity.Status.HIDDEN }
                    .also { visibleList ->
                        setItems(visibleList)
                    }
            }
            is MapViewState.Error -> {
                when {
                    state.msgRes != null -> {
                        binding.root.showSnackBar(state.msgRes)
                    }
                    state.msg != null -> {
                        binding.root.showSnackBar(state.msg)
                    }
                }
            }
            is MapViewState.ItemDetails -> {
                if (state.addToMap) addItem(state.item)
                openInfoDialog(state.item)
            }
            MapViewState.Loading -> {}
        }
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

    override fun onLocationReady(location: LatLng) {
        checkMapReadyThen {
            map.isMyLocationEnabled = true
            map.uiSettings.isMyLocationButtonEnabled = false
            moveMap(location)
        }
    }

    override fun onMapLoaded() {
        var hasResult = false
        getArgsLiveData<StationEntity>(ARG_STATION_LOCATION)?.observe(viewLifecycleOwner) { result ->
            hasResult = true
            viewModel.obtainEvent(MapEvent.ItemClicked(result))
        }

        removeArgsLiveData<StationEntity>(ARG_STATION_LOCATION)

        if (!hasResult) viewModel.obtainEvent(MapEvent.EnterScreen)

        context?.dataStore?.data?.let {
            flowCollect(it) { prefs ->
                map.mapType =
                    if (prefs[mapModeDefaultKey] == null || prefs[mapModeDefaultKey] == true) {
                        GoogleMap.MAP_TYPE_NORMAL
                    } else {
                        GoogleMap.MAP_TYPE_SATELLITE
                    }
            }
        }
    }

    private fun setItems(list: List<StationEntity>) {
        lifecycleScope.launch(Dispatchers.Default) {
            clusterManager.clearItems()
            clusterManager.addItems(list)

            withContext(Dispatchers.Main) {
                clusterManager.cluster()
            }
        }
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