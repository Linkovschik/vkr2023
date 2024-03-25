package com.example.demo.repository.model

import jakarta.persistence.*

@Entity
@Table(name = "route_segment")
class RouteSegmentModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null

    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @JoinColumn(name = "startPointId")
    var startPoint: PointModel? = null

    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @JoinColumn(name = "endPointId")
    var endPoint: PointModel? = null

    @Column(nullable = false)
    var distance: Double = 0.0

    @Column(nullable = false)
    var duration: Double = 0.0

    @OneToMany(cascade = [CascadeType.ALL])
    var steps: MutableList<RouteSegmentStepModel> = mutableListOf()
}