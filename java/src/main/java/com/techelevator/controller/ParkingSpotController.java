package com.techelevator.controller;

import com.techelevator.model.parking.SpotStatus;
import com.techelevator.service.ParkingSpotService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/parking-spots")
public class ParkingSpotController {

    private final ParkingSpotService parkingSpotService;

    public ParkingSpotController(ParkingSpotService parkingSpotService) {
        this.parkingSpotService = parkingSpotService;
    }

    @GetMapping("/availability")
    public Map<String, Object> getAvailability(@RequestParam long lotId) {
        long count = parkingSpotService.getAvailableSpotCount(lotId);
        boolean isFull = parkingSpotService.isLotFull(lotId);

        return Map.of(
                "availableCount", count,
                "isFull", isFull
        );
    }

    @GetMapping("/status")
    public List<SpotStatus> getSpotStatuses(@RequestParam long lotId) {
        return parkingSpotService.getSpotStatuses(lotId);
    }

}
