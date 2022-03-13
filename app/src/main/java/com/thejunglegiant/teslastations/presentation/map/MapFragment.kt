package com.thejunglegiant.teslastations.presentation.map

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
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
import com.thejunglegiant.teslastations.R
import com.thejunglegiant.teslastations.databinding.FragmentMapBinding
import com.thejunglegiant.teslastations.domain.entity.BoundsItem
import com.thejunglegiant.teslastations.domain.entity.StationEntity
import com.thejunglegiant.teslastations.domain.mapper.toLatLngBounds
import com.thejunglegiant.teslastations.extensions.*
import com.thejunglegiant.teslastations.presentation.cluster.MyClusterManager
import com.thejunglegiant.teslastations.presentation.core.BaseLocationFragment
import com.thejunglegiant.teslastations.presentation.core.StatusBarMode
import com.thejunglegiant.teslastations.presentation.core.ViewStateHandler
import com.thejunglegiant.teslastations.presentation.list.ListStationsFragment
import com.thejunglegiant.teslastations.presentation.list.filter.RegionFilterBottomDialog
import com.thejunglegiant.teslastations.presentation.map.models.MapAction
import com.thejunglegiant.teslastations.presentation.map.models.MapEvent
import com.thejunglegiant.teslastations.presentation.map.models.MapViewState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel

@SuppressLint("MissingPermission")
class MapFragment : BaseLocationFragment<FragmentMapBinding>(FragmentMapBinding::inflate),
    ViewStateHandler<MapViewState>, OnMapReadyCallback, GoogleMap.OnMapLoadedCallback {

    private val viewModel: MapViewModel by viewModel()

    // Stations' details dialog
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>

    // Google Map
    private lateinit var map: GoogleMap
    private lateinit var clusterManager: MyClusterManager

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
        initBottomDialog()
        initOnBackPressed()

        flowCollectLatest(viewModel.viewState, ::render)
        flowCollect(viewModel.action, ::handleAction)
    }

    private fun initOnBackPressed() {
        activity?.onBackPressedDispatcher?.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (bottomSheetBehavior.isHidden) {
                        activity?.onBackPressed()
                    } else {
                        bottomSheetBehavior.hide()
                    }
                }
            }
        )
    }

    private fun initBottomDialog() {
        bottomSheetBehavior = BottomSheetBehavior.from(binding.infoDialog.bottomSheetLayout)
        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    clusterManager.clearSelected()
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) = Unit
        })
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

    private fun setupMap() {
        val mapFragment = binding.map.getFragment<SupportMapFragment>()
        mapFragment.getMapAsync(this)
    }

    override fun render(state: MapViewState) {
        binding.loading.root.isVisible = state == MapViewState.Loading
        if (state !is MapViewState.Direction) binding.mapDefault.directionInfo.hide()
        if (state !is MapViewState.Error) {
            polyline?.remove()
            bottomSheetBehavior.hide()
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
                selectItem(state.item)
                openInfoDialog(state.item)
            }
            MapViewState.Loading -> Unit
            MapViewState.Idle -> Unit
        }
    }

    private fun handleAction(action: MapAction) {
        when (action) {
            is MapAction.ItemDeleted -> {
                removeItem(action.item)
                binding.root.showSnackBar(
                    R.string.station_deleted,
                    R.string.undo,
                    Snackbar.LENGTH_LONG
                ) {
                    viewModel.obtainEvent(MapEvent.UndoItemDeleteClicked(action.item))
                }
            }
        }
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
        bottomSheetBehavior.collapse()
    }

    private fun setItems(list: List<StationEntity>) {
        lifecycleScope.launch(Dispatchers.Default) {
            clusterManager.clearItems()
            clusterManager.addItems(list.map { Pair(it, false) })

            withContext(Dispatchers.Main) {
                clusterManager.cluster()
            }
        }
    }

    private fun selectItem(item: StationEntity) {
        clusterManager.setSelected(item)
        clusterManager.cluster()
    }

    private fun removeItem(item: StationEntity) {
        clusterManager.removeItem(item)
        clusterManager.cluster()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        // Setup map
        map = googleMap
        map.setOnMapLoadedCallback(this)

        // Setup clusterization
        clusterManager =
            MyClusterManager(binding.map.context, map, binding.map.width, binding.map.height) {
                viewModel.obtainEvent(MapEvent.ItemClicked(it.mapClusterItem as StationEntity))
                return@MyClusterManager true
            }
    }

    override fun onMapLoaded() {
        var hasResult = false
        setFragmentResultListener(ListStationsFragment.REQUEST_KEY_STATION_RESULT) { _, bundle ->
            hasResult = true
            val station =
                bundle.getSerializable(ListStationsFragment.KEY_STATION) as StationEntity?

            station?.let {
                viewModel.obtainEvent(MapEvent.ItemClicked(station))
            }
        }

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

    override fun onLocationReady(location: LatLng) {
        checkMapReadyThen {
            map.isMyLocationEnabled = true
            map.uiSettings.isMyLocationButtonEnabled = false
            moveMap(location)
        }
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