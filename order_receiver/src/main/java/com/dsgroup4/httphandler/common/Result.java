package com.dsgroup4.httphandler.common;


import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="result")
public class Result {
    @Id
    private String id;
    private int user_id;
    private String initiator;
    private boolean success;
    private double paid;

    public boolean isSuccess() {
        return success;
    }

    public double getPaid() {
        return paid;
    }

    public int getUser_id() {
        return user_id;
    }

    public String getId() {
        return id;
    }

    public String getInitiator() {
        return initiator;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setInitiator(String initiator) {
        this.initiator = initiator;
    }

    public void setPaid(double paid) {
        this.paid = paid;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }
}
