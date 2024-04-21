package com.example.demo.mvc

import jakarta.persistence.*

@Entity
@Table(name = "users")
class MyUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(unique = true)
    var name: String? = null
    var password: String? = null
    var roles: String? = null
}
