package com.example.demo.algorithm.service

import com.example.demo.algorithm.DefaultAlgorithm
import com.example.demo.algorithm.controller.MapSquareController
import com.example.demo.algorithm.model.CongestionResult
import com.example.demo.algorithm.model.MapSquare
import java.math.BigDecimal
import java.math.RoundingMode

open class MapCongestionService(
    private val startTimeInMinutes: Int = DefaultAlgorithm.START_TIME_IN_MINUTES_OF_DAY,
    private val endTimeInMinutes: Int = DefaultAlgorithm.END_TIME_IN_MINUTES_OF_DAY,
    private val timeWindowInMinutes: Int = DefaultAlgorithm.TIME_WINDOW_IN_MINUTES,
    private val congestionLimit: BigDecimal = BigDecimal(1e-10)
) {
    open fun calcCongestion(
        mapSquares: List<MapSquare>,
        startTimeInMinutes: Int,
        endTimeInMinutes: Int
    ): CongestionResult {
        val results = arrayListOf<BigDecimal>()

        for (currentMinute in startTimeInMinutes..endTimeInMinutes step timeWindowInMinutes) {
            results.add(mapSquares
                .map { MapSquareController(it).calcCongestion(currentMinute) }
                .filter { cong -> cong > congestionLimit }
                .reduce { acc, congestion -> acc + congestion }
                .divide(BigDecimal(mapSquares.size), 4, RoundingMode.HALF_UP)
            )
        }
        return CongestionResult(
            results.min(),
            results
                .sumOf { it }
                .divide(BigDecimal(results.size), 4, RoundingMode.HALF_UP),
            results.max()
        )
    }

    fun calcCongestion(mapSquares: List<MapSquare>): CongestionResult {
        return calcCongestion(mapSquares, this.startTimeInMinutes, this.endTimeInMinutes)
    }
}