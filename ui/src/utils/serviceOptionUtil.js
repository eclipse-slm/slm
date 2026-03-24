import * as yup from "yup";

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
            let rule = yup.string();

            switch (serviceOption.optionType) {
                case 'ENVIRONMENT_VARIABLE':
                case 'LABEL':
                    // Nothing to do, will be handled in next switch
                    break;

                case 'PORT':
                    rule = yup.number().integer().min(1).max(65536);
                    if (serviceOption.required) {
                        rule.required();
                    }
                    return rule;

                case 'VOLUME':
                    rule = yup.string().matches(new RegExp('^\\\\/$|(\\\\/[a-zA-Z_0-9-]+)+$'));
                    if (serviceOption.required) {
                        rule.required();
                    }
                    return;

                default:
                    break;
            }

            switch (serviceOption.valueType) {
                case 'STRING':
                case 'PASSWORD':
                case 'BOOLEAN':
                case 'ENUM':
                case 'AUTO_GENERATED_UUID':
                    break;

                case 'EMAIL':
                    rule = yup.string().email();
                    break;

                case 'NUMBER':
                    rule = yup.number();
                    break;

                case 'DECIMAL':
                    rule = yup.number();
                    break;

                case 'INTEGER':
                    rule = yup.number().integer();
                    break;

                case 'IP':
                    rule = yup.string().ipv4();
                    break;

                default:
                    break;
            }

            if (serviceOption.required) {
                rule.required();
            }

            return rule;
        },
    },
}

export { isEnvVarExisting, isServiceOptionExisting, deleteServiceOption, serviceOptionMixin }
