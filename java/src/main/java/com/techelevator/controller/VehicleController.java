package com.techelevator.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.techelevator.dao.JdbcUserVehicleDao;
import com.techelevator.dao.UserDao;
import com.techelevator.model.vehicleapi.Make;
import com.techelevator.model.vehicleapi.MakeResponse;
import com.techelevator.model.vehicleapi.Model;
import com.techelevator.model.vehicleapi.ModelResponse;
import com.techelevator.model.auth.User;
import com.techelevator.model.userprofile.UserVehicle;

@RestController
@CrossOrigin
public class VehicleController {

        @Value("https://vpic.nhtsa.dot.gov/api/vehicles")
        private String API_BASE_URL;

        private final RestClient restClient = RestClient.create();
        private final JdbcUserVehicleDao userVehicleDao;
        private final UserDao userDao;

        public VehicleController(JdbcUserVehicleDao userVehicleDao,UserDao userDao) {
                this.userVehicleDao = userVehicleDao;
                this.userDao = userDao;
        }

        @GetMapping("/vehicles")
        public List<Make> getAllMakes() {
                List<Make> results = new ArrayList<>();
                try {
                        MakeResponse apiData = restClient.get()
                                .uri(API_BASE_URL + "/getallmakes?format=json")
                                .retrieve()
                                .body(MakeResponse.class);
                        if (apiData != null && apiData.getResults() != null) {
                                results = apiData.getResults();
                        } else {
                                System.out.println("Received null or empty results from NHTSA.");
                                System.out.println("Full API data: " + apiData);
                                System.out.println("Results: " + apiData.getResults());
                        }
                } catch (RestClientResponseException | ResourceAccessException e) {
                        System.out.println("Error fetching makes: " + e.getMessage());
                }
                return results;
        }

        @GetMapping("/vehicles/{make}/models")
        public List<Model> getModelsForMake(@PathVariable String make) {
                List<Model> results = new ArrayList<>();
                try {
                        ModelResponse apiData = restClient.get()
                                .uri(API_BASE_URL + "/getmodelsformake/" + make + "?format=json")
                                .retrieve()
                                .body(ModelResponse.class);
                        if (apiData != null && apiData.getResults() != null) {
                                results = apiData.getResults();
                        } else {
                                System.out.println("Received null or empty results for models of make: " + make);
                        }
                } catch (RestClientResponseException | ResourceAccessException e) {
                        System.out.println("Error fetching models for " + make + ": " + e.getMessage());
                }
                return results;
        }

        @PostMapping("/vehicles/save")
        public ResponseEntity<UserVehicle> saveVehicle(@RequestBody UserVehicle req, Principal principal) {
                Integer userId = null;
                if (principal != null) {
                        User u = userDao.getUserByUsername(principal.getName());
                        if (u != null) userId = u.getId();
                }

                UserVehicle toSave = new UserVehicle();
                toSave.setUserId(userId);
                toSave.setMakeName(req.getMakeName());
                toSave.setModelName(req.getModelName());
                toSave.setColor(req.getColor());
                toSave.setLicensePlate(req.getLicensePlate());
                toSave.setVin(req.getVin());

                UserVehicle saved = userVehicleDao.insert(toSave);
                return ResponseEntity.ok(saved);
        }

        @GetMapping("/vehicles/mine")
        public List<UserVehicle> getMyVehicles(Principal principal) {
                if (principal == null) {
                        return new ArrayList<>();
                }
                int userId = userDao.findIdByUsername(principal.getName());
                return userVehicleDao.findByUserId(userId);
        }

}
