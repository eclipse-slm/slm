package org.eclipse.slm.notification_service.messaging;

import org.eclipse.slm.common.messaging.AbstractEventMessage;
import org.eclipse.slm.notification_service.model.*;

import java.util.Date;

public class NotificationEventMessage extends AbstractEventMessage<NotificationMessageEventType> {

    public static final String EXCHANGE_NAME = "notifications";

    public static final String ROUTING_KEY_PREFIX = "notification.";

    private String userId;

    private Date timestamp = new Date();

    private NotificationCategory category;

    private NotificationSubCategory subCategory;

    private NotificationEventType notificationEventType;

    private Object payload;

    public NotificationEventMessage(String userId,
                                    NotificationCategory category,
                                    NotificationSubCategory subCategory,
                                    NotificationEventType notificationEventType,
                                    Object payload) {
        super(EXCHANGE_NAME, ROUTING_KEY_PREFIX, NotificationMessageEventType.NOTIFY);
        this.userId = userId;
        this.timestamp = new Date();
        this.category = category;
        this.subCategory = subCategory;
        this.notificationEventType = notificationEventType;
        this.payload = payload;
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

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

    public NotificationCategory getCategory() {
        return category;
    }

    public void setCategory(NotificationCategory categories) {
        this.category = categories;
    }

    public NotificationSubCategory getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(NotificationSubCategory subCategory) {
        this.subCategory = subCategory;
    }

    public NotificationEventType getNotificationEventType() {
        return notificationEventType;
    }

    public void setNotificationEventType(NotificationEventType eventType) {
        this.notificationEventType = eventType;
    }
}
