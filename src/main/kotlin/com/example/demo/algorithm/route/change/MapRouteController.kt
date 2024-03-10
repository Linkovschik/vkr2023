package com.example.demo.algorithm.route.change

import com.example.demo.algorithm.DefaultAlgorithm.Companion.FORCEFULLY_AVOID_MUTATION_CHANCE
import com.example.demo.algorithm.model.MapMatrixContext
import com.example.demo.algorithm.model.MapRoute
import com.example.demo.algorithm.model.MapSquare
import me.piruin.geok.geometry.Point
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.min
import kotlin.random.Random

class MapRouteController(
    private val mapRoute: MapRoute,
    private val mapMatrixContext: MapMatrixContext
) {
    fun getAverageCongestionOnVisitedSquares(): BigDecimal {
        val averageCongestions = arrayListOf<Double>()
        for (currentMinute in mapRoute.getStartTimeInMinutesOfDay()..mapRoute.getEndTimeInMinutesOfDay() step mapMatrixContext.timeWindowInMinutes) {
            averageCongestions.add(mapRoute.getAverageCongestionOnVisitedSquares(currentMinute).toDouble())
        }
        return if (averageCongestions.average().isNaN()) BigDecimal.ZERO
        else averageCongestions.average().toBigDecimal()
    }

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
        val result = hashSetOf<MapSquare>()

        val probabilityOfAvoid = Random.nextInt(0, 100) / 100.0

        val actingVisitedSquares = mapRoute.getVisitedSquares()
            .filter {
                !Point(mapRoute.getMutableRouteData().start.lng to mapRoute.getMutableRouteData().start.lat).insideOf(it.polygon) &&
                        !Point(mapRoute.getMutableRouteData().end.lng to mapRoute.getMutableRouteData().end.lat).insideOf(
                            it.polygon
                        )
            }

        if (actingVisitedSquares.isNotEmpty() && probabilityOfAvoid < FORCEFULLY_AVOID_MUTATION_CHANCE) {
            val forceFullyAvoidSquareByNumber = Random.nextInt(0, actingVisitedSquares.size)
            result.add(actingVisitedSquares[forceFullyAvoidSquareByNumber])
        }

        result.addAll(actingVisitedSquares
            .filter {
                calcProbabilityToAvoid(
                    it,
                    startTimeInMinutes,
                    endTimeInMinutes
                ) < probabilityOfAvoid
            }
        )

        return result.toList()
    }

    private fun calcProbabilityToAvoid(square: MapSquare, startTimeInMinutes: Int, endTimeInMinutes: Int): Double {

        val calculatedCongestions = arrayListOf<Double>()
        for (currentMinute in startTimeInMinutes..endTimeInMinutes step mapMatrixContext.timeWindowInMinutes) {
            calculatedCongestions.add(square.getCongestionByMinuteOfDay(currentMinute).toDouble())
        }
        val calculatedCongestion =
            if (calculatedCongestions.average().isNaN()) BigDecimal.ZERO
            else calculatedCongestions.average()
                .toBigDecimal()

        return min(
            1.0,
            calculatedCongestion
                .divide(mapMatrixContext.maxCongestion, 4, RoundingMode.HALF_UP)
                .toDouble()
        )
    }
}