var routeEditComp = {
     props: {
        selectedRoute: {
          type: Object,
          required: false,
          default: {}
        }
     },
     data() {
     return {
            lang: {
              formatLocale: {
                firstDayOfWeek: 0,
              }
            }
        }
    },
    mounted() {

    },
    template:`      <div>
                        <label>Название маршрута</label>
                        <div>
                            <input v-model="selectedRoute.routeData.name">
                        </div>
                        <label>Примерное время начала маршрута</label>
                        <div>
                            <label>C</label>
                            <date-picker v-model="selectedRoute.routeData.startTimeMin" lang="en" :firstDayOfWeek=1  type="datetime" :format="'DD-MM-YYYY HH:mm'" :confirm="true" :show-second="false"></date-picker>
                            <label>по</label>
                            <date-picker v-model="selectedRoute.routeData.startTimeMax" lang="en" :firstDayOfWeek=1  type="datetime" :format="'DD-MM-YYYY HH:mm'" :confirm="true" :show-second="false"></date-picker>
                        </div>
                        <br />
                        <label>Примерное время окончания маршрута</label>
                        <div>
                            <label>C</label>
                            <date-picker v-model="selectedRoute.routeData.endTimeMin" lang="en" :firstDayOfWeek=1  type="datetime" :format="'DD-MM-YYYY HH:mm'" :confirm="true" :show-second="false"></date-picker>
                            <label>по</label>
                            <date-picker v-model="selectedRoute.routeData.endTimeMax" lang="en" :firstDayOfWeek=1  type="datetime" :format="'DD-MM-YYYY HH:mm'" :confirm="true" :show-second="false"></date-picker>
                        </div>
                    </div>`,

    computed: {
        dateText () {
          console.log(new Date(8.64e15))
          console.log(this.startTime)
          console.log(this.selectedRoute)
          return this.startTime ? DatePicker.methods.stringify(startTime.date, 'DD-YYYY-MM') : '';

        },
        selectedRouteName() {
            return this.selectedRoute.routeData.name
        }
    },
    watch: {
        selectedRouteName() {
            console.log(this.selectedRoute.routeData.name)
        }
    },
    methods: {
    }
};