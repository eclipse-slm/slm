import {useResourceDevicesStore} from "@/stores/resourceDevicesStore";

class DeviceUtils {

    static getProduct(resourceId) {
        const resourceDevicesStore = useResourceDevicesStore();

        let productValue = "N/A";
        let productValueChecked = "";
        let manufacturerProductDesignation = resourceDevicesStore.getSubmodelElementValueOfResourceSubmodel(resourceId,
            'Nameplate', '$.ManufacturerProductDesignation..en');
        if (manufacturerProductDesignation !== 'N/A' && manufacturerProductDesignation.length < 40) {
            productValue = manufacturerProductDesignation;
        } else if ((productValueChecked = resourceDevicesStore.getSubmodelElementValueOfResourceSubmodel(resourceId,
            'Nameplate', '$.ManufacturerProductType..en')) !== 'N/A') {
            productValue = productValueChecked;
        } else if ((productValue = resourceDevicesStore.getSubmodelElementValueOfResourceSubmodel(resourceId,
            'Nameplate', '$.ManufacturerProductType')) !== 'N/A') {
            productValue = productValueChecked;
        } else if ((productValue = resourceDevicesStore.getSubmodelElementValueOfResourceSubmodel(resourceId,
            'Nameplate', '$.OrderCodeOfManufacturer')) !== 'N/A') {
            productValue = productValueChecked;
        }

        return productValue;
    }

    static getVendor(resourceId) {
        const resourceDevicesStore = useResourceDevicesStore();
        if (resourceDevicesStore.getSubmodelElementValueOfResourceSubmodel(resourceId, 'Nameplate', '$.ManufacturerName..en') == 'N/A') {
            return resourceDevicesStore.getSubmodelElementValueOfResourceSubmodel(resourceId, "Nameplate", "$.ManufacturerName..de");
        } else {
            return resourceDevicesStore.getSubmodelElementValueOfResourceSubmodel(resourceId, "Nameplate", "$.ManufacturerName..en");
        }
    }
}

export default DeviceUtils;