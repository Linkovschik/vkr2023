package com.example.demo.repository.model

import jakarta.persistence.*
import java.sql.Time
import java.util.*

@Entity
@Table(name = "route")
class RouteModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null

    @OneToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    var segments: MutableList<RouteSegmentModel> = mutableListOf()

    @OneToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    var coordinates: MutableList<PointModel> = mutableListOf()

    @Column(nullable = false)
    var distance: Double = 0.0

    @Column(nullable = false)
    var duration: Double = 0.0

    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "startPointId")
    var startPoint: PointModel? = null

    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "endPointId")
    var endPoint: PointModel? = null

    @Column(nullable = false)
    var startTimeMin: Time = Time(6, 0, 0)

    @Column(nullable = false)
    var startTimeMax: Time = Time(6, 0, 0)

    @Column(nullable = false)
    var endTimeMin: Time = Time(10, 0, 0)

    @Column(nullable = false)
    var endTimeMax: Time = Time(10, 0, 0)

    @Column(nullable = false)
    var name: String? = UUID.randomUUID().toString()

    @Column(nullable = true)
    var startTime: Time? = null

    @Column(nullable = true)
    var endTime: Time? = null
}
