package com.techelevator.model.parking;

import java.math.BigDecimal;
import java.time.Instant;

public class Session {
    private int sessionId;
    private int spotId;
    private int userId;
    private Integer vehicleId;
    private Instant checkInTime;
    private Instant checkOutTime;
    private BigDecimal totalCharged;

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public int getSpotId() {
        return spotId;
    }

    public void setSpotId(int spotId) {
        this.spotId = spotId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Integer getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Integer vehicleId) {
        this.vehicleId = vehicleId;
    }

    public Instant getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(Instant checkInTime) {
        this.checkInTime = checkInTime;
    }

    public Instant getCheckOutTime() {
        return checkOutTime;
    }

    public void setCheckOutTime(Instant checkOutTime) {
        this.checkOutTime = checkOutTime;
    }

    public BigDecimal getTotalCharged() {
        return totalCharged;
    }

    public void setTotalCharged(BigDecimal totalCharged) {
        this.totalCharged = totalCharged;
    }
}
