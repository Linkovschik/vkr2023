package com.example.demo.repository.model

import jakarta.persistence.*

@Entity
@Table(name = "point")
class PointModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null

    @Column
    var lng: Double = 0.0

    @Column
    var lat: Double = 0.0
}
