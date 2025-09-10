package com.techelevator.dao;

import com.techelevator.model.parking.SmsPickupRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class JdbcSmsPickupRequestDao implements SmsPickupRequestDao {
    private final JdbcTemplate jdbcTemplate;
    public JdbcSmsPickupRequestDao(JdbcTemplate jdbcTemplate) {

        this.jdbcTemplate = jdbcTemplate;

    }

    @Override
    public void create(SmsPickupRequest req) {

        String sql = "INSERT INTO sms_pickup_requests " +
                "(session_id, user_id, status, estimated_time) VALUES (?, ?, ?, ?)";

        jdbcTemplate.update(sql,
                req.getSessionId(),
                req.getUserId(),
                req.getStatus(),
                req.getEstimatedTime()

        );
    }

    @Override
    public List<SmsPickupRequest> listPending() {

        String sql = "SELECT * FROM sms_pickup_requests WHERE status = 'PENDING' ORDER BY requested_at";
        SqlRowSet rows = jdbcTemplate.queryForRowSet(sql);
        List<SmsPickupRequest> list = new ArrayList<>();

        while (rows.next()) {
            SmsPickupRequest req = new SmsPickupRequest();
            req.setRequestId(rows.getInt("request_id"));
            req.setSessionId(rows.getInt("session_id"));
            req.setUserId(rows.getInt("user_id"));
            req.setRequestedAt(rows.getTimestamp("requested_at").toLocalDateTime());
            req.setStatus(rows.getString("status"));
            req.setEstimatedTime(rows.getInt("estimated_time"));
            req.setValetNotifiedAt(rows.getTimestamp("valet_notified_at") != null
                    ? rows.getTimestamp("valet_notified_at").toLocalDateTime() : null);
            list.add(req);

        }

        return list;

    }
}
