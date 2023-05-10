function isEnvVarExisting (envVarKey, environmentVariables) {
    for (const envVar of environmentVariables) {
        if (envVar.key === envVarKey) {
            return true
        }
    }
    return false
}

function isServiceOptionExisting (serviceOptionKey, serviceOptionCategories) {
    for (const serviceOptionCategory of serviceOptionCategories) {
        for (const serviceOption of serviceOptionCategory.serviceOptions) {
            if (serviceOption.key === serviceOptionKey) {
                return true
            }
        }
    }
    return false
}

function deleteServiceOption (serviceOptionKey, serviceOptionCategories) {
    for (const serviceOptionCategory of serviceOptionCategories) {
        var i = serviceOptionCategory.serviceOptions.length
        while (i--) {
            if (serviceOptionCategory.serviceOptions[i].key === serviceOptionKey) {
                serviceOptionCategory.serviceOptions.splice(i, 1)
            }
        }
    }

    return serviceOptionCategories
}

var serviceOptionMixin = {
    methods: {
        getValidationRulesForServiceOption (serviceOption) {
            const rule = {}

            if (serviceOption.required) {
                rule.required = true
            }

            switch (serviceOption.optionType) {
                case 'ENVIRONMENT_VARIABLE':
                case 'LABEL':
                    // Nothing to do, will be handled in next switch
                    break

                case 'PORT_MAPPING':
                    rule.integer = true
                    rule.min_value = 1
                    rule.max_value = 65536
                    return rule

                case 'VOLUME':
                    rule.alpha_dash = true
                    // rule.regex = '^\\/$|(\\/[a-zA-Z_0-9-]+)+$'
                    return rule

                default:
                    break
            }

            switch (serviceOption.valueType) {
                case 'STRING':
                case 'PASSWORD':
                case 'BOOLEAN':
                case 'ENUM':
                case 'AUTO_GENERATED_UUID':
                    break

                case 'EMAIL':
                    rule.email = true
                    break

                case 'NUMBER':
                    rule.numeric = true
                    break

                case 'DECIMAL':
                    rule.double = true
                    break

                case 'INTEGER':
                    rule.integer = true
                    break

                case 'IP':
                    rule.ip = true
                    break

                default:
                    break
            }

            return rule
        },
    },
}

export { isEnvVarExisting, isServiceOptionExisting, deleteServiceOption, serviceOptionMixin }
