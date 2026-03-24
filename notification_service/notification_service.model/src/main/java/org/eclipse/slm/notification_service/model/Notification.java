package org.eclipse.slm.notification_service.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.*;
import org.eclipse.slm.common.model.AbstractBaseEntityLong;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Date;
import java.util.Map;

@Entity
public class Notification extends AbstractBaseEntityLong {

    private String userId;

    private Date timestamp = new Date();

    private NotificationCategory category;

    private NotificationSubCategory subCategory;

    private NotificationEventType eventType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "LONGTEXT")
    private Map<String, Object> payload;

    private Boolean isRead = false;

    public Notification() {
    }

    public Notification(String userId, NotificationCategory category, NotificationSubCategory subCategory, NotificationEventType eventType, Object payload) {
        this.userId = userId;
        this.timestamp = new Date();
        this.category = category;
        this.subCategory = subCategory;
        this.eventType = eventType;
        this.payload = convertPayload(payload);
    }

    public Notification(String userId, Date timestamp, NotificationCategory category, NotificationSubCategory subCategory, NotificationEventType eventType, Object payload) {
        this.userId = userId;
        this.timestamp = timestamp;
        this.category = category;
        this.subCategory = subCategory;
        this.eventType = eventType;
        this.payload = convertPayload(payload);
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public NotificationCategory getCategory() {
        return category;
    }

    public void setCategory(NotificationCategory category) {
        this.category = category;
    }

    public NotificationSubCategory getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(NotificationSubCategory subCategory) {
        this.subCategory = subCategory;
    }

    public NotificationEventType getEventType() {
        return eventType;
    }

    public void setEventType(NotificationEventType eventType) {
        this.eventType = eventType;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }

    @Hidden
    public void setPayload(Object payload) {
        this.payload = convertPayload(payload);
    }

    public Boolean getRead() {
        return isRead;
    }

    public void setRead(Boolean read) {
        isRead = read;
    }

    private Map<String, Object> convertPayload(Object payload) {
        var objectMapper = new ObjectMapper();
        return objectMapper.convertValue(payload, new TypeReference<Map<String, Object>>() {});
    }

    @Override
    public String toString() {
        return "Notification{" +
                "id=" + this.getId() +
                ", category=" + this.category +
                ", subCategory=" + this.subCategory +
                ", eventType=" + this.eventType +
                ", userId='" + this.userId + '\'' +
                ", timestamp=" + this.timestamp +
                '}';
    }
}
