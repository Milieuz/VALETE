package com.techelevator.controller;

import com.techelevator.model.parking.ParkedVehicleDTO;
import com.techelevator.service.ParkedVehicleService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class ParkedVehicleController {

    private final ParkedVehicleService service;

    public ParkedVehicleController(ParkedVehicleService service) {
        this.service = service;
    }

    @GetMapping("/vehicles/parked")
    public List<ParkedVehicleDTO> getParkedVehicles(
            @RequestParam(required = false) Long lotId,
            @RequestParam(required = false) String color,
            @RequestParam(defaultValue = "spotId") String sort,
            @RequestParam(defaultValue = "asc") String order
    ) {
        return service.getParkedVehicles(lotId, color, sort, order);
    }

    // Adding the method of identical logic as above, but tied to non-Patron role user endpoint
    @RequestMapping(path = "/valet/parked", method=RequestMethod.GET)
    @PreAuthorize("hasAnyRole('VALET', 'ADMIN')")
    public List<ParkedVehicleDTO> getParkedVehiclesForValet (
            @RequestParam(required = false) Long lotId,
            @RequestParam(required = false) String color,
            @RequestParam(defaultValue = "spotId") String sort,
            @RequestParam(defaultValue = "asc") String order
    ) {
        return service.getParkedVehicles(lotId, color, sort, order);
    }
}
