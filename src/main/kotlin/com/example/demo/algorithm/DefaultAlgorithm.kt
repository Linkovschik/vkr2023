package com.example.demo.algorithm

import com.example.demo.algorithm.controller.MapMatrixController
import com.example.demo.algorithm.controller.MapRouteController
import com.example.demo.algorithm.model.MapRoute
import com.example.demo.algorithm.model.MapRouteDecision
import com.example.demo.algorithm.model.MapSquare
import com.example.demo.algorithm.service.MapCongestionService
import com.example.demo.algorithm.service.MapMatrixService
import com.example.demo.algorithm.service.SpeedCoefficientCalculatorService
import com.example.demo.geojson.service.MappingService
import com.example.demo.retrofit.DrivingService
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicInteger
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
        val START_TIME_IN_MINUTES_OF_DAY: Int = 6 * 60
        val END_TIME_IN_MINUTES_OF_DAY: Int = 10 * 60
        val COUNT_OF_DECISIONS_FOR_ONE_ROUTE: Int = 4
        val ROUTE_RANK_DECREASE_DEGREE: Double = 0.82
        val ROUTE_RANK_DECREASE_THRESHOLD: Double = 0.45
        val INCORRECT_DECISION_TIMES: Int = 5
        val MAX_SPEED_IN_METERS_PER_MINUTE: Double = 1665.0
        val MIN_SPEED_IN_METERS_PER_MINUTE: Double = 167.0
        val FORCEFULLY_AVOID_MUTATION_CHANCE: Double = 0.2
    }

    private var mapMatrixService: MapMatrixService = MapMatrixService()

    private var drivingService: DrivingService = DrivingService()

    private var mappingService: MappingService = MappingService()

    private var speedCoefficientCalculatorService: SpeedCoefficientCalculatorService =
        SpeedCoefficientCalculatorService()
    private var mapCongestionService: MapCongestionService = MapCongestionService(speedCoefficientCalculatorService)

    private val mapMatrixController =
        MapMatrixController(mapMatrixService.createMatrix(), mapCongestionService, speedCoefficientCalculatorService)

    private var allRoutes: MutableList<MapRoute> = mutableListOf()

    override fun rebuildRoutes(routes: List<MapRoute>): List<MapRouteDecision> {
        try {
            allRoutes = routes.toMutableList()

            val result = arrayListOf<MapRouteDecision>()

            mapMatrixController.updateMatrixState(allRoutes)

            for (iteration in 1..ALGORITHM_ITERATIONS_COUNT) {
                result.clear()

                if (iteration % REBUILD_MATRIX_ALGORITHM_ITERATIONS_COUNT == 0) {
                    mapMatrixController.updateMatrixState(allRoutes)
                }

                result.addAll(
                    allRoutes.parallelStream()
                        .map { updateRouteByAlgorithm(it) }.collect(Collectors.toList())
                )

            }

            return result

        } catch (e: Exception) {
            println(e.message)
        }

        return emptyList()
    }

    private fun updateRouteByAlgorithm(mapRoute: MapRoute): MapRouteDecision {

        var decision = selectBestDecision(createBaseDecisions(mapRoute))

        for (iteration in 1..ROUTE_DECISION_PER_ITERATIONS_COUNT) {
            decision = selectBestDecision(createDecisions(decision))
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

    private fun createBaseDecision(oldRoute: MapRoute): MapRouteDecision {
        val oldRouteController = MapRouteController(oldRoute, mapCongestionService, mapMatrixController)

        val startTime = oldRouteController.selectStartTime()
        val endTime = oldRouteController.selectEndTime()

        val defaultRankDegree = 1.0
        val defaultCongestion = mapMatrixController.getAvgCongestion()

        val defaultDecision =
            MapRouteDecision(oldRoute, defaultCongestion, startTime, endTime, defaultRankDegree)

        return defaultDecision
    }

    private fun createDecision(oldMapRouteDecision: MapRouteDecision): MapRouteDecision {
        val oldRouteController = MapRouteController(oldMapRouteDecision, mapCongestionService, mapMatrixController)

        val startTime = oldRouteController.selectStartTime()
        val endTime = oldRouteController.selectEndTime()

        val avgCongestion =
            mapCongestionService.calcCongestion(oldMapRouteDecision.visitedSquares, startTime, endTime).avgCongestion

        val avoidedPolygons = oldRouteController.selectSquaresToAvoid(startTime, endTime)
        if (avoidedPolygons.isEmpty())
            return MapRouteDecision(
                oldMapRouteDecision.mapRoute,
                avgCongestion,
                startTime,
                endTime,
                increaseRankDegree(oldMapRouteDecision.rankDegree)
            )

        val newRoute = createNewMapRoute(oldMapRouteDecision, avoidedPolygons)
            ?: return MapRouteDecision(
                oldMapRouteDecision.mapRoute,
                avgCongestion,
                startTime,
                endTime,
                decreaseRankDegreeMore(oldMapRouteDecision.rankDegree)
            )

        return if (newRoute.durationTimeInMinutesOfDay > endTime - startTime) {
            val newRankDegree = decreaseRankDegreeMore(oldMapRouteDecision.rankDegree)
            if (Random.nextDouble(0.0, newRankDegree) > ROUTE_RANK_DECREASE_THRESHOLD)
                MapRouteDecision(
                    oldMapRouteDecision.mapRoute,
                    avgCongestion,
                    startTime,
                    endTime,
                    newRankDegree
                )
            else
                MapRouteDecision(
                    newRoute,
                    avgCongestion,
                    startTime,
                    endTime,
                    newRankDegree
                )

        } else {
            val newRankDegree = decreaseRankDegree(oldMapRouteDecision.rankDegree)
            if (Random.nextDouble(0.0, newRankDegree) > ROUTE_RANK_DECREASE_THRESHOLD)
                MapRouteDecision(
                    oldMapRouteDecision.mapRoute,
                    avgCongestion,
                    startTime,
                    endTime,
                    newRankDegree
                )
            else
                MapRouteDecision(
                    newRoute,
                    avgCongestion,
                    startTime,
                    endTime,
                    newRankDegree
                )
        }
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

    private fun createBaseDecisions(oldRoute: MapRoute): List<MapRouteDecision> {
        val result = arrayListOf<MapRouteDecision>()

        for (num in 0..COUNT_OF_DECISIONS_FOR_ONE_ROUTE) {
            result.add(createBaseDecision(oldRoute))
        }

        return result
    }


    private fun createDecisions(oldRoute: MapRouteDecision): List<MapRouteDecision> {
        val result = arrayListOf<MapRouteDecision>()

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