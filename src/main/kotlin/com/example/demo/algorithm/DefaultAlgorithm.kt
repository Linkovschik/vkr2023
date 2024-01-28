package com.example.demo.algorithm

import com.example.demo.algorithm.service.MapMatrixService
import com.example.demo.algorithm.model.MapRoute
import com.example.demo.algorithm.controller.MapMatrixController
import com.example.demo.algorithm.controller.MapRouteController
import com.example.demo.algorithm.model.MapRouteDecision
import com.example.demo.algorithm.service.MapCongestionService
import com.example.demo.geojson.model.Route
import com.example.demo.geojson.service.MappingService
import com.example.demo.retrofit.DrivingService
import org.springframework.stereotype.Component
import kotlin.random.Random

@Component
class DefaultAlgorithm : Algorithm {
    companion object {
        val ALGORITHM_ITERATIONS_COUNT: Int = 100
        val TIME_WINDOW_IN_MINUTES: Int = 10
        val START_TIME_IN_MINUTES_OF_DAY: Int = 6 * 60
        val END_TIME_IN_MINUTES_OF_DAY: Int = 10 * 60
        val COUNT_OF_DECISIONS_FOR_ONE_ROUTE: Int = 7
        val ROUTE_RANK_DECREASE_DEGREE: Double = 0.9
        val ROUTE_RANK_DECREASE_IGNORE_PROBABILITY: Double = 0.6
    }

    private var mapMatrixService: MapMatrixService = MapMatrixService()

    private var drivingService: DrivingService = DrivingService()

    private var mappingService: MappingService = MappingService()
    private var mapCongestionService: MapCongestionService = MapCongestionService()

    private val mapMatrixController = MapMatrixController(mapMatrixService.createMatrix(), mapCongestionService)

    override fun rebuildRoutes(routes: List<Route>, allRoutes: List<Route>): List<Route> {

        try {
            val result = arrayListOf<Route>()

            for (route in routes) {
                if (!allRoutes.map { it.name }.contains(route.name))
                    continue

                result.add(updateRouteByAlgorithm(route))
            }


            var mapRoutes = routes.map { mapMatrixController.makeRouteVisitSquares(MapRoute(it)) }

            return result
        } catch (e: Exception) {
            println(e.message)
        }

        return emptyList()
    }

    private fun updateRouteByAlgorithm(route: Route): Route {
        var mapRoute = MapRoute(route)

        for (iteration in 0..ALGORITHM_ITERATIONS_COUNT) {
            val decision = selectBestDecision(createDecisions(mapRoute))
            mapRoute = decision
        }


        return mapRoute.routeData
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

    private fun createRoute(oldRoute: MapRoute): MapRouteDecision {
        val oldRouteController = MapRouteController(oldRoute, mapCongestionService, mapMatrixController)

        val startTime = oldRouteController.selectStartTime()
        val endTime = oldRouteController.selectEndTime()

        val avoidedPolygons = oldRouteController.selectSquaresToAvoid(startTime, endTime)

        if (avoidedPolygons.isEmpty())
            return MapRouteDecision(oldRoute, mapMatrixController.mediumCongestion, startTime, endTime)

        val featureCollection =
            drivingService.getGeoJsonRoute(
                oldRoute.routeData.start.convertToArray(),
                oldRoute.routeData.end.convertToArray(),
                avoidedPolygons
            )
                ?: return MapRouteDecision(oldRoute, mapMatrixController.mediumCongestion, startTime, endTime)

        val newRoute = MapRoute(mappingService.mapFeatureToRoute(featureCollection.features.first()))

        val rankDegreeErrorChance = Random.nextDouble(0.0, oldRoute.rankDegree)
        if (newRoute.durationTimeInMinutesOfDay > endTime - startTime && rankDegreeErrorChance < ROUTE_RANK_DECREASE_IGNORE_PROBABILITY) {
            oldRouteController.decreaseRankDegree(ROUTE_RANK_DECREASE_DEGREE)
            return MapRouteDecision(oldRoute, mapMatrixController.mediumCongestion, startTime, endTime)
        }

        val avgCongestion = mapCongestionService.calcAvgCongestion(avoidedPolygons, startTime, endTime)
        return MapRouteDecision(newRoute, avgCongestion, startTime, endTime)
    }

    private fun createDecisions(oldRoute: MapRoute): List<MapRouteDecision> {
        val result = arrayListOf<MapRouteDecision>()

        for (num in 0..COUNT_OF_DECISIONS_FOR_ONE_ROUTE) {
            result.add(createRoute(oldRoute))
        }

        return result
    }
}