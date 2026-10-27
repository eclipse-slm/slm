package org.eclipse.slm.notification_service.service.app.messaging.aas;

import org.eclipse.slm.aas.clients.shellrepository.AasRepositoryClientFactory;
import org.eclipse.slm.notification_service.service.app.messaging.resources.ResourceUserResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reports the state of the connection to the BaSyx MQTT broker and keeps the {@link SubmodelShellIndex} complete:
 * the index is (re)built on every subscription, because events missed while disconnected are not replayed, and
 * rebuilding is retried on each {@link #check()} until all resource shells could be read.
 */
public class AasBrokerConnectionMonitor {

    private final static Logger LOG = LoggerFactory.getLogger(AasBrokerConnectionMonitor.class);

    private final String brokerUrl;

    private final SubmodelShellIndex submodelShellIndex;

    private final ResourceUserResolver resourceUserResolver;

    private final AasRepositoryClientFactory aasRepositoryClientFactory;

    private volatile boolean subscribed = false;

    private volatile boolean indexComplete = false;

    public AasBrokerConnectionMonitor(String brokerUrl, SubmodelShellIndex submodelShellIndex,
                                      ResourceUserResolver resourceUserResolver, AasRepositoryClientFactory aasRepositoryClientFactory) {
        this.brokerUrl = brokerUrl;
        this.submodelShellIndex = submodelShellIndex;
        this.resourceUserResolver = resourceUserResolver;
        this.aasRepositoryClientFactory = aasRepositoryClientFactory;
    }

    public void onSubscribed() {
        this.subscribed = true;
        LOG.info("Subscribed to BaSyx events on MQTT broker '{}'", this.brokerUrl);
        this.refreshIndex();
    }

    public void onConnectionFailed(Throwable cause) {
        this.subscribed = false;
        LOG.warn("Connection to MQTT broker '{}' failed, BaSyx events are not received until it is re-established: {}",
                this.brokerUrl, cause != null ? cause.getMessage() : "unknown cause");
    }

    public void check() {
        if (!this.subscribed) {
            LOG.warn("Not subscribed to MQTT broker '{}', BaSyx events are not received. Retrying.", this.brokerUrl);
        } else if (!this.indexComplete) {
            this.refreshIndex();
        }
    }

    private synchronized void refreshIndex() {
        try {
            this.indexComplete = this.submodelShellIndex.initialize(
                    this.resourceUserResolver.getKnownResourceIds(), this.aasRepositoryClientFactory.getClient());
        } catch (Exception e) {
            this.indexComplete = false;
            LOG.warn("Unable to build submodel index from AAS repository: {}", e.getMessage());
        }
    }
}
