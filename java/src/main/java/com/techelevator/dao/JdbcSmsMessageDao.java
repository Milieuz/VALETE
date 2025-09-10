package com.techelevator.dao;

import com.techelevator.model.parking.SmsMessage;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class JdbcSmsMessageDao implements SmsMessageDao {

    private final JdbcTemplate jdbcTemplate;

    public JdbcSmsMessageDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void create(SmsMessage sms) {
        String sql = "INSERT INTO sms_messages " +
                "(session_id, user_id, phone_number, message_type, message_content, direction, status, twilio_sid) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.update(sql,
                sms.getSessionId(),
                sms.getUserId(),
                sms.getPhoneNumber(),
                sms.getMessageType(),
                sms.getMessageContent(),
                sms.getDirection(),
                sms.getStatus(),
                sms.getTwilioSid()

        );
    }

    @Override
    public List<SmsMessage> listBySessionId(int sessionId) {

        String sql = "SELECT * FROM sms_messages WHERE session_id = ? ORDER BY sent_at";
        SqlRowSet rows = jdbcTemplate.queryForRowSet(sql, sessionId);
        List<SmsMessage> messages = new ArrayList<>();

        while (rows.next()) {

            SmsMessage sms = new SmsMessage();
            sms.setMessageId(rows.getInt("message_id"));
            sms.setSessionId(rows.getInt("session_id"));
            sms.setUserId(rows.getInt("user_id"));
            sms.setPhoneNumber(rows.getString("phone_number"));
            sms.setMessageType(rows.getString("message_type"));
            sms.setMessageContent(rows.getString("message_content"));
            sms.setDirection(rows.getString("direction"));
            sms.setSentAt(rows.getTimestamp("sent_at").toLocalDateTime());
            sms.setStatus(rows.getString("status"));
            sms.setTwilioSid(rows.getString("twilio_sid"));
            messages.add(sms);

        }

        return messages;

    }
}
