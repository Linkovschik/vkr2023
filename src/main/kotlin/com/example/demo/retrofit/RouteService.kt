package com.example.demo.retrofit

import com.example.demo.geojson.dto.FeatureCollection
import com.example.demo.geojson.dto.RoutingAvoidPolygonModel
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface RouteService {
    @GET("directions/driving-car")
    fun getRoute(
            @Query("start") start: ArrayList<Double>,
            @Query("end") end: ArrayList<Double>): Call<FeatureCollection>

    @POST("directions/driving-car/geojson")
    fun postRoute(
            @Body postRouteBody: RoutingAvoidPolygonModel) : Call<FeatureCollection>
}