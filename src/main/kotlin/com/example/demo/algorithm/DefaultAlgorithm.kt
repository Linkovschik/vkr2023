package com.example.demo.algorithm

import com.example.demo.algorithm.controller.MapMatrixService
import com.example.demo.algorithm.service.MapMatrixController
import com.example.demo.geojson.model.Route
import com.example.demo.geojson.service.MappingService
import com.example.demo.retrofit.DrivingService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class DefaultAlgorithm : Algorithm {
    private var mapMatrixService: MapMatrixService = MapMatrixService()

    private var drivingService: DrivingService = DrivingService()

    private var mappingService: MappingService = MappingService()

    private val mapMatrixController = MapMatrixController(mapMatrixService.createMatrix())

    override fun rebuildRoutes(routes: List<Route>): List<Route> {

        try {

            routes.forEach { mapMatrixController.makeRouteVisitSquares(it) }

            return routes.map { updateRouteByAlgorithm(it) }
        }
        catch (e: Exception) {
            println(e.message)
        }

        return emptyList()
    }

    private fun updateRouteByAlgorithm(route: Route): Route {
        val avoidedPolygons = mapMatrixController.selectedSquaresToAvoid(route)
        val featureCollection =
                drivingService.getGeoJsonRoute(route.start.convertToArray(), route.end.convertToArray(), avoidedPolygons)
                        ?: return route

        return mappingService.mapFeatureToRoute(featureCollection.features.first())
    }
}