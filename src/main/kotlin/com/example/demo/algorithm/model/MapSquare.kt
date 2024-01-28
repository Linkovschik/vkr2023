package com.example.demo.algorithm.model

import com.example.demo.geojson.model.MyPoint
import me.piruin.geok.LatLng
import me.piruin.geok.geometry.Polygon


open class MapSquare(val botLeft: MyPoint,
                     val botRight: MyPoint,
                     val topRight: MyPoint,
                     val topLeft: MyPoint,
                     val visitedRoutes: ArrayList<MapRoute> = arrayListOf()
) {


    constructor(mapSquare: MapSquare) : this(mapSquare.botLeft,
        mapSquare.botRight,
        mapSquare.topRight,
        mapSquare.topLeft,
        mapSquare.visitedRoutes)

    val polygon = Polygon(
        LatLng(botLeft.lat, botLeft.lng),
        LatLng(botRight.lat, botRight.lng),
        LatLng(topRight.lat, topRight.lng),
        LatLng(topLeft.lat, topLeft.lng)
    )
}