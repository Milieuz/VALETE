package com.techelevator.model.parking;

import java.time.OffsetDateTime;

public class ParkedVehicleDTO {
    private long reservationId;
    private long spotId;
    private long lotId;
    private String licensePlate;
    private String make;
    private String model;
    private String color;
    private OffsetDateTime startTime;
    private OffsetDateTime endTime;
    private long userId;
    private String ownerName;

    // getters & setters
    public long getReservationId() { return reservationId; }
    public void setReservationId(long reservationId) { this.reservationId = reservationId; }

    public long getSpotId() { return spotId; }
    public void setSpotId(long spotId) { this.spotId = spotId; }

    public long getLotId() { return lotId; }
    public void setLotId(long lotId) { this.lotId = lotId; }

    public String getLicensePlate() { return licensePlate; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }

    public String getMake() { return make; }
    public void setMake(String make) { this.make = make; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public OffsetDateTime getStartTime() { return startTime; }
    public void setStartTime(OffsetDateTime startTime) { this.startTime = startTime; }

    public OffsetDateTime getEndTime() { return endTime; }
    public void setEndTime(OffsetDateTime endTime) { this.endTime = endTime; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

}
