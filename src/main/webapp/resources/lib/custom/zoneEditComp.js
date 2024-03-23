var zoneEditComp = {
     props: {
        selectedZone: {
          type: Object,
          required: false,
          default: { zoneData: {}}
        },
        tempZones: {
            type: Array,
            required: false,
            default: () => []
        },
        savedZones: {
            type: Array,
            required: false,
            default: () => []
        }
     },
     data() {
         return {
                congestion: this.selectedZone.zoneData.congestion
            }
    },
    mounted() {
        if (!this.selectedZone.zoneData)
            throw new Error("Selected zone has no zone data!")
    },
    template:`      <div>
                        <label>Id зоны</label>
                        <div>
                            <input v-model="selectedZone.zoneData.id">
                        </div>
                        <label>Загруженность зоны</label>
                        <div>
                            <input v-model="selectedZone.zoneData.congestion">
                        </div>
                       <button type="button" id="deleteZoneButton" class="btn btn-primary " v-on:click="deleteSelectedZone()">Удалить зону</button>
                    </div>`,

    computed: {

    },
    watch: {

    },
    methods: {
        deleteSelectedZone() {
            this.selectedZone.mapStructure.selectedZone = null
            this.selectedZone.mapStructure.removeObjectFromMap(this.selectedZone)
            this.removeFromSavedZones(this.selectedZone)
            this.removeFromTempZones(this.selectedZone)
        },
        removeFromSavedZones(zone) {
            for(var i = 0; i < this.savedZones.length; i++) {
                if(this.savedZones[i] === zone) {
                    this.savedZones.splice(i, 1);
                    break;
                }
            }
        },
        removeFromTempZones(zone) {
            for(var i = 0; i < this.tempZones.length; i++) {
                if(this.tempZones[i] === zone) {
                    this.tempZones.splice(i, 1);
                    break;
                }
            }
        }
    }
};