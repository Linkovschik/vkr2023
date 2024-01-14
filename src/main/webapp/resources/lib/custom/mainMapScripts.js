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
                var route = new Route(routeData, mapStructure)
                route.addOnMapCache()
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

function onPutRouteEnd() {
    mapStructure.mapState = MapStatesEnum.PutRouteEnd
}

function onBuildRoute() {
    mapStructure.mapState = MapStatesEnum.BuildRoute

    if (!buildRouteTemp.isBuildRouteTempCompleted()) {
        mapStructure.mapState = MapStatesEnum.Algorithm
        return
    }

    var polyStart = buildRouteTemp.startPoint.getLatLng()
    var polyEnd = buildRouteTemp.endPoint.getLatLng()

    var routeData = buildRoute(polyStart, polyEnd)
    var route = new Route(routeData, mapStructure)
    route.addOnMap()

    buildRouteTemp.clearBuildRouteTemp()

    mapStructure.mapState = MapStatesEnum.Algorithm
}

function onMapClick(e) {
    if (mapStructure.mapState == MapStatesEnum.PutRouteStart) {
        var point = new StartPoint(e.latlng.lat, e.latlng.lng, mapStructure)

        buildRouteTemp.setBuildRouteTempStartPoint(point)

        mapStructure.mapState = MapStatesEnum.Algorithm
    }

    if (mapStructure.mapState == MapStatesEnum.PutRouteEnd) {
        var point = new EndPoint(e.latlng.lat, e.latlng.lng, mapStructure)

        buildRouteTemp.setBuildRouteTempEndPoint(point)

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
            var route = new Route(routeData, mapStructure)
            route.addOnMap()
        });
    })
    .fail(function (jqxhr, textStatus, error) {
        var err = textStatus + ', ' + error;
    })
}