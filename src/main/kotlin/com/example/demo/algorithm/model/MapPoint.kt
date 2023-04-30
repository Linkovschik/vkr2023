package com.example.demo.algorithm.model

class MapPoint(val lng: Double = 0.0, val lat: Double = 0.0) {
    fun convertToArray() : ArrayList<Double> {
        return arrayListOf(lng, lat)
    }
}