package org.eclipse.slm.common.vault.model;

public class SecretEngineOption {
    String version;

    public SecretEngineOption() {
    }

    public SecretEngineOption(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "SecretEngineOption{" +
                "version='" + version + '\'' +
                '}';
    }
}
