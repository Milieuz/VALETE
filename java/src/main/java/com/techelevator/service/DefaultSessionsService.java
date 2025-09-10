package com.techelevator.service;

import com.techelevator.dao.ParkingSpotDao;
import com.techelevator.dao.SessionsDao;
import com.techelevator.exception.SpotUnavailableException;
import com.techelevator.model.parking.Session;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@Service
public class DefaultSessionsService implements SessionsService {

    private final ParkingSpotDao parkingSpotDao;
    private final SessionsDao sessionsDao;

    public DefaultSessionsService(ParkingSpotDao parkingSpotDao, SessionsDao sessionsDao) {
        this.parkingSpotDao = parkingSpotDao;
        this.sessionsDao = sessionsDao;
    }

    @Transactional
    @Override
    public Session checkInVehicle(int spotId, int userId, int vehicleId) {
        if (!parkingSpotDao.lockAndCheckAvailable(spotId)) {
            throw new SpotUnavailableException("Spot " + spotId + " is not available.");
        }
        if (sessionsDao.existsActiveBySpot(spotId)) {
            throw new IllegalStateException("Spot already has an active session");
        }
        Instant now = Instant.now();
        int sessionId = sessionsDao.insertSession(userId, spotId, now, vehicleId);
        parkingSpotDao.setAvailability(spotId, false);

        Session session = new Session();
        session.setSessionId(sessionId);
        session.setSpotId(spotId);
        session.setUserId(userId);
        session.setVehicleId(vehicleId);
        session.setCheckInTime(now);
        return session;
    }

    @Transactional
    @Override
    public Session checkOutById(int sessionId, BigDecimal totalCharged) {
        Session active = sessionsDao.findActiveSessionById(sessionId);
        if (active == null) {
            throw new IllegalStateException("No active session for id " + sessionId);
        }
        Instant now = Instant.now();
        parkingSpotDao.setAvailability(active.getSpotId(), true);
        if (!sessionsDao.updateCheckout(sessionId, now, totalCharged)) {
            throw new IllegalStateException("Checkout failed or session already closed");
        }
        active.setCheckOutTime(now);
        active.setTotalCharged(totalCharged);
        return active;
    }

    @Transactional
    @Override
    public Session checkOutBySpotAndUser(int spotId, int userId) {
        Session active = sessionsDao.findActiveByUserAndSpot(userId, spotId);
        if (active == null) {
            return null;
        }
        Instant now = Instant.now();
        parkingSpotDao.setAvailability(spotId, true);
        if (!sessionsDao.updateCheckout(active.getSessionId(), now, active.getTotalCharged())) {
            throw new IllegalStateException("Checkout failed or session already closed");
        }
        active.setCheckOutTime(now);
        return active;
    }

    @Override
    public Session findActiveSessionById(int sessionId) {
        return sessionsDao.findActiveSessionById(sessionId);
    }
}
