const MapStatesEnum = { "PutRouteStart": 5, "PutRouteEnd": 1, "BuildRoute": 2, "Algorithm": 3, "RouteEdit": 4 };

const ZoneMapStatesEnum = {"Default" : 3 ,"PutZone": 1, "ZoneEdit": 2};

class MapStructure {
    constructor(map) {
        this.map = map
        this.mapState = MapStatesEnum.Algorithm
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

        this.selectedRoute = null
        this.selectedZone = null
    }

    addObjectOnMap(mapLayerObject) {
        this.figuresOnMap.addLayer(mapLayerObject.layerObject)
    }

    removeObjectFromMap(mapLayerObject) {
        this.figuresOnMap.removeLayer(mapLayerObject.layerObject)
    }

    clearMapFromObjects() {
        this.figuresOnMap.clearLayers()
    }

}

class MapLayerObject {
    constructor(layerObject, mapStructure) {
        this.layerObject = layerObject
        this.mapStructure = mapStructure

        this.layerObject.relatedObject = this
    }

    addOnMap() {
        this.mapStructure.addObjectOnMap(this)
    }

    removeFromMap() {
        this.mapStructure.removeObjectFromMap(this)
    }

}

class Point extends MapLayerObject {
    constructor(lat, lng, mapStructure, draggable = true, icon = null, parents = new Set()) {
        if (!icon)
            throw new Error('Set icon type for Point child class!' + this.getClass())

        var pointMarker = L.marker(new L.LatLng(lat, lng), { icon: icon, draggable: draggable })
        super(pointMarker, mapStructure)

        this.mapStructure = mapStructure
        this.draggable = draggable
        this.icon = icon
        this.parents = parents

        this.enableDraggingUpdate()
        this.enableRouteRelatedPointClick()
    }

    setIcon(icon) {
        if (!this.layerObject instanceof L.Marker)
            return
        this.layerObject.setIcon(icon)
    }

    getIcon() {
        if (!this.layerObject instanceof L.Marker)
            return
        return this.layerObject.options.icon
    }

    enableDraggingUpdate() {
        if (!this.layerObject instanceof L.Marker)
            return

        var marker = this.layerObject
        marker.options.draggable = this.draggable
        marker.on('dragend', function(event) {
            var marker = event.target;
            var position = marker.getLatLng();
            marker.setLatLng(new L.LatLng(position.lat, position.lng));
         });
    }

    enableRouteRelatedPointClick() {
        if (!this.layerObject instanceof L.Marker)
            return

        var marker = this.layerObject
        marker.on('dblclick', function(event) {
            var marker = event.target;
            if (!marker.relatedObject instanceof Point)
                return

            var parent = marker.relatedObject.parents.values().next().value
            if (parent == null)
                return

            if (marker.relatedObject.mapStructure.selectedRoute)
                marker.relatedObject.mapStructure.selectedRoute = null
            else
                marker.relatedObject.mapStructure.selectedRoute = parent
         });
    }

    getLatLng() {
        return this.layerObject.getLatLng()
    }

    setLat(lat) {
        this.layerObject.setLatLng(new L.LatLng(lat, this.layerObject.getLatLng().lng), {draggable: this.draggable.toString()})
    }

    setLng(lng) {
        this.layerObject.setLatLng(new L.LatLng(this.layerObject.getLatLng().lat, lng), {draggable: this.draggable.toString()})
    }

    toggleDraggable() {
        if (this.layerObject.dragging) {
            if (this.layerObject.options.draggable) {
                this.layerObject.options.draggable = false
                this.layerObject.dragging.disable();
            }
            else {
                this.layerObject.options.draggable = true
                this.layerObject.dragging.enable();
            }
        }
    }

    mapToSendModel() {
        return {
            lng: (this.layerObject instanceof L.Marker)? this.layerObject.getLatLng().lng : 0.0,
            lat: (this.layerObject instanceof L.Marker)? this.layerObject.getLatLng().lat : 0.0
        }
    }

    addOnMap() {
        super.addOnMap()
    }

    addParent(parent) {
        this.parents.add(parent)
    }

    clearParents() {
        this.parents.clear()
    }
}

class StartPoint extends Point {
    constructor(lat, lng, mapStructure, draggable = true, icon = mapStructure.blueIcon, parents = new Set()) {
        super(lat, lng, mapStructure, draggable, icon, parents)
    }
}

