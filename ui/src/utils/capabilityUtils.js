export var capabilityUtilsMixin = {
  methods: {
    getCapability(capabilityId) {
      try {
        return this.$store.getters.availableSingleHostCapabilitiesNoDefault.find(
          cap => cap.id === capabilityId
        )
      } catch(e) {
        return null
      }
    },
    getParamsOfInstallAction(capabilityId) {
      try {
        if(this.getCapability(capabilityId).actions["INSTALL"].configParameters === undefined)
          return []
        else
          return this.getCapability(capabilityId).actions["INSTALL"].configParameters
      } catch(e) {
        return []
      }
    }
  }
}