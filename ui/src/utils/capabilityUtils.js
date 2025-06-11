import { useResourceDevicesStore } from "@/stores/resourceDevicesStore";

export function useCapabilityUtils() {
  const resourceDevicesStore = useResourceDevicesStore();

  const getCapability = (capabilityId) => {
    try {
      return resourceDevicesStore.availableSingleHostCapabilitiesNoDefault.find(
          cap => cap.id === capabilityId
      );
    } catch (e) {
      return null;
    }
  };

  const getParamsOfInstallAction = (capabilityId) => {
    try {
      const capability = getCapability(capabilityId);
      if (capability?.actions["INSTALL"].configParameters === undefined) {
        return [];
      } else {
        return capability.actions["INSTALL"].configParameters;
      }
    } catch (e) {
      return [];
    }
  };

  const getCapabilitiesByCapabilityClass = (capabilityClass) => {
    return resourceDevicesStore.availableSingleHostCapabilitiesNoDefault.filter(shc => shc.capabilityClass === capabilityClass)
  };

  const isCapabilityInstalledOnResource = (resource, capability) => {
    if(resource.capabilityServices !== null)
      return resource.capabilityServices.filter(capService => capService.capability.name === capability.name).length > 0
    else
      return false;
  };

  const getUniqueCapabilityClasses = () => {
    return [...new Set(
        resourceDevicesStore.availableSingleHostCapabilitiesNoDefault.map(shc => shc.capabilityClass)
    )].sort();
  };

  const isCapabilitySkipable = (capability) => {
    const installAction = capability.actions["INSTALL"];
    if(installAction !== undefined && installAction.skipable !== undefined)
      return installAction.skipable;
    else
      return false;
  };

  const hasCapabilityConfigParamsWithRequiredTypeAlways = (capabilityId) => {
    if(getCapability(capabilityId).actions["INSTALL"].configParameters === undefined)
      return false;

    return getCapability(capabilityId).actions["INSTALL"].configParameters.filter(
        param => param.requiredType === "ALWAYS"
    ).length !== 0;
  };

  const isDefineCapabilityDialogRequired = (capabilityId, skipInstall) => {
    //false
    if(getParamsOfInstallAction(capabilityId).length === 0)
      return false;
    else if (hasCapabilityConfigParamsWithRequiredTypeAlways(capabilityId) === false && skipInstall === true)
      return false;
    else
      return true;
  };

  return {
    getCapability,
    getParamsOfInstallAction,
    getCapabilitiesByCapabilityClass,
    getUniqueCapabilityClasses,
    isCapabilityInstalledOnResource,
    isDefineCapabilityDialogRequired,
    isCapabilitySkipable
  };
}