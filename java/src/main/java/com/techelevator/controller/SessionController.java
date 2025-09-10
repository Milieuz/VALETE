package com.techelevator.controller;

import com.techelevator.model.parking.Session;
import com.techelevator.service.SessionsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/sessions")
public class SessionController {

    private final SessionsService sessionsService;

    public SessionController(SessionsService sessionsService) {
        this.sessionsService = sessionsService;
    }

    @PostMapping("/checkin")
    public Session checkIn(
            @RequestParam int spotId,
            @RequestParam int userId,
            @RequestParam int vehicleId
    ) {
        return sessionsService.checkInVehicle(spotId, userId, vehicleId);
    }

    @PostMapping("/{id}/checkout")
    public Session checkOutById(
            @PathVariable int id,
            @RequestParam BigDecimal totalCharged
    ) {
        return sessionsService.checkOutById(id, totalCharged);
    }

    @PostMapping("/checkout")
    public ResponseEntity<?> checkoutBySpotAndUser(
            @RequestParam int spotId,
            @RequestParam int userId
    ) {
        Session ended = sessionsService.checkOutBySpotAndUser(spotId, userId);

        if (ended == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("No active session found for this spot/user.");
        }

        return ResponseEntity.ok(ended);
    }
}
