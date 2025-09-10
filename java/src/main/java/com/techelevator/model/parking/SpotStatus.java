package com.techelevator.model.parking;

public class SpotStatus {
    private long spotId;
    private boolean available;

    public SpotStatus() {
    }

    public SpotStatus(long spotId, boolean available) {
        this.spotId = spotId;
        this.available = available;
    }

    public long getSpotId() {
        return spotId;
    }

    public void setSpotId(long spotId) {
        this.spotId = spotId;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
