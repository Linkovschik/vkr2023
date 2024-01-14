var comp = {
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

        this.map.on('click', onMapClick);

        this.mapStructure = new MapStructure(this.map);
        this.buildRouteTemp = new BuildRouteTemp(this.mapStructure);
    },
    template:`      <div>
                        <div id="mapid" style="height: 640px"></div>
                        <br/>
                        <hr/>

                        <button type="button" id="startButton" class="btn btn-primary" onclick="onPutRouteStart()">Указать начало</button>
                        <button type="button" id="endButton" class="btn btn-primary d-none" onclick="onPutRouteEnd()">Указать конец</button>
                        <button type="button" id="buildRoute" class="btn btn-primary " onclick="onBuildRoute()" >Построить маршрут</button>

                        <br/>
                        <hr/>

                        <button type="button" id="updateRoutesButton" class="btn btn-primary " onclick="updateRoutesToDatabase()">Обновить имеющиеся на карте маршруты</button>
                        <button type="button" id="loadRoutesButton" class="btn btn-primary " onclick="loadRouteFromDatabase()">Загрузить маршруты из базы данных</button>
                        <button type="button" id="algorithmButton" class="btn btn-primary " onclick="startAlgorithm()">Перестроить маршруты по алгоритму</button>
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
      }
    }
};