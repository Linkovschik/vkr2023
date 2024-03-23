package com.example.demo.repository.mapper

import com.example.demo.geojson.model.Zone
import com.example.demo.repository.model.PointModel
import com.example.demo.repository.model.ZoneModel
import org.springframework.stereotype.Component

@Component
class ZoneModelMapper {
    fun mapZoneToZoneModel(zone: Zone?) : ZoneModel? {
        if (zone == null) return null

        val result = ZoneModel().apply {
            id = zone.id
            point = PointModel().apply {
                lat = zone.lat
                lng = zone.lng
            }
            congestion = zone.congestion
        }

        return result
    }

    fun mapZoneModelToZone(zoneModel: ZoneModel?) : Zone? {
        if (zoneModel == null) return null

        val result = Zone().apply {
            id = zoneModel.id
            lat = zoneModel.point?.lat ?: 0.0
            lng = zoneModel.point?.lng ?: 0.0
            congestion = zoneModel.congestion
        }

        return result
    }
}
