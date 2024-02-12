package com.example.demo.algorithm.controller

import com.example.demo.algorithm.model.MapRoute
import com.example.demo.algorithm.model.MapSquare
import com.example.demo.algorithm.service.MapCongestionService
import me.piruin.geok.geometry.Point
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.min
import kotlin.random.Random

class MapRouteController(
    private val mapRoute: MapRoute,
    private val mapCongestionService: MapCongestionService,
    private val mapMatrixData: MapMatrixData
) {
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
                !Point(mapRoute.routeData.start.lng to mapRoute.routeData.start.lat).insideOf(it.polygon) &&
                        !Point(mapRoute.routeData.end.lng to mapRoute.routeData.end.lat).insideOf(it.polygon)
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
            mapCongestionService.calcCongestion(arrayListOf(square), startTimeInMinutes, endTimeInMinutes).avgCongestion

        val maxCong = if (mapMatrixData.getMaxCongestion() == BigDecimal.ZERO) BigDecimal(1.0) else mapMatrixData.getMaxCongestion()

        val calculatedProbability = min(1.0, calculatedCongestion.divide(maxCong,4, RoundingMode.HALF_UP).toDouble())
        val avgProbability = mapMatrixData.getAvgCongestion().divide(maxCong,4, RoundingMode.HALF_UP).toDouble()

        return (calculatedProbability + avgProbability) / 2.0
    }

    fun addVisitSquare(square: MapSquare) {
        mapRoute.visitedSquares.add(square)
    }

    fun clear() {
        mapRoute.visitedSquares.clear()
    }
}