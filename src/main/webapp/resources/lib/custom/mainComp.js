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
            mapStatesEnum: MapStatesEnum,
            mapState: MapStatesEnum.Algorithm,
            mapStructure: null,
            buildRouteTemp: null,
            tempRoutes: [],
            savedRoutes: [],
            tempPoints: [],
            unselectedRouteColor: "black",
            selectedRouteColor: "red",
            selectedRouteSavedColor: "red"
        }
    },
    mounted() {
        this.map = L.map('mapid', {
            editable: true
        }).setView([54.7370888, 55.9555806], 15);
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
    template: `      <div>
                       <div id="mapid" style="height: 640px"></div>
                       <br/>
                       <hr/>

                       <button type="button" id="startButton" class="btn btn-primary" v-on:click="onPutRouteStart()">Указать начало</button>
                       <button type="button" id="endButton" class="btn btn-primary d-none" v-on:click="onPutRouteEnd()">Указать конец</button>
                       <button type="button" id="buildRoute" class="btn btn-primary " v-on:click="onBuildRoute()" >Построить маршрут</button>
                       <button type="button" id="updateTempRoutesButton" class="btn btn-primary " v-on:click="addTempRoutesToSavedRoutes()">Добавить созданные маршруты к зафиксированным</button>
                       <button type="button" id="clearTemp" class="btn btn-primary " v-on:click="clearTempRoutes()">Убрать несохранённые объекты</button>
                       <button type="button" id="clearCache" class="btn btn-primary " v-on:click="clearSavedRoutes()">Убрать загруженные объекты</button>

                       <br/>
                       <hr/>

                       <button type="button" id="updateRoutesButton" class="btn btn-primary " v-on:click="saveRoutes()">Обновить имеющиеся на карте маршруты</button>
                       <button type="button" id="loadRoutesButton" class="btn btn-primary " v-on:click="loadRoutes()">Загрузить маршруты из базы данных</button>
                       <button type="button" id="algorithmButton" class="btn btn-primary " v-on:click="startAlgorithm()">Перестроить маршруты по алгоритму</button>

                       <br />
                       <hr/>

                       <div v-if="mapStructure && mapStructure.selectedRoute && mapState == mapStatesEnum.RouteEdit">
                           <route-edit :selectedRoute=mapStructure.selectedRoute
                                       :savedRoutes=savedRoutes
                                       :tempRoutes=tempRoutes
                           >
                           </route-edit>
                       </div>
                   </div>`,

    computed: {
        selectedRoute() {
            if (!this.mapStructure)
                return null
            return this.mapStructure.selectedRoute;
        },
        allRoutes() {
             return this.tempRoutes.concat(this.savedRoutes)
        }
    },
    watch: {
        mapState: function(newVal, oldVal) {
            if (newVal) {
                if (oldVal == this.mapStatesEnum.RouteEdit)
                    this.mapStructure.selectedRoute = null

                console.log('Map state changed to: ' + newVal)
            }
        },
        selectedRoute: function(newVal, oldVal) {
            if (oldVal) {
                this.setRouteUnSelectedStyle(oldVal)
            }

            if (newVal) {
                this.setRouteSelectedStyle(newVal)
                this.mapState = this.mapStatesEnum.RouteEdit
            }

            if (newVal == null &&  this.mapState == this.mapStatesEnum.RouteEdit) {
                this.mapState = this.mapStatesEnum.Algorithm
            }
        },
        tempRoutes: function(newVal, oldVal) {
            if (newVal) {
                for (const route of newVal) {
                    if (this.savedRoutes.includes(route))
                        this.setRoutePreSavedStyle(route)
                    else
                        this.setRouteTempStyle(route)
                }
            }
        },
        savedRoutes: function(newVal, oldVal) {
            if (newVal) {
                for (const route of newVal) {
                    if (this.tempRoutes.includes(route))
                        this.setRoutePreSavedStyle(route)
                    else
                        this.setRouteSavedStyle(route)
                }
            }
        }
    },
    methods: {
        resetSelectedRoute() {
            this.mapStructure.selectedRoute = null
        },
        setRouteTempStyle(route) {
            route.layerObject.setStyle({
                color: "blue"
            });
        },
        setRouteSavedStyle(route) {
            route.layerObject.setStyle({
                color: "black"
            });
        },
        setRoutePreSavedStyle(route) {
            route.layerObject.setStyle({
                color: "green"
            });
        },
        setRouteSelectedStyle(route) {
            this.selectedRouteSavedColor = route.layerObject.options.color
            route.layerObject.setStyle({
                color: "red"
            });
        },
        setRouteUnSelectedStyle(route) {
            if (this.selectedRouteSavedColor) {
                route.layerObject.setStyle({
                    color: this.selectedRouteSavedColor
                });
            }
            else {
                route.layerObject.setStyle({
                    color: "black"
                });
            }
        },
        addTempRoutesToSavedRoutes() {
            this.savedRoutes = this.tempRoutes.concat(this.savedRoutes)
            this.resetSelectedRoute()
        },
        clearTempRoutes() {
            for (const route of this.tempRoutes) {
                route.removeFromMap()
            }
            this.tempRoutes = []
            this.resetSelectedRoute()
        },
        clearSavedRoutes() {
            for (const route of this.savedRoutes) {
                route.removeFromMap()
            }
            this.savedRoutes = []
            this.resetSelectedRoute()
        },
        removeFromSavedRoutes(route) {
            for(var i = 0; i < this.savedRoutes.length; i++) {
                if(this.savedRoutes[i] === route) {
                    this.savedRoutes.splice(i, 1);
                    break;
                }
            }
            this.resetSelectedRoute()
        },
        removeFromTempRoutes(route) {
            for(var i = 0; i < this.tempRoutes.length; i++) {
                if(this.tempRoutes[i] === route) {
                    this.tempRoutes.splice(i, 1);
                    break;
                }
            }
            this.resetSelectedRoute()
        },
        saveRoutes() {
            var loadRoutes = this.savedRoutes.filter(obj => obj instanceof Route).map(obj => obj.routeData)
            var routeDataToSave = {
                routes: loadRoutes
            }

            var data = JSON.stringify(routeDataToSave);
            $.ajax({
                    url: '/home/updateRoutes',
                    method: 'put',
                    dataType: 'json',
                    data: data,
                    contentType: 'application/json; charset=utf-8',
                    async: false,
                })
                .fail(function(jqxhr, textStatus, error) {
                    var err = textStatus + ', ' + error;
                })
        },
        loadRoutes() {
            var loadedRouteData = []
            $.getJSON({
                    url: '/home/loadRoutes',
                    async: false
                })
                .done(function(routeDataList) {
                    routeDataList.forEach((routeData) => {
                        loadedRouteData.push(routeData)
                    });
                })
                .fail(function(jqxhr, textStatus, error) {
                    var err = textStatus + ', ' + error;
                })

            this.clearSavedRoutes()
            for (const routeData of loadedRouteData) {
                var route = new Route(routeData, this.mapStructure)
                route.addOnMap()
                this.savedRoutes.push(route);
            }

            this.resetSelectedRoute()
        },
        buildRoute(routeStart, routeEnd) {
            var route = null

            var routeBuildData = {
                start: {
                    lng: routeStart.lng,
                    lat: routeStart.lat
                },
                end: {
                    lng: routeEnd.lng,
                    lat: routeEnd.lat
                }
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
                .done(function(routeData) {
                    if (!routeData.length)
                        throw new Error("Built route is undefiend or null")
                    route = routeData[0]
                })
                .fail(function(jqxhr, textStatus, error) {
                    var err = textStatus + ', ' + error;
                    console.log(err)
                })

            return route
        },
        onPutRouteStart() {
            this.mapState = this.mapStatesEnum.PutRouteStart
        },
        onPutRouteEnd() {
            this.mapState = this.mapStatesEnum.PutRouteEnd
        },
        onBuildRoute() {
            this.mapState = this.mapStatesEnum.BuildRoute

            if (!this.buildRouteTemp.isBuildRouteTempCompleted()) {
                this.mapState = this.mapStatesEnum.Algorithm
                return
            }

            var polyStart = this.buildRouteTemp.startPoint.getLatLng()
            var polyEnd = this.buildRouteTemp.endPoint.getLatLng()

            var routeData = this.buildRoute(polyStart, polyEnd)
            var route = new Route(routeData, this.mapStructure)
            route.addOnMap()
            this.tempRoutes.push(route)

            this.buildRouteTemp.clearBuildRouteTemp()

            this.mapState = this.mapStatesEnum.Algorithm
        },
        onMapClick(e) {
            if (this.mapState == this.mapStatesEnum.PutRouteStart) {
                var point = new StartPoint(e.latlng.lat, e.latlng.lng, this.mapStructure)

                this.buildRouteTemp.setBuildRouteTempStartPoint(point)

                this.mapState = this.mapStatesEnum.Algorithm
            }

            if (this.mapState == this.mapStatesEnum.PutRouteEnd) {
                var point = new EndPoint(e.latlng.lat, e.latlng.lng, this.mapStructure)

                this.buildRouteTemp.setBuildRouteTempEndPoint(point)

                this.mapState = this.mapStatesEnum.Algorithm
            }
        },
        startAlgorithm() {
            console.log("Маршруты перестраиваются")
            var routeDataToSave = {
                routes: this.mapStructure.mapObjects.filter(obj => obj instanceof Route).map(obj => obj.routeData)
            }

            var data = JSON.stringify(routeDataToSave);
            var routeDataList = []
            $.ajax({
                    url: '/home/startAlgorithm',
                    method: 'post',
                    dataType: 'json',
                    data: data,
                    contentType: 'application/json; charset=utf-8',
                    async: false,
                })
                .done(function(routeDataList) {
                    mapStructure.clearObjectsFromBothMapAndCache()
                    routeDataList.forEach((routeData) => {
                        routeDataList.push(routeData)
                    });
                })
                .fail(function(jqxhr, textStatus, error) {
                    var err = textStatus + ', ' + error;
                })

            for (const routeData of routeDataList) {
                var route = new Route(routeData, mapStructure)
                route.addOnMap()

                this.tempRoutes.push(zone);
            }
        }
    }
};