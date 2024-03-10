package com.example.demo.algorithm.controller

import com.example.demo.algorithm.model.MapRoute
import com.example.demo.algorithm.model.MapSquare
import com.example.demo.algorithm.route.visit.MapRouteVisitController
import com.example.demo.algorithm.service.SpeedCoefficientCalculatorService
import com.example.demo.algorithm.square.congestion.MapSquareCongestionController
import com.example.demo.algorithm.square.init.MapSquareInitController
import com.example.demo.algorithm.square.visit.MapSquareVisitController
import com.example.demo.geojson.model.MyPoint
import me.piruin.geok.geometry.LineString

class MapMatrixController(
    private val mapSquares: List<MapSquare>,
    private val speedCoefficientCalculatorService: SpeedCoefficientCalculatorService,
    private val startTimeInMinutes: Int,
    private val endTimeInMinutes: Int,
    private val timeWindowInMinutes: Int
) {

    fun updateMatrixState(routes: List<MapRoute>) {
        initAlgorithmDataForAllSquares()
        updateVisitorsOfSquares(routes)
        updateCongestionOfSquares()
    }

    private fun initAlgorithmDataForAllSquares() {
        mapSquares.forEach {
            MapSquareInitController(it, startTimeInMinutes, endTimeInMinutes, timeWindowInMinutes)
                .initAlgorithmDataForSquare()
        }
    }

    private fun updateVisitorsOfSquares(routes: List<MapRoute>) {
        routes.forEach { makeRouteVisitSquares(it) }
    }

    private fun updateCongestionOfSquares() {
        mapSquares.forEach {
            MapSquareCongestionController(
                it,
                speedCoefficientCalculatorService,
                startTimeInMinutes,
                endTimeInMinutes,
                timeWindowInMinutes
            ).updateCongestionOfSquare()
        }
    }

    fun makeRouteVisitSquares(route: MapRoute) {
        val mapRouteController = MapRouteVisitController(route)
        mapRouteController.clear()

        var currentSquares = mapSquares.toMutableList()
        val nextSquares = currentSquares.toMutableList()

        getLineStringArray(mapRouteController.getRouteDataCoordinates()).forEach { line ->
            currentSquares.forEach { square ->
                val mapSquareVisitController = MapSquareVisitController(square)
                if (mapSquareVisitController.intersect(line)) {
                    mapSquareVisitController.addVisitRoute(route)
                    mapRouteController.addVisitSquare(square)
                    nextSquares.remove(square)
                }
            }
            currentSquares = nextSquares.toMutableList()
        }
    }

    fun makeRouteStopVisitSquares(route: MapRoute) {
        val mapRouteController = MapRouteVisitController(route)
        mapRouteController.getVisitedSquares().forEach{ MapSquareVisitController(it).removeVisitRoute(route)}
        mapRouteController.clear()
    }

    private fun getLineStringArray(mapPoints: List<MyPoint>): List<LineString> {
        val result = arrayListOf<LineString>()

        for (i in 0..(mapPoints.size - 2) step 2) {
            result.add(LineString(mapPoints[i].lng to mapPoints[i].lat, mapPoints[i + 1].lng to mapPoints[i + 1].lat))
        }

        return result
    }
}