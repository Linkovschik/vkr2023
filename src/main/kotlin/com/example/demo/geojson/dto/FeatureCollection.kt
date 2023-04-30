package com.example.demo.geojson.dto

data class FeatureCollection (
        val type: String,
        val features: List<Feature>,
        val bbox: List<Double>,
        val metadata: Metadata
)

data class Feature (
        val bbox: List<Double>,
        val type: String,
        val properties: Properties,
        val geometry: Geometry
)

data class Geometry (
        val coordinates: ArrayList<ArrayList<Double>>,
        val type: String
)

data class Properties (
        val segments: List<Segment>,
        val summary: Summary,

        val way_points: List<Long>
)

data class Segment (
        val distance: Double,
        val duration: Double,
        val steps: List<Step>
)

data class Step (
        val distance: Double,
        val duration: Double,
        val type: Long,
        val instruction: String,
        val name: String,

        val way_points: List<Long>
)

data class Summary (
        val distance: Double,
        val duration: Double
)

data class Metadata (
        val attribution: String,
        val service: String,
        val timestamp: Long,
        val query: Query,
        val engine: Engine
)

data class Engine (
        val version: String,

        val build_date: String,

        val graph_date: String
)

data class Query (
        val coordinates: ArrayList<ArrayList<Double>>,
        val profile: String,
        val format: String
)