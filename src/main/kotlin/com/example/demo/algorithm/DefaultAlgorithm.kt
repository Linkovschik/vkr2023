package com.example.demo.algorithm

import com.example.demo.algorithm.controller.MapMatrixController
import com.example.demo.algorithm.model.MapMatrixContext
import com.example.demo.algorithm.route.change.MapRouteController
import com.example.demo.algorithm.model.MapRoute
import com.example.demo.algorithm.model.MapSquare
import com.example.demo.algorithm.service.MapMatrixService
import com.example.demo.algorithm.service.SpeedCoefficientCalculatorService
import com.example.demo.geojson.service.MappingService
import com.example.demo.retrofit.DrivingService
import org.springframework.stereotype.Component
import java.util.stream.Collectors
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

@Component
class DefaultAlgorithm : Algorithm {
    companion object {
        val ALGORITHM_ITERATIONS_COUNT: Int = 50
        val ROUTE_DECISION_PER_ITERATIONS_COUNT: Int = 10
        val REBUILD_MATRIX_ALGORITHM_ITERATIONS_COUNT: Int = 5
        val TIME_WINDOW_IN_MINUTES: Int = 10
        val ALGORITHM_START_TIME_IN_MINUTES_OF_DAY: Int = 6 * 60
        val ALGORITHM_END_TIME_IN_MINUTES_OF_DAY: Int = 10 * 60
        val COUNT_OF_DECISIONS_FOR_ONE_ROUTE: Int = 4
        val ROUTE_RANK_DECREASE_DEGREE: Double = 0.82
        val NEW_ROUTE_CONTRADICTION_IGNORE_CHANCE: Double = 0.3
        val INCORRECT_DECISION_TIMES: Int = 5
        val MAX_SPEED_IN_METERS_PER_MINUTE: Double = 1083.0
        val MIN_SPEED_IN_METERS_PER_MINUTE: Double = 167.0
        val FORCEFULLY_AVOID_MUTATION_CHANCE: Double = 0.2
    }

    private var mapMatrixService: MapMatrixService = MapMatrixService()

    private var drivingService: DrivingService = DrivingService()

    private var mappingService: MappingService = MappingService()

    private var speedCoefficientCalculatorService: SpeedCoefficientCalculatorService =
        SpeedCoefficientCalculatorService()
    private val mapMatrixController =
        MapMatrixController(
            mapMatrixService.createMatrix(),
            speedCoefficientCalculatorService,
            ALGORITHM_START_TIME_IN_MINUTES_OF_DAY,
            ALGORITHM_END_TIME_IN_MINUTES_OF_DAY,
            TIME_WINDOW_IN_MINUTES
        )

    private var allRoutes: MutableList<MapRoute> = mutableListOf()

    override fun rebuildRoutes(routes: List<MapRoute>): List<MapRoute> {
        try {
            allRoutes = routes.toMutableList()

            val result = arrayListOf<MapRoute>()

            mapMatrixController.updateMatrixState(allRoutes)

            for (iteration in 1..ALGORITHM_ITERATIONS_COUNT) {
                result.clear()

                if (iteration % REBUILD_MATRIX_ALGORITHM_ITERATIONS_COUNT == 0) {
                    mapMatrixController.updateMatrixState(allRoutes)
                }

                result.addAll(
                    allRoutes.stream()
                        .map { updateRouteByAlgorithm(it) }.collect(Collectors.toList())
                )

            }

            return result

        } catch (e: Exception) {
            println(e.message)
        }

        return emptyList()
    }

    private fun updateRouteByAlgorithm(mapRoute: MapRoute): MapRoute {

        var decision = selectBestDecision(createBaseDecisions(mapRoute))

        for (iteration in 1..ROUTE_DECISION_PER_ITERATIONS_COUNT) {
            decision = selectBestDecision(createDecisions(decision))
        }

        return decision
    }

    private fun selectBestDecision(decisions: List<MapRoute>): MapRoute {
        val sortedDecisions = getSortedDecisions(decisions)
        if (sortedDecisions.isEmpty())
            throw Exception("Невозможно выбрать лучшее решение из пустого списка решений!")

        val randomIndex = Random.nextInt(sortedDecisions.size / 2, sortedDecisions.size)
        return sortedDecisions[randomIndex]
    }

