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
    JobTarget target;
    JobGoal goal;
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

    public Notification(Category category, JobTarget jobTarget, JobGoal goal, String text, String owner) {
        this.category = category;
        this.target = jobTarget;
        this.goal = goal;
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

    public JobTarget getTarget() {
        return target;
    }

    public void setTarget(JobTarget target) {
        this.target = target;
    }

    public JobGoal getGoal() {
        return goal;
    }

    public void setGoal(JobGoal goal) {
        this.goal = goal;
    }

    @Override
    public String toString() {
        return "Notification{" +
                "id=" + this.id +
                ", category=" + this.category +
                ", target=" + this.target +
                ", goal=" + this.goal +
                ", text='" + this.text + '\'' +
                ", owner='" + this.owner + '\'' +
                ", isRead=" + this.isRead +
                ", date=" + this.date +
                '}';
    }
}
