package com.example.demo.geojson.service

import com.example.demo.geojson.dto.Feature
import com.example.demo.geojson.dto.Segment
import com.example.demo.geojson.dto.Step
import com.example.demo.geojson.model.Route
import com.example.demo.geojson.model.RouteSegment
import com.example.demo.geojson.model.RouteSegmentStep
import com.example.demo.algorithm.model.MapPoint
import org.springframework.stereotype.Component

@Component
class MappingService {

    fun mapFeatureToRoute(feature: Feature): Route {
        val routeCoordinates = feature.geometry.coordinates
        val properties = feature.properties

        return Route(
                properties.segments.map { mapSegment(it, routeCoordinates) },
                routeCoordinates.map { MapPoint(it.first(), it.last()) },
                properties.summary.distance,
                properties.summary.duration,
                MapPoint(routeCoordinates[properties.way_points.first().toInt()].first(), routeCoordinates[properties.way_points.first().toInt()].last()),
                MapPoint(routeCoordinates[properties.way_points.last().toInt()].first(), routeCoordinates[properties.way_points.last().toInt()].last())
        )
    }

    private fun mapSegment(segment: Segment, routeCoordinates: ArrayList<ArrayList<Double>>): RouteSegment {
        return RouteSegment(
                segment.steps.map { mapStep(it, routeCoordinates) },
                segment.distance,
                segment.duration
        )
    }

    private fun mapStep(step: Step, routeCoordinates: ArrayList<ArrayList<Double>>): RouteSegmentStep {
        return RouteSegmentStep(
                step.distance,
                step.duration,
                step.type,
                step.instruction,
                step.name,
                MapPoint(routeCoordinates[step.way_points.first().toInt()].first(), routeCoordinates[step.way_points.first().toInt()].last()),
                MapPoint(routeCoordinates[step.way_points.last().toInt()].first(), routeCoordinates[step.way_points.last().toInt()].last())
        )
    }
}