package com.example.demo.algorithm.controller

import com.example.demo.algorithm.model.MapRoute
import java.math.BigDecimal

interface MapMatrixData {
    fun getMaxCongestion(): BigDecimal
    fun getMinCongestion(): BigDecimal
    fun getAvgCongestion(): BigDecimal
    fun updateMatrixState(routes: List<MapRoute>)
}