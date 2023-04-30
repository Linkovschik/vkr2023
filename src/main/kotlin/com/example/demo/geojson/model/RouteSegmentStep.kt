package com.example.demo.geojson.model

import com.example.demo.algorithm.model.MapPoint

data class RouteSegmentStep (
        val distance: Double,
        val duration: Double,
        val type: Long,
        val instruction: String,
        val name: String,

        val start: MapPoint,
        val end: MapPoint
) {

}