package com.example.demo.geojson.model

import java.sql.Time
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger


data class Route (
        val id: Int? = null,
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
        var startTime: Time? = null,
        var endTime: Time? = null
) {

}