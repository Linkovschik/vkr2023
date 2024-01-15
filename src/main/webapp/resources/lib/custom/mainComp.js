var comp = {
     components: {
        'route-edit': routeEditComp
     },
     props: {
        config: {
          type: Object,
          required: false,
          default: () => ({})
        }
     },
     data() {
     return {
            map: null,
            mapStructure: null,
            buildRouteTemp: null
        }
    },
    mounted() {
        this.map = L.map('mapid', { editable: true }).setView([54.7370888, 55.9555806], 15);
        //привязка событий карты
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
        }).addTo(this.map);

        //L.geoJSON(states).addTo(map);

        console.log(this.config)


        this.map.on('click', this.onMapClick);

        this.mapStructure = new MapStructure(this.map);
        this.buildRouteTemp = new BuildRouteTemp(this.mapStructure);
    },
    template:`      <div>
                        <div id="mapid" style="height: 640px"></div>
                        <br/>
                        <hr/>

                        <button type="button" id="startButton" class="btn btn-primary" v-on:click="onPutRouteStart()">Указать начало</button>
                        <button type="button" id="endButton" class="btn btn-primary d-none" v-on:click="onPutRouteEnd()">Указать конец</button>
                        <button type="button" id="buildRoute" class="btn btn-primary " v-on:click="onBuildRoute()" >Построить маршрут</button>

                        <br/>
                        <hr/>

                        <button type="button" id="updateRoutesButton" class="btn btn-primary " v-on:click="updateRoutesToDatabase()">Обновить имеющиеся на карте маршруты</button>
                        <button type="button" id="loadRoutesButton" class="btn btn-primary " v-on:click="loadRouteFromDatabase()">Загрузить маршруты из базы данных</button>
                        <button type="button" id="algorithmButton" class="btn btn-primary " v-on:click="startAlgorithm()">Перестроить маршруты по алгоритму</button>

                        <route-edit>
                        </route-edit>
                    </div>`,

    computed: {
      mapState() {
        if (!this.mapStructure)
            return null
        return this.mapStructure.mapState
      },
      mapStateKey() {
        if (!this.mapStructure)
            return null
        return this.getMapStateEnumKeyByValue(this.mapStructure.mapState)
      }
    },
    watch: {
      mapState(val) {
        if(!this.mapStateKey)
            return
        console.log('Map state changed to: ' + this.mapStateKey)
      }
    },
    methods: {
        getMapStateEnumKeyByValue(val) {
            return Object.keys(MapStatesEnum).find(
              key => MapStatesEnum[key] === val
            )
        },
        updateRoutesToDatabase() {

            var routeDataToSave = {routes: this.mapStructure.mapObjects.filter(obj => obj instanceof Route).map(obj => obj.routeData) }

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
        },
        loadRouteFromDatabase() {
            var mapStructure = this.mapStructure
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
        },
        buildRoute(routeStart, routeEnd) {
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
                console.log(err)
            })


            return route
        },
        onPutRouteStart() {
            this.mapStructure.mapState = MapStatesEnum.PutRouteStart
        },
        onPutRouteEnd() {
            this.mapStructure.mapState = MapStatesEnum.PutRouteEnd
        },
        onBuildRoute() {
            this.mapStructure.mapState = MapStatesEnum.BuildRoute

            if (!this.buildRouteTemp.isBuildRouteTempCompleted()) {
                this.mapStructure.mapState = MapStatesEnum.Algorithm
                return
            }

            var polyStart = this.buildRouteTemp.startPoint.getLatLng()
            var polyEnd = this.buildRouteTemp.endPoint.getLatLng()

            var routeData = this.buildRoute(polyStart, polyEnd)
            var route = new Route(routeData, this.mapStructure)
            route.addOnMap()

            this.buildRouteTemp.clearBuildRouteTemp()

            this.mapStructure.mapState = MapStatesEnum.Algorithm
        },
        onMapClick(e) {
            if (this.mapStructure.mapState == MapStatesEnum.PutRouteStart) {
                var point = new StartPoint(e.latlng.lat, e.latlng.lng, this.mapStructure)

                this.buildRouteTemp.setBuildRouteTempStartPoint(point)

                this.mapStructure.mapState = MapStatesEnum.Algorithm
            }

            if (this.mapStructure.mapState == MapStatesEnum.PutRouteEnd) {
                var point = new EndPoint(e.latlng.lat, e.latlng.lng, this.mapStructure)

                this.buildRouteTemp.setBuildRouteTempEndPoint(point)

                this.mapStructure.mapState = MapStatesEnum.Algorithm
            }
        },
        startAlgorithm() {
            console.log("Маршруты перестраиваются")
            var routeDataToSave = {routes: this.mapStructure.mapObjects.filter(obj => obj instanceof Route).map(obj => obj.routeData) }
            var mapStructure = this.mapStructure
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
    }
};