package com.example.demo.algorithm.service

import com.example.demo.geojson.model.MyPoint
import com.example.demo.algorithm.model.MapSquare
import org.springframework.stereotype.Component
import java.math.RoundingMode

@Component
class MapMatrixService() {
    fun createMatrix(leftBotPoint: MyPoint = MyPoint(55.87409794116178, 54.668937265391214),
                     rightTopPoint: MyPoint = MyPoint(56.15974247241177, 54.86664699292725),
                     size: Int = 30,
                     scale: Int = 6): List<MapSquare> {
        val result = ArrayList<MapSquare>();

        val lngStep = floorByPrecision(rightTopPoint.lng - leftBotPoint.lng, scale) / size
        val latStep = floorByPrecision(rightTopPoint.lat - leftBotPoint.lat, scale) / size

        for (i in 0 until size) {
            for (j in 0 until size) {
                result.add(
                        MapSquare(
                                MyPoint(leftBotPoint.lng + lngStep * i, leftBotPoint.lat + latStep * j),
                                MyPoint(leftBotPoint.lng + lngStep * i, leftBotPoint.lat + latStep * (j + 1)),
                                MyPoint(leftBotPoint.lng + lngStep * (i + 1), leftBotPoint.lat + latStep * (j + 1)),
                                MyPoint(leftBotPoint.lng + lngStep * (i + 1), leftBotPoint.lat + latStep * j)
                        ))
            }
        }

        return result
    }

    private fun floorByPrecision(number: Double, scale: Int): Double {
        return number.toBigDecimal().setScale(scale, RoundingMode.FLOOR).toDouble()
    }
}