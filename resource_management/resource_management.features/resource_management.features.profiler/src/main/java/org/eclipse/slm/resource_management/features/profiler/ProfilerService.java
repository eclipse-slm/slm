package org.eclipse.slm.resource_management.features.profiler;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.eclipse.slm.awx.client.AwxClient;
import org.eclipse.slm.awx.client.AwxCredential;
import org.eclipse.slm.awx.client.AwxProjectUpdateFailedException;
import org.eclipse.slm.awx.client.observer.*;
import org.eclipse.slm.awx.model.ExtraVars;
import org.eclipse.slm.awx.model.JobTemplate;
import org.eclipse.slm.awx.model.Project;
import org.eclipse.slm.awx.model.Survey;
import org.eclipse.slm.awx.model.SurveyItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Component
public class ProfilerService implements IAwxJobObserverListener {
    private static final Logger LOG = LoggerFactory.getLogger(ProfilerService.class);

    private record AwxResourceIds(Set<Integer> projectIds, Set<Integer> jobTemplateIds) {
    }

    private final ProfilerJpaRepository profilerJpaRepository;
    private final AwxClient awxClient;
    private final AwxJobExecutor awxJobExecutor;

    @Value("${awx.default-execution-environment}")
    private String defaultExecutionEnvironment;

    public ProfilerService(
            ProfilerJpaRepository profilerJpaRepository, 
            AwxClient awxClient,
            AwxJobExecutor awxJobExecutor
    ) {
        this.profilerJpaRepository = profilerJpaRepository;
        this.awxClient = awxClient;
        this.awxJobExecutor = awxJobExecutor;
    }

    public Profiler createProfiler(Profiler profiler) {
        if(isProfilerActionAwxAction(profiler)) {
            AwxAction awxProfilerAction = (AwxAction) profiler.getAction();
            try {
                createAwxResources(awxProfilerAction);
            } catch (AwxProjectUpdateFailedException | SSLException | JsonProcessingException e) {
                LOG.error("Failed to clone AWX Project from " + awxProfilerAction.getAwxRepo() + " - " + awxProfilerAction.getAwxBranch());
            }
        }

        var newProfiler = profilerJpaRepository.save(profiler);
        return newProfiler;
    }

    public Profiler updateProfiler(UUID profilerId, Profiler profiler) {
        var existingProfiler = profilerJpaRepository.findById(profilerId)
                .orElseThrow(() -> new ProfilerNotFoundException(profilerId));

        if (!isProfilerActionAwxAction(profiler)) {
            throw new ProfilerBadRequestException("Action of profiler must be of type " + AwxAction.class.getSimpleName());
        }

        profiler.setId(profilerId);
        var oldAction = isProfilerActionAwxAction(existingProfiler) ? (AwxAction) existingProfiler.getAction() : null;
        var newAction = (AwxAction) profiler.getAction();

        if (oldAction == null || !hasSameSource(oldAction, newAction)) {
            return replaceAwxResources(profiler, oldAction, newAction);
        }

        if (!oldAction.hasSameParameter(newAction)) {
            var newParameter = newAction.getParameter() == null ? List.<SurveyItem>of() : newAction.getParameter();
            replaceSurvey(profilerId, newAction, newParameter);
        }

        return profilerJpaRepository.save(profiler);
    }

    public List<Profiler> getProfiler() {
        return profilerJpaRepository.findAll();
    }

    public Optional<Profiler> getProfiler(UUID profilerId) {
        return profilerJpaRepository.findById(profilerId);
    }

    public void runAllProfilerAction(JwtAuthenticationToken jwtAuthenticationToken) {
        List<Profiler> profiler = getProfiler();

        profiler.stream().forEach(pr -> {
            runProfilerAction(pr.getId(), jwtAuthenticationToken);
        });
    }
    public void runProfilerAction(UUID profilerId, JwtAuthenticationToken jwtAuthenticationToken) {
        Optional<Profiler> optionalProfiler = getProfiler(profilerId);

        if(optionalProfiler.isEmpty())
            return;

        Profiler profiler = optionalProfiler.get();

        if(isProfilerActionAwxAction(profiler)) {
            AwxAction action = (AwxAction) profiler.getAction();

            int awxJobId = awxJobExecutor.executeJob(
                    new AwxCredential(jwtAuthenticationToken),
                    action.getAwxRepo(),
                    action.getAwxBranch(),
                    action.getPlaybook(),
                    new ExtraVars(new HashMap<>())
            );
        }
    }

