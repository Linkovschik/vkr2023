package com.example.demo.repository.model

import jakarta.persistence.*

@Entity
@Table(name = "route_segment_step")
class RouteSegmentStepModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null

    @Column(nullable = false)
    var distance: Double = 0.0

    @Column(nullable = false)
    var duration: Double = 0.0

    @Column(nullable = false)
    var type: Long = 0

    @Column(nullable = false)
    var instruction: String = ""

    @Column(nullable = false)
    var name: String = ""

    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @JoinColumn(name = "startPointId")
    var start: PointModel? = null

    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @JoinColumn(name = "endPointId")
    var end: PointModel? = null
}