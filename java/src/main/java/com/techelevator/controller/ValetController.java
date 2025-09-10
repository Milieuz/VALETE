package com.techelevator.controller;

import com.techelevator.dao.ValetDao;
import com.techelevator.model.valet.ValetCheckInRequest;
import com.techelevator.model.valet.ValetDetails;
import com.techelevator.model.valet.ValetTicketSummary;
import com.techelevator.model.valet.ValetVehicle;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/valet")
@PreAuthorize("hasAnyRole('VALET', 'ADMIN')")
public class ValetController {

    private final ValetDao valetDao;
    public ValetController(ValetDao valetDao) { this.valetDao = valetDao; }

    @PostMapping("/check-in")
    public ResponseEntity<?> checkIn(@RequestBody ValetCheckInRequest req) {
        if (valetDao.isLotFull()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Lot is full"));
        }
        var spot = valetDao.pickFreeSpotOrThrow();
        var owner = valetDao.upsertOwner(req.fullName, req.phoneNumber);
        valetDao.upsertVehicle(owner.getId(), new ValetVehicle(
                req.makeName, req.modelName, req.color, req.licensePlate, req.vin
        ));
        valetDao.markSpotAvailability(spot.id(), false);
        var session = valetDao.checkIn(owner.getId(), spot.id());
        var details = valetDao.getDetails(session.getSessionId()); // ✅ fixed here
        return ResponseEntity.ok(details);
    }

    /*@GetMapping("/parked")
    public List<ValetTicketSummary> parked(@RequestParam(required = false) String q,
                                           @RequestParam(defaultValue = "spot") String sortBy,
                                           @RequestParam(defaultValue = "asc") String dir) {
        return valetDao.listParked(q, sortBy, dir);
    }*/

    @GetMapping("/ticket/{sessionId}")
    public ValetDetails ticket(@PathVariable int sessionId) {
        return valetDao.getDetails(sessionId);
    }

    @PostMapping("/pickup/{sessionId}")
    public ResponseEntity<?> pickup(@PathVariable int sessionId, @RequestBody(required = false) Map<String,String> body) {

        Integer customerId = null;

        customerId = (Integer) valetDao
                .listParked(null, "spot", "asc").stream()
                .filter(v -> v.ticketId == sessionId).findFirst()
                .map(v -> {
                    // fetch actual user id if needed; simplified for brevity
                    return null;
                }).orElse(null);

        valetDao.createPickupRequest(sessionId,
                body != null ? body.getOrDefault("notes","") : "");
        return ResponseEntity.ok(Map.of("message", "Pickup requested"));
    }

    @GetMapping("/pickups")
    public List<ValetTicketSummary> pickups() {
        return valetDao.listPickupRequests();
    }

    @PostMapping("/checkout/{sessionId}")
    public ValetDetails checkout(@PathVariable int sessionId) {
        // Example $5.00/hour - change as needed
        return valetDao.checkout(sessionId, new BigDecimal("5.00"));
    }
}
