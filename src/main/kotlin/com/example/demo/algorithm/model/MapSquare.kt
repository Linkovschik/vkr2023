package com.example.demo.algorithm.model

import com.example.demo.geojson.model.MyPoint
import me.piruin.geok.LatLng
import me.piruin.geok.geometry.Polygon
import java.math.BigDecimal
import kotlin.random.Random


open class MapSquare(
    val botLeft: MyPoint,
    val botRight: MyPoint,
    val topRight: MyPoint,
    val topLeft: MyPoint,
    val visitedRoutes: ArrayList<MapRoute> = arrayListOf(),
    val savedCongestion: BigDecimal = Random.nextDouble(0.0, 1.0).toBigDecimal()
) {


    constructor(mapSquare: MapSquare) : this(
        mapSquare.botLeft,
        mapSquare.botRight,
        mapSquare.topRight,
        mapSquare.topLeft,
        mapSquare.visitedRoutes
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
}