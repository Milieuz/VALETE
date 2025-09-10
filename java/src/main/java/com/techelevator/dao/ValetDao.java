package com.techelevator.dao;

import com.techelevator.model.auth.User;
import com.techelevator.model.parking.Session;
import com.techelevator.model.userprofile.UserVehicle;
import com.techelevator.model.valet.ValetDetails;
import com.techelevator.model.valet.ValetTicketSummary;
import com.techelevator.model.valet.ValetVehicle;

import java.util.List;

public interface ValetDao {
    boolean isLotFull();
    JdbcValetDao.Spot pickFreeSpotOrThrow();
    User upsertOwner(String fullName, String phone);
    UserVehicle upsertVehicle(int userId, ValetVehicle v);
    Session checkIn(int userId, int spotId);
    void markSpotAvailability(int spotId, boolean isAvailable);

    List<ValetTicketSummary> listParked(String q, String sortBy, String dir);
    ValetDetails getDetails(int sessionId);

    void createPickupRequest(int sessionId, /*int customerId*/ String notes);
    List<ValetTicketSummary> listPickupRequests();

    ValetDetails checkout(int sessionId, java.math.BigDecimal hourlyRate);
}
