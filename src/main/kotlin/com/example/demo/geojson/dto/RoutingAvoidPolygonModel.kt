package com.example.demo.geojson.dto

data class RoutingAvoidPolygonModel(
        val coordinates: ArrayList<ArrayList<Double>>,
        val options: RoutingOptions?
)

data class RoutingOptions (
        val avoid_polygons: AvoidPolygonOption
)

data class AvoidPolygonOption (
        val type: String,
        val coordinates: List<List<ArrayList<ArrayList<Double>>>>
)