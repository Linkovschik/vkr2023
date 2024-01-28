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
        var startTimeMin: Time? = null,
        var startTimeMax: Time? = null,
        var endTimeMin: Time? = null,
        var endTimeMax: Time? = null,
        var name: String? = UUID.randomUUID().toString()
) {

}