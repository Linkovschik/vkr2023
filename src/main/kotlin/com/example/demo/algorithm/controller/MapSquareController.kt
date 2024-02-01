package com.example.demo.algorithm.controller

import com.example.demo.geojson.model.MyPoint
import com.example.demo.algorithm.model.MapRoute
import com.example.demo.algorithm.model.MapRouteDecision
import com.example.demo.algorithm.model.MapSquare
import me.piruin.geok.geometry.LineString
import me.piruin.geok.geometry.Polygon
import java.math.BigDecimal

class MapSquareController(private val mapSquare: MapSquare) {

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

        val visitedRouteDecisions = mapSquare.visitedRoutes.filterIsInstance<MapRouteDecision>()
        for (visitedRoute in visitedRouteDecisions) {
            result += BigDecimal(visitedRoute.routeData.distance / (visitedRoute.durationTimeInMinutesOfDay))
        }

        return result / BigDecimal(visitedRouteDecisions.size)
    }

    fun clear() {
        mapSquare.visitedRoutes.clear()
    }
}