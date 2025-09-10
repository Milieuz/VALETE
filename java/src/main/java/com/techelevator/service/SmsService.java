package com.techelevator.service;

import com.techelevator.dao.SmsMessageDao;
import com.techelevator.dao.SmsPickupRequestDao;
import com.techelevator.dao.SessionsDao;
import com.techelevator.dao.JdbcUserDao;
import com.techelevator.model.parking.SmsMessage;
import com.techelevator.model.parking.SmsPickupRequest;
import com.techelevator.model.parking.Session;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SmsService {
    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.phone.number}")
    private String twilioPhoneNumber;

    @Autowired
    private SessionsDao sessionsDao;

    @Autowired
    private JdbcUserDao userDao;

    @Autowired
    private SmsMessageDao smsMessageDao;

    @Autowired
    private SmsPickupRequestDao smsPickupRequestDao;

  //Initialize Twilio

    @EventListener(ApplicationReadyEvent.class)
    public void initTwilio() {
        Twilio.init(accountSid, authToken);

    }

// Welcome SMS
    public void sendWelcomeSms(int sessionId, int userId, String customerName) {

        String phone = userDao.getUserById(userId).getPhoneNumber();
        String content = String.format(

                "🚗 Welcome, %s! Ticket: %d. Rate: $5/hr. Reply READY %d to request your car.",
                customerName, sessionId, sessionId

        );
        Message msg = Message.creator(
                new PhoneNumber(phone),
                new PhoneNumber(twilioPhoneNumber),
                content
        ).create();

        SmsMessage sms = new SmsMessage(
                0,
                sessionId,
                userId,
                phone,
                "WELCOME",
                content,
                "OUTBOUND",
                LocalDateTime.now(),
                "SENT",
                msg.getSid()
        );

        smsMessageDao.create(sms);

    }

   //Incoming SMS
    public void processIncomingSms(String fromPhone, String messageBody) {

        // Log inbound SMS
        SmsMessage inbound = new SmsMessage(
                0,
                null,
                null,
                fromPhone,
                "PICKUP_REQUEST",
                messageBody,
                "INBOUND",
                LocalDateTime.now(),
                "RECEIVED",
                null
        );

        smsMessageDao.create(inbound);

        // Parse ticket number

        String[] parts = messageBody.trim().split("\\s+");
        int sessionId = Integer.parseInt(parts[1]);

        // Fetch session

        Session session = sessionsDao.findActiveSessionById(sessionId);
        int userId = session.getUserId();

        // Create and log pickup request

        SmsPickupRequest req = new SmsPickupRequest(
                0,
                sessionId,
                userId,
                LocalDateTime.now(),
                "PENDING",
                10,
                null
        );

        smsPickupRequestDao.create(req);

        // Send confirmation SMS

        String confirmation = String.format(

                "✅ Got it! Bringing your car (Ticket %d). ETA: 10 min.", sessionId
        );

        Message msg = Message.creator(
                new PhoneNumber(fromPhone),
                new PhoneNumber(twilioPhoneNumber),
                confirmation
        ).create();

        SmsMessage outbound = new SmsMessage(
                0,
                sessionId,
                userId,
                fromPhone,
                "PICKUP_CONFIRMATION",
                confirmation,
                "OUTBOUND", 
                LocalDateTime.now(),
                "SENT",
                msg.getSid()

        );
        smsMessageDao.create(outbound);

    }

   //Send car Ready SMS when valet says ready
    public void sendCarReadySms(int sessionId, int userId) {

        String phone = userDao.getUserById(userId).getPhoneNumber();
        String content = String.format(
                "🎉 Your car (Ticket %d) is ready for pickup!", sessionId
        );

        Message msg = Message.creator(
                new PhoneNumber(phone),
                new PhoneNumber(twilioPhoneNumber),
                content
        ).create();

        SmsMessage sms = new SmsMessage(
                0,
                sessionId,
                userId,
                phone,
                "CAR_READY",
                content,
                "OUTBOUND",
                LocalDateTime.now(),
                "SENT",
                msg.getSid()
        );

        smsMessageDao.create(sms);

    }

//Pending Pickup requests for valets
    public List<SmsPickupRequest> getPendingPickupRequests() {
        return smsPickupRequestDao.listPending();
    }
}
