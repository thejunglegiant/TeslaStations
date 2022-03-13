package com.thejunglegiant.teslastations.presentation.cluster

abstract class MapClusterItem {
    abstract val clusterItemId: Long
    abstract val clusterItemLatitude: Double
    abstract val clusterItemLongitude: Double
    abstract val clusterItemSnippet: String
}
