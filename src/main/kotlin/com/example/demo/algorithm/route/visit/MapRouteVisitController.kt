package com.example.demo.algorithm.route.visit

import com.example.demo.algorithm.model.MapRoute
import com.example.demo.algorithm.model.MapSquare
import com.example.demo.geojson.model.MyPoint

class MapRouteVisitController(private val mapRoute: MapRoute) {
    fun addVisitSquare(square: MapSquare) {
        mapRoute.getMutableVisitedSquares().add(square)
    }

    fun getVisitedSquares(): List<MapSquare> {
        return mapRoute.getVisitedSquares()
    }

    fun clear() {
        mapRoute.getMutableVisitedSquares().clear()
    }

    fun getRouteDataCoordinates(): List<MyPoint> {
        return mapRoute.getMutableRouteData().coordinates
    }
}