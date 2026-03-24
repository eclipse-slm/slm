import {useCapabilitiesStore} from "@/stores/capabilitiesStore";

export function useCapabilityUtils() {
  const capabilitiesStore = useCapabilitiesStore();

  const getCapability = (capabilityId) => {
    try {
      return capabilitiesStore.availableSingleHostCapabilities.find(
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
    return capabilitiesStore.availableSingleHostCapabilities.filter(shc => shc.capabilityClass === capabilityClass)
  };

  const getUniqueCapabilityClasses = () => {
    return [...new Set(
        capabilitiesStore.availableSingleHostCapabilities.map(shc => shc.capabilityClass)
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

  const filterDeploymentCapabilityServices = (capabilityServiceIds) => {
    const deploymentCapabilityServices = [];
    capabilityServiceIds.forEach(capabilityServiceId => {
        const capabilityService = capabilitiesStore.capabilityServiceById(capabilityServiceId);
        const capability = capabilitiesStore.capabilityById(capabilityService.capabilityId);

        if (isDeploymentCapability(capability)) {
          deploymentCapabilityServices.push(capabilityServiceId);
        }
    });

    return deploymentCapabilityServices;
  };

  const filterConfigurationCapabilityServices = (capabilityServiceIds) => {
    const configrationCapabilityServices = [];
    capabilityServiceIds.forEach(capabilityServiceId => {
      const capabilityService = capabilitiesStore.capabilityServiceById(capabilityServiceId);
      const capability = capabilitiesStore.capabilityById(capabilityService.capabilityId);

      if (isConfigurationCapability(capability)) {
        configrationCapabilityServices.push(capabilityServiceId);
      }
    });

    return configrationCapabilityServices;
  };

  const isDeploymentCapability = (capability) => {
    return capability.capabilityClass === "DeploymentCapability";
  };

  const isConfigurationCapability = (capability) => {
    return capability.capabilityClass === "BaseConfigurationCapability";
  };

  return {
    getCapability,
    getParamsOfInstallAction,
    getCapabilitiesByCapabilityClass,
    getUniqueCapabilityClasses,
    isDefineCapabilityDialogRequired,
    isCapabilitySkipable,
    filterDeploymentCapabilityServices,
    filterConfigurationCapabilityServices,
    isDeploymentCapability,
    isConfigurationCapability,
  };
}