package org.eclipse.slm.resource_management.features.profiler;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.eclipse.slm.common.awx.client.AwxClient;
import org.eclipse.slm.common.awx.client.AwxCredential;
import org.eclipse.slm.common.awx.client.AwxProjectUpdateFailedException;
import org.eclipse.slm.common.awx.client.observer.*;
import org.eclipse.slm.common.awx.model.ExtraVars;
import org.eclipse.slm.common.awx.model.JobTemplate;
import org.eclipse.slm.common.awx.model.Survey;
import org.eclipse.slm.common.awx.model.SurveyItem;
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
                var jobTemplateCredentialNames = List.of("Consul", "HashiCorp Vault");
                JobTemplate jobTemplate;
                if(!awxProfilerAction.getUsername().equals("") && !awxProfilerAction.getPassword().equals("")) {
                    jobTemplate = awxClient.createJobTemplateAddExecuteRoleToDefaultTeamAddScmCredential(
                            awxProfilerAction.getAwxRepo(),
                            awxProfilerAction.getAwxBranch(),
                            awxProfilerAction.getPlaybook(),
                            awxProfilerAction.getUsername(),
                            awxProfilerAction.getPassword(),
                            jobTemplateCredentialNames,
                            defaultExecutionEnvironment);
                } else {
                    jobTemplate = awxClient.createJobTemplateAndAddExecuteRoleToDefaultTeam(
                            awxProfilerAction.getAwxRepo(),
                            awxProfilerAction.getAwxBranch(),
                            awxProfilerAction.getPlaybook(),
                            jobTemplateCredentialNames,
                            defaultExecutionEnvironment);
                }

                List<SurveyItem> params = awxProfilerAction.getParameter();
                if(params != null) {
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
            } catch (AwxProjectUpdateFailedException | SSLException | JsonProcessingException e) {
                LOG.error("Failed to clone AWX Project from " + awxProfilerAction.getAwxRepo() + " - " + awxProfilerAction.getAwxBranch());
            }
        }

        var newProfiler = profilerJpaRepository.save(profiler);
        return newProfiler;
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

        if (profiler.getAction().getActionClass().equals(AwxAction.class.getSimpleName())) {
            AwxAction awxCapabilityAction = (AwxAction) profiler.getAction();
            awxClient.deleteJobTemplate(
                    awxCapabilityAction.getAwxRepo(),
                    awxCapabilityAction.getAwxBranch(),
                    awxCapabilityAction.getPlaybook()
            );

            awxClient.deleteProject(
                    awxCapabilityAction.getAwxRepo(),
                    awxCapabilityAction.getAwxBranch()
            );
        }

        profilerJpaRepository.deleteById(profilerId);
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
