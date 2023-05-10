package org.eclipse.slm.service_management.service.rest.kubernetes;


import org.eclipse.slm.service_management.model.offerings.ServiceOfferingVersion;
import org.eclipse.slm.service_management.model.offerings.exceptions.InvalidServiceOfferingDefinitionException;
import org.eclipse.slm.service_management.model.offerings.kubernetes.KubernetesDeploymentDefinition;
import io.kubernetes.client.common.KubernetesObject;
import io.kubernetes.client.openapi.models.V1Deployment;
import io.kubernetes.client.util.generic.dynamic.Dynamics;
import org.apache.commons.io.IOUtils;
import org.eclipse.slm.service_management.model.offerings.options.*;
import org.eclipse.slm.service_management.service.rest.kubernetes.KubernetesManifestFile;
import org.json.JSONException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class KubernetesManifestFileParserTest {


    @DisplayName("Generate deployable manifest file, with service options")
    @Test
    public void generateDeployableManifestFileForServiceOffering() throws InvalidServiceOfferingDefinitionException, JSONException {

        // import strings
        String expectedFileContent = loadFileAsString("kubernetes_expected.yaml");
        String incomingFileContent = loadFileAsString("kubernetes_incoming.yaml");

        // create options
        var serviceOptionCategories = List.of(new ServiceOptionCategory(0, "Test", Arrays.asList(
                new ServiceOption("my-app|my-app", "MY_ENV_VAR1", "First Env Var", "my first env var", ServiceOptionType.ENVIRONMENT_VARIABLE, "defaultValue1.1", ServiceOptionValueType.STRING, false, false),
                new ServiceOption("my-app|my-app", "MY_ENV_VAR3", "Third Env Var", "my third env var", ServiceOptionType.ENVIRONMENT_VARIABLE, "defaultValue1.3", ServiceOptionValueType.STRING, false, false),
                new ServiceOption("my-second-app|my-app", "MY_ENV_VAR2", "Second Env Var", "my second env var", ServiceOptionType.ENVIRONMENT_VARIABLE, "defaultValue2.2", ServiceOptionValueType.STRING, false, false)
        )));

        // create deployment definition and offering
        var kubernetesDeploymentDefinition = new KubernetesDeploymentDefinition();
        kubernetesDeploymentDefinition.setManifestFile(incomingFileContent);
        var serviceOfferingVersion = new ServiceOfferingVersion(UUID.randomUUID(), null, "1.0.0", kubernetesDeploymentDefinition);
        serviceOfferingVersion.setServiceOptionCategories(serviceOptionCategories);

        // create service option, as they are created by an order from the ui
        var serviceOptionValues = List.of(
                new ServiceOptionValue("my-app|my-app|MY_ENV_VAR1", "testvalue1"),
                new ServiceOptionValue("my-app|my-app|MY_ENV_VAR3", "testvalue3"),
                new ServiceOptionValue("my-second-app|my-app|MY_ENV_VAR2", "testvalue2")
        );

        // DO PARSING
        var kubernetesManifestFile = KubernetesManifestFileParser.generateDeployableManifestFileForServiceOffering(serviceOfferingVersion, serviceOptionValues);

        this.assertManifests(expectedFileContent, kubernetesManifestFile.toConcatenatedString());
    }


    @DisplayName("Generate deployable manifest file, with crds, with service options")
    @Test
    public void generateDeployableManifestFileForServiceOfferingWithCrds() throws InvalidServiceOfferingDefinitionException, JSONException {

        // import strings
        String expectedFileContent = loadFileAsString("kubernetes_crd_env_expected.yaml");
        String incomingFileContent = loadFileAsString("kubernetes_crd_env_incoming.yaml");

        // create options
        var serviceOptionCategories = List.of(new ServiceOptionCategory(0, "Test", Arrays.asList(

                new ServiceOption("my-app|my-app", "MY_ENV_VAR1", "First Env Var",
                        "my first env var", ServiceOptionType.ENVIRONMENT_VARIABLE,
                        "defaultValue1.1", ServiceOptionValueType.STRING, false, false),
                new ServiceOption("my-second-app|my-app", "MY_ENV_VAR2", "Second Env Var",
                        "my second env var", ServiceOptionType.ENVIRONMENT_VARIABLE,
                        "defaultValue2.2", ServiceOptionValueType.STRING, false, false),

                new ServiceOption("$", "${{test_replace_1}}", "Second Env Var",
                        "my second env var", ServiceOptionType.STRING_REPLACE,
                        "defaultValue2.2", ServiceOptionValueType.STRING, false, false),
                new ServiceOption("$", "${{test_replace_2}}", "Second Env Var",
                        "my second env var", ServiceOptionType.STRING_REPLACE,
                        "defaultValue2.2", ServiceOptionValueType.STRING, false, false)
        )));

        // create deployment definition and offering
        var kubernetesDeploymentDefinition = new KubernetesDeploymentDefinition();
        kubernetesDeploymentDefinition.setManifestFile(incomingFileContent);
        var serviceOfferingVersion = new ServiceOfferingVersion(UUID.randomUUID(), null, "1.0.0", kubernetesDeploymentDefinition);
        serviceOfferingVersion.setServiceOptionCategories(serviceOptionCategories);

        // create service option, as they are created by an order from the ui
        var serviceOptionValues = List.of(
                new ServiceOptionValue("my-app|my-app|MY_ENV_VAR1", "testvalue.my-app.1"),
                new ServiceOptionValue("my-second-app|my-app|MY_ENV_VAR2", "testvalue.my-second-app.2"),

                new ServiceOptionValue("$|${{test_replace_1}}", "testvalue.w.1"),
                new ServiceOptionValue("$|${{test_replace_2}}", "testvalue.w.2")
        );

        // DO PARSING
        var kubernetesManifestFile = KubernetesManifestFileParser.generateDeployableManifestFileForServiceOffering(serviceOfferingVersion, serviceOptionValues);

        this.assertManifests(expectedFileContent, KubernetesManifestFileParser.manifestFinalizer(kubernetesManifestFile));
    }


    @DisplayName("Parse deployable manifest file")
    @Test
    public void parseManifestFileFromYaml() {

        // import strings
        String incomingFileContent = loadFileAsString("kubernetes_incoming.yaml");

        // Parse file
        KubernetesManifestFile kubernetesManifestFile = KubernetesManifestFileParser.parseManifestFile(incomingFileContent);

        // check kinds
        var parsedKinds = kubernetesManifestFile.getDefinitions().stream().map(obj -> ((KubernetesObject) obj.getParsed()).getKind()).collect(Collectors.toList());
        assertEquals(parsedKinds, Arrays.asList("Deployment", "Deployment", "Service"));

        // check deployments
        var parsedDeployments = kubernetesManifestFile.getDefinitions().stream().filter(definition -> definition.getParsed().getClass().getSimpleName().equalsIgnoreCase("V1Deployment")).toList();

        var parsedDeploymentNames = parsedDeployments.stream().map(obj -> ((V1Deployment) obj.getParsed()).getMetadata().getName()).collect(Collectors.toList());
        assertEquals(parsedDeploymentNames, Arrays.asList("my-app", "my-second-app"));

        var parsedContainerNames = parsedDeployments.stream().map(obj -> ((V1Deployment) obj.getParsed()).getSpec().getTemplate().getSpec().getContainers().stream().map(cont -> cont.getName()).collect(Collectors.toList())).collect(Collectors.toList());
        assertEquals(parsedContainerNames, Arrays.asList(List.of("my-app"), List.of("my-app")));

        var parsedEnvKeyNames = parsedDeployments.stream().map(obj -> ((V1Deployment) obj.getParsed()).getSpec().getTemplate().getSpec().getContainers().stream().map(cont -> cont.getEnv().stream().map(var -> var.getName()).collect(Collectors.toList())).collect(Collectors.toList())).collect(Collectors.toList());
        assertEquals(parsedEnvKeyNames, Arrays.asList(List.of(Arrays.asList("MY_ENV_VAR1", "MY_ENV_VAR2", "MY_ENV_VAR3")), List.of(Arrays.asList("MY_ENV_VAR1", "MY_ENV_VAR2", "MY_ENV_VAR3"))));

        var parsedEnvValueNames = parsedDeployments.stream().map(obj -> ((V1Deployment) obj.getParsed()).getSpec().getTemplate().getSpec().getContainers().stream().map(cont -> cont.getEnv().stream().map(var -> var.getValue()).collect(Collectors.toList())).collect(Collectors.toList())).collect(Collectors.toList());
        assertEquals(parsedEnvValueNames, Arrays.asList(List.of(Arrays.asList("my value", "my value", "my value")), List.of(Arrays.asList("my value", "my value", "my value"))));
    }


    @DisplayName("Parse deployable manifest file, with crds")
    @Test
    public void parseCustomResourceDefinitionFromYaml() throws JSONException {

        // load file and parse manifest
        String incomingFileContent = loadFileAsString("kubernetes_crd_incoming.yaml");
        KubernetesManifestFile kubernetesManifestFile = KubernetesManifestFileParser.parseManifestFile(incomingFileContent);

        this.assertManifests(incomingFileContent, KubernetesManifestFileParser.manifestFinalizer(kubernetesManifestFile));
    }


    @DisplayName("Parse deployable manifest file, with invalid yaml before replacement")
    @Test
    public void parseDefinitionFromInvalidYaml() throws JSONException, InvalidServiceOfferingDefinitionException {

        // load file and parse manifest
        String incomingFileContent = loadFileAsString("kubernetes_invalid_incoming.yaml");
        String expectedFileContent = loadFileAsString("kubernetes_invalid_expected.yaml");

        // create options
        var serviceOptionCategories = List.of(new ServiceOptionCategory(0, "Test", List.of(
                new ServiceOption("$", "${{image}}", "Image Definition",
                        "replaces complete image definition", ServiceOptionType.STRING_REPLACE,
                        "image: nginx:1.14.0", ServiceOptionValueType.STRING, false, false)
        )));

        // create deployment definition and offering
        var kubernetesDeploymentDefinition = new KubernetesDeploymentDefinition();
        kubernetesDeploymentDefinition.setManifestFile(incomingFileContent);
        var serviceOfferingVersion = new ServiceOfferingVersion(UUID.randomUUID(), null, "1.0.0", kubernetesDeploymentDefinition);
        serviceOfferingVersion.setServiceOptionCategories(serviceOptionCategories);

        // create service option, as they are created by an order from the ui
        var serviceOptionValues = List.of(
                new ServiceOptionValue("$|${{image}}", "image: nginx:1.14.0")
        );

        // DO PARSING
        var kubernetesManifestFile = KubernetesManifestFileParser.generateDeployableManifestFileForServiceOffering(serviceOfferingVersion, serviceOptionValues);

        this.assertManifests(expectedFileContent, KubernetesManifestFileParser.manifestFinalizer(kubernetesManifestFile));
    }


    private List<String> splitYamlToJsons(String yamlContent) {

        // use pure snakeyaml to separate yaml files, and use Kubernetes Dynamics to transform to JSON
        org.yaml.snakeyaml.Yaml snakeYaml = new org.yaml.snakeyaml.Yaml();
        return StreamSupport.stream(snakeYaml.loadAll(yamlContent).spliterator(), false)
                .map(obj -> Dynamics.fromYamlToJson(snakeYaml.dump(obj))).collect(Collectors.toList());
    }


    private void assertManifests(String expectedManifest, String actualManifest) throws JSONException {

        System.out.println("expected: " + splitYamlToJsons(expectedManifest));
        System.out.println("actual:   " + splitYamlToJsons(actualManifest));
        JSONAssert.assertEquals(
                splitYamlToJsons(expectedManifest).toString(),
                splitYamlToJsons(actualManifest).toString(),
                JSONCompareMode.LENIENT
        );
    }


    private static String loadFileAsString(String fileName) {

        ClassLoader classLoader = KubernetesManifestFileParserTest.class.getClassLoader();
        InputStream incomingInputStream = classLoader.getResourceAsStream(fileName);
        String incomingFileContent;
        try {
            assert incomingInputStream != null;
            incomingFileContent = IOUtils.toString(incomingInputStream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return incomingFileContent;
    }


//    private static List<Map<String, Object>> findSubtree(Object yamlObject, String searchKey) {
//        List<Map<String, Object>> results = new ArrayList<>();
//
//        if (yamlObject instanceof Map) {
//            Map<String, Object> yamlMap = (Map<String, Object>) yamlObject;
//
//            if (yamlMap.containsKey(searchKey)) {
//                results.add(yamlMap);
//            }
//
//            for (Object value : yamlMap.values()) {
//                results.addAll(findSubtree(value, searchKey));
//            }
//        } else if (yamlObject instanceof List) {
//            List<Object> yamlList = (List<Object>) yamlObject;
//
//            for (Object element : yamlList) {
//                results.addAll(findSubtree(element, searchKey));
//            }
//        }
//
//        return results;
//    }

}
