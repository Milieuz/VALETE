package com.techelevator.dao;

import com.techelevator.model.parking.SpotStatus;

import java.util.List;

public interface ParkingSpotDao {
    boolean lockAndCheckAvailable(int spotId);
    void setAvailability(int spotId, boolean isAvailable);
    long getAvailableCount(long lotId);
    boolean isLotFull(long lotId);
    List<SpotStatus> getSpotStatuses(long lotId);
}
