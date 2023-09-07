package org.eclipse.slm.service_management.service.rest.docker_compose;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.eclipse.slm.service_management.model.exceptions.ServiceOptionNotFoundException;
import org.eclipse.slm.service_management.model.offerings.ServiceOfferingVersion;
import org.eclipse.slm.service_management.model.offerings.docker.compose.DockerComposeDeploymentDefinition;
import org.eclipse.slm.service_management.model.offerings.exceptions.InvalidServiceOfferingDefinitionException;
import org.eclipse.slm.service_management.model.offerings.options.ServiceOptionType;
import org.eclipse.slm.service_management.model.offerings.options.ServiceOptionValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.util.*;

public final class DockerComposeFileParser {

    public final static Logger LOG = LoggerFactory.getLogger(DockerComposeFileParser.class);

    public static DockerComposeFile parseComposeFile(String composeFileContentYaml) throws JsonProcessingException {
        var objectMapper = new ObjectMapper(new YAMLFactory());
        objectMapper.findAndRegisterModules();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(DockerComposeFileVolume.class, new DockerComposeFileVolumeDeserializer());
        module.addDeserializer(DockerComposeFileEnvironment.class, new ArrayMapNodeDeserializer(DockerComposeFileEnvironment.class));
        module.addDeserializer(DockerComposeFileLabels.class, new ArrayMapNodeDeserializer(DockerComposeFileLabels.class));
        module.addDeserializer(DockerComposeFileDependsOn.class, new DockerComposeFileDependsOnDeserializer());
        objectMapper.registerModule(module);
        var dockerComposeFile = objectMapper.readValue(composeFileContentYaml, DockerComposeFile.class);

        return dockerComposeFile;
    }

    public static Map<String, Object> composeFileToYAML (DockerComposeFile dockerComposeFile) throws JsonProcessingException {
        var objectMapper = new ObjectMapper(new YAMLFactory());
        objectMapper.findAndRegisterModules();
        SimpleModule module = new SimpleModule();
        module.addSerializer(DockerComposeFileDependsOn.class, new DockerComposeFileDependsOnSerializer());
        objectMapper.registerModule(module);

        var dockerComposeFileAsString = objectMapper.writeValueAsString(dockerComposeFile);

        Yaml yaml = new Yaml();
        Map<String, Object> dockerComposeYaml = yaml.load(dockerComposeFileAsString);

        return dockerComposeYaml;
    }

    public static DockerComposeFile generateDeployableComposeFileForServiceOffering(
            ServiceOfferingVersion serviceOfferingVersion,
            Collection<ServiceOptionValue> serviceOptionValues)
            throws JsonProcessingException, ServiceOptionNotFoundException, InvalidServiceOfferingDefinitionException {
        if (!(serviceOfferingVersion.getDeploymentDefinition() instanceof DockerComposeDeploymentDefinition)) {
            throw new InvalidServiceOfferingDefinitionException("Compose file can only be generated for DockerComposeDeploymentDefinition and not for '"
                    + serviceOfferingVersion.getDeploymentDefinition().getClass() + "'");
        }
        var deploymentDefinition = (DockerComposeDeploymentDefinition) serviceOfferingVersion.getDeploymentDefinition();
        var composeFileContent = deploymentDefinition.getComposeFile();

        // Filter service option values by service option type
        var envVarServiceOptionValues =
                serviceOfferingVersion.filterServiceOptionValuesByOptionType(serviceOptionValues,
                        ServiceOptionType.ENVIRONMENT_VARIABLE);
        var portMappingsServiceOptionValues =
                serviceOfferingVersion.filterServiceOptionValuesByOptionType(serviceOptionValues,
                        ServiceOptionType.PORT_MAPPING);
        var volumeServiceOptionValues =
                serviceOfferingVersion.filterServiceOptionValuesByOptionType(serviceOptionValues,
                        ServiceOptionType.VOLUME);
        var labelServiceOptionValues =
                serviceOfferingVersion.filterServiceOptionValuesByOptionType(serviceOptionValues,
                        ServiceOptionType.LABEL);

        // 1) Replace variable values in .env file with values from service options
        var dotEnvFile = deploymentDefinition.getDotEnvFile();
        dotEnvFile = DockerComposeFileParser.replaceServiceOptionValuesInEnvFile(dotEnvFile, envVarServiceOptionValues, serviceOfferingVersion);
        // 2) Replace variable values in env files (e.g. env.list) with values from service options
        var envFiles = new HashMap<String, String>();
        for (var envFile : deploymentDefinition.getEnvFiles().entrySet()) {
            envFiles.put(envFile.getKey(), DockerComposeFileParser
                    .replaceServiceOptionValuesInEnvFile(envFile.getValue(), envVarServiceOptionValues, serviceOfferingVersion));
        }
        // 3) Merge .env and other env files with compose file
        var composeFile = DockerComposeFileParser.mergeComposeFileWithEnvFiles(composeFileContent, dotEnvFile, envFiles);

        // 4) Replace service option values in compose file...
        // 4.1) ...  for volumes
        composeFile = DockerComposeFileParser.replaceServiceOptionValuesForVolumes(volumeServiceOptionValues, composeFile);
        // 4.2) ...  for port mappings
        composeFile = DockerComposeFileParser.replaceServiceOptionValuesForPortMappings(portMappingsServiceOptionValues, composeFile);
        // 4.3) ...  for labels
        composeFile = DockerComposeFileParser.replaceServiceOptionValuesForLabels(labelServiceOptionValues, composeFile);
        // 4.4) ...  for environment variables
        composeFile = DockerComposeFileParser.replaceServiceOptionValuesForEnvironmentVariables(envVarServiceOptionValues, composeFile);

        return composeFile;
    }

