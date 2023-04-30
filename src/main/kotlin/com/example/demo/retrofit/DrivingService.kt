package com.example.demo.retrofit

import com.example.demo.algorithm.model.MapSquare
import com.example.demo.geojson.dto.AvoidPolygonOption
import com.example.demo.geojson.dto.FeatureCollection
import com.example.demo.geojson.dto.RoutingAvoidPolygonModel
import com.example.demo.geojson.dto.RoutingOptions
import okhttp3.OkHttpClient
import org.springframework.stereotype.Component
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Component
class DrivingService(
        private val retrofit: Retrofit = Retrofit.Builder()
                .baseUrl("http://localhost:8080/ors/v2/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(OkHttpClient.Builder().build())
                .build(),
) {

    private val service = retrofit.create(RouteService::class.java)

    fun getGeoJsonRoute(start: ArrayList<Double>, end: ArrayList<Double>): FeatureCollection? {
        val callSync = service.getRoute(start, end)

        return try {
            val response = callSync.execute()
            response.body()
        } catch (e: Exception) {
            System.out.println("API EXCEPTION: " + e.message + "; Stacktrace " + e.stackTrace)
            null
        }
    }

    fun getGeoJsonRoute(start: ArrayList<Double>, end: ArrayList<Double>, avoidedSqaures: List<MapSquare>): FeatureCollection? {
        var routingOptions: RoutingOptions? = null
        if (!avoidedSqaures.isEmpty()) {
            val avoidPolygonOption = AvoidPolygonOption("MultiPolygon", listOf(avoidedSqaures.map { it.convertToArray() }))
            routingOptions = RoutingOptions(avoidPolygonOption)
        }
        val postRouteBody = RoutingAvoidPolygonModel(arrayListOf(start, end), routingOptions)

        val callSync = service.postRoute(postRouteBody)

        return try {
            val response = callSync.execute()
            response.body()
        } catch (e: Exception) {
            System.out.println("API EXCEPTION: " + e.message + "; Stacktrace " + e.stackTrace)
            null
        }
    }
}