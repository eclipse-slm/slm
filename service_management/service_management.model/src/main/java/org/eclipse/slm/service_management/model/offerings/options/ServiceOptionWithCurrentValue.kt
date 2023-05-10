package org.eclipse.slm.service_management.model.offerings.options

import com.fasterxml.jackson.annotation.JsonIgnore


class ServiceOptionWithCurrentValue (

    @JsonIgnore
    var serviceOption: ServiceOption,

    var currentValue: Any?,

    ): ServiceOption(
    serviceOption.relation, serviceOption.key, serviceOption.name, serviceOption.description, serviceOption.optionType,
    serviceOption.defaultValue, serviceOption.valueType, serviceOption.required, serviceOption.editable) {
}
