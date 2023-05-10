package org.eclipse.slm.common.awx.model;

public class UserGetResponse {
    int count;
    String next;
    String previous;
    User[] results;

    public UserGetResponse() {
    }

    public UserGetResponse(int count, String next, String previous, User[] results) {
        this.count = count;
        this.next = next;
        this.previous = previous;
        this.results = results;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getNext() {
        return next;
    }

    public void setNext(String next) {
        this.next = next;
    }

    public String getPrevious() {
        return previous;
    }

    public void setPrevious(String previous) {
        this.previous = previous;
    }

    public User[] getResults() {
        return results;
    }

    public void setResults(User[] results) {
        this.results = results;
    }
}
