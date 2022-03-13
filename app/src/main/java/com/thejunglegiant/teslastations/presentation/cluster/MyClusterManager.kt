package com.thejunglegiant.teslastations.presentation.cluster

import android.content.Context
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.algo.NonHierarchicalViewBasedAlgorithm

class MyClusterManager(
    context: Context,
    map: GoogleMap,
    mapWidth: Int,
    mapHeight: Int,
    onItemClickListener: (item: ObjectClusterItem) -> Boolean
) {

    private val clusterManager: ClusterManager<ObjectClusterItem> =
        ClusterManager<ObjectClusterItem>(context, map)
            .apply {
                setOnClusterItemClickListener {
//                    this.removeItem(it)
                    onItemClickListener.invoke(it)
                }
                algorithm = NonHierarchicalViewBasedAlgorithm(mapWidth, mapHeight)
                renderer = StationsRender(context, map, this)
                map.setOnCameraIdleListener(this)
            }

    private val mapItems: MutableMap<Long, ObjectClusterItem> = mutableMapOf()

    fun addItem(item: Pair<MapClusterItem, Boolean>) {
        addItems(listOf(item))
    }

    fun addItems(items: List<Pair<MapClusterItem, Boolean>>) {
        items.forEach {
            if (mapItems.keys.contains(it.first.clusterItemId)) {
                mapItems.remove(it.first.clusterItemId)
            }
        }
        val newMapItems = items.map { item ->
            ObjectClusterItem(
                item.first,
                LatLng(item.first.clusterItemLatitude, item.first.clusterItemLongitude),
                item.second
            )
        }
        newMapItems
            .map { it.copy(isSelected = !it.isSelected) }
            .forEach {
                clusterManager.removeItem(it)
            }

        mapItems.putAll(newMapItems.associateBy { it.mapClusterItem.clusterItemId })
        clusterManager.addItems(mapItems.values.toList())
    }

    fun removeItem(item: MapClusterItem) {
        removeItems(listOf(item))
    }

    fun removeItems(items: List<MapClusterItem>) {
        items.forEach {
            if (mapItems.containsKey(it.clusterItemId)) {
                clusterManager.removeItem(mapItems[it.clusterItemId])
                mapItems.remove(it.clusterItemId)
            }
        }
    }

    fun setSelected(item: MapClusterItem) {
        setSelected(listOf(item))
    }

    fun setSelected(items: List<MapClusterItem>) {
        clearSelected()
        addItems(items.map { item -> Pair(item, true) })
    }

    fun clearSelected() {
        addItems(
            mapItems
                .asSequence()
                .filter { clusterItem -> clusterItem.value.isSelected }
                .map {
                    Pair(
                        it.value.mapClusterItem,
                        false
                    )
                }
                .toList()
        )
        cluster()
    }

    fun clearItems() {
        mapItems.clear()
        clusterManager.clearItems()
    }

    fun cluster() {
        clusterManager.cluster()
    }
}