package com.example.demo.algorithm.controller

import com.example.demo.geojson.model.MyPoint
import com.example.demo.algorithm.model.MapRoute
import com.example.demo.algorithm.model.MapRouteDecision
import com.example.demo.algorithm.model.MapSquare
import com.example.demo.algorithm.service.SpeedCoefficientCalculatorService
import me.piruin.geok.geometry.LineString
import me.piruin.geok.geometry.Polygon
import java.lang.Math.log
import java.lang.Math.max
import java.math.BigDecimal
import kotlin.math.log10
import kotlin.math.min

class MapSquareController(
    private val mapSquare: MapSquare,
    private val speedCoefficientCalculatorService: SpeedCoefficientCalculatorService
) {

    fun intersect(point: MyPoint): Boolean {
        return point.lng > mapSquare.botLeft.lng &&
                point.lng < mapSquare.topRight.lng &&
                point.lat > mapSquare.botLeft.lat &&
                point.lat < mapSquare.topRight.lat
    }

    fun intersect(polygon: Polygon): Boolean {
        return mapSquare.polygon.bbox.intersectWith(polygon.bbox)
    }

    fun intersect(line: LineString): Boolean {
        return mapSquare.polygon.bbox.intersectWith(line.bbox)
    }

    fun notIntersect(point: MyPoint): Boolean {
        return !intersect(point)
    }

    fun convertToArray(): ArrayList<ArrayList<Double>> {
        return arrayListOf(
            mapSquare.botLeft.convertToArray(),
            mapSquare.botRight.convertToArray(),
            mapSquare.topRight.convertToArray(),
            mapSquare.topLeft.convertToArray(),
            mapSquare.botLeft.convertToArray()
        )
    }

    fun addVisitRoute(route: MapRoute) {
        mapSquare.visitedRoutes.add(route)
    }

    fun calcCongestion(timeInMinutesOfDay: Int): BigDecimal {
        var result = BigDecimal.ZERO

        var visitedRouteDecisions = mapSquare.visitedRoutes.filterIsInstance<MapRouteDecision>()
        if (visitedRouteDecisions.isEmpty()) {
            visitedRouteDecisions = mapSquare.visitedRoutes.map { MapRouteDecision(it) }
        }

        if (visitedRouteDecisions.isEmpty())
            return result

        for (visitedRoute in visitedRouteDecisions) {
            val speed = speedDependOnPassedPathAdjustCoefficient(
                visitedRoute,
                timeInMinutesOfDay
            ) * (visitedRoute.routeData.distance / visitedRoute.durationTimeInMinutesOfDay)

            result += visitedRoute.avgCongestion.min(BigDecimal(speedCoefficientCalculatorService.calculate(speed)))
        }

        return result / BigDecimal(visitedRouteDecisions.size)

    }

    private fun speedDependOnPassedPathAdjustCoefficient(
        visitedRoute: MapRouteDecision,
        timeInMinutesOfDay: Int
    ): Double {
        val pathPart =
            max(
                0.1 + 1e-10,
                (timeInMinutesOfDay - visitedRoute.startTimeInMinutes) / visitedRoute.durationTimeInMinutesOfDay.toDouble()
            )

        return max(log10(pathPart * 10.0), 0.1)
    }

    fun clear() {
        mapSquare.visitedRoutes.clear()
    }
}