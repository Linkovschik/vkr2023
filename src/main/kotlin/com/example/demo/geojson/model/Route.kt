package com.example.demo.geojson.model

import com.example.demo.algorithm.model.MapPoint
import java.util.concurrent.atomic.AtomicInteger

private val count: AtomicInteger = AtomicInteger(1)

data class Route (
        val segments: List<RouteSegment>,
        val coordinates: List<MapPoint>,
        val distance: Double,
        val duration: Double,
        val start: MapPoint,
        val end: MapPoint,
        val startTime: Double = 0.0,
        val id: Int = count.incrementAndGet()
) {

}