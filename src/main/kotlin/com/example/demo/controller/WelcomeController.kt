package com.example.demo.controller

import com.example.demo.algorithm.DefaultAlgorithm
import com.example.demo.algorithm.model.MapRoute
import com.example.demo.geojson.model.MyPoint
import com.example.demo.geojson.model.Route
import com.example.demo.geojson.model.Zone
import com.example.demo.geojson.service.MappingService
import com.example.demo.mvc.MyUser
import com.example.demo.mvc.UserRepository
import com.example.demo.repository.impl.PointRepository
import com.example.demo.repository.impl.RouteRepository
import com.example.demo.repository.impl.SegmentRepository
import com.example.demo.repository.impl.ZoneRepository
import com.example.demo.repository.mapper.RouteModelMapper
import com.example.demo.repository.mapper.ZoneModelMapper
import com.example.demo.repository.model.PointModel
import com.example.demo.repository.model.RouteModel
import com.example.demo.repository.model.ZoneModel
import com.example.demo.retrofit.DrivingService
import com.example.demo.web.RouteBuildModel
import com.example.demo.web.UpdateRoutesModel
import com.example.demo.web.UpdateZonesModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import java.io.File
import java.sql.Connection
import java.sql.PreparedStatement
import javax.sql.DataSource


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
    private lateinit var segmentRepository: SegmentRepository

    @Autowired
    private lateinit var pointRepository: PointRepository

    @Autowired
    private lateinit var zoneRepository: ZoneRepository

    @Autowired
    private lateinit var routeModelMapper: RouteModelMapper

    @Autowired
    private lateinit var zoneModelMapper: ZoneModelMapper

    @Autowired
    private lateinit var datasource: DataSource

    @Autowired
    private lateinit var repository: UserRepository
    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder


    @PostMapping("/new-user")
    fun addUser(@RequestBody user: MyUser): String {
        user.password = passwordEncoder.encode(user.password)

        repository.save(user)
        return "User is saved"
    }

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
    fun updateRoutes(@RequestBody updateRoutesModel: UpdateRoutesModel): UpdateRoutesModel {
        routeTable.clear()
        routeTable.addAll(updateRoutesModel.routes)
        val savedRoutes = writeRoutesToFile(updateRoutesModel)
        return UpdateRoutesModel()
            .apply {
                routes = savedRoutes.mapNotNull { routeModelMapper.mapRouteModelToRoute(it) }
            }
    }

    @PutMapping("/updateZones")
    @ResponseStatus(value = HttpStatus.OK)
    fun updateZones(@RequestBody updateZonesModel: UpdateZonesModel) {
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

    @GetMapping("/loadRoutesResult")
    @ResponseBody
    fun loadRoutesResult(): List<Route> {
        val result = arrayListOf<Route>();

        val conn = datasource.connection

        val sql = """
            select 
            ird.route_id,
            ird.distance, 
            ird.duration,
            ird.start_lng,
            ird.start_lat,
            ird.end_lng,
            ird.end_lat,
            ird.start_time_min,
            ird.start_time_max, 
            ird.end_time_min,
            ird.end_time_max,
            ird.route_name,
            ird.start_time, 
            ird.end_time,
            ird.coordinates
            from iteration_route_data ird 
            where ird.iteration_index = ?
            """.trimIndent()
        val preparedStatement: PreparedStatement = conn.prepareStatement(sql)
        val iterationIndex = selectMaxIterationIndex(conn)
        preparedStatement.setInt(1,iterationIndex)

        val resultSet = preparedStatement.executeQuery()

        while (resultSet.next()) {
            val routeId = resultSet.getInt(1)
            result.add(
                Route(
                    routeId,
                    emptyList(),
                    Gson().fromJson(resultSet.getString(15), object : TypeToken<List<MyPoint>>() {}.type),
                    //selectRouteCoordinates(conn, iterationIndex, routeId),
                    resultSet.getDouble(2),
                    resultSet.getDouble(3),
                    MyPoint(resultSet.getDouble(4), resultSet.getDouble(5)),
                    MyPoint(resultSet.getDouble(6), resultSet.getDouble(7)),
                    resultSet.getTime(8),
                    resultSet.getTime(9),
                    resultSet.getTime(10),
                    resultSet.getTime(11),
                    resultSet.getString(12),
                    resultSet.getTime(13),
                    resultSet.getTime(14)
                )
            )
        }

        return result
    }

    private fun selectMaxIterationIndex(conn: Connection): Int {
        val sql = """
            select MAX(ird.iteration_index) from iteration_route_data ird;
            """.trimIndent()
        val preparedStatement: PreparedStatement = conn.prepareStatement(sql)

        val resultSet = preparedStatement.executeQuery()

        if (resultSet.next())
            return resultSet.getInt(1)
        else
            return 0

    }

    private fun selectRouteCoordinates(conn: Connection, iterationIndex: Int, routeId: Int):  List<MyPoint>{
        val result = mutableListOf<MyPoint>()

        val sql = """
            select irmd.lng, irmd.lat 
            from iteration_route_modeling_data irmd 
            where iteration_index = ?
            and route_id = ?
            """.trimIndent()
        val preparedStatement: PreparedStatement = conn.prepareStatement(sql)
        preparedStatement.setInt(1, iterationIndex)
        preparedStatement.setInt(2, routeId)

        val resultSet = preparedStatement.executeQuery()

        while (resultSet.next()) {
            result.add(MyPoint(resultSet.getDouble(1), resultSet.getDouble(2)))
        }

        return result;
    }

    private fun writeZonesToFile(
        updateZonesModel: UpdateZonesModel,
        filePath: String = "D:\\VKR\\project\\fakeDB\\zonesFakeDB.txt"
    ) {
        val gson = Gson()
        val jsonString: String = gson.toJson(updateZonesModel)
        val file = File(filePath)
        file.writeText(jsonString)

        val existingZoneList = zoneRepository.findAll()
        val existingZoneIdList = existingZoneList.map { it.id }

        // create new
        zoneRepository.saveAll(updateZonesModel
            .savedZones
            .filter { !existingZoneIdList.contains(it.id) }
            .map {
                zoneModelMapper.mapZoneToZoneModel(it)
            }
        )

        // update existing
        updateZonesModel
            .savedZones
            .forEach {
                val zoneModel = it.id?.let { r -> zoneRepository.findById(r).orElse(null) }
                if (zoneModel != null)
                    updateZoneModel(zoneModel, it)
            }

        // delete not existing
        existingZoneIdList
            .filter { !updateZonesModel.savedZones.map { it.id }.contains(it) }
            .forEach {
                val zoneModelToDelete = it?.let { r -> zoneRepository.findById(r).orElse(null) }
                if (zoneModelToDelete != null)
                    zoneRepository.delete(zoneModelToDelete)
            }


        zoneRepository.flush()
        pointRepository.flush()
    }


    private fun readZonesFromFile(filePath: String = "D:\\VKR\\project\\fakeDB\\zonesFakeDB.txt"): UpdateZonesModel {
        val gson = Gson()
        val file = File(filePath)
        val result = file.readText()
        if (result.isBlank())
            return UpdateZonesModel()

        return UpdateZonesModel(zoneRepository.findAll().mapNotNull { zoneModelMapper.mapZoneModelToZone(it) })
    }

    private fun writeRoutesToFile(
        updateRoutesModel: UpdateRoutesModel,
        filePath: String = "D:\\VKR\\project\\fakeDB\\fakeDB.txt"
    ): MutableList<RouteModel> {
        val gson = Gson()
        val jsonString: String = gson.toJson(updateRoutesModel)
        val file = File(filePath)
        file.writeText(jsonString)

        val existingRouteList = routeRepository.findAll()
        val existingRouteIdList = existingRouteList.map { it.id }

        // create new
        routeRepository.saveAll(updateRoutesModel
            .routes
            .filter { !existingRouteIdList.contains(it.id) }
            .map {
                routeModelMapper.mapRouteToRouteModel(it)
            }
        )

        // update existing
        updateRoutesModel
            .routes
            .forEach {
                val routeModel = it.id?.let { r -> routeRepository.findById(r).orElse(null) }
                if (routeModel != null)
                    updateRouteModelData(routeModel, it)
            }

        // delete not existing
        existingRouteIdList
            .filter { !updateRoutesModel.routes.map { it.id }.contains(it) }
            .forEach {
                val routeModelToDelete = it?.let { r -> routeRepository.findById(r).orElse(null) }
                if (routeModelToDelete != null)
                    routeRepository.delete(routeModelToDelete)
            }


        routeRepository.flush()
        segmentRepository.flush()
        pointRepository.flush()

        return routeRepository.findAll()
    }

    private fun readRoutesFromFile(filePath: String = "D:\\VKR\\project\\fakeDB\\fakeDB.txt"): UpdateRoutesModel {
        val gson = Gson()
        val file = File(filePath)
        val result = file.readText()
        if (result.isBlank())
            return UpdateRoutesModel()
        //return gson.fromJson(result, UpdateRoutesModel::class.java)

        return UpdateRoutesModel(routeRepository.findAll().mapNotNull { routeModelMapper.mapRouteModelToRoute(it) })
    }

    private fun updateRouteModelData(routeModelToUpdate: RouteModel?, route: Route?) {
        if (routeModelToUpdate == null || route == null) return
        if (routeModelToUpdate.id != route.id) return

        routeModelToUpdate.name = route.name
        routeModelToUpdate.startTimeMin = route.startTimeMin
        routeModelToUpdate.startTimeMax = route.startTimeMax
        routeModelToUpdate.endTimeMin = route.endTimeMin
        routeModelToUpdate.endTimeMax = route.endTimeMax

        routeModelToUpdate.distance = route.distance
        routeModelToUpdate.duration = route.duration
        routeModelToUpdate.startTime = route.startTime
        routeModelToUpdate.endTime = route.endTime

        val segmentsToClearIds = routeModelToUpdate.segments.map { it.id }
        routeModelToUpdate.segments.clear()
        routeModelToUpdate.segments.addAll(route.segments.mapNotNull { routeModelMapper.mapRouteSegmentToModel(it) }
            .toMutableList())
        segmentRepository.deleteAll(segmentRepository.findAllById(segmentsToClearIds))

        val pointsToClearById = routeModelToUpdate.coordinates.map { it.id }
        routeModelToUpdate.coordinates.clear()
        routeModelToUpdate.coordinates.addAll(route.coordinates.mapNotNull { routeModelMapper.mapPointToPointModel(it) }
            .toMutableList())
        pointRepository.deleteAll(pointRepository.findAllById(pointsToClearById))

        routeModelToUpdate.startPoint = null
        routeModelToUpdate.startPoint = routeModelMapper.mapPointToPointModel(route.start)
        routeModelToUpdate.endPoint = null
        routeModelToUpdate.endPoint = routeModelMapper.mapPointToPointModel(route.end)

        routeRepository.save(routeModelToUpdate)
    }

    private fun updateZoneModel(zoneModel: ZoneModel?, zone: Zone?) {
        if (zoneModel == null || zone == null) return
        if (zoneModel.id != zone.id) return

        zoneModel.congestion = zone.congestion
        zoneModel.point = null
        zoneModel.point = PointModel()
            .apply {
                lat = zone.lat
                lng = zone.lng
            }
    }

}