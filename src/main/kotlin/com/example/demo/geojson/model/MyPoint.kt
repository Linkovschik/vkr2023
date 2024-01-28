package com.example.demo.geojson.model

import com.google.gson.Gson

data class MyPoint(val lng: Double = 0.0, val lat: Double = 0.0) {
    fun convertToArray() : ArrayList<Double> {
        return arrayListOf(lng, lat)
    }

    fun toStr() : String {
        val gson = Gson()
        return gson.toJson(this)
    }
}