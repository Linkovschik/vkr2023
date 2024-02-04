package com.example.demo.algorithm.model

import java.math.BigDecimal

data class CongestionResult(
    val minCongestion: BigDecimal,
    val avgCongestion: BigDecimal,
    val maxCongestion: BigDecimal
) {
}