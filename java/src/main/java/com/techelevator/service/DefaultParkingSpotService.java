package com.techelevator.service;

import com.techelevator.dao.ParkingSpotDao;
import com.techelevator.model.parking.SpotStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DefaultParkingSpotService implements ParkingSpotService {

    private final ParkingSpotDao dao;

    public DefaultParkingSpotService(ParkingSpotDao dao) {
        this.dao = dao;
    }

    @Override
    public long getAvailableSpotCount(long lotId) {
        return dao.getAvailableCount(lotId);
    }

    @Override
    public boolean isLotFull(long lotId) {
        return dao.isLotFull(lotId);
    }

    @Override
    public List<SpotStatus> getSpotStatuses(long lotId) {
        return dao.getSpotStatuses(lotId);
    }
}
