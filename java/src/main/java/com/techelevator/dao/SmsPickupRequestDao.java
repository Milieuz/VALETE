package com.techelevator.dao;

import com.techelevator.model.parking.SmsPickupRequest;
import java.util.List;

public interface SmsPickupRequestDao {
    void create(SmsPickupRequest request);
    List<SmsPickupRequest> listPending();

}
