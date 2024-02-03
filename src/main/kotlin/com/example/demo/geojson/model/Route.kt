package com.example.demo.geojson.model

import java.sql.Time
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

private val count: AtomicInteger = AtomicInteger(1)

data class Route (
        val segments: List<RouteSegment>,
        val coordinates: List<MyPoint>,
        val distance: Double,
        val duration: Double,
        val start: MyPoint,
        val end: MyPoint,
        var startTimeMin: Time = Time(6, 0, 0),
        var startTimeMax: Time = Time(6, 0, 0),
        var endTimeMin: Time = Time(10, 0, 0),
        var endTimeMax: Time = Time(10, 0, 0),
        var name: String? = UUID.randomUUID().toString(),
        val id: Int = count.incrementAndGet()
) {

}