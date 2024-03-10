package com.example.demo.algorithm.square.visit

import com.example.demo.algorithm.model.MapRoute
import com.example.demo.algorithm.model.MapSquare
import me.piruin.geok.geometry.LineString
import me.piruin.geok.geometry.Polygon

class MapSquareVisitController(
    private val mapSquare: MapSquare
) {

    fun intersect(polygon: Polygon): Boolean {
        return mapSquare.polygon.bbox.intersectWith(polygon.bbox)
    }

    fun intersect(line: LineString): Boolean {
        return mapSquare.polygon.bbox.intersectWith(line.bbox)
    }

    fun addVisitRoute(route: MapRoute) {
        mapSquare.visitedRoutes.add(route)
    }

    fun removeVisitRoute(route: MapRoute) {
        mapSquare.visitedRoutes.remove(route)
    }
}