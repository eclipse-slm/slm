package org.eclipse.slm.resource_management.service.rest.aas.resources.digitalnameplate

import java.util.Date

class DigitalNameplateV3 private constructor (
    val uriOfTheProduct: String = "N/A",
    val manufacturerName: String = "N/A",
    val manufacturerProductDesignation: String = "N/A",
    val addressInformation: String = "N/A",
    val manufacturerProductRoot: String?,
    val manufacturerProductFamily: String?,
    val manufacturerProductType: String?,
    val orderCodeOfManufacturer: String?,
    val productArticleNumberOfManufacturer: String?,
    val serialNumber: String?,
    val yearOfConstruction: String?,
    val dateOfManufacture: Date?,
    val hardwareVersion: String?,
    val firmwareVersion: String?,
    val softwareVersion: String?,
    val countryOfOrigin: String?,
    val uniqueFacilityIdentifier: String?
)
{
    constructor () : this("N/A",
        "N/A",
        "N/A",
        "N/A", null,
        null, null,
        null, null,
        null, null, null,
        null, null, null,
        null, null)

    data class Builder(
        var uriOfTheProduct: String,
        var manufacturerName: String,
        var manufacturerProductDesignation: String,
        var addressInformation: String,
    )
    {
        private var manufacturerProductRoot: String? = null
        private var manufacturerProductFamily: String? = null
        private var manufacturerProductType: String? = null
        private var orderCodeOfManufacturer: String? = null
        private var productArticleNumberOfManufacturer: String? = null
        private var serialNumber: String? = null
        private var yearOfConstruction: String? = null
        private var dateOfManufacture: Date? = null
        private var hardwareVersion: String? = null
        private var firmwareVersion: String? = null
        private var softwareVersion: String? = null
        private var countryOfOrigin: String? = null
        private var uniqueFacilityIdentifier: String? = null

        fun uriOfTheProduct(uriOfTheProduct: String) = apply { this.uriOfTheProduct = uriOfTheProduct }
        fun manufacturerName(manufacturerName: String) = apply { this.manufacturerName = manufacturerName }
        fun manufacturerProductDesignation(manufacturerProductDesignation: String) = apply { this.manufacturerProductDesignation = manufacturerProductDesignation }
        fun addressInformation(addressInformation: String) = apply { this.addressInformation = addressInformation }
        fun manufacturerProductRoot(manufacturerProductRoot: String) = apply { this.manufacturerProductRoot = manufacturerProductRoot }
        fun manufacturerProductFamily(manufacturerProductFamily: String) = apply { this.manufacturerProductFamily = manufacturerProductFamily }
        fun manufacturerProductType(manufacturerProductType: String) = apply { this.manufacturerProductType = manufacturerProductType }
        fun orderCodeOfManufacturer(orderCodeOfManufacturer: String) = apply { this.orderCodeOfManufacturer = orderCodeOfManufacturer }
        fun productArticleNumberOfManufacturer(productArticleNumberOfManufacturer: String) = apply { this.productArticleNumberOfManufacturer = productArticleNumberOfManufacturer }
        fun serialNumber(serialNumber: String) = apply { this.serialNumber = serialNumber }
        fun yearOfConstruction(yearOfConstruction: String) = apply { this.yearOfConstruction = yearOfConstruction }
        fun dateOfManufacture(dateOfManufacture: Date) = apply { this.dateOfManufacture = dateOfManufacture }
        fun hardwareVersion(hardwareVersion: String) = apply { this.hardwareVersion = hardwareVersion }
        fun firmwareVersion(firmwareVersion: String) = apply { this.firmwareVersion = firmwareVersion }
        fun softwareVersion(softwareVersion: String) = apply { this.softwareVersion = softwareVersion }
        fun countryOfOrigin(countryOfOrigin: String) = apply { this.countryOfOrigin = countryOfOrigin }
        fun uniqueFacilityIdentifier(uniqueFacilityIdentifier: String) = apply { this.uniqueFacilityIdentifier = uniqueFacilityIdentifier }

        fun build() = DigitalNameplateV3(
            uriOfTheProduct,
            manufacturerName,
            manufacturerProductDesignation,
            addressInformation,
            manufacturerProductRoot,
            manufacturerProductFamily,
            manufacturerProductType,
            orderCodeOfManufacturer,
            productArticleNumberOfManufacturer,
            serialNumber,
            yearOfConstruction,
            dateOfManufacture,
            hardwareVersion,
            firmwareVersion,
            softwareVersion,
            countryOfOrigin,
            uniqueFacilityIdentifier,
        )
    }
}