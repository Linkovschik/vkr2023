package com.example.demo.algorithm.controller

import com.example.demo.algorithm.model.MapRoute
import com.example.demo.algorithm.model.MapSquare
import com.example.demo.algorithm.service.MapCongestionService
import me.piruin.geok.geometry.Point
import kotlin.math.min
import kotlin.random.Random

class MapRouteController(
    val mapRoute: MapRoute,
    val mapCongestionService: MapCongestionService,
    val mapMatrixController: MapMatrixController,
) : MapRoute(mapRoute) {
    fun selectStartTime(): Int {
        return selectTimeInMinutes(mapRoute.minStartTimeInMinutesOfDay, mapRoute.maxStartTimeInMinutesOfDay)
    }

    fun selectEndTime(): Int {
        return selectTimeInMinutes(mapRoute.minEndTimeInMinutesOfDay, mapRoute.maxEndTimeInMinutesOfDay)
    }

    private fun selectTimeInMinutes(low: Int, high: Int): Int {
        return Random.nextInt(low, high + 1)
    }

    fun selectSquaresToAvoid(startTimeInMinutes: Int, endTimeInMinutes: Int): List<MapSquare> {
        val probabilityOfAvoid = Random.nextDouble(0.0, 1.0)
        return mapRoute.visitedSquares
            .filter {
                !Point(mapRoute.routeData.start.lat to mapRoute.routeData.start.lng).insideOf(it.polygon) &&
                        !Point(mapRoute.routeData.end.lat to mapRoute.routeData.end.lng).insideOf(it.polygon)
            }
            .filter {
                calcProbabilityToAvoid(
                    it,
                    startTimeInMinutes,
                    endTimeInMinutes
                ) > probabilityOfAvoid
            }
    }

    private fun calcProbabilityToAvoid(square: MapSquare, startTimeInMinutes: Int, endTimeInMinutes: Int): Double {
        val calculatedCongestion =
            mapCongestionService.calcAvgCongestion(arrayListOf(square), startTimeInMinutes, endTimeInMinutes)

        val calculatedProbability = min(1.0, calculatedCongestion.divide(mapMatrixController.maxCongestion).toDouble())
        val avgProbability = mapMatrixController.mediumCongestion.divide(mapMatrixController.maxCongestion).toDouble()

        return (calculatedProbability + avgProbability) / 2.0
    }

    fun decreaseRankDegree(decreaseRankDegreeCoefficient: Double) {
        mapRoute.rankDegree * min(decreaseRankDegreeCoefficient, 0.95)
    }

    fun addVisitSquare(square: MapSquare) {
        mapRoute.visitedSquares.add(square)
    }

    fun clear() {
        mapRoute.visitedSquares.clear()
    }
}