package com.techelevator.controller;

import com.techelevator.dao.ValetDao;
import com.techelevator.model.valet.ValetDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@CrossOrigin
@RequestMapping("/patron")
public class PatronController {

    private static final BigDecimal HOURLY_RATE = new BigDecimal("5.00");

    private final ValetDao valetDao;

    public PatronController(ValetDao valetDao) {
        this.valetDao = valetDao;
    }

    @PostMapping("/pickup-request/{sessionId}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> requestPickup(@PathVariable int sessionId) {
        try {
            valetDao.createPickupRequest(sessionId, null);
            // Return a current estimate so the UI can show totals immediately
            ValetDetails d = valetDao.getDetails(sessionId);
            if (d == null) {
                return ResponseEntity.badRequest().body(new ApiMessage("Session not found."));
            }
            long hours = (d.minutesParked + 59) / 60;
            d.amountDueCents = HOURLY_RATE.multiply(BigDecimal.valueOf(hours)).movePointRight(2).longValue();
            return ResponseEntity.ok(d);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(new ApiMessage(ex.getMessage()));
        }
    }

    @GetMapping("/estimate/{sessionId}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> estimate(@PathVariable int sessionId) {
        ValetDetails d = valetDao.getDetails(sessionId);
        if (d == null) {
            return ResponseEntity.badRequest().body(new ApiMessage("No active session for this ticket."));
        }
        long hours = (d.minutesParked + 59) / 60;
        d.amountDueCents = HOURLY_RATE.multiply(BigDecimal.valueOf(hours)).movePointRight(2).longValue();
        return ResponseEntity.ok(d);
    }

    static class ApiMessage {
        public final String message;
        ApiMessage(String message) { this.message = message; }
    }
}