class EndPoint extends Point {
    constructor(lat, lng, mapStructure, draggable = true, icon = mapStructure.blackIcon, parents = new Set()) {
        super(lat, lng, mapStructure, draggable, icon, parents)
    }
}

class Zone extends MapLayerObject {
    constructor(zoneData, mapStructure, draggable = true, icon = mapStructure.blackIcon, parents = new Set()) {
        var lat = zoneData.lat
        var lng = zoneData.lng
        if (!icon)
            throw new Error('Set icon type for Point child class!' + this.getClass())

        var marker = L.marker(new L.LatLng(lat, lng), { icon: icon, draggable: false })
        super(marker, mapStructure)

        this.zoneData = zoneData

        this.layerObject.on('dblclick', function(event) {
            var marker = event.target;
            if (!marker.relatedObject instanceof Zone)
                return

            if (marker.relatedObject.mapStructure.selectedZone)
                marker.relatedObject.mapStructure.selectedZone = null
            else
                marker.relatedObject.mapStructure.selectedZone = marker.relatedObject

        });

        this.layerObject.on('dragend', function(event) {
            var marker = event.target;
            if (!marker.relatedObject instanceof Zone)
                return
            var position = marker.getLatLng();
            marker.setLatLng(new L.LatLng(position.lat, position.lng));
            marker.relatedObject.updateZoneDataCoordinates(position.lat, position.lng)
         });
    }

    updateZoneDataCoordinates(lat, lng) {
        this.zoneData.lat = lat
        this.zoneData.lng = lng
    }

    setIcon(icon) {
        if (!this.layerObject instanceof L.Marker)
            return
        this.layerObject.setIcon(icon)
    }

    getIcon() {
        if (!this.layerObject instanceof L.Marker)
            return
        return this.layerObject.options.icon
    }
}

class Route extends MapLayerObject {
    constructor(routeData, mapStructure, polylineColorName = 'black') {
        if (!polylineColorName)
            throw new Error('Set polylineColorName for Route child class!' + this.getClass())

        var polyline = L.polyline(routeData.coordinates.map(mpoint => [mpoint.lat, mpoint.lng]), {color: polylineColorName});
        super(polyline)

        this.routeData = routeData
        this.mapStructure = mapStructure

        var start = this.routeData.start
        var startPoint = new StartPoint(start.lat, start.lng, this.mapStructure, false)

        var end = this.routeData.end
        var endPoint = new EndPoint(end.lat, end.lng, this.mapStructure, false)

        this.start = startPoint
        this.end = endPoint

        this.start.addParent(this)
        this.end.addParent(this)

    }

    removeFromMap() {
        this.start.removeFromMap()
        super.removeFromMap()
        this.end.removeFromMap()
    }

    addOnMap() {
        if (!this.layerObject instanceof L.Polyline)
            return

        this.start.addOnMap()
        super.addOnMap()
        this.end.addOnMap()
    }

    mapToSendModel() {
        return {
            start: this.start.mapToSendModel(),
            end: this.end.mapToSendModel(),
            routeData: this.routeData
        }
    }
}

class BuildRouteTemp {
    constructor() {
        this.startPoint = null
        this.endPoint = null
    }

    setBuildRouteTempStartPoint(startPoint) {
        this.clearBuildRouteTempStartPoint()
        this.startPoint = startPoint
        this.startPoint.addOnMap()
    }

    setBuildRouteTempEndPoint(endPoint) {
        this.clearBuildRouteTempEndPoint()
        this.endPoint = endPoint
        this.endPoint.addOnMap()
    }

    isBuildRouteTempCompleted() {
        return this.startPoint != null && this.endPoint != null
    }

    clearBuildRouteTempStartPoint() {
        if (this.startPoint != null) {
            this.startPoint.removeFromMap()
            this.startPoint = null
        }
    }

    clearBuildRouteTempEndPoint() {
        if (this.endPoint != null) {
            this.endPoint.removeFromMap()
            this.endPoint = null
        }
    }

    clearBuildRouteTemp() {
        this.clearBuildRouteTempStartPoint()
        this.clearBuildRouteTempEndPoint()
    }
}

class Driver {
    constructor() {
        this.mapRoute = null
    }
}