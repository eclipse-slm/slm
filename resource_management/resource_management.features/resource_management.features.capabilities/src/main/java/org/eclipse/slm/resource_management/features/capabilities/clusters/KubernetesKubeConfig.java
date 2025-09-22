package org.eclipse.slm.resource_management.features.capabilities.clusters;

import java.io.File;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

/** KubeConfig represents a kubernetes client configuration */
public class KubernetesKubeConfig {
    private static final Logger log = LoggerFactory.getLogger(KubernetesKubeConfig.class);

    private ArrayList<Object> clusters;
    private ArrayList<Object> contexts;
    private ArrayList<Object> users;
    String currentContextName;
    Map<String, Object> currentContext;
    Map<String, Object> currentCluster;
    Map<String, Object> currentUser;
    String currentNamespace;
    Object preferences;
    private File file;


    /** Load a Kubernetes config from a Reader */
    public static KubernetesKubeConfig loadKubeConfig(Reader input) {
        Yaml yaml = new Yaml(new SafeConstructor(new LoaderOptions()));
        Object config = yaml.load(input);
        Map<String, Object> configMap = (Map<String, Object>) config;

        ArrayList<Object> contexts = (ArrayList<Object>) configMap.get("contexts");
        ArrayList<Object> clusters = (ArrayList<Object>) configMap.get("clusters");
        ArrayList<Object> users = (ArrayList<Object>) configMap.get("users");
        String currentContext = (String) configMap.get("current-context");
        Object preferences = configMap.get("preferences");

        KubernetesKubeConfig kubeConfig = new KubernetesKubeConfig(contexts, clusters, users);
        kubeConfig.setContext(currentContext);
        kubeConfig.setPreferences(preferences);

        return kubeConfig;
    }


    public KubernetesKubeConfig(
            ArrayList<Object> contexts, ArrayList<Object> clusters, ArrayList<Object> users) {
        this.contexts = contexts;
        this.clusters = clusters;
        this.users = users;
    }


    private void setPreferences(Object preferences) {
        this.preferences = preferences;
    }

    public String getCurrentContextName() {
        return currentContextName;
    }

    public boolean setContext(String context) {
        if (context == null) {
            return false;
        }
        currentContextName = context;
        currentCluster = null;
        currentUser = null;
        Map<String, Object> ctx = findObject(contexts, context);
        if (ctx == null) {
            return false;
        }
        currentContext = (Map<String, Object>) ctx.get("context");
        if (currentContext == null) {
            return false;
        }
        String cluster = (String) currentContext.get("cluster");
        String user = (String) currentContext.get("user");
        currentNamespace = (String) currentContext.get("namespace");
        if (cluster != null) {
            Map<String, Object> obj = findObject(clusters, cluster);
            if (obj != null) {
                currentCluster = (Map<String, Object>) obj.get("cluster");
                currentCluster.put("name", cluster);
            }
        }
        if (user != null) {
            Map<String, Object> obj = findObject(users, user);
            if (obj != null) {
                currentUser = (Map<String, Object>) obj.get("user");
                currentUser.put("name", user);
            }
        }
        return true;
    }

    public Map<String, Object> getCurrentCluster(){

        for (Object cluster : getClusters()){
            Map<String, Object> clusterMap = (Map<String, Object>) cluster;
            if (clusterMap.get("name").equals(currentContext.get("cluster"))){
                return clusterMap;
            }
        }
        return null;
    }
    public Map<String, Object> getCurrentUser(){

        for (Object user : getUsers()){
            Map<String, Object> userMap = (Map<String, Object>) user;
            if (userMap.get("name").equals(currentContext.get("user"))){
                return userMap;
            }
        }
        return null;
    }

    public ArrayList<Object> getContexts() {
        return contexts;
    }

    public ArrayList<Object> getClusters() {
        return clusters;
    }

    public ArrayList<Object> getUsers() {
        return users;
    }

    public String getNamespace() {
        return currentNamespace;
    }

    public Object getPreferences() {
        return preferences;
    }

    public String getServer() {
        return getData(currentCluster, "server");
    }

    public String getCertificateAuthorityData() {
        return getData(currentCluster, "certificate-authority-data");
    }

    public String getCertificateAuthorityFile() {
        return getData(currentCluster, "certificate-authority");
    }

    public String getClientCertificateFile() {
        return getData(currentUser, "client-certificate");
    }

    public String getClientCertificateData() {
        return getData(currentUser, "client-certificate-data");
    }

    public String getClientKeyFile() {
        return getData(currentUser, "client-key");
    }

    public String getClientKeyData() {
        return getData(currentUser, "client-key-data");
    }

    public String getUsername() {
        return getData(currentContext, "user");
    }

//    public String getPassword() {
//        return getData(currentContext, "password");
//    }

    private static String getData(Map<String, Object> obj, String key) {
        if (obj == null) {
            return null;
        }
        return (String) obj.get(key);
    }

    private static Map<String, Object> findObject(ArrayList<Object> list, String name) {
        if (list == null) {
            return null;
        }
        for (Object obj : list) {
            Map<String, Object> map = (Map<String, Object>) obj;
            if (name.equals(map.get("name"))) {
                return map;
            }
        }
        return null;
    }
}
