var routeEditComp = {
     data() {
     return {
            startTime: '',
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
                        <date-picker v-model="startTime" lang="en" firstDayOfWeek="1"  type="datetime"></date-picker>
                        {{startTime}}
                    </div>`,

    computed: {
        dateText () {
          console.log("dfgfgg")
          return this.startTime ? DatePicker.methods.stringify(startTime.date, 'DD-YYYY-MM') : '';

        }
    },
    watch: {
    },
    methods: {
    }
};