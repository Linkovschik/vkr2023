<template>
  <div :id="mainComponent">
    <template>
      <b-list-group v-if="!isSingleContract" class="widget__tab-group">
        <div v-for="contract in contracts" class="widget__tab">
          <b-list-group-item
            :key="`tab-${contract.billingId}-${contract.id}`"
            href="#"
            :active="compareContracts(contract, selectedContract)"
            @click.prevent="setSelectedContract(contract)"
          >
            <strong>{{ contract.title }}</strong>
          </b-list-group-item>
        </div>
      </b-list-group>

      <template v-if="isContractSelected">
        <div class="widget-content">
          <div v-if="showCustomer" class="widget-content__row">
            <contract-customer
              v-if="showCustomer"
              :key="`contract-customer-${selectedContract.id}-${selectedContract.billingId}`"
              :contract-id="selectedContract.id"
              :groups="customerGroups"
              :billing-id="selectedContract.billingId"
            ></contract-customer>
          </div>

          <div v-if="showContractHeader || showBalance" class="widget-content__row">
            <contract-header
              v-if="showContractHeader"
              :key="`contract-header-${selectedContract.id}-${selectedContract.billingId}`"
              :contract-id="selectedContract.id"
              :billing-id="selectedContract.billingId"
            ></contract-header>

            <contract-balance-widget
              v-if="showBalance"
              :key="`contract-balance-widget-${selectedContract.id}-${selectedContract.billingId}`"
              :contract-id="selectedContract.id"
              :billing-id="selectedContract.billingId"
              :show-details="showBalanceDetails"
              :show-expiry-date="showBalanceExpiryDate"
            ></contract-balance-widget>
          </div>
          <div v-if="showOptions" class="widget-content__row">
          <contract-options
            v-if="showOptions"
            :key="`new-options-${selectedContract.billingId}-${selectedContract.id}`"
            :billing-id="selectedContract.billingId"
            :contract-id="selectedContract.id"
            :currency="currency"
          ></contract-options>
            </div>
          <div v-if="showServices" class="widget-content__row">
          <contract-services
            v-if="showServices"
            :key="`new-services-${selectedContract.billingId}-${selectedContract.id}`"
            :modes="serviceModes"
            :types="serviceTypes"
            :statuses="serviceStatuses"
            :billing-id="selectedContract.billingId"
            :contract-id="selectedContract.id"
            :show-tariffs="showTariffs"
            :show-promos="showPromos"
            :show-modules="showModules"
            :show-products="showProducts"
            :currency="currency"
          ></contract-services>
            </div>
        </div>
      </template>
    </template>
  </div>
</template>

<script lang="text/babel">
export default {
  name: 'DefaultContractWidget',
  components: {
    'contract-services': 'url:/components/ui/contract/ContractServices.vue',
    'contract-options': 'url:/components/ui/contract/ContractOptions.vue',
    'contract-header': 'url:/components/ui/contract/ContractHeader.vue',
    'contract-customer': 'url:/components/ui/contract/ContractCustomer.vue',
    'contract-balance-widget': 'url:/components/ui/contract/ContractBalanceWidget.vue'
  },
  props: {
    type: {
      type: String,
      required: false,
      validator: (value) => ['table', 'default'].includes(value),
      default: 'default'
    },
    contracts: {
      type: Array,
      required: false,
      validator: (val) => {
        return val.every(({ id, title, billingId }) => !_.isNil(id) && !_.isNil(title) && !_.isNil(billingId))
      },
      default: () => []
    },
    config: {
      type: Object,
      required: false,
      default: () => ({})
    },
    features: {
      type: Array,
      required: false,
      default: () => []
    },
    parentLoading: {
      type: Boolean,
      required: false,
      default: false
    }
  },
  data() {
    return {
      selectedContract: undefined
    }
  },
  computed: {
    uuid() {
      return this.$utils.generateUUID()
    },
    hasContracts() {
      return !_.isEmpty(this.contracts)
    },
    isSingleContract() {
      return this.contracts.length === 1
    },
    isContractSelected() {
      return !_.isNil(this.selectedContract)
    },
    showServices() {
      return this.features.includes('contract:services')
    },
    showOptions() {
      return this.features.includes('contract:options')
    },
    showBalance() {
      return this.features.includes('contract:balance')
    },
    showBalanceDetails() {
      return this.features.includes('contract:balance:details')
    },
    showPromos() {
      return this.features.includes('service:promos')
    },
    showTariffs() {
      return this.features.includes('service:tariffs')
    },
    showContractPromos() {
      return this.features.includes('contract:promos')
    },
    showContractHeader() {
      return this.features.includes('contract:header')
    },
    showModules() {
      return this.features.includes('service:modules')
    },
    showProducts() {
      return this.features.includes('service:products')
    },
    showCustomer() {
      return this.features.includes('contract:customer')
    },
    showBalanceExpiryDate() {
      return this.features.includes('contract:balance:expiry-date')
    },
    currency() {
      return this.config?.currency ?? 'руб'
    },
    serviceModes() {
      return this.config?.service?.modes ?? []
    },
    serviceTypes() {
      return this.config?.service?.types ?? []
    },
    serviceStatuses() {
      return this.config?.service?.statuses ?? []
    },
    customerGroups() {
      return this.config?.customer?.groups ?? []
    }
  },
  async mounted() {
    if (!this.isContractSelected) {
      let contract = _.first(this.contracts)
      if (!_.isNil(contract)) {
        this.setSelectedContract(contract)
      }
    }
  },
  watch: {
    contracts(val) {
      let newContracts = val ?? []
      if (this.isContractSelected) {
        if (_.some(newContracts ?? [], contract => this.compareContracts(contract, this.selectedContract))) {
          return
        }
      }

      let contract = _.first(newContracts)
      this.setSelectedContract(contract)
    }
  },
  methods: {
    setSelectedContract(contract) {
      this.selectedContract = _.cloneDeep(contract)
    },
    compareContracts(contractA, contractB) {
      return contractA?.id === contractB?.id && contractA?.billingId === contractB?.billingId
    }
  }
}
</script>
