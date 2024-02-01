package com.example.demo.algorithm.model

import com.example.demo.geojson.model.Route
import java.sql.Time
import kotlin.math.round

open class MapRoute(
    val routeData: Route,
    val visitedSquares: MutableList<MapSquare> = arrayListOf()
) {

    val minStartTimeInMinutesOfDay: Int = convertTimeToDayMinutes(routeData.startTimeMin)
    val maxStartTimeInMinutesOfDay: Int = convertTimeToDayMinutes(routeData.startTimeMax)

    val minEndTimeInMinutesOfDay: Int = convertTimeToDayMinutes(routeData.endTimeMin)
    val maxEndTimeInMinutesOfDay: Int = convertTimeToDayMinutes(routeData.endTimeMax)

    val durationTimeInMinutesOfDay: Int = convertSecondToMinutes(routeData.duration)

    constructor(mapRoute: MapRoute) : this(mapRoute.routeData, mapRoute.visitedSquares)

    private fun convertTimeToDayMinutes(time: Time?): Int {
        if (time == null)
            return 0
        return time.minutes.plus(time.hours.times(60))
    }

    private fun convertSecondToMinutes(time: Double?): Int {
        if (time == null)
            return 0
        return round(time / 60.0).toInt()
    }
}