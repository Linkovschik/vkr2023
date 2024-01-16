var routeEditComp = {
     props: {
        selectedRoute: {
          type: Object,
          required: false,
          default: null
        }
     },
     data() {
     return {
            lang: {
              formatLocale: {
                firstDayOfWeek: 0,
              }
            },
        }
    },
    mounted() {

    },
    template:`      <div>
                        <label>Примерное время начала маршрута</label>
                        <div>
                            <label>C</label>
                            <date-picker v-model="selectedRoute.startTimeMin" lang="en" :firstDayOfWeek=1  type="datetime" :format="'DD-MM-YYYY HH:mm'" :confirm="true" :show-second="false"></date-picker>
                            <label>по</label>
                            <date-picker v-model="selectedRoute.startTimeMax" lang="en" :firstDayOfWeek=1  type="datetime" :format="'DD-MM-YYYY HH:mm'" :confirm="true" :show-second="false"></date-picker>
                        </div>
                        <br />
                        <label>Примерное время окончания маршрута</label>
                        <div>
                            <date-picker v-model="selectedRoute.endTimeMin" lang="en" :firstDayOfWeek=1  type="datetime" :format="'DD-MM-YYYY HH:mm'" :confirm="true" :show-second="false"></date-picker>
                            <date-picker v-model="selectedRoute.endTimeMax" lang="en" :firstDayOfWeek=1  type="datetime" :format="'DD-MM-YYYY HH:mm'" :confirm="true" :show-second="false"></date-picker>
                        </div>
                    </div>`,

    computed: {
        dateText () {
          console.log(new Date(8.64e15))
          console.log(this.startTime)
          console.log(this.selectedRoute)
          return this.startTime ? DatePicker.methods.stringify(startTime.date, 'DD-YYYY-MM') : '';

        }
    },
    watch: {
    },
    methods: {
    }
};