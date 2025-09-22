package org.eclipse.slm.common.awx.client.observer;

import org.eclipse.slm.common.awx.client.AwxClient;
import org.eclipse.slm.common.awx.client.AwxCredential;
import org.eclipse.slm.common.awx.model.ExtraVars;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLException;

@Component
public class AwxJobExecutor {

    public final static Logger LOG = LoggerFactory.getLogger(AwxJobExecutor.class);

    private final AwxClient awxClient;

    @Autowired
    public AwxJobExecutor(AwxClient awxClient)
    {
        this.awxClient = awxClient;
    }

    @Autowired
    private AwxJobObserverInitializer awxJobObserverInitializer;

    public int executeJob(
            AwxCredential awxCredential,
            String gitRepo, String branch, String playbook,
            ExtraVars extraVars) {
        var jobId = awxClient.runJobTemplate(awxCredential, gitRepo, branch, playbook, extraVars);
        return jobId;
    }

    public AwxJobObserver executeJobAndObserve(
            JwtAuthenticationToken jwtAuthenticationToken,
            String gitRepo, String branch, String playbook,
            ExtraVars extraVars,
            JobTarget jobTarget, JobGoal jobGoal, IAwxJobObserverListener listener
    ) throws SSLException {
        var jobId = this.executeJob(
                new AwxCredential(jwtAuthenticationToken),
                gitRepo,
                branch,
                playbook,
                extraVars
        );

        var awxJobObserver = awxJobObserverInitializer.initNewObserver(jobId, jobTarget, jobGoal, listener);

        return awxJobObserver;
    }
}
