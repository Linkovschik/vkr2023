package com.example.demo.algorithm.model

import java.math.BigDecimal

class MapRouteDecision(
    mapRoute: MapRoute,
    val avgCongestion: BigDecimal,
    val startTimeInMinutes: Int = mapRoute.maxStartTimeInMinutesOfDay,
    var endTimeInMinutes: Int = mapRoute.minEndTimeInMinutesOfDay,
) : MapRoute(mapRoute) {
}