    public void deleteProfiler(UUID profilerId) {
        Optional<Profiler> optionalProfiler = profilerJpaRepository.findById(profilerId);

        if(optionalProfiler.isEmpty())
            return;

        Profiler profiler = optionalProfiler.get();

        if (isProfilerActionAwxAction(profiler)) {
            try {
                removeAwxResources((AwxAction) profiler.getAction(), otherActions(profilerId));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ProfilerRuntimeException(profilerId, "Interrupted while removing AWX resources");
            }
        }

        profilerJpaRepository.deleteById(profilerId);
    }

    private void createAwxResources(AwxAction action) throws AwxProjectUpdateFailedException, SSLException, JsonProcessingException {
        var jobTemplateCredentialNames = List.of("Consul", "HashiCorp Vault");
        JobTemplate jobTemplate;
        if(!action.getUsername().equals("") && !action.getPassword().equals("")) {
            jobTemplate = awxClient.createJobTemplateAddExecuteRoleToDefaultTeamAddScmCredential(
                    action.getAwxRepo(),
                    action.getAwxBranch(),
                    action.getPlaybook(),
                    action.getUsername(),
                    action.getPassword(),
                    jobTemplateCredentialNames,
                    defaultExecutionEnvironment);
        } else {
            jobTemplate = awxClient.createJobTemplateAndAddExecuteRoleToDefaultTeam(
                    action.getAwxRepo(),
                    action.getAwxBranch(),
                    action.getPlaybook(),
                    jobTemplateCredentialNames,
                    defaultExecutionEnvironment);
        }

        List<SurveyItem> params = action.getParameter();
        if(params != null && !params.isEmpty()) {
            createSurvey(jobTemplate, params);
        }
    }

    private void createSurvey(JobTemplate jobTemplate, List<SurveyItem> params) {
        awxClient.createSurvey(
                jobTemplate.getId(),
                new Survey(
                        "Survey",
                        "Survey for "+jobTemplate.getName(),
                        params
                )
        );
        awxClient.enableSurvey(jobTemplate.getId());
    }

    private void replaceSurvey(UUID profilerId, AwxAction action, List<SurveyItem> params) {
        var jobTemplate = findJobTemplate(action)
                .orElseThrow(() -> new ProfilerRuntimeException(profilerId,
                        "AWX job template for " + action.getAwxRepo() + " - " + action.getAwxBranch() + " - " + action.getPlaybook() + " not found"));

        awxClient.deleteSurvey(jobTemplate.getId());
        if (!params.isEmpty()) {
            createSurvey(jobTemplate, params);
        }
    }

    private Profiler replaceAwxResources(Profiler profiler, AwxAction oldAction, AwxAction newAction) {
        var profilerId = profiler.getId();
        var existingIds = findAwxResourceIds(List.of(newAction));

        Profiler savedProfiler;
        try {
            createAwxResources(newAction);
            savedProfiler = profilerJpaRepository.save(profiler);
        } catch (Exception | AwxProjectUpdateFailedException e) {
            LOG.error("Failed to switch profiler " + profilerId + " to " + newAction.getAwxRepo() + " - " + newAction.getAwxBranch(), e);
            removeCreatedAwxResources(newAction, existingIds);
            throw new ProfilerRuntimeException(profilerId, "Failed to create AWX resources: " + e);
        }

        if (oldAction != null) {
            var actionsInUse = new ArrayList<>(otherActions(profilerId));
            actionsInUse.add(newAction);
            try {
                removeAwxResources(oldAction, actionsInUse);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOG.error("Interrupted while removing old AWX resources of " + oldAction.getAwxRepo(), e);
            } catch (Exception e) {
                LOG.error("Failed to remove old AWX resources of " + oldAction.getAwxRepo(), e);
            }
        }

        return savedProfiler;
    }

