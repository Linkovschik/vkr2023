package com.example.demo.controller

import com.example.demo.algorithm.DefaultAlgorithm
import com.example.demo.algorithm.model.MapPoint
import com.example.demo.geojson.model.Route
import com.example.demo.geojson.service.MappingService
import com.example.demo.retrofit.DrivingService
import com.example.demo.web.RouteBuildModel
import com.example.demo.web.UpdateRoutesModel
import com.google.gson.Gson
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import java.io.File


@Controller
@RequestMapping("/home")
class WelcomeController {

    private var routeTable = mutableListOf<Route>()

    @Autowired
    private lateinit var drivingService: DrivingService

    @Autowired
    private lateinit var mappingService: MappingService

    @Autowired
    private lateinit var defaultAlgorithm: DefaultAlgorithm

    @GetMapping("/welcome")
    fun welcome(model: Model): String {
        val point = MapPoint()
        model.addAttribute("point", point);
        return "/index"
    }

    @PostMapping("/startAlgorithm")
    @ResponseBody
    fun startAlgorithm(@RequestBody updateRoutesModel: UpdateRoutesModel): List<Route> {
        val result = defaultAlgorithm.rebuildRoutes(updateRoutesModel.routes)
        return result.toMutableList()
    }

    @PostMapping("/buildRoute")
    @ResponseBody
    fun buildRoute(@RequestBody routeBuildModel: RouteBuildModel): Route {
        var route = Route(emptyList(), emptyList(), 0.0, 0.0, MapPoint(0.0, 0.0), MapPoint(0.0, 0.0))

        if (routeBuildModel == null)
            return route

        val featureCollection = drivingService.getGeoJsonRoute(
                routeBuildModel.start.convertToArray(),
                routeBuildModel.end.convertToArray(),
                emptyList()
        ) ?: return route


        route = mappingService.mapFeatureToRoute(featureCollection.features.first())

        return route
    }

    @PostMapping("/updateRoutes")
    fun updateRoutes(@RequestBody updateRoutesModel: UpdateRoutesModel) {
        routeTable.clear()
        routeTable.addAll(updateRoutesModel.routes)
        writeRoutesToFile(updateRoutesModel)
    }

    @GetMapping("/loadRoutes")
    @ResponseBody
    fun loadRoutes(): List<Route> {
        routeTable.clear()
        val updateRoutesModel = readRoutesFromFile()
        routeTable.addAll(updateRoutesModel.routes.toMutableList())
        return routeTable
    }

    private fun writeRoutesToFile(updateRoutesModel: UpdateRoutesModel,
                                  filePath: String = "D:\\projects\\demo\\fakeDB\\fakeDB.txt") {
        val gson = Gson()
        val jsonString: String = gson.toJson(updateRoutesModel)
        val file = File(filePath)
        file.writeText(jsonString)
    }

    private fun readRoutesFromFile(filePath: String = "D:\\projects\\demo\\fakeDB\\fakeDB.txt"): UpdateRoutesModel {
        val gson = Gson()
        val file = File(filePath)
        val result = file.readText()
        return gson.fromJson(result, UpdateRoutesModel::class.java)
    }
}