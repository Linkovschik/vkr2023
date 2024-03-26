package com.example.demo.repository.impl

import com.example.demo.repository.model.RouteSegmentModel
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SegmentRepository : JpaRepository<RouteSegmentModel, Int> {

}