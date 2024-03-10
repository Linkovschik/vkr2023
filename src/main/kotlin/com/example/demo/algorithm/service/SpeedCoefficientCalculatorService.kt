package com.example.demo.algorithm.service

import com.example.demo.algorithm.DefaultAlgorithm.Companion.MAX_SPEED_IN_METERS_PER_MINUTE
import com.example.demo.algorithm.DefaultAlgorithm.Companion.MIN_SPEED_IN_METERS_PER_MINUTE
import org.springframework.stereotype.Component

class SpeedCoefficientCalculatorService(
    private val maxSpeed: Double = MAX_SPEED_IN_METERS_PER_MINUTE,
    private val minSpeed: Double = MIN_SPEED_IN_METERS_PER_MINUTE
) {
    fun calculate(speed: Double): Double {
        if (maxSpeed < minSpeed + 1e-10)
            throw Exception("Максимальная скорость должна быть больше минимальной")

        var speedDiff = speed - minSpeed

        if (speedDiff < 1e-10)
            speedDiff = minSpeed

        return speedDiff / (maxSpeed - minSpeed)
    }
}