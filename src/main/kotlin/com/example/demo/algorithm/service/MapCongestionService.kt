package com.example.demo.algorithm.service

import com.example.demo.algorithm.DefaultAlgorithm
import com.example.demo.algorithm.controller.MapSquareController
import com.example.demo.algorithm.model.MapSquare
import java.math.BigDecimal

open class MapCongestionService(
    private val startTimeInMinutes: Int = DefaultAlgorithm.START_TIME_IN_MINUTES_OF_DAY,
    private val endTimeInMinutes: Int = DefaultAlgorithm.END_TIME_IN_MINUTES_OF_DAY,
    private val timeWindowInMinutes: Int = DefaultAlgorithm.TIME_WINDOW_IN_MINUTES
) {
    open fun calcMaxCongestion(mapSquares: List<MapSquare>, startTimeInMinutes: Int, endTimeInMinutes: Int): BigDecimal {
        val results = arrayListOf<BigDecimal>()

        for (currentMinute in startTimeInMinutes..endTimeInMinutes step timeWindowInMinutes) {
            results.add(mapSquares
                .map { MapSquareController(it).calcCongestion(currentMinute) }
                .reduce { acc, congestion -> acc + congestion }
                .divide(BigDecimal(mapSquares.size))
            )
        }

        return results
            .max()
            .divide(BigDecimal(results.size))
    }

    open fun calcMinCongestion(mapSquares: List<MapSquare>, startTimeInMinutes: Int, endTimeInMinutes: Int): BigDecimal {
        val results = arrayListOf<BigDecimal>()

        for (currentMinute in startTimeInMinutes..endTimeInMinutes step timeWindowInMinutes) {
            results.add(mapSquares
                .map { MapSquareController(it).calcCongestion(currentMinute) }
                .reduce { acc, congestion -> acc + congestion }
                .divide(BigDecimal(mapSquares.size))
            )
        }

        return results
            .min()
            .divide(BigDecimal(results.size))
    }

    open fun calcAvgCongestion(mapSquares: List<MapSquare>, startTimeInMinutes: Int, endTimeInMinutes: Int): BigDecimal {
        val results = arrayListOf<BigDecimal>()

        for (currentMinute in startTimeInMinutes..endTimeInMinutes step timeWindowInMinutes) {
            results.add(mapSquares
                .map { MapSquareController(it).calcCongestion(currentMinute) }
                .reduce { acc, congestion -> acc + congestion }
                .divide(BigDecimal(mapSquares.size))
            )
        }

        return results
            .max()
            .minus(results.min())
            .divide(BigDecimal(results.size))
    }

    fun calcMaxCongestion(mapSquares: List<MapSquare>): BigDecimal {
        return calcMaxCongestion(mapSquares, this.startTimeInMinutes, this.endTimeInMinutes)
    }

    fun calcMinCongestion(mapSquares: List<MapSquare>): BigDecimal {
        return calcMinCongestion(mapSquares, this.startTimeInMinutes, this.endTimeInMinutes)
    }

    fun calcAvgCongestion(mapSquares: List<MapSquare>): BigDecimal {
        return calcAvgCongestion(mapSquares, this.startTimeInMinutes, this.endTimeInMinutes)
    }
}