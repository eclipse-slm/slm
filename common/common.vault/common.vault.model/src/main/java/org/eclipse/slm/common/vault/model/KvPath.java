package org.eclipse.slm.common.vault.model;

public class KvPath {
    KvVersion kvVersion = KvVersion.v2;
    String secretEngine;
    String path;

    public KvPath() {
    }

    public KvPath(String secretEngine, String path) {
        this.secretEngine = secretEngine;
        this.path = path;
    }

    public KvVersion getKvVersion() {
        return kvVersion;
    }

    public void setKvVersion(KvVersion kvVersion) {
        this.kvVersion = kvVersion;
    }

    public String getSecretEngine() {
        return secretEngine;
    }

    public void setSecretEngine(String secretEngine) {
        this.secretEngine = secretEngine;
    }

    public String getPath() {
        if(path.startsWith("/"))
            return path;
        else
            return "/" + path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFullPath() {
        if(kvVersion == KvVersion.v1) {
            return secretEngine + "/" + path;
        } else {
            return secretEngine + "/data/" + path;
        }
    }
}
