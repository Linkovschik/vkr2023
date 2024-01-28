package com.example.demo.algorithm.controller

import com.example.demo.algorithm.model.MapRoute
import com.example.demo.algorithm.model.MapSquare
import com.example.demo.algorithm.service.MapCongestionService
import com.example.demo.geojson.model.MyPoint
import me.piruin.geok.geometry.LineString
import java.math.BigDecimal

class MapMatrixController(
    val mapSquares: List<MapSquare>,
    private val mapCongestionService: MapCongestionService
) {

    var maxCongestion: BigDecimal = mapCongestionService.calcMaxCongestion(mapSquares)
    var mediumCongestion: BigDecimal = mapCongestionService.calcAvgCongestion(mapSquares)

    fun updateMatrixState(routes: List<MapRoute>) {
        clearSquares()
        routes.forEach { makeRouteVisitSquares(it) }
    }

    fun makeRouteVisitSquares(route: MapRoute) {
        val mapRouteController = MapRouteController(route, mapCongestionService, this)
        mapRouteController.clear()

        var currentSquares = mapSquares.toMutableList()
        val nextSquares = currentSquares.toMutableList()

        getLineStringArray(route.routeData.coordinates).forEach { line ->
            currentSquares.forEach { square ->
                val mapSquareController = MapSquareController(square)
                if (mapSquareController.intersect(line)) {
                    mapSquareController.addVisitRoute(route)
                    mapRouteController.addVisitSquare(square)
                    nextSquares.remove(square)
                }
            }
            currentSquares = nextSquares.toMutableList()
        }
    }

    fun clearSquares() {
        mapSquares.forEach { MapSquareController(it).clear() }
    }

    private fun getLineStringArray(mapPoints: List<MyPoint>): List<LineString> {
        val result = arrayListOf<LineString>()

        for (i in 0..(mapPoints.size - 2) step 2) {
            result.add(LineString(mapPoints[i].lat to mapPoints[i].lng, mapPoints[i + 1].lat to mapPoints[i + 1].lng))
        }

        return result
    }
}