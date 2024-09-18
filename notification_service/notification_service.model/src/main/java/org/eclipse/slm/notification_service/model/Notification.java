package org.eclipse.slm.notification_service.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.util.Date;

@Entity
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    long id;
    Category category;
    String text;
    String owner;
    Boolean isRead = false;
    Date date = new Date();

    public Notification() {
    }

    public Notification(Category category, String text, String owner) {
        this.category = category;
        this.text = text;
        this.owner = owner;
    }

    public Notification(long id, Category category, String text, String owner, Boolean isRead) {
        this.id = id;
        this.category = category;
        this.text = text;
        this.owner = owner;
        this.isRead = isRead;
    }

    public Notification(long id, Category category, String text, String owner, Boolean isRead, Date date) {
        this.id = id;
        this.category = category;
        this.text = text;
        this.owner = owner;
        this.isRead = isRead;
        this.date = date;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Boolean getRead() {
        return isRead;
    }

    public void setRead(Boolean read) {
        isRead = read;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "Notification{" +
                "id=" + id +
                ", category=" + category +
                ", text='" + text + '\'' +
                ", owner='" + owner + '\'' +
                ", isRead=" + isRead +
                ", date=" + date +
                '}';
    }
}
