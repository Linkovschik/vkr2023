package com.example.demo.geojson.model

import com.example.demo.algorithm.model.MapPoint

data class RouteSegment(
        val steps: List<RouteSegmentStep>,
        val distance: Double,
        val duration: Double
) {
    fun getStartPoint(): MapPoint {
        return steps.first().start
    }

    fun getEndPoint(): MapPoint {
        return steps.last().end
    }
}