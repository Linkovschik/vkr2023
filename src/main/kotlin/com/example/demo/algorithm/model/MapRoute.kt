package com.example.demo.algorithm.model

import com.example.demo.geojson.model.Route
import java.math.BigDecimal
import java.sql.Time
import kotlin.math.round

open class MapRoute(
    private val routeData: Route,
    private val visitedSquares: MutableList<MapSquare> = arrayListOf()
) {

    val minStartTimeInMinutesOfDay: Int = convertTimeToDayMinutes(routeData.startTimeMin)
    val maxStartTimeInMinutesOfDay: Int = convertTimeToDayMinutes(routeData.startTimeMax)

    val minEndTimeInMinutesOfDay: Int = convertTimeToDayMinutes(routeData.endTimeMin)
    val maxEndTimeInMinutesOfDay: Int = convertTimeToDayMinutes(routeData.endTimeMax)

    val durationTimeInMinutesOfDay: Int = convertSecondToMinutes(routeData.duration)
    val distanceInMeters: Double = routeData.distance

    var rankDegree: Double = 1.0

    fun getAverageCongestionOnVisitedSquares(timeInMinutesOfDay: Int): BigDecimal {
        if (visitedSquares.isEmpty())
            return BigDecimal.ZERO

        val congestionByMinutes = arrayListOf<Double>()
        for (visitedSquare in visitedSquares) {
            congestionByMinutes.add(visitedSquare.getCongestionByMinuteOfDay(timeInMinutesOfDay).toDouble())
        }

        return if (congestionByMinutes.average().isNaN()) BigDecimal.ZERO
        else congestionByMinutes.average().toBigDecimal()
    }

    fun updateStartTimeByMinutesOfDay(startTime: Int) {
        routeData.startTime = convertDayMinutesToTime(startTime)
    }

    fun updateEndTimeByMinutesOfDay(endTime: Int) {
        routeData.endTime = convertDayMinutesToTime(endTime)
    }

    fun getStartTimeInMinutesOfDay(): Int {
        return if (routeData.startTime == null) maxStartTimeInMinutesOfDay else convertTimeToDayMinutes(routeData.startTime)
    }

    fun getEndTimeInMinutesOfDay(): Int {
        return if (routeData.endTime == null) minEndTimeInMinutesOfDay else convertTimeToDayMinutes(routeData.endTime)
    }

    fun getMutableVisitedSquares(): MutableList<MapSquare> {
        return visitedSquares
    }

    fun getVisitedSquares(): List<MapSquare> {
        return visitedSquares
    }

    fun getMutableRouteData(): Route {
        return routeData
    }

    private fun convertTimeToDayMinutes(time: Time?): Int {
        if (time == null)
            return 0
        return time.minutes.plus(time.hours.times(60))
    }

    private fun convertDayMinutesToTime(minutesOfDay: Int): Time {
        val hours = minutesOfDay.div(60)
        val minutes = minutesOfDay.rem(60)
        return Time(hours, minutes, 0)
    }

    private fun convertSecondToMinutes(time: Double?): Int {
        if (time == null)
            return 0
        return round(time / 60.0).toInt()
    }
}