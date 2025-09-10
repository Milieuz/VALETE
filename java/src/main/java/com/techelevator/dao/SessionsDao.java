package com.techelevator.dao;

import com.techelevator.model.parking.Session;

import java.math.BigDecimal;
import java.time.Instant;

public interface SessionsDao {

    int insertSession(int userId, int spotId, Instant checkInTime, Integer vehicleId);

    boolean existsActiveBySpot(int spotId);

    boolean existsActiveByUserAndSpot(int userId, int spotId);

    boolean updateCheckout(int sessionId, Instant checkOutTime, BigDecimal totalCharged);

    Session findActiveSessionById(int sessionId);

    Session findActiveByUserAndSpot(int userId, int spotId);
}
