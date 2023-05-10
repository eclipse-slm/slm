package org.eclipse.slm.service_management.service.rest.kubernetes;

import org.eclipse.slm.service_management.model.offerings.ServiceOfferingVersion;
import org.eclipse.slm.service_management.model.offerings.exceptions.InvalidServiceOfferingDefinitionException;
import org.eclipse.slm.service_management.model.offerings.kubernetes.KubernetesDeploymentDefinition;
import org.eclipse.slm.service_management.model.offerings.options.ServiceOptionType;
import org.eclipse.slm.service_management.model.offerings.options.ServiceOptionValue;
import io.kubernetes.client.openapi.models.*;
import io.kubernetes.client.util.ModelMapper;
import io.kubernetes.client.util.Yaml;
import org.eclipse.slm.service_management.service.rest.kubernetes.KubernetesGenericObject;
import org.eclipse.slm.service_management.service.rest.kubernetes.KubernetesManifestFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public final class KubernetesManifestFileParser {

    public final static Logger LOG = LoggerFactory.getLogger(KubernetesManifestFileParser.class);

    private static ModelMapper modelMapper = null;


    public static KubernetesManifestFile parseManifestFile(String rawFileContentYaml) {

        // operate on raw string in order to catch invalid yaml here
        List<KubernetesGenericObject> objList = Arrays.stream(rawFileContentYaml.split( "(\n)*---(\n)*" )).map(obj -> {

            // parse yaml file, and check if api+kind is registered
            Map<String, Object> data;
            try {
                data = Yaml.getSnakeYaml().load(obj);
            } catch (Exception e) {
                LOG.warn("Could not parse object from String: " + e);
                LOG.warn("Keeping raw string instance instead!");
                return new KubernetesGenericObject(obj, "unknown", "unknown");
            }

            if (data == null){
                return null;
            }

            // try to infer kind and apiVersion in order to find class to parse to
            String kind = (String) data.get("kind");
            String apiVersion = (String) data.get("apiVersion");

            if (modelMapper == null){
                LOG.warn("initializing model map");
                initModelMapper();
                LOG.warn("initialized model map");
            }

            Class<?> clazz = modelMapper.getApiTypeClass(apiVersion, kind);
            if (clazz == null) {

                // Kubernetes class is not registered, only keep raw, since it can not be parsed as object
                LOG.warn("Unknown apiVersionKind " + apiVersion + "/" + kind + ". Keeping raw string instead of parsed instance!");
                return new KubernetesGenericObject(Yaml.dump(data), apiVersion, kind);
            } else {

                // try parsing object
                LOG.info("Known apiVersionKind " + apiVersion + "/" + kind + ". Trying to parse instance!");
                try {
                    var parsedObject = Yaml.loadAs(new StringReader(Yaml.getSnakeYaml().dump(data)), clazz);
                    return new KubernetesGenericObject(Yaml.dump(data), apiVersion, kind, parsedObject);
                } catch (Exception e) {
                    LOG.warn("Could not parse object of kind " + apiVersion + "/" + kind + ". Reason: " + e);
                    LOG.warn("Keeping raw string instance instead!");
                    return new KubernetesGenericObject(Yaml.dump(data), apiVersion, kind);
                }
            }
        }).filter(out -> out!=null).collect(Collectors.toList());

        return new KubernetesManifestFile(rawFileContentYaml, objList);

    }


    /**
     * init model map with some examples from: https://kubernetes.io/docs/reference/kubernetes-api/
     * this is required due to: https://github.com/kubernetes-client/java/issues/1659
     */
    private static void initModelMapper() {
        modelMapper = new ModelMapper();

        // some examples from https://kubernetes.io/docs/reference/kubernetes-api/

        // workloads
        modelMapper.addModelMap("", "v1", "Pod", V1Pod.class);
        modelMapper.addModelMap("", "v1", "PodTemplate", V1PodTemplate.class);
        modelMapper.addModelMap("", "v1", "ReplicationController", V1ReplicaSet.class);
        modelMapper.addModelMap("apps", "v1", "ReplicaSet", V1ReplicaSet.class);
        modelMapper.addModelMap("apps", "v1", "Deployment", V1Deployment.class);
        modelMapper.addModelMap("apps", "v1", "StatefulSet", V1StatefulSet.class);
        modelMapper.addModelMap("apps", "v1", "DaemonSet", V1DaemonSet.class);
        modelMapper.addModelMap("batch", "v1", "Job", V1Job.class);

        // service resources
        modelMapper.addModelMap("", "v1", "Service", V1Service.class);
        modelMapper.addModelMap("", "v1", "Endpoints", V1Endpoints.class);
        // modelMapper.addModelMap("", "V1beta1", "Endpoints", V1beta1EndpointSlice.class);
        modelMapper.addModelMap("networking.k8s.io", "v1", "Ingress", V1Ingress.class);
        modelMapper.addModelMap("networking.k8s.io", "v1", "IngressClass", V1IngressClass.class);

        // config and storage resources
        modelMapper.addModelMap("", "v1", "ConfigMap", V1ConfigMap.class);
        modelMapper.addModelMap("", "v1", "Secret", V1Secret.class);
        modelMapper.addModelMap("", "v1", "PersistentVolume", V1PersistentVolume.class);
        modelMapper.addModelMap("", "v1", "PersistentVolumeClaim", V1PersistentVolumeClaim.class);
        modelMapper.addModelMap("storage.k8s.io", "v1", "StorageClass", V1StorageClass.class);

        // cluster resource
        modelMapper.addModelMap("", "v1", "Node", V1Node.class);
        modelMapper.addModelMap("", "v1", "Namespace", V1Namespace.class);
        // modelMapper.addModelMap("", "v1", "Event", V1beta1Event.class);
    }


    /**
     * Finalize manifest. Currently validating YAML by reloading the complete string
     * @param manifestFile as string
     * @return the final manifest as string
     */
    public static String manifestFinalizer(KubernetesManifestFile manifestFile){

        // reparse full manifest, in order to fail if manifest, or parts of it, is invalid!
        Iterable<Object> rawDefinitionsList = Yaml.getSnakeYaml().loadAll(manifestFile.toConcatenatedString());
        List<Object> finalList = new ArrayList<>();

        // use iterator, to catch cases where yaml is not valid at all
        while (rawDefinitionsList.iterator().hasNext()){
            try {
                finalList.add(rawDefinitionsList.iterator().next());
            } catch (Exception e) {
                LOG.error("Could not parse yaml object before deployment (invalid yaml): " + e.getMessage());
                throw e;
            }
        }

        // re-dump yaml as string
        String finalManifestOutput = Yaml.getSnakeYaml().dumpAll(finalList.iterator());
        LOG.info("Final manifest is valid yaml!");
        return finalManifestOutput;
    }


    public static KubernetesManifestFile generateDeployableManifestFileForServiceOffering(ServiceOfferingVersion serviceOfferingVersion, Collection<ServiceOptionValue> serviceOptionValues) throws InvalidServiceOfferingDefinitionException {
        if (!(serviceOfferingVersion.getDeploymentDefinition() instanceof KubernetesDeploymentDefinition)) {
            throw new InvalidServiceOfferingDefinitionException("Manifest file can only be generated for KubernetesDeploymentDefinition and not for '" + serviceOfferingVersion.getDeploymentDefinition().getClass() + "'");
        }
        var deploymentDefinition = (KubernetesDeploymentDefinition) serviceOfferingVersion.getDeploymentDefinition();

        // Filter service option values by service option type
        var envVarServiceOptionValues = serviceOfferingVersion.filterServiceOptionValuesByOptionType(serviceOptionValues, ServiceOptionType.ENVIRONMENT_VARIABLE);
        var stringReplaceServiceOptionValues = serviceOfferingVersion.filterServiceOptionValuesByOptionType(serviceOptionValues, ServiceOptionType.STRING_REPLACE);

        // 1) Parse manifest file
        KubernetesManifestFile manifestFile = KubernetesManifestFileParser.parseManifestFile(deploymentDefinition.getManifestFile());

        // 2) Replace env variables in container spec with service option values
        manifestFile = KubernetesManifestFileParser.replaceServiceOptionValuesForEnvironmentVariables(envVarServiceOptionValues, manifestFile);

        // 3) Replace variables directly by string replace with service option values
        manifestFile = KubernetesManifestFileParser.replaceServiceOptionValuesForStringReplace(stringReplaceServiceOptionValues, manifestFile);

        return manifestFile;
    }

    private static KubernetesManifestFile replaceServiceOptionValuesForStringReplace(List<ServiceOptionValue> stringReplaceServiceOptionValues, KubernetesManifestFile manifestFile) {

        manifestFile.setDefinitions(manifestFile.getDefinitions().stream().map(obj -> {

            if (stringReplaceServiceOptionValues.size() > 0){

                for (ServiceOptionValue sov : stringReplaceServiceOptionValues){

                    // unwrap service option id
                    String sovId = sov.getServiceOptionId().replace("$|", "");

                    // if unparsed kubernetes, directly replace string
                    if (obj.getParsed() == null){
                        int occ = (obj.getRaw().length() - obj.getRaw().replace(sovId, "").length()) / sovId.length();

                        if(occ > 0){
                            LOG.info("replacing string '" + sovId + "' with '" + sov.getValue() + "' (" + occ + " x times)");
                            obj.setRaw(obj.getRaw().replace(sovId, sov.getValue().toString()));
                        }

                    } else {

                        // generate string from parsed object
                        String rawFromParsed = Yaml.dump(obj.getParsed());
                        int occ = (rawFromParsed.length() - rawFromParsed.replace(sovId, "").length()) / sovId.length();

                        if(occ > 0){
                            LOG.info("replacing string '" + sovId + "' with '" + sov.getValue() + "' (" + occ + " x times)");
                            obj.setRaw(rawFromParsed.replace(sovId, sov.getValue().toString()));

                            // try to parse again, from raw string
                            try {
                                obj.setParsed(Yaml.load(obj.getRaw()));
                            } catch (IOException e) {
                                throw new RuntimeException("Could not reparse Yaml after string replace: " + e);
                            }
                        }

                    }
                }
            }
            return obj;
        }).collect(Collectors.toList()));

        return manifestFile;
    }


    public static KubernetesManifestFile replaceServiceOptionValuesForEnvironmentVariables(List<ServiceOptionValue> serviceOptionValues, KubernetesManifestFile kubernetesManifestFile) {

        for (KubernetesGenericObject definition: kubernetesManifestFile.getDefinitions()){

            // only replace env for known api+kind types
            var clazz = ModelMapper.getApiTypeClass(definition.getApiVersion(), definition.getKind());
            if (clazz != null){

                // only replace env in V1Deployments
                if (clazz.getSimpleName().equalsIgnoreCase("V1Deployment")) {
                    V1Deployment parsedDeployment = (V1Deployment) definition.getParsed();

                    // include standard containers as well as init-containers
                    List<V1Container> containers = Stream.of(
                            parsedDeployment.getSpec().getTemplate().getSpec().getContainers(),
                            parsedDeployment.getSpec().getTemplate().getSpec().getInitContainers()
                    ).flatMap(x -> x == null? null : x.stream()).toList();

                    for (var container : containers) {
                        if (container.getEnv() != null) {

                            for (V1EnvVar envVarEntry : container.getEnv()) {
                                var serviceOptionId = parsedDeployment.getMetadata().getName() + "|" + container.getName() + "|" + envVarEntry.getName();
                                var optionalServiceOptionValue = serviceOptionValues.stream().filter(sov -> sov.getServiceOptionId().equals(serviceOptionId)).findAny();

                                if (optionalServiceOptionValue.isPresent()) {
                                    var serviceOptionValue = optionalServiceOptionValue.get();
                                    LOG.info(String.format("replacing envVar '%s'(deployment='%s', container='%s', var='%s') to '%s'", serviceOptionValue.getServiceOptionId(), parsedDeployment.getMetadata().getName(), container.getName(), envVarEntry.getName(), serviceOptionValue.getValue()));

                                    // replace value in env var, by indexing the list of containers and env vars by the objects itself
                                    int containerIndex = parsedDeployment.getSpec().getTemplate().getSpec().getContainers().indexOf(container);
                                    if (containerIndex == -1){
                                        containerIndex = parsedDeployment.getSpec().getTemplate().getSpec().getInitContainers().indexOf(container);
                                        parsedDeployment.getSpec().getTemplate().getSpec().getInitContainers().get(containerIndex).getEnv().get(container.getEnv().indexOf(envVarEntry)).setValue(serviceOptionValue.getValue().toString());
                                    } else {
                                        parsedDeployment.getSpec().getTemplate().getSpec().getContainers().get(containerIndex).getEnv().get(container.getEnv().indexOf(envVarEntry)).setValue(serviceOptionValue.getValue().toString());
                                    }
                                }
                            }
                        }
                    }
                    // find and replace object
                    kubernetesManifestFile.getDefinitions().set(kubernetesManifestFile.getDefinitions().indexOf(definition),
                            new KubernetesGenericObject(definition.getRaw(), definition.getApiVersion(), definition.getKind(), parsedDeployment));
                }

            }
        }
        return kubernetesManifestFile;
    }
}
