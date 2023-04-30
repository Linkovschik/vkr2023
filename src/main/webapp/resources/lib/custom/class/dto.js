class MapLayerObject {
    constructor(layerObject) {
        this.layerObject = layerObject
    }
}

class Point extends MapLayerObject {
    constructor(lat, lng, marker
    ) {
        super(marker)
        this.lng = lng
        this.lat = lat

    }
    
    mapToSendModel() {
        return {
            lng: this.lng,
            lat: this.lat
        }
    }
}

class Route extends MapLayerObject {
    constructor(routeData, start, end, polyline) {
        super(polyline)
        this.start = start
        this.end = end
        this.routeData = routeData
    }

    mapToSendModel() {
        return {
            start: this.start.mapToSendModel(),
            end: this.end.mapToSendModel()
        }
    }
}

const MapStatesEnum = { "PutRouteStart": 0, "PutRouteEnd": 1, "Algorithm": 2 };

class MapStructure {
    constructor(map) {
        this.map = map
        this.mapState = MapStatesEnum.Algorithm
        this.mapObjects = []
        this.mapTempObjects = []
        this.figuresOnMap = L.layerGroup([]).addTo(map)
        this.greenIcon = new L.Icon({
            iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-green.png',
            shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',
            iconSize: [25, 41],
            iconAnchor: [12, 41],
            popupAnchor: [1, -34],
            shadowSize: [41, 41]
        })
        this.redIcon = new L.Icon({
            iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-red.png',
            shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',
            iconSize: [25, 41],
            iconAnchor: [12, 41],
            popupAnchor: [1, -34],
            shadowSize: [41, 41]
        })
        this.blackIcon = new L.Icon({
            iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-black.png',
            shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',
            iconSize: [25, 41],
            iconAnchor: [12, 41],
            popupAnchor: [1, -34],
            shadowSize: [41, 41]
        })
        this.blueIcon = new L.Icon({
            iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-blue.png',
            shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',
            iconSize: [25, 41],
            iconAnchor: [12, 41],
            popupAnchor: [1, -34],
            shadowSize: [41, 41]
        })
    }

    addObjectToBothMapAndTemp(mapLayerObject) {
        this.figuresOnMap.addLayer(mapLayerObject.layerObject)
        this.mapTempObjects.push(mapLayerObject)
    }

    popObjectFromTempAndMap() {
        var res = this.mapTempObjects.pop()
        this.figuresOnMap.removeLayer(res.layerObject)
        return res
    }

    clearObjectsFromBothMapAndTemp() {
        this.mapTempObjects.forEach(element => {
            this.figuresOnMap.removeLayer(element.layerObject)
        })
        this.mapTempObjects = []
    }

    addObjectToBothMapAndCache(mapLayerObject) {
        this.figuresOnMap.addLayer(mapLayerObject.layerObject)
        this.mapObjects.push(mapLayerObject)
    }

    removeObjectFromBothMapAndCache(mapLayerObject) {
        this.figuresOnMap.removeLayer(mapLayerObject.layerObject)
        this.mapObjects.remove(mapLayerObject)
    }

    clearObjectsFromBothMapAndCache() {
        this.mapObjects.forEach(element => {
            this.figuresOnMap.removeLayer(element.layerObject)
        })
        this.mapObjects = []
    }
}

class Driver {
    constructor() {
        this.mapRoute = null
    }
}