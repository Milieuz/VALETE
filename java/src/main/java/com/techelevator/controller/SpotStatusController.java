package com.techelevator.controller;

import com.techelevator.model.parking.SpotStatus;
import com.techelevator.service.ParkingSpotService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/available")
public class SpotStatusController {

    private final ParkingSpotService parkingSpotService;

    public SpotStatusController(ParkingSpotService parkingSpotService) {
        this.parkingSpotService = parkingSpotService;
    }

    // GET /available/spots?lotId=...
    @GetMapping(path = "/spots", params = "lotId")
    public List<SpotStatus> getSpotStatuses(@RequestParam long lotId) {
        return parkingSpotService.getSpotStatuses(lotId);
    }

    // GET /available/count?lotId=...
    @GetMapping(path = "/count", params = "lotId")
    public long getAvailableCount(@RequestParam long lotId) {
        return parkingSpotService.getAvailableSpotCount(lotId);
    }

    // GET /available/full?lotId=...
    @GetMapping(path = "/full", params = "lotId")
    public boolean isLotFull(@RequestParam long lotId) {
        return parkingSpotService.isLotFull(lotId);
    }
}
