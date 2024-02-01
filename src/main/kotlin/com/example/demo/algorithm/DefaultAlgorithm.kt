package com.example.demo.algorithm

import com.example.demo.algorithm.service.MapMatrixService
import com.example.demo.algorithm.model.MapRoute
import com.example.demo.algorithm.controller.MapMatrixController
import com.example.demo.algorithm.controller.MapRouteController
import com.example.demo.algorithm.model.MapRouteDecision
import com.example.demo.algorithm.model.MapSquare
import com.example.demo.algorithm.service.MapCongestionService
import com.example.demo.geojson.service.MappingService
import com.example.demo.retrofit.DrivingService
import org.springframework.stereotype.Component
import kotlin.math.min
import kotlin.random.Random

@Component
class DefaultAlgorithm : Algorithm {
    companion object {
        val ALGORITHM_ITERATIONS_COUNT: Int = 100
        val REBUILD_MATRIX_ALGORITHM_ITERATIONS_COUNT: Int = 10
        val TIME_WINDOW_IN_MINUTES: Int = 10
        val START_TIME_IN_MINUTES_OF_DAY: Int = 6 * 60
        val END_TIME_IN_MINUTES_OF_DAY: Int = 10 * 60
        val COUNT_OF_DECISIONS_FOR_ONE_ROUTE: Int = 7
        val ROUTE_RANK_DECREASE_DEGREE: Double = 0.95
        val ROUTE_RANK_DECREASE_IGNORE_PROBABILITY: Double = 0.6
    }

    private var mapMatrixService: MapMatrixService = MapMatrixService()

    private var drivingService: DrivingService = DrivingService()

    private var mappingService: MappingService = MappingService()
    private var mapCongestionService: MapCongestionService = MapCongestionService()

    private val mapMatrixController = MapMatrixController(mapMatrixService.createMatrix(), mapCongestionService)

    private var allRoutes: MutableList<MapRoute> = mutableListOf()

    override fun rebuildRoutes(routes: List<MapRoute>): List<MapRouteDecision> {

        try {
            allRoutes = routes.toMutableList()

            val result = arrayListOf<MapRouteDecision>()

            for (route in routes) {
                if (!allRoutes.map { it.routeData.name }.contains(route.routeData.name))
                    continue

                val updatedRoute = updateRouteByAlgorithm(route)
                result.add(updatedRoute)

                allRoutes = allRoutes.dropWhile { it.routeData.name == updatedRoute.routeData.name }.toMutableList()
                allRoutes.add(updatedRoute)
            }

            return result

        } catch (e: Exception) {
            println(e.message)
        }

        return emptyList()
    }

    private fun updateRouteByAlgorithm(mapRoute: MapRoute): MapRouteDecision {

        var decision = selectBestDecision(createDecisions(mapRoute))

        for (iteration in 1..ALGORITHM_ITERATIONS_COUNT) {
            decision = selectBestDecision(createDecisions(decision))

            if (iteration % REBUILD_MATRIX_ALGORITHM_ITERATIONS_COUNT == 0) {
                mapMatrixController.updateMatrixState(allRoutes)
            }
        }

        return decision
    }

    private fun selectBestDecision(decisions: List<MapRouteDecision>): MapRouteDecision {
        val sortedDecisions = getSortedDecisions(decisions)
        if (sortedDecisions.isEmpty())
            throw Exception("Невозможно выбрать лучшее решение из пустого списка решений!")

        val randomIndex = Random.nextInt(sortedDecisions.size / 2, sortedDecisions.size)
        return sortedDecisions[randomIndex]
    }

    private fun getSortedDecisions(decisions: List<MapRouteDecision>): List<MapRouteDecision> {
        return decisions.sortedWith(compareBy({ it.durationTimeInMinutesOfDay }, { it.avgCongestion }))
    }

    private fun createDecision(oldRoute: MapRoute): MapRouteDecision {
        val oldRouteController = MapRouteController(oldRoute, mapCongestionService, mapMatrixController)

        val startTime = oldRouteController.selectStartTime()
        val endTime = oldRouteController.selectEndTime()

        var defaultRankDegree = 1.0
        if (oldRoute is MapRouteDecision) {
            defaultRankDegree = oldRoute.rankDegree
        }

        val defaultDecision =
            MapRouteDecision(oldRoute, mapMatrixController.getAvgCongestion(), startTime, endTime, defaultRankDegree)

        val avoidedPolygons = oldRouteController.selectSquaresToAvoid(startTime, endTime)
        if (avoidedPolygons.isEmpty()) return defaultDecision

        val newRoute = createNewMapRoute(oldRoute, avoidedPolygons) ?: return defaultDecision

        var newRankDegree = defaultRankDegree
        val rankDegreeErrorChance = Random.nextDouble(0.0, defaultRankDegree)
        if (newRoute.durationTimeInMinutesOfDay > endTime - startTime && rankDegreeErrorChance < ROUTE_RANK_DECREASE_IGNORE_PROBABILITY) {
            newRankDegree = decreaseRankDegree(defaultRankDegree)
            return MapRouteDecision(oldRoute, mapMatrixController.getAvgCongestion(), startTime, endTime, newRankDegree)
        }

        val avgCongestion = mapCongestionService.calcAvgCongestion(avoidedPolygons, startTime, endTime)
        return MapRouteDecision(newRoute, avgCongestion, startTime, endTime)
    }

    private fun createNewMapRoute(
        oldRoute: MapRoute,
        avoidedPolygons: List<MapSquare>
    ): MapRoute? {
        val featureCollection =
            drivingService.getGeoJsonRoute(
                oldRoute.routeData.start.convertToArray(),
                oldRoute.routeData.end.convertToArray(),
                avoidedPolygons
            ) ?: return null

        return MapRoute(mappingService.mapFeatureToRoute(featureCollection.features.first()))
    }

    private fun createDecisions(oldRoute: MapRoute): List<MapRouteDecision> {
        val result = arrayListOf<MapRouteDecision>()

        for (num in 0..COUNT_OF_DECISIONS_FOR_ONE_ROUTE) {
            result.add(createDecision(oldRoute))
        }

        return result
    }

    private fun decreaseRankDegree(defaultRankDegree: Double): Double {
        return defaultRankDegree * min(ROUTE_RANK_DECREASE_DEGREE, 0.95)
    }
}