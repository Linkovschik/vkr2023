package com.example.demo.repository.model

import jakarta.persistence.*

@Entity
@Table(name = "zone")
class ZoneModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null

    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @JoinColumn(name = "pointId")
    var point: PointModel? = null

    @Column(nullable = false)
    var congestion: Double = 0.0
}
