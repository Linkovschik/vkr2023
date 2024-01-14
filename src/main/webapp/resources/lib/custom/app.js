var mainComponent = new Vue({
    el: '#app',
    name: 'App',
    components: {
    },
    computed: {
      mapState() {
        if (!mapStructure)
            return null
        return mapStructure.mapState
      },
      mapStateKey() {
        if (!mapStructure)
            return null
        return getMapStateEnumKeyByValue(mapStructure.mapState)
      }
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
})