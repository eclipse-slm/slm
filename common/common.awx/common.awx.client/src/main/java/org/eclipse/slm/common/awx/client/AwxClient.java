package org.eclipse.slm.common.awx.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.module.kotlin.KotlinModule;
import jakarta.annotation.PostConstruct;
import org.eclipse.slm.common.awx.client.observer.JobFinalState;
import org.eclipse.slm.common.awx.client.observer.JobState;
import org.eclipse.slm.common.awx.model.*;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;
import reactor.retry.Repeat;

import javax.net.ssl.SSLException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class AwxClient {

    private static final Logger log = LoggerFactory.getLogger(AwxClient.class);

    private final String DEFAULT_ORGANISATION = "Service Lifecycle Management";
    private final String DEFAULT_INVENTORY = "empty";
    private final String DEFAULT_TEAM = "user";

    private final String DEFAULT_CONSUL_CREDENTIAL_TYPE_NAME = "Consul";
    private final String DEFAULT_VAULT_CREDENTIAL_TYPE_NAME = "HashiCorp Vault";

    public String awxUrl;

    public String awxHost;
    public String awxPort;

    @Value("${awx.username}")
    private String awxUsername;

    @Value("${awx.password}")
    private String awxPassword;

    private ObjectMapper objectMapper;

    private RestTemplate restTemplate;


    private List<String> finalStates = Stream.of(JobFinalState.values())
            .map(JobFinalState::name)
            .map(String::toLowerCase)
            .collect(Collectors.toList());

    private List<String> states = Stream.of(JobState.values())
            .map(JobState::name)
            .map(String::toLowerCase)
            .collect(Collectors.toList());

    @Autowired
    public AwxClient(
            @Value("${awx.scheme}") String scheme,
            @Value("${awx.host}") String host,
            @Value("${awx.port}") String port
    ) {
        this.awxHost = host;
        this.awxPort = port;
        this.awxUrl = scheme + "://" + host + ":" + port;
    }

    @PostConstruct
    public void init() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, SSLException {
        this.objectMapper = new ObjectMapper();
        var kotlinModule = new KotlinModule.Builder()
                .nullIsSameAsDefault(true)
                .build();
        this.objectMapper.registerModule(kotlinModule);
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        var mappingJacksonHttpMessageConverter = new MappingJackson2HttpMessageConverter();
        mappingJacksonHttpMessageConverter.setObjectMapper(this.objectMapper);

        this.restTemplate = new RestTemplateBuilder()
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .additionalMessageConverters(mappingJacksonHttpMessageConverter)
                .build();
        this.restTemplate.setRequestFactory(
                new HttpComponentsClientHttpRequestFactory()
        );
    }

    //region Organization
    public Organization createOrganization(OrganizationCreateRequest organizationCreateRequest) throws JsonProcessingException {
        HttpHeaders headers = getAdminAuthHeader();

        //Check if Organization exists already based on name
        Results<Organization> existingOrganization = getOrganizationByName(organizationCreateRequest.getName());

        Organization returnedOrganization = null;

        if(existingOrganization.getCount() == 0) {
            String url = this.getAwxApiUrl() + "/organizations/";

            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<OrganizationCreateRequest> httpEntity = new HttpEntity<>(organizationCreateRequest, headers);

            ResponseEntity<Organization> response
                    = restTemplate.postForEntity(url, httpEntity, Organization.class);

            returnedOrganization = response.getBody();
        } else {
            returnedOrganization = existingOrganization.getResults().stream().collect(Collectors.toList()).get(0);
        }

        return returnedOrganization;
    }

    public Results<Organization> getOrganizations() throws JsonProcessingException {
        ResponseEntity<String> response = this.restTemplate.exchange(
                this.getAwxApiUrl() +"/organizations/",
                HttpMethod.GET,
                new HttpEntity<>(getAdminAuthHeader()),
                String.class
        );

        var results = this.objectMapper.readValue(response.getBody(), new TypeReference<Results<Organization>>() {});
        return results;
    }

    public Organization getOrganizationById(int organizationId) {
        ResponseEntity<Organization> response = this.restTemplate.exchange(
                this.getAwxApiUrl() +"/organizations/"+organizationId+"/",
                HttpMethod.GET,
                new HttpEntity<>(getAdminAuthHeader()),
                Organization.class
        );

        return response.getBody();
    }

    public Results<Organization> getOrganizationByName(String name) throws JsonProcessingException {
        ResponseEntity<ObjectNode> response = this.restTemplate.exchange(
                this.getAwxApiUrl() +"/organizations/?name="+name,
                HttpMethod.GET,
                new HttpEntity<>(getAdminAuthHeader()),
                ObjectNode.class
        );
        var json = this.objectMapper.writeValueAsString(response.getBody());
        return this.objectMapper.readValue(json, new TypeReference<Results<Organization>>() {});
    }

    public Organization getDefaultOrganisation() throws JsonProcessingException {
        ResponseEntity<ObjectNode> response = this.restTemplate.exchange(
                this.getAwxApiUrl() + "/organizations/?name="+DEFAULT_ORGANISATION,
                HttpMethod.GET,
                new HttpEntity<>(getAdminAuthHeader()),
                ObjectNode.class
        );

        var results = getOrganizationByName(DEFAULT_ORGANISATION);

        if(results.getCount() == 0) {
            Organization createdOrganization = createOrganization(new OrganizationCreateRequest(
                    DEFAULT_ORGANISATION
            ));

            return createdOrganization;

        } else {
            return results.getResults().stream().collect(Collectors.toList()).get(0);
        }
    }
    //endregion

    //region Teams
    public Results<Team> getTeams() {

        return getTeamsFiltered("");
    }

    public Results<Team> getTeamsFiltered(String filter) {
        String filterExp = filter.equals("") ? "" : "?"+filter;
        ResponseEntity<Results<Team>> response = this.restTemplate.exchange(
                this.getAwxApiUrl() + "/teams/"+filterExp,
                HttpMethod.GET,
                new HttpEntity<>(getAdminAuthHeader()),
                new ParameterizedTypeReference<>() {}
        );

        return response.getBody();
    }

    public Team getDefaultTeam() throws JsonProcessingException {
        Organization defaultOrganization = this.getDefaultOrganisation();
        Results<Team> response = getTeamsFiltered("organization="+defaultOrganization.getId()+"&name="+DEFAULT_TEAM);

        if(response.getCount() != 0)
            return response.getResults().stream().collect(Collectors.toList()).get(0);

        Team defaultTeam = createDefaultTeam();

        return defaultTeam;
    }

    public Team createTeam(TeamCreateRequest teamCreateRequest) {
        HttpHeaders header = getAdminAuthHeader();
        HttpEntity<?> entity = new HttpEntity<>(teamCreateRequest,header);

        ResponseEntity<Team> response = restTemplate.postForEntity(
                this.getAwxApiUrl() + "/teams/",
                entity,
                Team.class
        );

        return response.getBody();
    }

    public Team createDefaultTeam() throws JsonProcessingException {
        Organization defaultOrganization = this.getDefaultOrganisation();
        TeamCreateRequest teamCreateRequest = new TeamCreateRequest(
                DEFAULT_TEAM,
                "",
                defaultOrganization.getId()
        );

        try {
            return createTeam(teamCreateRequest);
        } catch (HttpClientErrorException e) {
            log.info("Default team exists already => skip creation of default team");
            return getDefaultTeam();
        }
    }

    public void deleteTeam(int teamId) {
        String url = this.getAwxApiUrl() + "/teams/" + teamId + "/";

        ResponseEntity<ObjectNode> response = restTemplate.exchange(
                url,
                HttpMethod.DELETE,
                new HttpEntity<>(getAdminAuthHeader()),
                ObjectNode.class
        );

        return;
    }

    public Results<ObjectRole> getRolesOfTeam(int teamId) {

        ResponseEntity<Results<ObjectRole>> response = restTemplate.exchange(
                this.getAwxApiUrl() + "/teams/" + teamId + "/roles/",
                HttpMethod.GET,
                new HttpEntity<>(getAdminAuthHeader()),
                new ParameterizedTypeReference<>() {}
        );

        return response.getBody();
    }

    public void addRoleToTeam(int teamId, int roleId) {
        Map<String, Integer> body = new HashMap<>();
        body.put("id", roleId);

        ResponseEntity<ObjectNode> response = restTemplate.postForEntity(
                this.getAwxApiUrl() + "/teams/" + teamId + "/roles/",
                new HttpEntity<>(body, getAdminAuthHeader()),
                ObjectNode.class
        );
    }

    public void addProjectRoleToDefaultTeam(int projectId, ProjectObjectRoleName roleName) throws JsonProcessingException {
        Team team = getDefaultTeam();
        ObjectRole projectRole = getObjectRoleOfProjectByName(projectId, roleName);
        addRoleToTeam(
                team.getId(),
                projectRole.getId()
        );
    }

    public void addJobTemplateRoleToDefaultTeam(int jobTemplateId, JobTemplateObjectRoleName roleName) throws JsonProcessingException {
        Team team = getDefaultTeam();
        ObjectRole projectRole = getObjectRoleOfJobTemplateByName(jobTemplateId, roleName);
        addRoleToTeam(
                team.getId(),
                projectRole.getId()
        );
    }
    //endregion

    //region Inventory
    public Inventory createInventory(int organizationId, String inventoryName) throws JsonProcessingException {
        HttpHeaders headers = getAdminAuthHeader();
        InventoryDTOApiCreate inventoryDtoApiCreate = new InventoryDTOApiCreate(
                inventoryName,
                organizationId
        );

        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<InventoryDTOApiCreate> httpEntity = new HttpEntity<>(inventoryDtoApiCreate, headers);

        try {
            ResponseEntity<Inventory> response = restTemplate.postForEntity(
                    this.getAwxApiUrl() + "/inventories/",
                    httpEntity,
                    Inventory.class
            );

            return response.getBody();
        } catch(HttpClientErrorException e) {
            log.info("Skip create of default inventory, because it exists already");
            return getInventoryByName(inventoryName).getResults().stream().collect(Collectors.toList()).get(0);
        }
    }

    public Inventory createDefaultInventory() throws JsonProcessingException {
        Organization organization = getDefaultOrganisation();

        return createInventory(
                organization.getId(),
                DEFAULT_INVENTORY
        );
    }

    public Inventory getDefaultInventory() throws JsonProcessingException {
        HttpHeaders header = getAdminAuthHeader();
        HttpEntity<?> entity = new HttpEntity<>(header);

        ResponseEntity<ObjectNode> response = this.restTemplate.exchange(
                this.getAwxApiUrl() + "/inventories/?name="+DEFAULT_INVENTORY,
                HttpMethod.GET,
                entity,
                ObjectNode.class
        );

        var json = this.objectMapper.writeValueAsString(response.getBody());
        var inventoryQueryResult = this.objectMapper.readValue(json, new TypeReference<Results<Inventory>>() {});
        Inventory defaultInventory = null;

        if(inventoryQueryResult.getCount() == 0) {
            defaultInventory = this.createDefaultInventory();
        } else {
            defaultInventory = inventoryQueryResult.getResults().stream().collect(Collectors.toList()).get(0);
        }

        return defaultInventory;
    }

    public Results<Inventory> getInventories() throws JsonProcessingException {
        String url = this.getAwxApiUrl() + "/inventories/";

        ResponseEntity<Results<Inventory>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(null, getAdminAuthHeader()),
                new ParameterizedTypeReference<Results<Inventory>>() {}
        );

        return response.getBody();
    }

    public Results<Inventory> getInventoryByName(String inventoryName) throws JsonProcessingException {
        String url = this.getAwxApiUrl() + "/inventories/?name=" + inventoryName;

        ResponseEntity<Results<Inventory>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(null, getAdminAuthHeader()),
                new ParameterizedTypeReference<Results<Inventory>>() {}
        );

        return response.getBody();
    }

    public Inventory getInventory(int inventoryId) throws JsonProcessingException {
        String url = this.getAwxApiUrl() + "/inventories/" + inventoryId + "/";
        ResponseEntity<Inventory> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(null, getAdminAuthHeader()),
                Inventory.class
        );
        return response.getBody();
    }

    public void deleteInventory(int inventoryId) {
        ResponseEntity<ObjectNode> response = restTemplate.exchange(
                this.getAwxApiUrl() + "/inventories/" + inventoryId + "/",
                HttpMethod.DELETE,
                new HttpEntity<>(getAdminAuthHeader()),
                ObjectNode.class
        );
    }

    public Collection<Host> getHostsOfInventory(int inventoryId) throws JsonProcessingException {
        ResponseEntity<ObjectNode> response
                = restTemplate.getForEntity(this.getAwxApiUrl() + "/inventories/" + inventoryId + "/hosts/", ObjectNode.class);

        var json = this.objectMapper.writeValueAsString(response.getBody());
        var hostResults = this.objectMapper.readValue(json, new TypeReference<Results<Host>>() {});
        return hostResults.getResults();
    }
    //endregion

    //region Jobs
    public Results<Job> getJobs(String jwtToken, String username) {
        String accessToken = getAccessToken(jwtToken);
        String url = this.getAwxApiUrl() + "/jobs/?order_by=-id&created_by__username=" + username;

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        HttpEntity<String> httpEntity = new HttpEntity<>(null, headers);

        ResponseEntity<Results<Job>> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, new ParameterizedTypeReference<Results<Job>>() {});
        Results<Job> jobResults = response.getBody();
        return jobResults;
    }

    public Job getJob(int jobId) {
        String url = this.getAwxApiUrl() + "/jobs/"+jobId+"/";

        ResponseEntity<Job> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(getAdminAuthHeader()),
                Job.class
        );

        return response.getBody();
    }
    //endregion

    //region Projects
    public Results<Project> getProjects() {
        HttpHeaders header = getAdminAuthHeader();
        String url = this.getAwxApiUrl() + "/projects/";

        HttpEntity<String> httpEntity = new HttpEntity<>(null, header);
        ResponseEntity<Results<Project>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<Results<Project>>() {}
        );

        return response.getBody();
    }

    public ProjectUpdateRun updateProject(int projectId) {
        String url = this.getAwxApiUrl() + "/projects/" + projectId + "/update/";
        ResponseEntity<ProjectUpdateRun> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(getAdminAuthHeader()),
                ProjectUpdateRun.class
        );

        return response.getBody();
    }

    public void cancelProjectUpdate(int projectUpdateId) {
        String url = this.getAwxApiUrl() + "/project_updates/" + projectUpdateId + "/cancel/";
        ResponseEntity<ObjectNode> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(getAdminAuthHeader()),
                ObjectNode.class
        );
    }

    public ProjectUpdate getProjectUpdates(int projectId) {
        String url = this.getAwxApiUrl() + "/projects/" + projectId + "/";
        ResponseEntity<ProjectUpdate> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(getAdminAuthHeader()),
                ProjectUpdate.class
//                new ParameterizedTypeReference<Results<Project>>() {}
        );

        return response.getBody();
    }

    public Project getProjectFromId(int projectId) {
        HttpHeaders header = getAdminAuthHeader();
        String url = this.getAwxApiUrl() + "/projects/?id=" + projectId;

        HttpEntity<String> httpEntity = new HttpEntity<>(null, header);
        ResponseEntity<Results<Project>> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, new ParameterizedTypeReference<Results<Project>>() {});
        Results<Project> projectResults = response.getBody();

        return projectResults.getResults().stream().collect(Collectors.toList()).get(0);
    }

    public Results<Project> getProjectFromRepoAndBranch(String gitRepoUrl, String branch) {
        return getProjectFromRepoAndBranch(getAdminAuthHeader(), gitRepoUrl, branch);
    }

    private Results<Project> getProjectFromRepoAndBranch(HttpHeaders headers, String gitRepoUrl, String branch) {
        String url = this.getAwxApiUrl() + "/projects/?scm_type=git&scm_url__icontains=" + gitRepoUrl + "&scm_branch=" + branch;

        HttpEntity<String> httpEntity = new HttpEntity<>(null, headers);
        ResponseEntity<Results<Project>> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, new ParameterizedTypeReference<Results<Project>>() {});
        Results<Project> projectResults = response.getBody();

        return projectResults;
    }

    public Results<Project> getProjectFromCredential(
            int credentialId
    ) {
        String url = this.getAwxApiUrl() + "/projects/?credential=" + credentialId;

        ResponseEntity<Results<Project>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(null, getAdminAuthHeader()),
                new ParameterizedTypeReference<Results<Project>>() {}
        );
        Results<Project> projectResults = response.getBody();

        return projectResults;
    }

    public Results<Project> getProjectFromRepoBranchAndCredential(
            String scmUrl,
            String scmBranch,
            int credentialId
    ) {
        String url = this.getAwxApiUrl() + "/projects/?scm_type=git&scm_url__icontains=" + scmUrl + "&scm_branch=" + scmBranch + "&credential=" + credentialId;

        ResponseEntity<Results<Project>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(null, getAdminAuthHeader()),
                new ParameterizedTypeReference<Results<Project>>() {}
        );
        Results<Project> projectResults = response.getBody();

        return projectResults;
    }

    public Results<Project> getProjectFromRepoAndBranch(AwxCredential awxCredential, String gitRepoUrl, String branch) {
        HttpHeaders headers = new HttpHeaders();
        headers.addAll(getAuthHeader(awxCredential));

        return getProjectFromRepoAndBranch(headers, gitRepoUrl, branch);
    }

    public Results<ObjectRole> getObjectRolesOfProject(int projectId) {

        ResponseEntity<Results<ObjectRole>> response = restTemplate.exchange(
                this.getAwxApiUrl() + "/projects/" + projectId + "/object_roles/",
                HttpMethod.GET,
                new HttpEntity<>(getAdminAuthHeader()),
                new ParameterizedTypeReference<>() {}
        );

        return response.getBody();
    }

    public ObjectRole getObjectRoleOfProjectByName(int projectId, ProjectObjectRoleName roleName) {
        ResponseEntity<Results<ObjectRole>> response = restTemplate.exchange(
                this.getAwxApiUrl() + "/projects/" + projectId + "/object_roles/?search="+roleName.name(),
                HttpMethod.GET,
                new HttpEntity<>(getAdminAuthHeader()),
                new ParameterizedTypeReference<>() {}
        );

        return response.getBody().getResults().stream().collect(Collectors.toList()).get(0);
    }

    public Project createProject(ProjectDTOApiCreate project) throws JsonProcessingException {
        Organization defaultOrganization = getDefaultOrganisation();
        project.setOrganization(defaultOrganization.getId());

        HttpHeaders headers = getAdminAuthHeader();

        //Check if Project exists already based on url and branch/version
//        Results<Project> existingProjects = getProjectFromRepoAndBranch(headers, project.getScm_url(), project.getScm_branch());
//
//        Project returnedProject = null;

//        if(existingProjects.getCount() == 0) {
        try {
            String url = this.getAwxApiUrl() + "/projects/";

            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<ProjectDTOApiCreate> httpEntity = new HttpEntity<>(project, headers);

            ResponseEntity<Project> response
                    = restTemplate.postForEntity(url, httpEntity, Project.class);

            return response.getBody();
        } catch (HttpClientErrorException e) {
            log.info("Project exists already => skip creation of project");

            if(project.getCredential() != null) {
                return getProjectFromRepoBranchAndCredential(
                        project.getScm_url(),
                        project.getScm_branch(),
                        project.getCredential()
                ).getResults().stream().collect(Collectors.toList()).get(0);
            } else {
                return getProjectFromRepoAndBranch(
                        project.getScm_url(),
                        project.getScm_branch()
                ).getResults().stream().collect(Collectors.toList()).get(0);
            }
        }
//        } else {
//            returnedProject = existingProjects.getResults().stream().collect(Collectors.toList()).get(0);
//        }
//
//        return returnedProject;
    }

    public Project createProjectAndWait(ProjectDTOApiCreate project) throws SSLException, AwxProjectUpdateFailedException, JsonProcessingException {
        Results<Project> queryProject = getProjectFromRepoAndBranch(
                getAdminAuthHeader(),
                project.getScm_url(),
                project.getScm_branch()
        );

        Project createdProject = null;

        if(queryProject.getCount() == 0) {
            createdProject = createProject(project);
            Map<String, Object> currentJob = (Map<String, Object>) createdProject.getSummary_fields().get("current_job");
            int jobId = (int) currentJob.get("id");
            JobUpdate jobUpdate = waitForProjectUpdateUpdate(jobId);
        } else {
            log.info("Skipped creation of project because project exists already.");
            createdProject = queryProject.getResults().stream().collect(Collectors.toList()).get(0);
        }

        return createdProject;
    }

    public void deleteProject(int projectId) throws InterruptedException {
        int tries = 0;
        int tryLimit = 60;

        HttpHeaders headers = getAdminAuthHeader();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> httpEntity = new HttpEntity<>(headers);

        while(tries <= tryLimit) {
            try {
                ResponseEntity<ObjectNode> response = restTemplate.exchange(
                        this.getAwxApiUrl() + "/projects/" + projectId + "/",
                        HttpMethod.DELETE,
                        httpEntity,
                        ObjectNode.class
                );

                log.info("Delete awx project with id = " + projectId);
                break;
            } catch (HttpClientErrorException e) {
                tries++;
                TimeUnit.SECONDS.sleep(2);
                log.warn(e.getMessage());
            }
        }
    }

    public void deleteProject(String scmUrl, String scmBranch) {
        Results<Project> queryProject = getProjectFromRepoAndBranch(
                getAdminAuthHeader(),
                scmUrl,
                scmBranch
        );

        if(queryProject.getCount() > 0) {
            Project project = queryProject.getResults().stream().collect(Collectors.toList()).get(0);
            Results<JobTemplate> projectJobTemplateResults = getJobTemplateFromProjectId(project.getId());

            try {
                if(projectJobTemplateResults.getCount() == 0) {
                    deleteProject(project.getId());

                    if(project.getCredential() != null) {
                        Credential projectCredential = getCredentialById(project.getCredential());
                        Results<Project> projectsUsingCredential = getProjectFromCredential(projectCredential.getId());

                        if(projectsUsingCredential.getCount() == 0) {
                            deleteCredential(projectCredential.getId());
                        }
                    }
                } else {
                    log.error("Skip deletion of awx project with id = " + project.getId() + " because job templates are linked to this project.");
                }
            } catch(InterruptedException e) {
                log.error("Failed to delete awx project with id = " + project.getId());
            }
        }
    }
    //endregion

    //region JobTemplates
    public Results<JobTemplate> getJobTemplates() {
        HttpHeaders header = getAdminAuthHeader();
        ResponseEntity<Results<JobTemplate>> response = restTemplate.exchange(
                this.getAwxApiUrl() + "/job_templates/",
                HttpMethod.GET,
                new HttpEntity<>(header),
                new ParameterizedTypeReference<Results<JobTemplate>>() {}
        );

        return response.getBody();
    }

    public JobTemplate getJobTemplateById(int jobTemplateId) {
        String url = this.getAwxApiUrl() + "/job_templates/" + jobTemplateId + "/";

        ResponseEntity<JobTemplate> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(null, getAdminAuthHeader()),
                JobTemplate.class
        );

        return response.getBody();
    }

    public Results<JobTemplate> getJobTemplateByName(String name) {
        String url = this.getAwxApiUrl() + "/job_templates/?name=" + name;

        ResponseEntity<Results<JobTemplate>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(null, getAdminAuthHeader()),
                new ParameterizedTypeReference<Results<JobTemplate>>() {});
        Results<JobTemplate> jobTemplateResults = response.getBody();

        return jobTemplateResults;
    }

    public Results<JobTemplate> getJobTemplateFromProjectId(int projectId) {
        String url = this.getAwxApiUrl() + "/job_templates/?project=" + projectId;

        ResponseEntity<Results<JobTemplate>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(null, getAdminAuthHeader()),
                new ParameterizedTypeReference<Results<JobTemplate>>() {});
        Results<JobTemplate> jobTemplateResults = response.getBody();

        return jobTemplateResults;
    }

    public Results<JobTemplate> getJobTemplateFromProjectIdAndPlaybook(int projectId, String playbook) {
        String url = this.getAwxApiUrl() + "/job_templates/?project=" + projectId + "&playbook=" + playbook;

        ResponseEntity<Results<JobTemplate>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(null, getAdminAuthHeader()),
                new ParameterizedTypeReference<Results<JobTemplate>>() {});
        Results<JobTemplate> jobTemplateResults = response.getBody();

        return jobTemplateResults;
    }

    public Results<JobTemplate> getJobTemplateFromProjectIdAndPlaybook(AwxCredential awxCredential, int projectId, String playbook) {
        String url = this.getAwxApiUrl() + "/job_templates/?project=" + projectId + "&playbook=" + playbook;

        HttpHeaders headers = new HttpHeaders();
        headers.addAll(getAuthHeader(awxCredential));
//        headers.add("Authorization", "Bearer " + accessToken);
        HttpEntity<String> httpEntity = new HttpEntity<>(null, headers);

        ResponseEntity<Results<JobTemplate>> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, new ParameterizedTypeReference<Results<JobTemplate>>() {});
        Results<JobTemplate> projectResults = response.getBody();

        return projectResults;
    }

    public int runJobTemplate(AwxCredential awxCredential, String gitRepo, String branch, String playbook, ExtraVars extraVars) {
//        String accessToken = getAccessToken(jwtToken);

        log.info("Lookup AWX project based on " + gitRepo + " - " + branch + " - " + playbook);

        Results<Project> projectResults = getProjectFromRepoAndBranch(awxCredential, gitRepo, branch);
        Project selectedProject = projectResults.getResults().iterator().next();

        if(projectResults.getCount() > 1) {
            log.warn("More than one project has been found with repo '" + gitRepo + "' and branch '" + branch + "'");
        }

        log.info("Project with id = " + selectedProject.getId() + " and name = '" + selectedProject.getName() + "'");

        Results<JobTemplate> resultsJobTemplates = getJobTemplateFromProjectIdAndPlaybook(
                awxCredential,
                selectedProject.getId(),
                playbook
        );
        JobTemplate selectedJobTemplate = resultsJobTemplates.getResults().iterator().next();

        if(resultsJobTemplates.getCount() > 1) {
            log.warn("More than one job template has been found with repo id '" + selectedProject.getId() + "' and playbook '" + playbook + "'");
        }

        log.info("JobTemplate with id = " + selectedJobTemplate.getId() + " and name = '" + selectedJobTemplate.getName() + "'");

        return runJobTemplate(awxCredential, selectedJobTemplate.getId(), extraVars);
    }

    public int runJobTemplate(AwxCredential awxCredential, int jobTemplateId, ExtraVars extraVars) {
        String url = this.getAwxApiUrl() + "/job_templates/" + jobTemplateId + "/launch/";

        HttpHeaders headers = new HttpHeaders();
        headers.addAll(getAuthHeader(awxCredential));
//        headers.add("Authorization", "Bearer " + jwtToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ExtraVars> httpEntity = new HttpEntity<>(extraVars, headers);

        ResponseEntity<JobLaunch> response
                = restTemplate.postForEntity(url, httpEntity, JobLaunch.class);
        return response.getBody().getJob();
    }

    public JobTemplate createJobTemplate(String scmUrl, String scmBranch, String playbook, List<String> referencedJobTemplateCredentials, String defaultExecutionEnvironmentName)
            throws AwxProjectUpdateFailedException, SSLException, JsonProcessingException {
        var projectToCreate = new ProjectDTOApiCreate(
                scmUrl,
                scmBranch
        );
        var executionEnvironment = getExecutionEnvironmentByName(defaultExecutionEnvironmentName);
        if (executionEnvironment.isPresent()) {
            projectToCreate.setDefault_environment(executionEnvironment.get().getId());
        }
        Project project = createProjectAndWait(projectToCreate);

        JobTemplate createdJobTemplate = createJobTemplate(
                project.getId(),
                playbook,
                referencedJobTemplateCredentials
        );

        return createdJobTemplate;
    }

    public List<JobTemplate> createJobTemplates(String scmUrl, String scmBranch, List<String> playbooks, List<String> referencedJobTemplateCredentials) throws AwxProjectUpdateFailedException, SSLException, JsonProcessingException {
        List<JobTemplate> jobTemplateList = new ArrayList<>();
        for(String playbook : playbooks) {
            jobTemplateList.add(
                    createJobTemplate(scmUrl, scmBranch, playbook, referencedJobTemplateCredentials, "")
            );
        }
        return jobTemplateList;
    }

    public JobTemplate createJobTemplate(String scmUrl, String scmBranch, String playbook, Credential scmCredential, List<String> referencedJobTemplateCredentials, String defaultExecutionEnvironmentName) throws AwxProjectUpdateFailedException, SSLException, JsonProcessingException {
        var projectToCreate = new ProjectDTOApiCreate(
                scmUrl,
                scmBranch,
                scmCredential
        );
        var executionEnvironment = getExecutionEnvironmentByName(defaultExecutionEnvironmentName);
        if(executionEnvironment.isPresent()){
            projectToCreate.setDefault_environment(executionEnvironment.get().getId());
        }
        Project project = createProjectAndWait(projectToCreate);


        JobTemplate createdJobTemplate = createJobTemplate(
                project.getId(),
                playbook,
                referencedJobTemplateCredentials
        );

        return createdJobTemplate;
    }

    private Optional<ExecutionEnvironment> getExecutionEnvironmentByName(String name) {
        String url = this.getAwxApiUrl() + "/execution_environments/?search=" + name;

        ResponseEntity<Results<ExecutionEnvironment>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(null, getAdminAuthHeader()),
                new ParameterizedTypeReference<Results<ExecutionEnvironment>>() {});
        Results<ExecutionEnvironment> executionEnvironmentResults = response.getBody();
        if(executionEnvironmentResults.getCount() > 0){
            for (ExecutionEnvironment executionEnvironment : executionEnvironmentResults.getResults()) {
                if (name.equals(executionEnvironment.getName())) {
                    return Optional.of(executionEnvironment);
                }
            }
        }

        return Optional.empty();
    }

    public Optional<ExecutionEnvironment> createOrUpdateExecutionEnvironment(ExecutionEnvironmentCreate executionEnvironmentCreate) {
        String url = this.getAwxApiUrl() + "/execution_environments/";
        HttpHeaders headers = getAdminAuthHeader();

        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ExecutionEnvironmentCreate> httpEntity = new HttpEntity<>(executionEnvironmentCreate, headers);

        var ee = this.getExecutionEnvironmentByName(executionEnvironmentCreate.getName());
        if (ee.isEmpty()) {
            restTemplate.postForEntity(url, httpEntity, ExecutionEnvironment.class);

            return this.getExecutionEnvironmentByName(executionEnvironmentCreate.getName());
        }

        log.info("ExecutionEnvironment exists already => update ExecutionEnvironment");
        url += ee.get().getId() + "/";
        restTemplate.exchange(url, HttpMethod.PUT, httpEntity, Void.class);

        return this.getExecutionEnvironmentByName(executionEnvironmentCreate.getName());

    }

    public JobTemplate createJobTemplateAndAddExecuteRoleToDefaultTeam(
            String scmUrl,
            String scmBranch,
            String playbook,
            List<String> referencedJobTemplateCredentials,
            String defaultExecutionEnvironment) throws JsonProcessingException, AwxProjectUpdateFailedException, SSLException {
        JobTemplate jobTemplate = createJobTemplate(scmUrl, scmBranch, playbook, referencedJobTemplateCredentials, defaultExecutionEnvironment);

        addProjectRoleToDefaultTeam(
                jobTemplate.getProject(),
                ProjectObjectRoleName.Read
        );

        addJobTemplateRoleToDefaultTeam(
                jobTemplate.getId(),
                JobTemplateObjectRoleName.Execute
        );

        return jobTemplate;
    }

    public JobTemplate createJobTemplateAndAddExecuteRoleToDefaultTeamAddConsulVaultCredential(
            String scmUrl,
            String scmBranch,
            String playbook,
            List<String> referencedJobTemplateCredentials
    ) throws JsonProcessingException, AwxProjectUpdateFailedException, SSLException {
        JobTemplate jobTemplate = createJobTemplateAndAddExecuteRoleToDefaultTeam(
                scmUrl,
                scmBranch,
                playbook,
                referencedJobTemplateCredentials,
                "");

        Credential consulCredential = getCredentialByName(
                DEFAULT_CONSUL_CREDENTIAL_TYPE_NAME
        );
        Credential vaultCredential = getCredentialByName(
                DEFAULT_VAULT_CREDENTIAL_TYPE_NAME
        );

        addCredentialToJobTemplate(jobTemplate.getId(), consulCredential.getId());
        addCredentialToJobTemplate(jobTemplate.getId(), vaultCredential.getId());

        return jobTemplate;
    }

    public JobTemplate createJobTemplateAddExecuteRoleToDefaultTeamAddScmCredential(
            String scmUrl,
            String scmBranch,
            String playbook,
            String scmUsername,
            String scmPassword,
            List<String> referencedJobTemplateCredentials,
            String defaultExecutionEnvironment) throws JsonProcessingException, AwxProjectUpdateFailedException, SSLException {
        Credential scmCredential = createSourceControlCredentialForDefaultOrga(
                scmUsername,
                scmPassword,
                scmUrl
        );

        JobTemplate jobTemplate = createJobTemplate(
                scmUrl,
                scmBranch,
                playbook,
                scmCredential,
                referencedJobTemplateCredentials,
                defaultExecutionEnvironment
        );

        addProjectRoleToDefaultTeam(
                jobTemplate.getProject(),
                ProjectObjectRoleName.Read
        );

        addJobTemplateRoleToDefaultTeam(
                jobTemplate.getId(),
                JobTemplateObjectRoleName.Execute
        );

        return jobTemplate;
    }

    public JobTemplate createJobTemplateAddExecuteRoleToDefaultTeamAddScmCredentialAddConsulVaultCredential(
            String scmUrl,
            String scmBranch,
            String playbook,
            String username,
            String password,
            List<String> referencedJobTemplateCredentials,
            String defaultExecutionEnvironment
    ) throws JsonProcessingException, AwxProjectUpdateFailedException, SSLException {

        JobTemplate jobTemplate = createJobTemplateAddExecuteRoleToDefaultTeamAddScmCredential(
                scmUrl,
                scmBranch,
                playbook,
                username,
                password,
                referencedJobTemplateCredentials,
                defaultExecutionEnvironment);

        Credential consulCredential = getCredentialByName(
                DEFAULT_CONSUL_CREDENTIAL_TYPE_NAME
        );
        Credential vaultCredential = getCredentialByName(
                DEFAULT_VAULT_CREDENTIAL_TYPE_NAME
        );

        addCredentialToJobTemplate(jobTemplate.getId(), consulCredential.getId());
        addCredentialToJobTemplate(jobTemplate.getId(), vaultCredential.getId());

        return jobTemplate;
    }

    public JobTemplate createJobTemplate(int projectId, String playbook, List<String> referencedJobTemplateCredentials) throws JsonProcessingException {
        Results<JobTemplate> existingJobTemplatesResults = getJobTemplateFromProjectIdAndPlaybook(projectId, playbook);

        if(existingJobTemplatesResults.getCount() != 0) {
            log.info("Job Template with projectID='" + projectId + "' and playbook='" + playbook + "' exists already => Skip create.");

            return existingJobTemplatesResults.getResults().stream().collect(Collectors.toList()).get(0);
        } else {
            Organization defaultOrganization = getDefaultOrganisation();
            Inventory defaultInventory = getDefaultInventory();
            Project project = getProjectFromId(projectId);

            HttpHeaders headers = getAdminAuthHeader();

            JobTemplateDTOApiCreate jobTemplateDTOApiCreate = new JobTemplateDTOApiCreate(
                    defaultOrganization.getId(),
                    projectId,
                    defaultInventory.getId(),
                    project.getScm_url() + " - " + project.getScm_branch() + " - " + playbook,
                    playbook
            );

            String url = this.getAwxApiUrl() + "/job_templates/";

            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<JobTemplateDTOApiCreate> httpEntity = new HttpEntity<>(jobTemplateDTOApiCreate, headers);

            var createdJobTemplate = restTemplate.postForEntity(url, httpEntity, JobTemplate.class).getBody();

            if (referencedJobTemplateCredentials.size() > 0) {
                var credentials = this.getCredentials().getResults();
                for (var credential : credentials) {
                    if (referencedJobTemplateCredentials.contains(credential.getName())) {
                        addCredentialToJobTemplate(createdJobTemplate.getId(), credential.getId());
                    }
                }
            }

            return createdJobTemplate;
        }
    }

    public void updateJobTemplate(JobTemplate jobTemplate) {
        String url = this.getAwxApiUrl() + "/job_templates/" + jobTemplate.getId() + "/";

        restTemplate.exchange(
                url,
                HttpMethod.PATCH,
                new HttpEntity<>(jobTemplate, getAdminAuthHeader()),
                ObjectNode.class
        );
    }

    public void deleteJobTemplate(int jobTemplateId) throws InterruptedException {
        int tries = 0;
        int tryLimit = 30;

        HttpHeaders headers = getAdminAuthHeader();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> httpEntity = new HttpEntity<>(headers);

        while(tries <= tryLimit) {
            try {
                ResponseEntity<ObjectNode> response = restTemplate.exchange(
                        this.getAwxApiUrl() + "/job_templates/" + jobTemplateId + "/",
                        HttpMethod.DELETE,
                        httpEntity,
                        ObjectNode.class
                );
                break;
            } catch (HttpClientErrorException e) {
                tries++;
                TimeUnit.SECONDS.sleep(2);
                log.warn(e.getMessage());
            }
        }
    }

    public void deleteJobTemplate(String projectScmUrl, String projectScmBranch, String playbook)  {
        String jobTemplateName = projectScmUrl + " - " + projectScmBranch + " - " + playbook;

        Results<JobTemplate> jobTemplateResult = getJobTemplateByName(jobTemplateName);

        if(jobTemplateResult.getCount() > 0) {
            JobTemplate jobTemplate = jobTemplateResult.getResults().stream().collect(Collectors.toList()).get(0);
            try {
                deleteJobTemplate(jobTemplate.getId());
            } catch (InterruptedException e) {
                log.error("Failed to delete job template with id = " + jobTemplate.getId());
            }
        }

    }

    public Results<ObjectRole> getObjectRolesOfJobTemplate(int jobTemplateId) {
        ResponseEntity<Results<ObjectRole>> response = restTemplate.exchange(
                this.getAwxApiUrl() + "/job_templates/" + jobTemplateId + "/object_roles/",
                HttpMethod.GET,
                new HttpEntity<>(getAdminAuthHeader()),
                new ParameterizedTypeReference<>() {}
        );

        return response.getBody();
    }

    public ObjectRole getObjectRoleOfJobTemplateByName(int jobTemplateId, JobTemplateObjectRoleName roleName) {

        ResponseEntity<Results<ObjectRole>> response = restTemplate.exchange(
                this.getAwxApiUrl() + "/job_templates/" + jobTemplateId + "/object_roles/?search="+roleName.name(),
                HttpMethod.GET,
                new HttpEntity<>(getAdminAuthHeader()),
                new ParameterizedTypeReference<>() {}
        );

        return response.getBody().getResults().stream().collect(Collectors.toList()).get(0);
    }
    //endregion

    //region Surveys
    public Optional<Survey> getSurvey(int jobTemplateId) {
        String url = this.getAwxApiUrl() + "/job_templates/"+jobTemplateId+"/survey_spec/";
        ResponseEntity<SurveyDTOApi> response;
        try {
            response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(getAdminAuthHeader()),
                    SurveyDTOApi.class
            );
        } catch(RestClientException e) {
            return Optional.empty();
        }

        return Optional.of(new Survey(response.getBody()));
    }

    public void createSurvey(int jobTemplateId, Survey survey) {
        String url = this.getAwxApiUrl() + "/job_templates/"+jobTemplateId+"/survey_spec/";
        HttpHeaders headers = getAdminAuthHeader();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<SurveyDTOApi> httpEntity = new HttpEntity<>(new SurveyDTOApi(survey), headers);

        var response = restTemplate.postForEntity(url, httpEntity, ObjectNode.class).getBody();
    }

    public void enableSurvey(int jobTemplateId) {
        JobTemplate jobTemplate = getJobTemplateById(jobTemplateId);

        jobTemplate.setSurvey_enabled(true);

        updateJobTemplate(jobTemplate);
    }

    public void deleteSurvey(int jobTemplateId) {
        String url = this.getAwxApiUrl() + "/job_templates/"+jobTemplateId+"/survey_spec/";
        restTemplate.exchange(
                url,
                HttpMethod.DELETE,
                new HttpEntity<>(getAdminAuthHeader()),
                ObjectNode.class
        );
    }
    //endregion

    //region Credentials
    public Results<Credential> getCredentials() {
        ResponseEntity<Results<Credential>> response = restTemplate.exchange(
                this.getAwxApiUrl() + "/credentials/",
                HttpMethod.GET,
                new HttpEntity<>(getAdminAuthHeader()),
                new ParameterizedTypeReference<>() {}
        );

        return response.getBody();
    }

    public Credential getCredentialByName(String name) {
        ResponseEntity<Results<Credential>> response = restTemplate.exchange(
                this.getAwxApiUrl() + "/credentials/?name="+name,
                HttpMethod.GET,
                new HttpEntity<>(getAdminAuthHeader()),
                new ParameterizedTypeReference<>() {}
        );

        return response.getBody().getResults().stream().collect(Collectors.toList()).get(0);
    }

    public Credential getCredentialById(int id) {
        ResponseEntity<Credential> response = restTemplate.exchange(
                this.getAwxApiUrl() + "/credentials/"+id+"/",
                HttpMethod.GET,
                new HttpEntity<>(getAdminAuthHeader()),
                new ParameterizedTypeReference<>() {}
        );

        return response.getBody();
    }

    public Credential getCredentialByUrlAndUsername(String url, String username) {
        return getCredentialByName(url+" - "+username);
    }

    public void addCredentialToJobTemplate(int jobTemplateId, int credentialId) {
        HttpHeaders headers = getAdminAuthHeader();

        String url = this.getAwxApiUrl() + "/job_templates/"+jobTemplateId+"/credentials/";
        HashMap<String, Integer> body = new HashMap<>();
        body.put("id", credentialId);

        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<HashMap<String, Integer>> httpEntity = new HttpEntity<>(body, headers);

        try {
            restTemplate.postForEntity(url, httpEntity, ObjectNode.class);
        } catch(HttpClientErrorException e) {
            log.warn("Not able to assign credential with id="+credentialId+" to job template with id="+jobTemplateId);
            log.warn(e.getMessage());
        }
    }

    public Credential createCredential(CredentialDTOApiCreate credentialDTOApiCreate) {
        HttpHeaders headers = getAdminAuthHeader();

        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CredentialDTOApiCreate> httpEntity = new HttpEntity<>(credentialDTOApiCreate, headers);

        try {
            ResponseEntity<Credential> response = restTemplate.postForEntity(
                    this.getAwxApiUrl() + "/credentials/",
                    httpEntity,
                    Credential.class
            );

            return response.getBody();
        } catch (HttpClientErrorException e) {
            log.info("Credential for user/url exists already => update credential");

            Credential existingCredential = getCredentialByName(
                    credentialDTOApiCreate.getName()
            );

            return updateCredential(
                existingCredential.getId(),
                new CredentialDTOApiUpdate(
                        credentialDTOApiCreate.getName(),
                        credentialDTOApiCreate.getDescription(),
                        credentialDTOApiCreate.getOrganization(),
                        credentialDTOApiCreate.getCredential_type(),
                        credentialDTOApiCreate.getInputs()
                )
            );
        }
    }

    public Credential updateCredential(int credentialId, CredentialDTOApiUpdate credentialDTOApiUpdate) {
        HttpHeaders headers = getAdminAuthHeader();

        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CredentialDTOApiUpdate> httpEntity = new HttpEntity<>(credentialDTOApiUpdate, headers);

        ResponseEntity<Credential> response = restTemplate.exchange(
            this.getAwxApiUrl() + "/credentials/"+credentialId+"/",
                HttpMethod.PUT,
                httpEntity,
                Credential.class
        );

        return response.getBody();
    }

    public void deleteCredential(int credentialId) {
        HttpHeaders headers = getAdminAuthHeader();

        ResponseEntity<ObjectNode> response = restTemplate.exchange(
                this.getAwxApiUrl() + "/credentials/"+credentialId+"/",
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                ObjectNode.class
        );

        log.info("Delete awx credential with id = " + credentialId);
    }

    public Credential createSourceControlCredentialForDefaultOrga(
            String username,
            String password,
            String url
    ) throws JsonProcessingException {
        CredentialType credentialType = getCredentialTypeByName(CredentialTypeName.SOURCE_CONTROL);
        Organization defaultOrga = getDefaultOrganisation();

        CredentialDTOApiCreate credentialDtoApiCreate = new CredentialDTOApiCreate(
                url+" - "+username,
                "",
                defaultOrga.getId(),
                credentialType.getId(),
                new HashMap<String, String>() {{
                    put("username", username);
                    put("password", password);
                }}

        );

        return createCredential(credentialDtoApiCreate);
    }

    public CredentialType createCredentialType(CredentialTypeDTOApiCreate credentialTypeDTOApiCreate) {
        HttpHeaders headers = getAdminAuthHeader();

        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CredentialTypeDTOApiCreate> httpEntity = new HttpEntity<>(credentialTypeDTOApiCreate, headers);

        try {
            ResponseEntity<CredentialType> response = restTemplate.postForEntity(
                    this.getAwxApiUrl() + "/credential_types/",
                    httpEntity,
                    CredentialType.class
            );

            return response.getBody();
        } catch (HttpClientErrorException e) {
            log.info("CredentialType exists already => skip creation of credential type");

            return getCustomCredentialTypeByName(credentialTypeDTOApiCreate.getName());
        }
    }

    public Results<CredentialType> getCredentialTypes() {
        ResponseEntity<Results<CredentialType>> response = restTemplate.exchange(
                this.getAwxApiUrl() + "/credential_types/",
                HttpMethod.GET,
                new HttpEntity<>(getAdminAuthHeader()),
                new ParameterizedTypeReference<>() {}
        );

        return response.getBody();
    }

    public CredentialType getCredentialTypeByName(CredentialTypeName credentialTypeName) {
        return getCustomCredentialTypeByName(credentialTypeName.getPrettyName());
    }

    public CredentialType getCustomCredentialTypeByName(String customCredentialTypeName) {
        ResponseEntity<Results<CredentialType>> response = restTemplate.exchange(
                this.getAwxApiUrl() + "/credential_types/?name="+customCredentialTypeName,
                HttpMethod.GET,
                new HttpEntity<>(getAdminAuthHeader()),
                new ParameterizedTypeReference<>() {}
        );

        return response.getBody().getResults().stream().collect(Collectors.toList()).get(0);
    }
    //endregion

    //region JobUpdate
    private JobUpdate
    waitForProjectUpdateUpdate(int jobId) throws SSLException, AwxProjectUpdateFailedException {
        List<String> runningStates = Arrays.asList(
                JobState.PENDING.name().toLowerCase(),
                JobState.RUNNING.name().toLowerCase()
        );
        JobUpdate finalJobUpdate = null;


        for(int i = 0; i < 3; i++) {
            log.info("Start observing job with id '" + jobId + "'");

            finalJobUpdate = getWebClient().get()
                .uri("/project_updates/{id}/", jobId)
                .headers(headers -> headers.setBasicAuth(awxUsername, awxPassword))
                .retrieve()
                .bodyToMono(JobUpdate.class)
//                .repeatWhen(longFlux -> Flux.interval(Duration.ofSeconds(2)))
                .repeatWhen(Repeat.times(30).fixedBackoff(Duration.ofSeconds(2)))
                .onBackpressureDrop()
                .takeUntil(jobUpdate -> finalStates.contains(jobUpdate.getStatus()))
                .blockLast(Duration.ofMinutes(3L));

            log.info("Stop observing job with id '" + jobId + "'");

            if(!runningStates.contains( finalJobUpdate.getStatus() )) {
                log.info("Finale State: " + finalJobUpdate.getStatus());
                break;
            }

            log.info("Cancel job update with id '" + finalJobUpdate.getId() + "'");
            cancelProjectUpdate(finalJobUpdate.getId());

            log.info("Cancel job update with id '" + finalJobUpdate.getId() + "'");
            jobId = updateProject(finalJobUpdate.getProject()).getId();
        }


        List<String> failedStates = Arrays.asList(
                JobState.FAILED.name().toLowerCase(),
                JobState.ERROR.name().toLowerCase(),
                JobState.CANCELED.name().toLowerCase()
        );

        String finalJobStatus = finalJobUpdate.getStatus().toLowerCase();

        if(failedStates.contains(finalJobStatus)) {
            throw new AwxProjectUpdateFailedException(
                    "Project Update with ID '"+jobId+"' failed with status '"+finalJobStatus+"'",
                    finalJobUpdate
            );
        }

        return finalJobUpdate;
    }

    public Job waitForJobUpdate(int jobId) throws AwxProjectUpdateFailedException, SSLException {
        log.info("Start observing job with id '" + jobId + "'");
        Job finalJobUpdate = getWebClient().get()
                .uri("/jobs/{id}/", jobId)
                .headers(headers -> headers.setBasicAuth(awxUsername, awxPassword))
                .retrieve()
                .bodyToMono(Job.class)
                .repeatWhen(longFlux -> Flux.interval(Duration.ofSeconds(2)))
                .onBackpressureDrop()
                .takeUntil(jobUpdate -> finalStates.contains(jobUpdate.getStatus()))
                .blockLast(Duration.ofMinutes(3L));

        List<String> failedStates = Arrays.asList(
                JobState.FAILED.name().toLowerCase(),
                JobState.ERROR.name().toLowerCase(),
                JobState.CANCELED.name().toLowerCase()
        );

        log.info("Stop observing job with id '" + jobId + "'");
        log.info("Finale State: " + finalJobUpdate.getStatus());

        String finalJobStatus = finalJobUpdate.getStatus().toLowerCase();

        if(failedStates.contains(finalJobStatus)) {
            log.error("Project Update with ID '"+jobId+"' failed with status '"+finalJobStatus+"'");
        }

        return finalJobUpdate;
    }
    //endregion

    public String getAwxUrl() {
        return awxUrl;
    }

    private String getAwxApiUrl()
    {
        return awxUrl + "/api/v2";
    }
    private WebClient getWebClient() throws SSLException {
        SslContext sslContext = SslContextBuilder
                .forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                .build();
        TcpClient tcpClient = TcpClient.create().secure(sslContextSpec -> sslContextSpec.sslContext(sslContext));
        HttpClient httpClient = HttpClient.from(tcpClient);
        var webClient = WebClient.builder()
                .baseUrl(awxUrl + "/api/v2")
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();

        return webClient;
    }

    public String getAccessToken(String jwtToken) {
        String url = awxUrl + "/jwt/token/";

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + jwtToken);
        HttpEntity<String> httpEntity = new HttpEntity<>(null, headers);

        RestTemplate newRestTemplate = new RestTemplate();
        String accessToken = newRestTemplate.postForObject(url, httpEntity, String.class);

        return accessToken;
    }

    private LinkedMultiValueMap<String, String> getAuthHeader(AwxCredential awxCredential) {
        LinkedMultiValueMap<String, String> linkedMultiValueMap = new LinkedMultiValueMap();
        if(awxCredential.jwtAuthenticationToken != null) {
            linkedMultiValueMap.add(
                    HttpHeaders.AUTHORIZATION,
                    "Bearer " +
                            getAccessToken(awxCredential.jwtAuthenticationToken.getToken().getTokenValue())
            );
        } else {
            String str = (awxCredential.username == null ? "" : awxCredential.username) + ":" + (awxCredential.password == null ? "" : awxCredential.password);
            String encodedStr = new Base64().encodeAsString(str.getBytes());

            linkedMultiValueMap.add(
                    "Authorization",
                    "Basic " + encodedStr
            );
        }

        return linkedMultiValueMap;
    }

    private HttpHeaders getAdminAuthHeader() {
        String plainCreds = awxUsername+":"+awxPassword;
        String base64Creds = new String(Base64.encodeBase64(plainCreds.getBytes()));

        HttpHeaders header = new HttpHeaders();
        header.add("Authorization", "Basic " + base64Creds);

        return header;
    }

    public void setAwxPort(int port) {
        try {
            this.awxPort = String.valueOf(port);
            URL initialUrl = new URL(awxUrl);
            URL newURL = new URL(initialUrl.getProtocol(), initialUrl.getHost(), port, initialUrl.getFile());

            awxUrl = newURL.toString();
        } catch(MalformedURLException e) {
            e.printStackTrace();
        }
    }
}
