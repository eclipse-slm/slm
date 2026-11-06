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
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class ProfilerService implements IAwxJobObserverListener {
    private static final Logger LOG = LoggerFactory.getLogger(ProfilerService.class);

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

        if (profiler.getAction() == null || !isProfilerActionAwxAction(profiler)) {
            throw new ProfilerBadRequestException("Action of profiler must be of type " + AwxAction.class.getSimpleName());
        }

        profiler.setId(profilerId);
        var oldAction = (AwxAction) existingProfiler.getAction();
        var newAction = (AwxAction) profiler.getAction();

        if (hasSameSource(oldAction, newAction)) {
            var oldParameter = oldAction.getParameter() == null ? List.<SurveyItem>of() : oldAction.getParameter();
            var newParameter = newAction.getParameter() == null ? List.<SurveyItem>of() : newAction.getParameter();
            if (!oldParameter.equals(newParameter)) {
                replaceSurvey(profilerId, newAction, newParameter);
            }
        } else {
            replaceAwxResources(profilerId, oldAction, newAction);
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
            removeAwxResources((AwxAction) profiler.getAction(), profilerId, false);
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
        if(params != null) {
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

    private void replaceAwxResources(UUID profilerId, AwxAction oldAction, AwxAction newAction) {
        var projectExisted = findProject(newAction).isPresent();
        var jobTemplateExisted = findJobTemplate(newAction).isPresent();

        try {
            createAwxResources(newAction);
        } catch (Exception | AwxProjectUpdateFailedException e) {
            LOG.error("Failed to create AWX resources for " + newAction.getAwxRepo() + " - " + newAction.getAwxBranch(), e);
            if (!jobTemplateExisted) {
                try {
                    awxClient.deleteJobTemplate(newAction.getAwxRepo(), newAction.getAwxBranch(), newAction.getPlaybook());
                } catch (Exception cleanupException) {
                    LOG.error("Failed to remove AWX job template of " + newAction.getAwxRepo(), cleanupException);
                }
            }
            if (!projectExisted) {
                try {
                    awxClient.deleteProject(newAction.getAwxRepo(), newAction.getAwxBranch());
                } catch (Exception cleanupException) {
                    LOG.error("Failed to remove AWX project of " + newAction.getAwxRepo(), cleanupException);
                }
            }
            throw new ProfilerRuntimeException(profilerId, "Failed to create AWX resources: " + e);
        }

        removeAwxResources(oldAction, profilerId, true);
    }

    private void removeAwxResources(AwxAction action, UUID ownerProfilerId, boolean ignoreFailures) {
        var otherActions = profilerJpaRepository.findAll().stream()
                .filter(other -> !ownerProfilerId.equals(other.getId()))
                .filter(this::isProfilerActionAwxAction)
                .map(other -> (AwxAction) other.getAction())
                .toList();

        var sharedProject = otherActions.stream().anyMatch(other -> hasSameProject(other, action));
        var sharedJobTemplate = sharedProject && otherActions.stream()
                .anyMatch(other -> hasSameSource(other, action));

        if (!sharedJobTemplate) {
            try {
                awxClient.deleteJobTemplate(action.getAwxRepo(), action.getAwxBranch(), action.getPlaybook());
            } catch (Exception e) {
                if (!ignoreFailures) throw e;
                LOG.error("Failed to remove old AWX job template of " + action.getAwxRepo(), e);
            }
        }
        if (!sharedProject) {
            try {
                awxClient.deleteProject(action.getAwxRepo(), action.getAwxBranch());
            } catch (Exception e) {
                if (!ignoreFailures) throw e;
                LOG.error("Failed to remove old AWX project of " + action.getAwxRepo(), e);
            }
        }
    }

    private Optional<Project> findProject(AwxAction action) {
        var projects = awxClient.getProjectFromRepoAndBranch(action.getAwxRepo(), action.getAwxBranch());
        if (projects == null || projects.getResults().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(projects.getResults().iterator().next());
    }

    private Optional<JobTemplate> findJobTemplate(AwxAction action) {
        return findProject(action).flatMap(project -> {
            var jobTemplates = awxClient.getJobTemplateFromProjectIdAndPlaybook(project.getId(), action.getPlaybook());
            if (jobTemplates == null || jobTemplates.getResults().isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(jobTemplates.getResults().iterator().next());
        });
    }

    private static boolean hasSameProject(AwxAction a, AwxAction b) {
        return a.getAwxRepo().equals(b.getAwxRepo()) && a.getAwxBranch().equals(b.getAwxBranch());
    }

    private static boolean hasSameSource(AwxAction a, AwxAction b) {
        return hasSameProject(a, b) && a.getPlaybook().equals(b.getPlaybook());
    }

    private Boolean isProfilerActionAwxAction(Profiler profiler) {
        return profiler.getAction().getActionClass().equals(AwxAction.class.getSimpleName());
    }

    @Override
    public void onJobStateChanged(AwxJobObserver sender, JobState newState) {
        
    }

    @Override
    public void onJobStateFinished(AwxJobObserver sender, JobFinalState finalState) {

    }
}
