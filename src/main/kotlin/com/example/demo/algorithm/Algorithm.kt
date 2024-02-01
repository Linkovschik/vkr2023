package com.example.demo.algorithm

import com.example.demo.algorithm.model.MapRoute
import org.springframework.stereotype.Component

@Component
interface Algorithm {
    fun rebuildRoutes(routes: List<MapRoute>) : List<MapRoute>
}