package com.techelevator.service;

import com.techelevator.dao.JdbcParkedVehicleDao;
import com.techelevator.model.parking.ParkedVehicleDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ParkedVehicleService {

    private final JdbcParkedVehicleDao dao;

    public ParkedVehicleService(JdbcParkedVehicleDao dao) {
        this.dao = dao;
    }

    public List<ParkedVehicleDTO> getParkedVehicles(Long lotId, String color, String sort, String order) {
        return dao.findParked(lotId, color, sort, order);
    }

    public List<ParkedVehicleDTO> getActiveByLot(long lotId) {
        return dao.findActiveByLot(lotId);
    }
}
