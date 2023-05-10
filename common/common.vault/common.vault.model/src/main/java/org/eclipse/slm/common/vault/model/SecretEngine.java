package org.eclipse.slm.common.vault.model;

public class SecretEngine {
    String type;
    String description;
    SecretEngineConfig config;
    SecretEngineOption options;

    public SecretEngine() {
    }

    public SecretEngine(String type, String description, SecretEngineConfig config, SecretEngineOption options) {
        this.type = type;
        this.description = description;
        this.config = config;
        this.options = options;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public SecretEngineConfig getConfig() {
        return config;
    }

    public void setConfig(SecretEngineConfig config) {
        this.config = config;
    }

    public SecretEngineOption getOptions() {
        return options;
    }

    public void setOptions(SecretEngineOption options) {
        this.options = options;
    }

    @Override
    public String toString() {
        return "SecretEngine{" +
                "type='" + type + '\'' +
                ", description='" + description + '\'' +
                ", config=" + config +
                ", options=" + options +
                '}';
    }
}
