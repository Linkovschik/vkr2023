package com.example.demo.algorithm.model

import com.example.demo.geojson.model.MyPoint
import me.piruin.geok.LatLng
import me.piruin.geok.geometry.Polygon
import java.math.BigDecimal


open class MapSquare(
    val botLeft: MyPoint,
    val botRight: MyPoint,
    val topRight: MyPoint,
    val topLeft: MyPoint,
    val visitedRoutes: ArrayList<MapRoute> = arrayListOf(),
    var congestionByMinuteOfDay: MutableMap<Int, BigDecimal> = mutableMapOf()
) {

    constructor(mapSquare: MapSquare) : this(
        mapSquare.botLeft,
        mapSquare.botRight,
        mapSquare.topRight,
        mapSquare.topLeft,
        mapSquare.visitedRoutes,
        mapSquare.congestionByMinuteOfDay
    )

    val polygon = Polygon(
        LatLng(botLeft.lat, botLeft.lng),
        LatLng(botRight.lat, botRight.lng),
        LatLng(topRight.lat, topRight.lng),
        LatLng(topLeft.lat, topLeft.lng)
    )

    fun convertToArray(): ArrayList<ArrayList<Double>> {
        return arrayListOf(
            botLeft.convertToArray(),
            botRight.convertToArray(),
            topRight.convertToArray(),
            topLeft.convertToArray(),
            botLeft.convertToArray()
        )
    }

    fun getCongestionByMinuteOfDay(timeInMinutesOfDay: Int): BigDecimal {
        var min = Int.MAX_VALUE
        var closest: Int = timeInMinutesOfDay

        for (keyMinuteOfDay in congestionByMinuteOfDay.keys) {
            val diff: Int = Math.abs(keyMinuteOfDay - timeInMinutesOfDay)
            if (diff < min) {
                min = diff
                closest = keyMinuteOfDay
            }
        }

        return congestionByMinuteOfDay[closest] ?: BigDecimal.ZERO
    }
}