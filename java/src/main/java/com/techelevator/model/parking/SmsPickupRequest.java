package com.techelevator.model.parking;

import java.time.LocalDateTime;

public class SmsPickupRequest {

    private int requestId;
    private Integer sessionId;
    private Integer userId;
    private LocalDateTime requestedAt;
    private String status;
    private int estimatedTime;
    private LocalDateTime valetNotifiedAt;


    public SmsPickupRequest() { }


    public SmsPickupRequest(int requestId,
                            Integer sessionId,
                            Integer userId,
                            LocalDateTime requestedAt,
                            String status,
                            int estimatedTime,
                            LocalDateTime valetNotifiedAt) {

        this.requestId = requestId;
        this.sessionId = sessionId;
        this.userId = userId;
        this.requestedAt = requestedAt;
        this.status = status;
        this.estimatedTime = estimatedTime;
        this.valetNotifiedAt = valetNotifiedAt;

    }

    // Getters and Setters
    public int getRequestId() {

        return requestId;
    }
    public void setRequestId(int requestId) {

        this.requestId = requestId;
    }

    public Integer getSessionId() {

        return sessionId;
    }
    public void setSessionId(Integer sessionId) {

        this.sessionId = sessionId;
    }

    public Integer getUserId() {

        return userId;

    }
    public void setUserId(Integer userId) {

        this.userId = userId;

    }

    public LocalDateTime getRequestedAt() {

        return requestedAt;
    }
    public void setRequestedAt(LocalDateTime requestedAt) {

        this.requestedAt = requestedAt;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public int getEstimatedTime() {
        return estimatedTime;
    }
    public void setEstimatedTime(int estimatedTime) {
        this.estimatedTime = estimatedTime;
    }

    public LocalDateTime getValetNotifiedAt() {
        return valetNotifiedAt;
    }
    public void setValetNotifiedAt(LocalDateTime valetNotifiedAt) {
        this.valetNotifiedAt = valetNotifiedAt;
    }
}