    private void removeCreatedAwxResources(AwxAction action, AwxResourceIds existingIds) {
        try {
            var currentIds = findAwxResourceIds(List.of(action));
            for (var jobTemplateId : currentIds.jobTemplateIds()) {
                if (!existingIds.jobTemplateIds().contains(jobTemplateId)) {
                    awxClient.deleteJobTemplate(jobTemplateId);
                }
            }
            for (var project : findProjects(action)) {
                if (!existingIds.projectIds().contains(project.getId())) {
                    removeProject(project);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.error("Interrupted while removing created AWX resources of " + action.getAwxRepo(), e);
        } catch (Exception e) {
            LOG.error("Failed to remove created AWX resources of " + action.getAwxRepo(), e);
        }
    }

    private void removeAwxResources(AwxAction action, List<AwxAction> actionsInUse) throws InterruptedException {
        var project = findProjectWithSameUrl(findProjects(action), action);
        if (project.isEmpty()) {
            LOG.warn("No AWX project with URL " + action.getAwxRepo() + " and branch " + action.getAwxBranch() + " to remove");
            return;
        }

        var idsInUse = findAwxResourceIds(actionsInUse);
        for (var jobTemplate : findJobTemplates(project.get(), action.getPlaybook())) {
            if (!idsInUse.jobTemplateIds().contains(jobTemplate.getId())) {
                awxClient.deleteJobTemplate(jobTemplate.getId());
            }
        }
        if (!idsInUse.projectIds().contains(project.get().getId())) {
            removeProject(project.get());
        }
    }

    // Does what AwxClient.deleteProject(String, String) does, but for a given project: that method
    // removes the first project whose URL contains the given one, which may belong to another source
    private void removeProject(Project project) throws InterruptedException {
        if (awxClient.getJobTemplateFromProjectId(project.getId()).getCount() > 0) {
            LOG.info("Keeping AWX project " + project.getId() + " because job templates are linked to it");
            return;
        }

        awxClient.deleteProject(project.getId());
        var credentialId = project.getCredential();
        if (credentialId != null && awxClient.getProjectFromCredential(credentialId).getCount() == 0) {
            awxClient.deleteCredential(credentialId);
        }
    }

    private List<AwxAction> otherActions(UUID profilerId) {
        return profilerJpaRepository.findAll().stream()
                .filter(other -> !profilerId.equals(other.getId()))
                .filter(this::isProfilerActionAwxAction)
                .map(other -> (AwxAction) other.getAction())
                .toList();
    }

    private AwxResourceIds findAwxResourceIds(List<AwxAction> actions) {
        var projectIds = new HashSet<Integer>();
        var jobTemplateIds = new HashSet<Integer>();
        for (var action : actions) {
            for (var project : findProjects(action)) {
                projectIds.add(project.getId());
                findJobTemplates(project, action.getPlaybook()).forEach(jobTemplate -> jobTemplateIds.add(jobTemplate.getId()));
            }
        }
        return new AwxResourceIds(projectIds, jobTemplateIds);
    }

    // AWX matches the URL as a case-insensitive substring, so this can include projects of other URLs.
    // The client reuses the first of them when creating resources and running jobs.
    private List<Project> findProjects(AwxAction action) {
        var projects = awxClient.getProjectFromRepoAndBranch(action.getAwxRepo(), action.getAwxBranch());
        return projects == null ? List.of() : List.copyOf(projects.getResults());
    }

    private List<JobTemplate> findJobTemplates(Project project, String playbook) {
        var jobTemplates = awxClient.getJobTemplateFromProjectIdAndPlaybook(project.getId(), playbook);
        return jobTemplates == null ? List.of() : List.copyOf(jobTemplates.getResults());
    }

    private Optional<JobTemplate> findJobTemplate(AwxAction action) {
        var projects = findProjects(action);
        return findProjectWithSameUrl(projects, action)
                .or(() -> projects.stream().findFirst())
                .flatMap(project -> findJobTemplates(project, action.getPlaybook()).stream().findFirst());
    }

    private static Optional<Project> findProjectWithSameUrl(List<Project> projects, AwxAction action) {
        return projects.stream().filter(project -> action.getAwxRepo().equals(project.getScm_url())).findFirst();
    }

    private static boolean hasSameSource(AwxAction a, AwxAction b) {
        return a.getAwxRepo().equals(b.getAwxRepo())
                && a.getAwxBranch().equals(b.getAwxBranch())
                && a.getPlaybook().equals(b.getPlaybook());
    }

    private Boolean isProfilerActionAwxAction(Profiler profiler) {
        return profiler.getAction() != null && profiler.getAction().getActionClass().equals(AwxAction.class.getSimpleName());
    }

    @Override
    public void onJobStateChanged(AwxJobObserver sender, JobState newState) {
        
    }

    @Override
    public void onJobStateFinished(AwxJobObserver sender, JobFinalState finalState) {

    }
}
