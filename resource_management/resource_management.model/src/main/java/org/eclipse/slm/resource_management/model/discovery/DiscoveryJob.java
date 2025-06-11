package org.eclipse.slm.resource_management.model.discovery;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.*;

@Entity
public class DiscoveryJob {

    @JsonProperty
    @Id
    @Column(name = "id", length = 36, unique = true, nullable = false)
    private UUID id = UUID.randomUUID();

    @JsonProperty
    private String driverId;

    @JsonProperty
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Date startDate = Calendar.getInstance().getTime();

    @JsonProperty
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Date finishDate;

    @JsonProperty
    private DiscoveryJobState state = DiscoveryJobState.CREATED;

    @JsonProperty
    @Column(columnDefinition = "LONGTEXT")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<DiscoveredResource> discoveryResult = new ArrayList<>();

    public DiscoveryJob(String driverId) {
        this.driverId = driverId;
    }

    public DiscoveryJob() {

    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getFinishDate() {
        return finishDate;
    }

    public void setFinishDate(Date finishDate) {
        this.finishDate = finishDate;
    }

    public DiscoveryJobState getState() {
        return state;
    }

    public void setState(DiscoveryJobState state) {
        this.state = state;
    }

    public List<DiscoveredResource> getDiscoveryResult() {
        return discoveryResult;
    }

    public void setDiscoveryResult(List<DiscoveredResource> discoveryResult) {
        this.discoveryResult = discoveryResult;
    }
}
