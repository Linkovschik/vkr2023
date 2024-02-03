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
        if (!this.selectedRoute.routeData)
            throw new Error("Selected route has no route data!")

        if (!this.selectedRoute.routeData.startTimeMin)
            this.selectedRoute.routeData.startTimeMin = this.getFormattedTime(this.startDateTime)

        if (!this.selectedRoute.routeData.startTimeMax)
            this.selectedRoute.routeData.startTimeMax = this.getFormattedTime(this.startTimeMin)

        if (!this.selectedRoute.routeData.endTimeMin)
            this.selectedRoute.routeData.endTimeMin = this.getFormattedTime(this.endDateTime)

        if (!this.selectedRoute.routeData.endTimeMax)
            this.selectedRoute.routeData.endTimeMax = this.getFormattedTime(this.endDateTime)
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
        },
        morningStartDate() {
            if (!this.morningStartHour)
                throw new Error("Error in initialization!")
            var dateTime = new Date(moment().format('YYYY-MM-DD') + ' 00:00:00')
            dateTime.setHours(this.morningStartHour)
            return dateTime
        },
        morningEndDate() {
            if (!this.morningEndHour)
                throw new Error("Error in initialization!")
            var dateTime = new Date(moment().format('YYYY-MM-DD') + ' 00:00:00')
            dateTime.setHours(this.morningEndHour)
            return dateTime
        },
        eveningStartDate() {
            if (!this.eveningStartHour)
                throw new Error("Error in initialization!")
            var dateTime = new Date(moment().format('YYYY-MM-DD') + ' 00:00:00')
            dateTime.setHours(this.eveningStartHour)
            return dateTime
        },
        eveningEndDate() {
            if (!this.eveningEndHour)
                throw new Error("Error in initialization!")
            var dateTime = new Date(moment().format('YYYY-MM-DD') + ' 00:00:00')
            dateTime.setHours(this.eveningEndHour)
            return dateTime
        },
        startDateTime() {
            return this.morningStartDate
        },
        endDateTime() {
            return this.morningEndDate
        }
    },
    watch: {
        selectedRoute() {
            if (!this.selectedRoute.routeData)
                throw new Error("Selected route has no route data!")

            if (!this.selectedRoute.routeData.startTimeMin)
                this.selectedRoute.routeData.startTimeMin = this.getFormattedTime(this.startDateTime)

            if (!this.selectedRoute.routeData.startTimeMax)
                this.selectedRoute.routeData.startTimeMax = this.getFormattedTime(this.startTimeMin)

            if (!this.selectedRoute.routeData.endTimeMin)
                this.selectedRoute.routeData.endTimeMin = this.getFormattedTime(this.endDateTime)

            if (!this.selectedRoute.routeData.endTimeMax)
                this.selectedRoute.routeData.endTimeMax = this.getFormattedTime(this.endDateTime)
        },
        startTimeMin() {
            this.checkStartTimeMin()
            this.checkStartTimeMax()
            this.checkEndTimeMin()
            this.checkEndTimeMax()
        },
        startTimeMax() {
            this.checkStartTimeMin()
            this.checkStartTimeMax()
            this.checkEndTimeMin()
            this.checkEndTimeMax()
        },
        endTimeMin() {
            this.checkStartTimeMin()
            this.checkStartTimeMax()
            this.checkEndTimeMin()
            this.checkEndTimeMax()
        },
        endTimeMax() {
            this.checkStartTimeMin()
            this.checkStartTimeMax()
            this.checkEndTimeMin()
            this.checkEndTimeMax()
        }
    },
    methods: {
        checkStartTimeMin() {
            if (!this.startTimeMin)
                return

            if (this.startTimeMin < this.startDateTime) {
                this.selectedRoute.routeData.startTimeMin = this.getFormattedTime(this.startDateTime)
            }

            if (this.startTimeMin > this.endDateTime) {
                this.selectedRoute.routeData.startTimeMin = this.getFormattedTime(this.endDateTime)
            }
        },
        checkStartTimeMax() {
            if (!this.startTimeMax)
                return

            if (this.startTimeMax < this.startDateTime) {
                this.selectedRoute.routeData.startTimeMax = this.getFormattedTime(this.startDateTime)
            }

            if (this.startTimeMax > this.endDateTime) {
                this.selectedRoute.routeData.startTimeMax = this.getFormattedTime(this.endDateTime)
            }

            if (this.startTimeMin && this.startTimeMax < this.startTimeMin) {
                this.selectedRoute.routeData.startTimeMax = this.getFormattedTime(this.startTimeMin)
            }

            if (this.endTimeMin && this.startTimeMax > this.endTimeMin) {
                this.selectedRoute.routeData.startTimeMax = this.getFormattedTime(this.endTimeMin)
            }
        },
        checkEndTimeMin() {
            if (!this.endTimeMin)
                return

            if (this.endTimeMin < this.startDateTime) {
                this.selectedRoute.routeData.endTimeMin = this.getFormattedTime(this.startDateTime)
            }

            if (this.endTimeMin > this.endDateTime) {
                this.selectedRoute.routeData.endTimeMin = this.getFormattedTime(this.endDateTime)
            }

            if (this.startTimeMin && this.endTimeMin < this.startTimeMin) {
                this.selectedRoute.routeData.endTimeMin = this.getFormattedTime(this.startTimeMin)
            }

            if (this.startTimeMax && this.endTimeMin < this.startTimeMax) {
                this.selectedRoute.routeData.endTimeMin = this.getFormattedTime(this.startTimeMax)
            }

            if (this.endTimeMax && this.endTimeMax < this.endTimeMin) {
                this.selectedRoute.routeData.endTimeMin = this.getFormattedTime(this.endTimeMax)
            }
        },
        checkEndTimeMax() {
            if (!this.endTimeMax)
                return

            if (this.endTimeMax < this.startDateTime) {
                this.selectedRoute.routeData.endTimeMax = this.getFormattedTime(this.startDateTime)
            }

            if (this.endTimeMax > this.endDateTime) {
                this.selectedRoute.routeData.endTimeMax = this.getFormattedTime(this.endDateTime)
            }

            if (this.endTimeMin && this.endTimeMax < this.endTimeMin) {
                this.selectedRoute.routeData.endTimeMax = this.getFormattedTime(this.endTimeMin)
            }
        },
        getDurationInMinutes(start, end) {
            var duration = moment.duration(moment(end).diff(moment(start)))
            return duration.asMinutes()
        },
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