package com.techelevator.dao;

import com.techelevator.model.parking.SpotStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class JdbcParkingSpotDao implements ParkingSpotDao {

    private final JdbcTemplate jdbcTemplate;

    public JdbcParkingSpotDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean lockAndCheckAvailable(int spotId) {
        String sql = "SELECT is_available FROM parking_spots WHERE id = ? FOR UPDATE";
        Boolean available = jdbcTemplate.queryForObject(sql, Boolean.class, spotId);
        if (available == null) {
            throw new IllegalArgumentException("Spot not found: " + spotId);
        }
        return available;
    }

    @Override
    public void setAvailability(int spotId, boolean isAvailable) {
        String sql = "UPDATE parking_spots SET is_available = ? WHERE id = ?";
        jdbcTemplate.update(sql, isAvailable, spotId);
    }

    private static final String AVAILABILITY_CONDITION = """
        FROM public.parking_spots ps
        WHERE ps.lot_id = ?
          AND ps.is_available = true
        """;

    @Override
    public long getAvailableCount(long lotId) {
        String sql = "SELECT COUNT(*) " + AVAILABILITY_CONDITION;
        return jdbcTemplate.queryForObject(sql, Long.class, lotId);
    }

    @Override
    public boolean isLotFull(long lotId) {
        return getAvailableCount(lotId) == 0;
    }

    @Override
    public List<SpotStatus> getSpotStatuses(long lotId) {
        String sql = """
            SELECT ps.id AS spot_id,
                   ps.is_available
            FROM public.parking_spots ps
            WHERE ps.lot_id = ?
            ORDER BY ps.spot_number
            """;

        List<SpotStatus> statuses = new ArrayList<>();
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sql, lotId);
        while (rs.next()) {
            statuses.add(new SpotStatus(
                    rs.getLong("spot_id"),
                    rs.getBoolean("is_available")
            ));
        }
        return statuses;
    }
}
