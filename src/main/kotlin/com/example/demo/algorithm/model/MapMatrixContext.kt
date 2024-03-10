package com.example.demo.algorithm.model

import com.example.demo.algorithm.DefaultAlgorithm
import java.math.BigDecimal

data class MapMatrixContext(
    val timeWindowInMinutes: Int  = DefaultAlgorithm.TIME_WINDOW_IN_MINUTES,
    val startTimeInMinutesOfDay: Int = DefaultAlgorithm.ALGORITHM_START_TIME_IN_MINUTES_OF_DAY,
    val endTimeInMinutesOfDay: Int = DefaultAlgorithm.ALGORITHM_END_TIME_IN_MINUTES_OF_DAY,
    val maxCongestion: BigDecimal = BigDecimal.valueOf(1.0),
    val mediumCongestion: BigDecimal = BigDecimal.valueOf(0.5),
    val minCongestion: BigDecimal = BigDecimal.valueOf(0.0)
)
