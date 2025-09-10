package com.techelevator.controller;

import com.techelevator.model.parking.SmsPickupRequest;
import com.techelevator.service.SmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sms")
@CrossOrigin
public class SmsController {

    @Autowired
    private SmsService smsService;

//Twilo Webhook
    @PostMapping("/webhook")
    public ResponseEntity<String> handleIncomingSms(

            @RequestParam("From") String from,
            @RequestParam("Body") String body) {

        smsService.processIncomingSms(from, body);

        // Return empty TwiML so Twilio stops (Twilio markup language)

        String twiml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Response></Response>";

        return ResponseEntity.ok()
                .header("Content-Type", "application/xml")
                .body(twiml);

    }

//List SMS requests to valets
    @GetMapping("/pickup-requests")
    public ResponseEntity<List<SmsPickupRequest>> getPickupRequests() {
        List<SmsPickupRequest> list = smsService.getPendingPickupRequests();
        return ResponseEntity.ok(list);
    }
}
