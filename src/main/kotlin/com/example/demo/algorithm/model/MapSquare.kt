package com.example.demo.algorithm.model

import com.example.demo.algorithm.SECONDS_IN_A_DAY
import java.math.BigDecimal


class MapSquare(val botLeft: MapPoint, val borRight: MapPoint, val topRight: MapPoint, val topLeft: MapPoint, val timeWindowInSeconds: Int = 600) {
    val timeBasedVisitedData = ArrayList<Long>(SECONDS_IN_A_DAY / timeWindowInSeconds)
    var visited: BigDecimal = BigDecimal.ZERO

    fun intersect(point: MapPoint): Boolean {
        return point.lng > botLeft.lng &&
                point.lng < topRight.lng &&
                point.lat > botLeft.lat &&
                point.lat < topRight.lat
    }

    fun notIntersect(point: MapPoint): Boolean {
        return !intersect(point)
    }


    fun convertToArray(): ArrayList<ArrayList<Double>> {
        return arrayListOf(
                botLeft.convertToArray(),
                borRight.convertToArray(),
                topRight.convertToArray(),
                topLeft.convertToArray(),
                botLeft.convertToArray()
        )
    }
}