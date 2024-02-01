package com.example.demo.algorithm.controller

import com.example.demo.algorithm.model.MapRoute
import java.math.BigDecimal

class MapMatrixControllerProxy(
    private val mapMatrixController: MapMatrixController
) : MapMatrixData {
    override fun getMaxCongestion(): BigDecimal {
        return mapMatrixController.getMaxCongestion()
    }

    override fun getMinCongestion(): BigDecimal {
        return mapMatrixController.getMinCongestion()
    }

    override fun getAvgCongestion(): BigDecimal {
        return mapMatrixController.getAvgCongestion()
    }

    override fun updateMatrixState(routes: List<MapRoute>) {
        return mapMatrixController.updateMatrixState(routes)
    }
}