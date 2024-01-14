<template>
  <div :id="mainComponent">
    <template>
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
    </template>
  </div>
</template>

<script lang="text/babel">
export default {
  name: 'MainComponent',
  components: {
  },
  data() {
    map: null,
    mapStructure: null,
    buildRouteTemp: null
  },
  mounted() {
    this.map = L.map('mapid', { editable: true }).setView([54.7370888, 55.9555806], 15);
    //привязка событий карты
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
    }).addTo(map);

    //L.geoJSON(states).addTo(map);

    this.map.on('click', onMapClick);

    this.mapStructure = new MapStructure(map);
    this.buildRouteTemp = new BuildRouteTemp(mapStructure);
  }
  computed: {
    mapState() {
      return mapStructure.mapState
    },
    mapStateKey() {
      return getMapStateEnumKeyByValue(mapStructure.mapState)
    }
  },
  async mounted() {

  },
  watch: {
    mapState(val) {
      console.log('Map state changed to: ' + mapStateKey)
    }
  },
  methods: {
    getMapStateEnumKeyByValue(val) {
      return Object.keys(MapStatesEnum).find(
        key => MapStatesEnum[key] === val
      )
    }
  }
}
</script>
