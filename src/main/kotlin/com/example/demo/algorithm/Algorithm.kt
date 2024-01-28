package com.example.demo.algorithm

import com.example.demo.geojson.model.Route
import org.springframework.stereotype.Component

@Component
interface Algorithm {
    fun rebuildRoutes(routes: List<Route>, allRoutes: List<Route>) : List<Route>
}