package com.thejunglegiant.teslastations.presentation.cluster

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

data class ObjectClusterItem(
    val mapClusterItem: MapClusterItem,
    val itemPosition: LatLng,
    val isSelected: Boolean
) : ClusterItem {

    override fun getPosition(): LatLng = itemPosition

    override fun getTitle(): String = mapClusterItem.clusterItemId.toString()

    override fun getSnippet(): String = mapClusterItem.clusterItemSnippet
}
