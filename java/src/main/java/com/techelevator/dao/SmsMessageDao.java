package com.techelevator.dao;

import com.techelevator.model.parking.SmsMessage;
import java.util.List;

public interface SmsMessageDao {
    void create(SmsMessage sms);
    List<SmsMessage> listBySessionId(int sessionId);

}