    public static String replaceServiceOptionValuesInEnvFile(
            String envFile, List<ServiceOptionValue> serviceOptionValues, ServiceOfferingVersion serviceOfferingVersion)
            throws ServiceOptionNotFoundException {
        var newEnvFile = "";

        // Parse key value pairs from env files
        var envFileKeyValuePairs = DockerComposeFileParser.getKeyValuePairs(envFile);
        for (var serviceOptionValue : serviceOptionValues) {
            var serviceOption =  serviceOfferingVersion.getServiceOptionOfValue(serviceOptionValue);
            if (envFileKeyValuePairs.containsKey(serviceOption.getKey()))
            {
                envFileKeyValuePairs.put(serviceOption.getKey(), serviceOptionValue.getValue());
            }
        }

        // Generate env file from key value pairs
        for (var envFileKeyValuePair : envFileKeyValuePairs.entrySet()) {
            newEnvFile += envFileKeyValuePair.getKey() + "=" + envFileKeyValuePair.getValue() + "\n";
        }

        return newEnvFile;
    }

    public static DockerComposeFile mergeComposeFileWithEnvFiles(String composeFileYaml, String dotEnvFile, Map<String, String> envFiles) throws JsonProcessingException {
        // Replace variables from .env file in docker compose file content string
        var dotEnvVariables = DockerComposeFileParser.getKeyValuePairs(dotEnvFile);
        for (var dotEnvVariable : dotEnvVariables.entrySet())
        {
            composeFileYaml = composeFileYaml.replace("${" + dotEnvVariable.getKey() + "}", dotEnvVariable.getValue().toString());
        }

        // Parse docker compose file content
        var objectMapper = new ObjectMapper(new YAMLFactory());
        objectMapper.findAndRegisterModules();
        var dockerComposeFile = parseComposeFile(composeFileYaml);

        // Parse key value pairs from env files
        var envFileKeyValuePairs = new HashMap<String, Map<String, Object>>();
        for (var envFile : envFiles.entrySet())
        {
            var keyValuePairs = DockerComposeFileParser.getKeyValuePairs(envFile.getValue());
            envFileKeyValuePairs.put(envFile.getKey(), keyValuePairs);
        }

        // Replace env_file sections of services with corresponding content of env file
        for (var serviceEntry : dockerComposeFile.getServices().entrySet())
        {
            var dockerComposeService = serviceEntry.getValue();
            for (var envFileReference : dockerComposeService.getEnvFiles())
            {
                if (envFileKeyValuePairs.containsKey(envFileReference))
                {
                    for (var envVarInFile : envFileKeyValuePairs.get(envFileReference).entrySet())
                    {
                        dockerComposeService.getEnvironment().put(envVarInFile.getKey(), envVarInFile.getValue());
                    }
                }
                else
                {
                    throw new IllegalArgumentException("Env file '" + envFileReference + "' referenced in service '"
                            + serviceEntry.getKey() + "', but env file was not provided in arguments");
                }
            }
            dockerComposeService.setEnvFiles(new ArrayList<>());
        }

        return dockerComposeFile;
    }

    public static Map<String, Object> getKeyValuePairs(String input)
    {
        var result = new HashMap<String, Object>();

        var lines = input.split("\\R");
        for (var line : lines)
        {
            if (line.length() > 0 && !line.startsWith("#"))
            {
                String[] kv = line.split("=");
                String key = kv[0].trim();
                String value = "";
                if (kv.length == 2) {
                    value = kv[1].trim();
                }
                result.put(key, value);
            }
        }

        return result;
    }

