var zoneComp = {
    components: {
        'zone-edit': zoneEditComp
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
            mapStatesEnum: ZoneMapStatesEnum,
            mapStructure: null,
            mapState: ZoneMapStatesEnum.Default,
            tempZones: [],
            savedZones: [],
            selectedZoneSavedIcon: null
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
    },
    template: `      <div>
                       <div id="mapid" style="height: 640px"></div>
                       <br/>
                       <hr/>

                       <button type="button" id="setPutZoneStateButton" class="btn btn-primary" v-on:click="onPutZone()">Указать зону</button>

                       <br/>
                       <hr/>
                       <button type="button" id="saveZonesButton" class="btn btn-primary " v-on:click="saveZones()">Сохранить имеющиеся на карте ЧЁРНЫЕ зоны</button>
                       <button type="button" id="loadZonesButton" class="btn btn-primary " v-on:click="loadZones()">Загрузить на карту сохранённые ЧЁРНЫЕ зоны</button>
                       <br />
                       <button type="button" id="saveZonesButton" class="btn btn-primary " v-on:click="addTempZonesToSavedZones()">Добавить ГОЛУБЫЕ зоны к ЧЁРНЫМ</button>
                       <button type="button" id="clearTempZonesButton" class="btn btn-primary " v-on:click="clearTempZones()">Очистить имеющиеся на карте ГОЛУБЫЕ зоны</button>
                       <button type="button" id="clearSavedZonesButton" class="btn btn-primary " v-on:click="clearSaveZones()">Очистить имеющиеся на карте ЧЁРНЫЕ зоны</button>
                       <hr/>

                       <div v-if="mapStructure && mapStructure.selectedZone && mapState == mapStatesEnum.ZoneEdit">
                           <zone-edit :selectedZone=mapStructure.selectedZone
                                      :savedZones=savedZones
                                      :tempZones=tempZones
                           >
                           </zone-edit>
                       </div>
                   </div>`,

    computed: {
        allZones() {
            return this.tempZones.concat(this.savedZones)
        },
        selectedZone() {
            if (this.mapStructure)
                return this.mapStructure.selectedZone
        }
    },
    watch: {
        mapState: function(newVal, oldVal) {
            if (newVal) {
                if (oldVal == this.mapStatesEnum.ZoneEdit)
                    this.mapStructure.selectedZone = null
            }
        },
        allZones: function(newVal, oldVal) {
            newVal.forEach((zone) => {
            })
        },
        selectedZone: function(newVal, oldVal) {
            if (oldVal) {
                if (oldVal.layerObject.dragging) {
                    oldVal.layerObject.options.draggable = false
                    oldVal.layerObject.dragging.disable();
                }
                this.setZoneUnSelectedStyle(oldVal)
            }

            if (newVal) {
                newVal.layerObject.options.draggable = true
                newVal.layerObject.dragging.enable();
                this.setZoneSelectedStyle(newVal)
                this.mapState = this.mapStatesEnum.ZoneEdit
            }

            if (newVal == null &&  this.mapState == this.mapStatesEnum.ZoneEdit) {
                this.mapState = this.mapStatesEnum.Default
            }
        },
        tempZones: function(newVal, oldVal) {
            if (newVal) {
                for (const zone of newVal) {
                    if (this.savedZones.includes(zone))
                        this.setZonePreSavedStyle(zone)
                    else
                        this.setZoneTempStyle(zone)
                }
            }
        },
        savedZones: function(newVal, oldVal) {
            if (newVal) {
                for (const zone of newVal) {
                    if (this.tempZones.includes(zone))
                        this.setZonePreSavedStyle(zone)
                    else
                        this.setZoneSavedStyle(zone)
                }
            }
        }
    },
    methods: {
        resetSelectedZone() {
            this.mapStructure.selectedZone = null
        },
        addTempZonesToSavedZones() {
            this.savedZones = this.tempZones.concat(this.savedZones)
            this.tempZones = []
            this.resetSelectedZone()
        },
        clearTempZones() {
            for (const zone of this.tempZones) {
                this.mapStructure.removeObjectFromMap(zone)
            }

            this.tempZones = []
            this.resetSelectedZone()
        },
        clearSaveZones() {
            for (const zone of this.savedZones) {
                this.mapStructure.removeObjectFromMap(zone)
            }

            this.savedZones = []
            this.resetSelectedZone()
        },
        removeFromSavedZones(zone) {
            for(var i = 0; i < this.savedZones.length; i++) {
                if(this.savedZones[i] === zone) {
                    this.savedZones.splice(i, 1);
                    break;
                }
            }
            this.resetSelectedZone()
        },
        removeFromTempZones(zone) {
            for(var i = 0; i < this.tempZones.length; i++) {
                if(this.tempZones[i] === zone) {
                    this.tempZones.splice(i, 1);
                    break;
                }
            }
            this.resetSelectedZone()
        },
        setZoneTempStyle(zone) {
            zone.setIcon(this.mapStructure.blueIcon)
        },
        setZoneSavedStyle(zone) {
            zone.setIcon(this.mapStructure.blackIcon)
        },
        setZonePreSavedStyle(zone) {
            zone.setIcon(this.mapStructure.greenIcon)
        },
        setZoneSelectedStyle(zone) {
            this.selectedZoneSavedIcon = zone.getIcon()
            zone.setIcon(this.mapStructure.redIcon)
        },
        setZoneUnSelectedStyle(zone) {
            zone.setIcon(this.selectedZoneSavedIcon)
            this.selectedZoneSavedIcon = null
        },
        onPutZone() {
            this.mapState = this.mapStatesEnum.PutZone
        },
        saveZones() {
            var savedZonesData = this.savedZones.filter(obj => obj instanceof Zone).map(obj => obj.zoneData)
            var zoneDataToSave = {
                savedZones: savedZonesData
            }

            console.log(zoneDataToSave)
            var data = JSON.stringify(zoneDataToSave);
            $.ajax({
                    url: '/home/updateZones',
                    method: 'put',
                    dataType: 'json',
                    data: data,
                    contentType: 'application/json; charset=utf-8',
                    async: false,
                })
                .fail(function(jqxhr, textStatus, error) {
                    var err = textStatus + ', ' + error;
                    console.log(err)
                })

            this.loadZones()
        },
        loadZones() {

            var result = []
            $.getJSON({
                    url: '/home/loadZones',
                    async: false
            })
            .done(function(zoneDataList) {
                zoneDataList.forEach((zoneData) => {
                    result.push(zoneData)
                });
            })
            .fail(function(jqxhr, textStatus, error) {
                var err = textStatus + ', ' + error;
            })

           this.clearSaveZones()
           var zoneDataList = result;
           for (const zoneData of zoneDataList) {
                var zone = new Zone(zoneData, this.mapStructure)
                this.mapStructure.addObjectOnMap(zone)

               this.savedZones.push(zone);
           }
        },
        onMapClick(e) {
            if (this.mapState == this.mapStatesEnum.PutZone) {
                zoneData = {
                    id: null,
                    lat: e.latlng.lat,
                    lng: e.latlng.lng,
                    congestion: 0.5
                };

                var zone = new Zone(zoneData, this.mapStructure, true, this.mapStructure.blackIcon)
                this.mapStructure.addObjectOnMap(zone)

                this.tempZones.push(zone);
                this.mapState = this.mapStatesEnum.Default
            }
            else {
            }
        }
    }
};