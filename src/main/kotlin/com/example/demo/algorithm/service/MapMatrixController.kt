package com.example.demo.algorithm.service

import com.example.demo.algorithm.model.MapSquare
import com.example.demo.geojson.model.Route
import java.math.BigDecimal
import kotlin.random.Random

class MapMatrixController(val mapSquares: List<MapSquare>) {
    fun makeRouteVisitSquares(route: Route) {
        var currentSquares = mapSquares.toMutableList()
        val nextSquares = currentSquares.toMutableList()
        route.coordinates.forEach { point ->
            currentSquares.forEach { square ->
                if (square.intersect(point)) {
                    square.visited += BigDecimal.ONE
                    nextSquares.remove(square)
                }
            }
            currentSquares = nextSquares.toMutableList()
        }
    }

    fun selectedSquaresToAvoid(route: Route): List<MapSquare> {
        val sortedByVisitedSquares = getVisitedSquaresOrderedByCountOfVisits()
        val potentiallyAvoidedSquares = sortedByVisitedSquares.filter { it.notIntersect(route.start) && it.notIntersect(route.end) }.shuffled()

        val randomStartIndex = Random.nextInt(potentiallyAvoidedSquares.size)
        val randomEndIndex = randomStartIndex + Random.nextInt(potentiallyAvoidedSquares.size - randomStartIndex - 1)
        return potentiallyAvoidedSquares.subList(randomStartIndex, randomEndIndex)
    }

    private fun getVisitedSquaresOrderedByCountOfVisits(): List<MapSquare> {
        val visitedComparator = Comparator { sqr1: MapSquare, sqr2: MapSquare -> sqr1.visited.compareTo(sqr2.visited) }
        return mapSquares.sortedWith(visitedComparator).filter { it.visited != BigDecimal.ZERO }
    }
}