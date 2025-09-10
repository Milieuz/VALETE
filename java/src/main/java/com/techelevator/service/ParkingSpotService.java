package com.techelevator.service;

import com.techelevator.model.parking.SpotStatus;
import java.util.List;

public interface ParkingSpotService {
    long getAvailableSpotCount(long lotId);
    boolean isLotFull(long lotId);
    List<SpotStatus> getSpotStatuses(long lotId);
}
