package com.thejunglegiant.teslastations.presentation.cluster

import android.content.Context
import android.util.SparseIntArray
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.thejunglegiant.teslastations.R
import com.thejunglegiant.teslastations.extensions.bitmapFromRes

class StationsRender(
    private val context: Context, map: GoogleMap, clusterManager: ClusterManager<ObjectClusterItem>
) : DefaultClusterRenderer<ObjectClusterItem>(context, map, clusterManager) {

    private val iconStyles by lazy {
        SparseIntArray().apply {
            put(20, ContextCompat.getColor(context, R.color.cluster_item_up_to_20))
            put(50, ContextCompat.getColor(context, R.color.cluster_item_up_to_50))
            put(100, ContextCompat.getColor(context, R.color.cluster_item_up_to_100))
            put(200, ContextCompat.getColor(context, R.color.cluster_item_up_to_200))
            put(500, ContextCompat.getColor(context, R.color.cluster_item_up_to_500))
            put(1000, ContextCompat.getColor(context, R.color.cluster_item_up_to_1000))
            put(0, ContextCompat.getColor(context, R.color.cluster_item_more_then_1000))
        }
    }

    override fun getColor(clusterSize: Int): Int {
        return when (clusterSize) {
            in 0 until 20 -> iconStyles.get(20)
            in 20 until 50 -> iconStyles.get(50)
            in 50 until 100 -> iconStyles.get(100)
            in 100 until 200 -> iconStyles.get(200)
            in 200 until 500 -> iconStyles.get(500)
            in 500 until 1000 -> iconStyles.get(1000)
            else -> iconStyles.get(0)
        }
    }

    override fun onBeforeClusterItemRendered(
        item: ObjectClusterItem,
        markerOptions: MarkerOptions
    ) {
        super.onBeforeClusterItemRendered(item, markerOptions)
        markerOptions
            .icon(
                bitmapFromRes(
                    context.resources,
                    if (item.isSelected) {
                        R.drawable.ic_selected_marker
                    } else {
                        R.drawable.ic_marker
                    }
                )
            )
    }

    override fun onClusterItemUpdated(item: ObjectClusterItem, marker: Marker) {
        super.onClusterItemUpdated(item, marker)

        marker
            .setIcon(
                bitmapFromRes(
                    context.resources,
                    if (item.isSelected) {
                        R.drawable.ic_selected_marker
                    } else {
                        R.drawable.ic_marker
                    }
                )
            )
    }
}