    private fun getSortedDecisions(decisions: List<MapRoute>): List<MapRoute> {
        return decisions.sortedWith(
            compareBy(
                { it.durationTimeInMinutesOfDay },
                { MapRouteController(it, MapMatrixContext()).getAverageCongestionOnVisitedSquares() }
            )
        )
    }

    private fun createBaseDecision(oldRoute: MapRoute): MapRoute {
        val oldRouteController = MapRouteController(oldRoute, MapMatrixContext())

        val startTime = oldRouteController.selectStartTime()
        val endTime = oldRouteController.selectEndTime()
        oldRoute.updateStartTimeByMinutesOfDay(startTime)
        oldRoute.updateEndTimeByMinutesOfDay(endTime)

        return oldRoute
    }

    private fun createDecision(oldMapRoute: MapRoute): MapRoute {
        val oldRouteController = MapRouteController(oldMapRoute, MapMatrixContext())

        val startTime = oldRouteController.selectStartTime()
        val endTime = oldRouteController.selectEndTime()

        oldMapRoute.updateStartTimeByMinutesOfDay(startTime)
        oldMapRoute.updateEndTimeByMinutesOfDay(endTime)

        val avoidedPolygons = oldRouteController.selectSquaresToAvoid(startTime, endTime)
        if (avoidedPolygons.isEmpty()) {
            oldMapRoute.rankDegree = increaseRankDegree(oldMapRoute.rankDegree)
            return oldMapRoute
        }

        val newRoute = createNewMapRoute(oldMapRoute, avoidedPolygons, startTime, endTime)

        if (newRoute == null) {
            oldMapRoute.rankDegree = decreaseRankDegree(oldMapRoute.rankDegree)
            return oldMapRoute
        }


        return if (newRoute.durationTimeInMinutesOfDay > newRoute.getEndTimeInMinutesOfDay() - newRoute.getStartTimeInMinutesOfDay()) {
            if (Random.nextInt(0, 100) / 100.0 < NEW_ROUTE_CONTRADICTION_IGNORE_CHANCE) {
                newRoute
            } else {
                oldMapRoute.rankDegree = decreaseRankDegreeMore(oldMapRoute.rankDegree)
                oldMapRoute
            }
        } else {
            newRoute
        }
    }

    private fun createNewMapRoute(
        oldRoute: MapRoute,
        avoidedPolygons: List<MapSquare>,
        startTime: Int,
        endTime: Int
    ): MapRoute? {
        val featureCollection =
            drivingService.getGeoJsonRoute(
                oldRoute.getMutableRouteData().start.convertToArray(),
                oldRoute.getMutableRouteData().end.convertToArray(),
                avoidedPolygons
            ) ?: return null

        val routeData = mappingService.mapFeatureToRoute(
            featureCollection.features.first()
        )

        val newRoute = MapRoute(routeData)
        newRoute.updateStartTimeByMinutesOfDay(startTime)
        newRoute.updateEndTimeByMinutesOfDay(endTime)

        mapMatrixController.makeRouteVisitSquares(newRoute)
        mapMatrixController.makeRouteStopVisitSquares(oldRoute)

        return newRoute
    }

    private fun createBaseDecisions(oldRoute: MapRoute): List<MapRoute> {
        val result = arrayListOf<MapRoute>()

        for (num in 0..COUNT_OF_DECISIONS_FOR_ONE_ROUTE) {
            result.add(createBaseDecision(oldRoute))
        }

        return result
    }


    private fun createDecisions(oldRoute: MapRoute): List<MapRoute> {
        val result = arrayListOf<MapRoute>()

        for (num in 0..COUNT_OF_DECISIONS_FOR_ONE_ROUTE) {
            result.add(createDecision(oldRoute))
        }

        return result
    }

    private fun decreaseRankDegree(defaultRankDegree: Double): Double {
        return defaultRankDegree * min(ROUTE_RANK_DECREASE_DEGREE, 0.95)
    }

    private fun increaseRankDegree(defaultRankDegree: Double): Double {
        return max(defaultRankDegree * (2.0 - min(ROUTE_RANK_DECREASE_DEGREE, 0.95)), 1.0)
    }

    private fun decreaseRankDegreeMore(defaultRankDegree: Double): Double {
        var result = defaultRankDegree
        for (i in 1..INCORRECT_DECISION_TIMES)
            result = decreaseRankDegree(result)
        return result
    }
}