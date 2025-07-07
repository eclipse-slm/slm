import {useResourceDevicesStore} from "@/stores/resourceDevicesStore";

class DeviceUtils {

    static getProduct(resourceId) {
        const resourceDevicesStore = useResourceDevicesStore();

        let productValue = "N/A";
        const paths = [
            '$.ManufacturerProductDesignation..de',
            '$.ManufacturerProductDesignation..["de-DE"]',
            '$.ManufacturerProductDesignation..en',
            '$.ManufacturerProductDesignation..["en-EN"]',
            '$.ManufacturerProductDesignation..["en-US"]',
            '$.ManufacturerProductDesignation..["en-GB"]',
        ];
        for (const path of paths) {
            const value = resourceDevicesStore.getSubmodelElementValueOfResourceSubmodel(resourceId, "Nameplate", path);
            if (value !== 'N/A') {
                productValue = value;
            }
        }

        if (productValue !== 'N/A' && productValue.length < 40) {
            return productValue;
        } else {
            const paths = [
                '$.ManufacturerProductType..de',
                '$.ManufacturerProductType..["de-DE"]',
                '$.ManufacturerProductType..en',
                '$.ManufacturerProductType..["en-EN"]',
                '$.ManufacturerProductType..["en-US"]',
                '$.ManufacturerProductType..["en-GB"]',
            ];
            for (const path of paths) {
                const value = resourceDevicesStore.getSubmodelElementValueOfResourceSubmodel(resourceId, "Nameplate", path);
                if (value !== 'N/A') {
                    productValue = value;
                }

                if (productValue !== 'N/A') {
                    return productValue;
                } else {
                    productValue = resourceDevicesStore.getSubmodelElementValueOfResourceSubmodel(resourceId,
                        'Nameplate', '$.OrderCodeOfManufacturer');
                    return productValue;
                }
            }
        }
    }

    static getVendor(resourceId) {
        const resourceDevicesStore = useResourceDevicesStore();

        const paths = [
            '$.ManufacturerName..de',
            '$.ManufacturerName..["de-DE"]',
            '$.ManufacturerName..en',
            '$.ManufacturerName..["en-EN"]',
            '$.ManufacturerName..["en-US"]',
            '$.ManufacturerName..["en-GB"]',
        ];

        for (const path of paths) {
            const value = resourceDevicesStore.getSubmodelElementValueOfResourceSubmodel(resourceId, "Nameplate", path);
            if (value !== 'N/A') {
                return value;
            }
        }
        return 'N/A';
    }
}

export default DeviceUtils;