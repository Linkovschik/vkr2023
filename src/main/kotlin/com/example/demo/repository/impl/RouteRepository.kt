package com.example.demo.repository.impl

import com.example.demo.repository.model.RouteModel
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RouteRepository : JpaRepository<RouteModel, Int> {

}