package com.example.demo.algorithm.controller

import com.example.demo.algorithm.model.MapRoute
import com.example.demo.algorithm.model.MapSquare
import com.example.demo.algorithm.service.MapCongestionService
import com.example.demo.algorithm.service.SpeedCoefficientCalculatorService
import com.example.demo.geojson.model.MyPoint
import me.piruin.geok.geometry.LineString
import java.math.BigDecimal

class MapMatrixController(
    private val mapSquares: List<MapSquare>,
    private val mapCongestionService: MapCongestionService,
    private val speedCoefficientCalculatorService: SpeedCoefficientCalculatorService
) : MapMatrixData {

    private var maxCongestion: BigDecimal = BigDecimal.ZERO
    private var mediumCongestion: BigDecimal = BigDecimal.ZERO
    private var minCongestion: BigDecimal = BigDecimal.ZERO

    override fun updateMatrixState(routes: List<MapRoute>) {
        clearState()
        routes.forEach { makeRouteVisitSquares(it) }
        val congestionResult = mapCongestionService.calcCongestion(mapSquares)
        minCongestion = congestionResult.minCongestion
        mediumCongestion = congestionResult.avgCongestion
        maxCongestion = congestionResult.maxCongestion
    }

    override fun getMaxCongestion(): BigDecimal {
        return maxCongestion
    }

    override fun getMinCongestion(): BigDecimal {
        return minCongestion
    }

    override fun getAvgCongestion(): BigDecimal {
        return mediumCongestion
    }

    private fun clearState() {
        mapSquares.forEach { MapSquareController(it, speedCoefficientCalculatorService).clear() }
    }

    private fun makeRouteVisitSquares(route: MapRoute) {
        val mapRouteController = MapRouteController(route, mapCongestionService, this)
        mapRouteController.clear()

        var currentSquares = mapSquares.toMutableList()
        val nextSquares = currentSquares.toMutableList()

        getLineStringArray(route.routeData.coordinates).forEach { line ->
            currentSquares.forEach { square ->
                val mapSquareController = MapSquareController(square, speedCoefficientCalculatorService)
                if (mapSquareController.intersect(line)) {
                    mapSquareController.addVisitRoute(route)
                    mapRouteController.addVisitSquare(square)
                    nextSquares.remove(square)
                }
            }
            currentSquares = nextSquares.toMutableList()
        }
    }

    private fun getLineStringArray(mapPoints: List<MyPoint>): List<LineString> {
        val result = arrayListOf<LineString>()

        for (i in 0..(mapPoints.size - 2) step 2) {
            result.add(LineString(mapPoints[i].lng to mapPoints[i].lat, mapPoints[i + 1].lng to mapPoints[i + 1].lat))
        }

        return result
    }
}