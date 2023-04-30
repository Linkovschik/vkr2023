map = L.map('mapid', { editable: true }).setView([54.7370888, 55.9555806], 15);
//привязка событий карты
L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
}).addTo(map);

//L.geoJSON(states).addTo(map);

map.on('click', onMapClick);

var mapStructure = new MapStructure(map);

function updateRoutesToDatabase() {

    var routeDataToSave = {routes: mapStructure.mapObjects.filter(obj => obj instanceof Route).map(obj => obj.routeData) }

    var data = JSON.stringify(routeDataToSave);
    $.ajax({
        url: '/home/updateRoutes',         
        method: 'post',            
        dataType: 'json',          
        data: data,
        contentType: 'application/json; charset=utf-8',
        async: false,
    })
    .fail(function (jqxhr, textStatus, error) {
        var err = textStatus + ', ' + error;
    })
}

function loadRouteFromDatabase() {
    mapStructure.clearObjectsFromBothMapAndCache()

    $.getJSON({
        url: '/home/loadRoutes',
        async: false
    })
        .done(function (routeDataList) {
            routeDataList.forEach((routeData) => {
                var start = routeData.start
                var startMarker =  L.marker(L.latLng(start.lat, start.lng), { icon: mapStructure.blueIcon })
                var startPoint = new Point(start.lat, start.lng, startMarker)

                var end = routeData.end
                var endMarker =  L.marker(L.latLng(end.lat, end.lng), { icon: mapStructure.blackIcon })
                var endPoint = new Point(end.lat, end.lng, endMarker)

                var polyline = L.polyline(routeData.coordinates.map(mpoint => [mpoint.lat, mpoint.lng]), {color: 'red'});

                var route = new Route(routeData, startPoint, endPoint, polyline)
        
                mapStructure.addObjectToBothMapAndCache(route)
            });
        })
        .fail(function (jqxhr, textStatus, error) {
            var err = textStatus + ', ' + error;
        })
}

function buildRoute(routeStart, routeEnd) {
    var route = null

    var routeBuildData = {
        start: {lng: routeStart.lng, lat: routeStart.lat},
        end: {lng: routeEnd.lng, lat: routeEnd.lat},
    }

    var data = JSON.stringify(routeBuildData);
    $.ajax({
        url: '/home/buildRoute',         
        method: 'post',            
        dataType: 'json',          
        data: data,
        contentType: 'application/json; charset=utf-8',
        async: false,
    })
    .done(function (routeData) {
        route = routeData
    })
    .fail(function (jqxhr, textStatus, error) {
        var err = textStatus + ', ' + error;
    })


    return route
}

function onPutRouteStart() {
    mapStructure.mapState = MapStatesEnum.PutRouteStart
}

function OnPutRouteEnd() {
    mapStructure.mapState = MapStatesEnum.PutRouteEnd
}

function onMapClick(e) {
    if (mapStructure.mapState == MapStatesEnum.PutRouteStart) {
        var pointMarker = L.marker(e.latlng, { icon: mapStructure.blueIcon })
        var point = new Point(e.latlng.lat, e.latlng.lng, pointMarker)

        mapStructure.addObjectToBothMapAndTemp(point)
    }

    if (mapStructure.mapState == MapStatesEnum.PutRouteEnd) {
        var pointMarker = L.marker(e.latlng, { icon: mapStructure.blackIcon })
        var point = new Point(e.latlng.lat, e.latlng.lng, pointMarker)

        mapStructure.addObjectToBothMapAndTemp(point)

        var endPoint = mapStructure.popObjectFromTempAndMap()
        var startPoint = mapStructure.popObjectFromTempAndMap()

        mapStructure.clearObjectsFromBothMapAndTemp()

        var polyStart = L.latLng(startPoint.lat, startPoint.lng)
        var polyEnd = L.latLng(endPoint.lat, endPoint.lng)

        var routeData = buildRoute(polyStart, polyEnd)
        var polyline = L.polyline(routeData.coordinates.map(mpoint => [mpoint.lat, mpoint.lng]), {color: 'red'});

        var route = new Route(routeData, startPoint, endPoint, polyline)

        mapStructure.addObjectToBothMapAndCache(route)

        mapStructure.mapState = MapStatesEnum.Algorithm
    }
}

function startAlgorithm() {
    console.log("Маршруты перестраиваются")
    var routeDataToSave = {routes: mapStructure.mapObjects.filter(obj => obj instanceof Route).map(obj => obj.routeData) }

    var data = JSON.stringify(routeDataToSave);
    $.ajax({
        url: '/home/startAlgorithm',         
        method: 'post',            
        dataType: 'json',          
        data: data,
        contentType: 'application/json; charset=utf-8',
        async: false,
    })
    .done(function (routeDataList) {
        mapStructure.clearObjectsFromBothMapAndCache()
        routeDataList.forEach((routeData) => {
            var start = routeData.start
            var startMarker =  L.marker(L.latLng(start.lat, start.lng), { icon: mapStructure.blueIcon })
            var startPoint = new Point(start.lat, start.lng, startMarker)

            var end = routeData.end
            var endMarker =  L.marker(L.latLng(end.lat, end.lng), { icon: mapStructure.blackIcon })
            var endPoint = new Point(end.lat, end.lng, endMarker)

            var polyline = L.polyline(routeData.coordinates.map(mpoint => [mpoint.lat, mpoint.lng]), {color: 'red'});

            var route = new Route(routeData, startPoint, endPoint, polyline)
    
            mapStructure.addObjectToBothMapAndCache(route)
        });
    })
    .fail(function (jqxhr, textStatus, error) {
        var err = textStatus + ', ' + error;
    })
}