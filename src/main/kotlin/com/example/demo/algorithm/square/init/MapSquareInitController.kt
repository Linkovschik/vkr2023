package com.example.demo.algorithm.square.init

import com.example.demo.algorithm.model.MapSquare
import java.math.BigDecimal

class MapSquareInitController(
    private val mapSquare: MapSquare,
    private val startTimeInMinutes: Int,
    private val endTimeInMinutes: Int,
    private val timeWindowInMinutes: Int
) {
    fun initAlgorithmDataForSquare() {
        mapSquare.visitedRoutes.clear()
        for (currentMinute in startTimeInMinutes..endTimeInMinutes step timeWindowInMinutes) {
            mapSquare.congestionByMinuteOfDay[currentMinute] = BigDecimal.ZERO
        }
    }
}