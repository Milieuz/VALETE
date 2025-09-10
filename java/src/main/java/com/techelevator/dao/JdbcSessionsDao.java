package com.techelevator.dao;

import com.techelevator.model.parking.Session;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;

@Repository
public class JdbcSessionsDao implements SessionsDao {

    private final JdbcTemplate jdbcTemplate;

    public JdbcSessionsDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public int insertSession(int userId, int spotId, Instant checkInTime, Integer vehicleId) {
        String sql = """
            INSERT INTO sessions (user_id, spot_id, check_in_time, vehicle_id)
            VALUES (?, ?, ?, ?)
            RETURNING id
        """;
        return jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                userId,
                spotId,
                Timestamp.from(checkInTime),
                vehicleId
        );
    }

    @Override
    public boolean existsActiveBySpot(int spotId) {
        String sql = """
            SELECT EXISTS (
                SELECT 1
                FROM sessions
                WHERE spot_id = ? AND check_out_time IS NULL
            )
        """;
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, spotId));
    }

    @Override
    public boolean existsActiveByUserAndSpot(int userId, int spotId) {
        String sql = """
            SELECT EXISTS (
                SELECT 1
                FROM sessions
                WHERE user_id = ? AND spot_id = ? AND check_out_time IS NULL
            )
        """;
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, userId, spotId));
    }

    @Override
    public boolean updateCheckout(int sessionId, Instant checkOutTime, BigDecimal totalCharged) {
        String sql = """
            UPDATE sessions
            SET check_out_time = ?, total_charged = ?
            WHERE id = ? AND check_out_time IS NULL
        """;
        return jdbcTemplate.update(sql, Timestamp.from(checkOutTime), totalCharged, sessionId) > 0;
    }

    @Override
    public Session findActiveSessionById(int sessionId) {
        String sql = """
            SELECT id, spot_id, user_id, vehicle_id, check_in_time, check_out_time, total_charged
            FROM sessions
            WHERE id = ? AND check_out_time IS NULL
        """;
        return jdbcTemplate.query(sql, rs -> rs.next() ? mapRow(rs) : null, sessionId);
    }

    @Override
    public Session findActiveByUserAndSpot(int userId, int spotId) {
        String sql = """
        SELECT id, spot_id, user_id, vehicle_id, check_in_time, check_out_time, total_charged
        FROM sessions
        WHERE user_id = ? AND spot_id = ? AND check_out_time IS NULL
        ORDER BY id DESC
        LIMIT 1
    """;
        return jdbcTemplate.query(sql,
                rs -> rs.next() ? mapRow(rs) : null, // ✅ ternary has both outcomes
                userId, spotId
        );
    }


    private Session mapRow(ResultSet rs) throws SQLException {
        Session session = new Session();
        session.setSessionId(rs.getInt("id"));
        session.setSpotId(rs.getInt("spot_id"));
        session.setUserId(rs.getInt("user_id"));
        session.setVehicleId((Integer) rs.getObject("vehicle_id"));
        Timestamp inTs = rs.getTimestamp("check_in_time");
        if (inTs != null) session.setCheckInTime(inTs.toInstant());
        Timestamp outTs = rs.getTimestamp("check_out_time");
        if (outTs != null) session.setCheckOutTime(outTs.toInstant());
        session.setTotalCharged((BigDecimal) rs.getObject("total_charged"));
        return session;
    }
}
