package com.example.demo.repository.impl

import com.example.demo.repository.model.ZoneModel
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ZoneRepository : JpaRepository<ZoneModel, Int> {

}