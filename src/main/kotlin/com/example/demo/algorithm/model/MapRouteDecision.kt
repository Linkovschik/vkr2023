package com.example.demo.algorithm.model

import java.math.BigDecimal

class MapRouteDecision(
    mapRoute: MapRoute,
    val avgCongestion: BigDecimal,
    val startTimeInMinutes: Int = mapRoute.maxStartTimeInMinutesOfDay,
    val endTimeInMinutes: Int = mapRoute.minEndTimeInMinutesOfDay,
    val rankDegree: Double = 1.0,
) : MapRoute(mapRoute)