const MapStatesEnum = { "PutRouteStart": 0, "PutRouteEnd": 1, "BuildRoute": 2, "Algorithm": 3, "RouteEdit": 4 };

const IndexZObjectKeepTypeMap = new Map([
  ["temp", 1],
  ["cache", 2]
]);

class MapLayerObject {
    constructor(layerObject, mapStructure) {
        this.layerObject = layerObject
        this.mapStructure = mapStructure

        this.layerObject.relatedObject = this
    }

    addOnMap() {
        this.layerObject.ZIndex=IndexZObjectKeepTypeMap.get("temp")
        this.mapStructure.addObjectToBothMapAndTemp(this)
    }

    removeFromMap() {
        this.mapStructure.removeObjectFromBothMapAndTemp(this)
    }

    addOnMapCache() {
        this.layerObject.ZIndex=IndexZObjectKeepTypeMap.get("cache")
        this.mapStructure.addObjectToBothMapAndCache(this)
    }

    removeFromMapCache() {
        this.mapStructure.removeObjectFromBothMapAndCache(this)
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

            parent.markAsSelected()
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
        marker.options.draggable = !this.draggable
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

    addOnMapCache() {
        super.addOnMapCache()
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

    markAsSelected() {
        this.mapStructure.setSelectedRoute(this)
    }

    removeFromMap() {
        this.start.removeFromMap()
        super.removeFromMap()
        this.end.removeFromMap()
    }

    removeFromMap() {
        this.start.removeFromMapCache()
        super.removeFromMapCache()
        this.end.removeFromMapCache()
    }

    addOnMap() {
        if (!this.layerObject instanceof L.Polyline)
            return

        this.layerObject.setStyle({
            color: 'blue'
        });

        this.start.addOnMap()
        super.addOnMap()
        this.end.addOnMap()
    }

    addOnMapCache() {
        if (!this.layerObject instanceof L.Polyline)
            return

        this.layerObject.setStyle({
            color: 'red'
        });

        this.start.addOnMapCache()
        super.addOnMapCache()
        this.end.addOnMapCache()
    }

    mapToSendModel() {
        return {
            start: this.start.mapToSendModel(),
            end: this.end.mapToSendModel(),
            routeData: this.routeData
        }
    }
}

class MapStructure {
    constructor(map) {
        this.map = map
        this.mapState = MapStatesEnum.Algorithm
        this.mapObjects = []
        this.mapTempObjects = []
        this.figuresOnMap = L.layerGroup([]).addTo(map)
        this.tempFiguresOnMap = L.layerGroup([]).addTo(map)
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
        this.adjustLayerZIndexes()
    }

    adjustLayerZIndexes() {
        this.map.on('layeradd',function(event){
           if (IndexZObjectKeepTypeMap.get("temp") == event.layer.ZIndex && event.layer instanceof L.Path)
            event.layer.bringToFront()
        });
    }

    setSelectedRoute(route) {
        if (this.mapState != MapStatesEnum.Algorithm && this.mapState != MapStatesEnum.RouteEdit)
            return

        if (!route instanceof Route)
            return

        if (this.selectedRoute === route && this.mapState == MapStatesEnum.RouteEdit) {
            this.selectedRoute = null
            this.mapState = MapStatesEnum.Algorithm
            return
        }

        this.mapState = MapStatesEnum.RouteEdit
        this.selectedRoute = route
    }


    addObjectToBothMapAndTemp(mapLayerObject) {
        this.tempFiguresOnMap.addLayer(mapLayerObject.layerObject)
        this.mapTempObjects.push(mapLayerObject)
    }

    popObjectFromTempAndMap() {
        var res = this.mapTempObjects.pop()
        this.tempFiguresOnMap.removeLayer(res.layerObject)
        return res
    }

    removeObjectFromBothMapAndTemp(mapLayerObject) {
        for(var i = 0; i < this.mapTempObjects.length; i++) {
            if(this.mapTempObjects[i] === mapLayerObject) {
                this.tempFiguresOnMap.removeLayer(this.mapTempObjects[i].layerObject)
                this.mapTempObjects.splice(i, 1);
                break;
            }
        }
    }

    clearObjectsFromBothMapAndTemp() {
        this.mapTempObjects.forEach(element => {
            this.tempFiguresOnMap.removeLayer(element.layerObject)
        })
        this.mapTempObjects = []
    }

    addObjectToBothMapAndCache(mapLayerObject) {
        this.figuresOnMap.addLayer(mapLayerObject.layerObject)
        this.mapObjects.push(mapLayerObject)
    }

    removeObjectFromBothMapAndCache(mapLayerObject) {
        for(var i = 0; i < this.mapObjects.length; i++) {
            if(this.mapObjects[i] === mapLayerObject) {
                this.figuresOnMap.removeLayer(this.mapObjects[i].layerObject)
                this.mapObjects.splice(i, 1);
                break;
            }
        }
    }

    clearObjectsFromBothMapAndCache() {
        this.mapObjects.forEach(element => {
            this.figuresOnMap.removeLayer(element.layerObject)
        })
        this.mapObjects = []
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