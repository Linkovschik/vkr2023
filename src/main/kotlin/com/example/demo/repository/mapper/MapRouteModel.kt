package com.example.demo.repository.mapper

import com.example.demo.geojson.model.MyPoint
import com.example.demo.geojson.model.Route
import com.example.demo.geojson.model.RouteSegment
import com.example.demo.geojson.model.RouteSegmentStep
import com.example.demo.repository.model.PointModel
import com.example.demo.repository.model.RouteModel
import com.example.demo.repository.model.RouteSegmentModel
import com.example.demo.repository.model.RouteSegmentStepModel
import org.springframework.stereotype.Component

@Component
class MapRouteModel {
    fun mapRouteToRouteModel(route: Route?): RouteModel? {
        if (route == null) return null

        val routeModel = RouteModel()
            .apply {
                segments = route.segments.mapNotNull { mapRouteSegmentToModel(it) }
                coordinates = route.coordinates.mapNotNull { mapPointToPointModel(it) }
                distance = route.distance
                duration = route.duration
                startPoint = mapPointToPointModel(route.start)
                endPoint = mapPointToPointModel(route.end)
                startTimeMin = route.startTimeMin
                startTimeMax = route.startTimeMax
                endTimeMin = route.endTimeMin
                endTimeMax = route.endTimeMax
                name = route.name
                startTime = route.startTime
                endTime = route.endTime
            }

        return routeModel
    }

    fun mapRouteSegmentToModel(routeSegment: RouteSegment?): RouteSegmentModel? {
        if (routeSegment == null) return null


        val routeSegmentModel = RouteSegmentModel()
            .apply {
                startPoint = mapPointToPointModel(routeSegment.getStartPoint())
                endPoint = mapPointToPointModel(routeSegment.getEndPoint())
                distance = routeSegment.distance
                duration = routeSegment.duration
                steps = routeSegment.steps.mapNotNull { mapStepToStepModel(it) }
            }


        return routeSegmentModel
    }

    fun mapStepToStepModel(routeSegmentStep: RouteSegmentStep?): RouteSegmentStepModel? {
        if (routeSegmentStep == null) return null

        val routeSegmentStepModel = RouteSegmentStepModel()
            .apply {
                distance = routeSegmentStep.distance
                duration = routeSegmentStep.duration
                type = routeSegmentStep.type
                instruction = routeSegmentStep.instruction
                name = routeSegmentStep.name
                start = mapPointToPointModel(routeSegmentStep.start)
                end = mapPointToPointModel(routeSegmentStep.end)
            }

        return routeSegmentStepModel
    }

    fun mapPointToPointModel(myPoint: MyPoint?): PointModel? {
        if (myPoint == null) return null

        val pointModel = PointModel()
            .apply {
                lat = myPoint.lat
                lng = myPoint.lng
            }

        return pointModel
    }


    fun mapRouteModelToRouteTo(routeModel: RouteModel?): Route? {
        if (routeModel == null) return null

        return Route(
            id = routeModel.id,
            segments = routeModel.segments.mapNotNull { mapRouteSegmentModelToRouteSegment(it) },
            coordinates = routeModel.coordinates.mapNotNull { mapPointModelToPoint(it) },
            distance = routeModel.distance,
            duration = routeModel.duration,
            start = mapPointModelToPoint(routeModel.startPoint) ?: MyPoint(),
            end = mapPointModelToPoint(routeModel.endPoint) ?: MyPoint(),
            startTimeMin = routeModel.startTimeMin,
            startTimeMax = routeModel.startTimeMax,
            endTimeMin = routeModel.endTimeMin,
            endTimeMax = routeModel.endTimeMax,
            name = routeModel.name,
            startTime = routeModel.startTime,
            endTime = routeModel.endTime
        )
    }

    fun mapRouteSegmentModelToRouteSegment(routeSegmentModel: RouteSegmentModel?): RouteSegment? {
        if (routeSegmentModel == null) return null

        return RouteSegment(
            distance = routeSegmentModel.distance,
            duration = routeSegmentModel.duration,
            steps = routeSegmentModel.steps.mapNotNull { mapStepModelToRouteSegmentStep(it) }
        )
    }

    fun mapStepModelToRouteSegmentStep(routeSegmentStepModel: RouteSegmentStepModel?): RouteSegmentStep? {
        if (routeSegmentStepModel == null) return null

        return RouteSegmentStep(
            distance = routeSegmentStepModel.distance,
            duration = routeSegmentStepModel.duration,
            type = routeSegmentStepModel.type,
            instruction = routeSegmentStepModel.instruction,
            name = routeSegmentStepModel.name,
            start = mapPointModelToPoint(routeSegmentStepModel.start) ?: MyPoint(),
            end = mapPointModelToPoint(routeSegmentStepModel.end) ?: MyPoint()
        )
    }

    fun mapPointModelToPoint(pointModel: PointModel?): MyPoint? {
        if (pointModel == null) return null

        return MyPoint(
            lat = pointModel.lat,
            lng = pointModel.lng
        )
    }
}