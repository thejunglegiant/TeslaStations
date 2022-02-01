package com.thejunglegiant.teslastations.presentation.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.algo.GridBasedAlgorithm
import com.google.maps.android.clustering.algo.NonHierarchicalViewBasedAlgorithm
import com.google.maps.android.clustering.algo.PreCachingAlgorithmDecorator
import com.google.maps.android.clustering.algo.ScreenBasedAlgorithm
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.thejunglegiant.teslastations.R
import com.thejunglegiant.teslastations.databinding.FragmentMapBinding
import com.thejunglegiant.teslastations.domain.entity.StationEntity
import com.thejunglegiant.teslastations.presentation.core.StatusBarMode
import org.koin.androidx.viewmodel.ext.android.viewModel

class MapFragment : Fragment(R.layout.fragment_map), OnMapReadyCallback {

    private val viewModel: MapViewModel by viewModel()
    private lateinit var binding: FragmentMapBinding

    private lateinit var map: GoogleMap
    private lateinit var clusterManager: ClusterManager<StationEntity>

    private fun addItems(list: List<StationEntity>) {
        clusterManager.addItems(list)
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

        val mapFragment = binding.map.getFragment<SupportMapFragment>()
        mapFragment.getMapAsync(this)
    }

    override fun onResume() {
        super.onResume()
        StatusBarMode.Translucent.onFragmentResumed(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        clusterManager = ClusterManager<StationEntity>(context, map).apply {
            algorithm = NonHierarchicalViewBasedAlgorithm(binding.map.width, binding.map.height)
            renderer = StationsRender(binding.map.context, map, this)
            map.setOnCameraIdleListener(this)
        }

        viewModel.stationsList.observe(viewLifecycleOwner) {
            addItems(it)
        }
    }
}