package com.example.demo.geojson.model

data class RouteSegment(
        val steps: List<RouteSegmentStep>,
        val distance: Double,
        val duration: Double
) {
    fun getStartPoint(): MyPoint {
        return steps.first().start
    }

    fun getEndPoint(): MyPoint {
        return steps.last().end
    }
}