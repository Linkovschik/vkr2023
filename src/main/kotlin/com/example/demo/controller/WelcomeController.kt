package com.example.demo.controller

import com.example.demo.algorithm.DefaultAlgorithm
import com.example.demo.algorithm.model.MapRoute
import com.example.demo.geojson.model.MyPoint
import com.example.demo.geojson.model.Route
import com.example.demo.geojson.model.Zone
import com.example.demo.geojson.service.MappingService
import com.example.demo.repository.impl.RouteRepository
import com.example.demo.repository.mapper.MapRouteModel
import com.example.demo.retrofit.DrivingService
import com.example.demo.web.RouteBuildModel
import com.example.demo.web.UpdateRoutesModel
import com.example.demo.web.UpdateZonesModel
import com.google.gson.Gson
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import java.io.File


@Controller
@RequestMapping("/home")
class WelcomeController {

    private var routeTable = mutableListOf<Route>()

    private var zoneTable = mutableListOf<Zone>()

    @Autowired
    private lateinit var drivingService: DrivingService

    @Autowired
    private lateinit var mappingService: MappingService

    @Autowired
    private lateinit var defaultAlgorithm: DefaultAlgorithm

    @Autowired
    private lateinit var routeRepository: RouteRepository

    @Autowired
    private lateinit var mapRouteModel: MapRouteModel

    @GetMapping("/welcome")
    fun welcome(model: Model): String {
        val point = MyPoint()
        model.addAttribute("point", point);
        return "/index"
    }

    @GetMapping("/zones")
    fun editZone(model: Model): String {
        val point = MyPoint()
        model.addAttribute("point", point);
        return "/zones"
    }

    @PostMapping("/startAlgorithm")
    @ResponseBody
    fun startAlgorithm(@RequestBody updateRoutesModel: UpdateRoutesModel): List<Route> {

        val result = defaultAlgorithm.rebuildRoutes(updateRoutesModel.routes.map { MapRoute(it) })
        return result
            .map { it.getMutableRouteData() }
            .toMutableList()

    }

    @PostMapping("/buildRoute")
    @ResponseBody
    fun buildRoute(@RequestBody routeBuildModel: RouteBuildModel): List<Route> {
        val routeResult = arrayListOf<Route>()

        val featureCollection = drivingService.getGeoJsonRoute(
            routeBuildModel.start.convertToArray(),
            routeBuildModel.end.convertToArray(),
            emptyList()
        ) ?: return routeResult

        val route = mappingService.mapFeatureToRoute(featureCollection.features.first())
        routeResult.add(route)

        return routeResult
    }

    @PutMapping("/updateRoutes")
    @ResponseStatus(value = HttpStatus.OK)
    fun updateRoutes(@RequestBody updateRoutesModel: UpdateRoutesModel){
        routeTable.clear()
        routeTable.addAll(updateRoutesModel.routes)
        writeRoutesToFile(updateRoutesModel)
    }

    @PutMapping("/updateZones")
    @ResponseStatus(value = HttpStatus.OK)
    fun updateZones(@RequestBody updateZonesModel: UpdateZonesModel){
        zoneTable.clear()
        zoneTable.addAll(updateZonesModel.savedZones)
        writeZonesToFile(updateZonesModel)
    }

    @GetMapping("/loadZones")
    @ResponseBody
    fun loadZones(): List<Zone> {
        zoneTable.clear()
        val updateZonesModel = readZonesFromFile()
        zoneTable.addAll(updateZonesModel.savedZones.toMutableList())
        return zoneTable
    }


    @GetMapping("/loadRoutes")
    @ResponseBody
    fun loadRoutes(): List<Route> {
        routeTable.clear()
        val updateRoutesModel = readRoutesFromFile()
        routeTable.addAll(updateRoutesModel.routes.toMutableList())
        return routeTable
    }

    private fun writeZonesToFile(
        updateZonesModel: UpdateZonesModel,
        filePath: String = "D:\\VKR\\project\\fakeDB\\zonesFakeDB.txt"
    ) {
        val gson = Gson()
        val jsonString: String = gson.toJson(updateZonesModel)
        val file = File(filePath)
        file.writeText(jsonString)
    }


    private fun readZonesFromFile(filePath: String = "D:\\VKR\\project\\fakeDB\\zonesFakeDB.txt"): UpdateZonesModel {
        val gson = Gson()
        val file = File(filePath)
        val result = file.readText()
        if (result.isBlank())
            return UpdateZonesModel()
        return gson.fromJson(result, UpdateZonesModel::class.java)
    }

    private fun writeRoutesToFile(
        updateRoutesModel: UpdateRoutesModel,
        filePath: String = "D:\\VKR\\project\\fakeDB\\fakeDB.txt"
    ) {
        val gson = Gson()
        val jsonString: String = gson.toJson(updateRoutesModel)
        val file = File(filePath)
        file.writeText(jsonString)

        routeRepository.deleteAll()
        routeRepository.flush()
        routeRepository.saveAllAndFlush(updateRoutesModel.routes.map { mapRouteModel.mapRouteToRouteModel(it) })
    }

    private fun readRoutesFromFile(filePath: String = "D:\\VKR\\project\\fakeDB\\fakeDB.txt"): UpdateRoutesModel {
        val gson = Gson()
        val file = File(filePath)
        val result = file.readText()
        if (result.isBlank())
            return UpdateRoutesModel()
        //return gson.fromJson(result, UpdateRoutesModel::class.java)

        return UpdateRoutesModel(routeRepository.findAll().mapNotNull { mapRouteModel.mapRouteModelToRouteTo(it) })
    }
}