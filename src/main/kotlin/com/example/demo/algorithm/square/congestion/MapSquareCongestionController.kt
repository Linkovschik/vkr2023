package com.example.demo.algorithm.square.congestion

import com.example.demo.algorithm.model.MapRoute
import com.example.demo.algorithm.model.MapSquare
import com.example.demo.algorithm.service.SpeedCoefficientCalculatorService
import java.math.BigDecimal
import kotlin.math.log10

class MapSquareCongestionController(
    private val mapSquare: MapSquare,
    private val speedCoefficientCalculatorService: SpeedCoefficientCalculatorService,
    private val startTimeInMinutes: Int,
    private val endTimeInMinutes: Int,
    private val timeWindowInMinutes: Int
) {
    fun updateCongestionOfSquare() {
        for (currentMinute in startTimeInMinutes..endTimeInMinutes step timeWindowInMinutes) {
            mapSquare.congestionByMinuteOfDay[currentMinute] = calcCongestion(currentMinute)
        }
    }

    fun calcCongestion(timeInMinutesOfDay: Int): BigDecimal {
        var result = BigDecimal.ZERO

        val visitedRoutes = mapSquare.visitedRoutes

        if (visitedRoutes.isEmpty())
            return result

        for (visitedRoute in visitedRoutes) {
            val speed = speedDependOnPassedPathAdjustCoefficient(
                visitedRoute,
                timeInMinutesOfDay
            ) * (visitedRoute.distanceInMeters / visitedRoute.durationTimeInMinutesOfDay)

            result += BigDecimal(speedCoefficientCalculatorService.calculate(speed))
        }

        return result / BigDecimal(visitedRoutes.size)

    }

    private fun speedDependOnPassedPathAdjustCoefficient(
        visitedRoute: MapRoute,
        timeInMinutesOfDay: Int
    ): Double {
        val pathPart =
            Math.max(
                0.1 + 1e-10,
                (timeInMinutesOfDay - visitedRoute.getStartTimeInMinutesOfDay()) / visitedRoute.durationTimeInMinutesOfDay.toDouble()
            )

        return 1.0 - Math.max(log10(pathPart * 10.0), 1.0)
    }
}