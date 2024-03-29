package org.eclipse.slm.notification_service.persistence.api;

import org.eclipse.slm.notification_service.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByOwner(String owner);

    List<Notification> findByIsReadAndOwner(Boolean isRead, String owner);
}
