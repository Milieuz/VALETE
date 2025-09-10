package com.techelevator.service;

import com.techelevator.model.parking.Session;

import java.math.BigDecimal;

public interface SessionsService {
    Session checkInVehicle(int spotId, int userId, int vehicleId);
    Session checkOutById(int sessionId, BigDecimal totalCharged);
    Session findActiveSessionById(int sessionId);
    Session checkOutBySpotAndUser(int spotId, int userId);
}
