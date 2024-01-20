var routeEditComp = {
     props: {
        selectedRoute: {
          type: Object,
          required: false,
          default: { routeData: {}}
        }
     },
     data() {
     return {
            lang: {
              formatLocale: {
                firstDayOfWeek: 0,
              }
            },
            morningStartHour: 6,
            morningEndHour: 10,
            eveningStartHour: 16,
            eveningEndHour: 20,
            maxDurationInHours: 4
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
                            <input type="time" v-model="selectedRoute.routeData.startTimeMin" min="06:00:00" max="10:00:00" step="2"/>
                            <label>по</label>
                            <input type="time" v-model="selectedRoute.routeData.startTimeMax" min="06:00:00" max="10:00:00" step="2"/>
                        </div>
                        <br />
                        <label>Примерное время окончания маршрута</label>
                        <div>
                            <label>C</label>
                            <input type="time" v-model="selectedRoute.routeData.endTimeMin" min="06:00:00" max="10:00:00" step="2"/>
                            <label>по</label>
                            <input type="time" v-model="selectedRoute.routeData.endTimeMax" min="06:00:00" max="10:00:00" step="2"/>
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
        },
        startTimeMin() {
            if (!this.selectedRoute.routeData)
                throw new Error("Selected route has no route data!")

            var dateTime = new Date(moment().format('YYYY-MM-DD') + ' ' + this.selectedRoute.routeData.startTimeMin)
            return dateTime
        },
        startTimeMax() {
            if (!this.selectedRoute.routeData)
                throw new Error("Selected route has no route data!")
            var dateTime = new Date(moment().format('YYYY-MM-DD') + ' ' + this.selectedRoute.routeData.startTimeMax)
            return dateTime
        },
        endTimeMin() {
            if (!this.selectedRoute.routeData)
                throw new Error("Selected route has no route data!")
            var dateTime = new Date(moment().format('YYYY-MM-DD') + ' ' + this.selectedRoute.routeData.endTimeMin)
            return dateTime
        },
        endTimeMax() {
            if (!this.selectedRoute.routeData)
                throw new Error("Selected route has no route data!")
            var dateTime = new Date(moment().format('YYYY-MM-DD') + ' ' + this.selectedRoute.routeData.endTimeMax)
            return dateTime
        }
    },
    watch: {
        selectedRouteName() {
            console.log(this.selectedRoute.routeData.name)
        },
        startTimeMin() {
            if (!this.startTimeMin)
                return

            if (this.startTimeMin.getHours() < this.morningStartHour) {
                console.log("Установим минимальное возможный утренний час для времени начала маршрута")
                var min = this.startTimeMin
                min.setHours(this.morningStartHour)
                this.selectedRoute.routeData.startTimeMin = this.getFormattedTime(min)
            }

            this.selectedRoute.routeData.startTimeMax = null
            this.selectedRoute.routeData.endTimeMin = null
            this.selectedRoute.routeData.endTimeMax = null
        },
        startTimeMax() {
            if (!this.startTimeMax)
                return

            if (!this.startTimeMin) {
                this.selectedRoute.routeData.startTimeMax = null
                return
            }

            if (this.startTimeMin && this.startTimeMax < this.startTimeMin) {
                this.selectedRoute.routeData.startTimeMax = this.getFormattedTime(moment(this.startTimeMin).toDate())
            }

            if (this.endTimeMax && this.startTimeMax > this.endTimeMax) {
                this.selectedRoute.routeData.startTimeMax = this.getFormattedTime(moment(this.endTimeMax).toDate())
            }

            if (this.maxDurationInHours < this.getDurationInHours(this.startTimeMin, this.startTimeMax)) {
                this.selectedRoute.routeData.startTimeMax = this.getFormattedTime(this.addHours(this.startTimeMin, this.maxDurationInHours))
            }
        },
        endTimeMin() {
            if (!this.endTimeMin)
                return

            if (!this.endTimeMax) {
                this.selectedRoute.routeData.endTimeMin = null
                return
            }

            if (!this.startTimeMin) {
                this.selectedRoute.routeData.endTimeMin = null
                return
            }

            if (this.startTimeMin && this.endTimeMin < this.startTimeMin) {
                this.selectedRoute.routeData.endTimeMin = this.getFormattedTime(moment(this.startTimeMin).toDate())
            }

            if (this.endTimeMax && this.endTimeMin > this.endTimeMax) {
                this.selectedRoute.routeData.endTimeMin = this.getFormattedTime(moment(this.endTimeMax).toDate())
            }
        },
        endTimeMax() {
            if (!this.endTimeMax)
                return

            if (this.endTimeMax.getHours() > this.eveningEndHour) {
                console.log("Установим максимально возможный вечерний час для времени окончания маршрута")
                var max = this.endTimeMax
                max.setHours(this.eveningEndHour)
                this.selectedRoute.routeData.endTimeMax = this.getFormattedTime(max)
            }

            if (this.startTimeMin && this.startTimeMin >= this.endTimeMax) {
                console.log("Установим окончание маршрута с разницей в 30 минут")
                this.selectedRoute.routeData.endTimeMax = this.getFormattedTime(moment(this.startTimeMin).add(30, 'm').toDate())
            }

            if (this.startTimeMin && this.maxDurationInHours < this.getDurationInHours(this.startTimeMin, this.endTimeMax)) {
                this.selectedRoute.routeData.endTimeMax = this.getFormattedTime(this.addHours(this.startTimeMin, this.maxDurationInHours))
            }

            this.selectedRoute.routeData.endTimeMin = null
        }
    },
    methods: {
        createDateAsUTC(date) {
            return new Date(Date.UTC(date.getFullYear(), date.getMonth(), date.getDate(), date.getHours(), date.getMinutes(), date.getSeconds()));
        },
        convertDateToUTC(date) {
            return new Date(date.getUTCFullYear(), date.getUTCMonth(), date.getUTCDate(), date.getUTCHours(), date.getUTCMinutes(), date.getUTCSeconds());
        },
        getDurationInHours(start, end) {
            var duration = moment.duration(moment(end).diff(moment(start)))
            return duration.asHours()
        },
        addHours(date, hours) {
            var temp =  moment(date).add(hours, 'hours').toDate()
            return temp
        },
        subtractHours(date, hours) {
            return moment(date).subtract(hours, 'hours').toDate()
        },
        getFormattedTime(date) {
            return moment(date).format('HH:mm:ss')
        }
    }
};