    public static DockerComposeFile replaceServiceOptionValuesForVolumes(List<ServiceOptionValue> serviceOptionValues, DockerComposeFile dockerComposeFile) {

        for (var service : dockerComposeFile.getServices().entrySet())
        {
            for (int i = 0; i < service.getValue().getVolumes().size(); i++)
            {
                var volume = service.getValue().getVolumes().get(i);
                var serviceOptionId = service.getKey() + "|" + volume.getTarget();
                var optionalServiceOptionValue = serviceOptionValues
                        .stream().filter(sov -> sov.getServiceOptionId().equals(serviceOptionId)).findAny();
                if (optionalServiceOptionValue.isPresent())
                {
                    var serviceOptionValue = optionalServiceOptionValue.get();
                    volume = new DockerComposeFileVolume(serviceOptionValue.getValue() + ":" + volume.getTarget());
                    service.getValue().getVolumes().set(i, volume);
                }
            }
        }

        return dockerComposeFile;
    }

    public static DockerComposeFile replaceServiceOptionValuesForLabels(List<ServiceOptionValue> serviceOptionValues, DockerComposeFile dockerComposeFile) {
        for (var service : dockerComposeFile.getServices().entrySet())
        {
            for (var labelEntry : service.getValue().getLabels().entrySet()) {
                var labelVal = labelEntry.getValue();
                var serviceOptionId = service.getKey() + "|" + labelEntry.getKey();
                var optionalServiceOptionValue = serviceOptionValues
                        .stream().filter(sov -> sov.getServiceOptionId().equals(serviceOptionId)).findAny();
                if (optionalServiceOptionValue.isPresent())
                {
                    var serviceOptionValue = optionalServiceOptionValue.get();
                    service.getValue().getLabels().put(labelEntry.getKey(), serviceOptionValue.getValue());
                }
            }
        }

        return dockerComposeFile;
    }

    public static DockerComposeFile replaceServiceOptionValuesForEnvironmentVariables(List<ServiceOptionValue> serviceOptionValues, DockerComposeFile dockerComposeFile) {
        for (var service : dockerComposeFile.getServices().entrySet()) {
            for (var envVarEntry : service.getValue().getEnvironment().entrySet()) {
                var serviceOptionId = service.getKey() + "|" + envVarEntry.getKey();
                var optionalServiceOptionValue = serviceOptionValues
                        .stream().filter(sov -> sov.getServiceOptionId().equals(serviceOptionId)).findAny();
                if (optionalServiceOptionValue.isPresent()) {
                    var serviceOptionValue = optionalServiceOptionValue.get();
                    service.getValue().getEnvironment().put(envVarEntry.getKey(), serviceOptionValue.getValue());
                }
            }
        }

        return dockerComposeFile;
    }

    public static DockerComposeFile replaceServiceOptionValuesForPortMappings(List<ServiceOptionValue> serviceOptionValues, DockerComposeFile dockerComposeFile) {
        for (var service : dockerComposeFile.getServices().entrySet())
        {
            for (int i = 0; i < service.getValue().getPorts().size(); i++)
            {
                var portMapping = service.getValue().getPorts().get(i);
                var containerPort = portMapping.split(":")[1];
                var serviceOptionId = service.getKey() + "|" + containerPort;
                var optionalServiceOptionValue = serviceOptionValues
                        .stream().filter(sov -> sov.getServiceOptionId().equals(serviceOptionId)).findAny();
                if (optionalServiceOptionValue.isPresent())
                {
                    var serviceOptionValue = optionalServiceOptionValue.get();
                    portMapping = serviceOptionValue.getValue() + ":" + containerPort;
                    service.getValue().getPorts().set(i, portMapping);
                }
            }
        }

        return dockerComposeFile;
    }

    public static HashMap<String, String> getServiceMetaData(DockerComposeFile deployableComposeFile) {
        var serviceMetaData = new HashMap<String, String>();

        return serviceMetaData;
    }

    public static List<Integer> getServicePorts(DockerComposeFile deployableComposeFile) {
        var exposedPorts = new ArrayList<Integer>();
        for (var service : deployableComposeFile.getServices().values()) {
            for (var portMapping : service.getPorts()) {
                var hostPort = portMapping.split(":")[0];
                exposedPorts.add(Integer.parseInt(hostPort));
            }
        }

        return exposedPorts;
    }
}
