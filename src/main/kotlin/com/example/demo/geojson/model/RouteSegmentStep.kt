package com.example.demo.geojson.model

data class RouteSegmentStep (
    val distance: Double,
    val duration: Double,
    val type: Long,
    val instruction: String,
    val name: String,

    val start: MyPoint,
    val end: MyPoint
) {

}