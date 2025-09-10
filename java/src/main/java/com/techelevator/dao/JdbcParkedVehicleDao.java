package com.techelevator.dao;

import com.techelevator.model.parking.ParkedVehicleDTO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class JdbcParkedVehicleDao {

    private final JdbcTemplate jdbcTemplate;

    public JdbcParkedVehicleDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<ParkedVehicleDTO> MAPPER = new RowMapper<>() {
        @Override
        public ParkedVehicleDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
            ParkedVehicleDTO dto = new ParkedVehicleDTO();
            dto.setReservationId(rs.getLong("reservation_id"));
            dto.setSpotId(rs.getLong("spot_id"));
            dto.setLotId(rs.getLong("lot_id"));
            dto.setLicensePlate(rs.getString("license_plate"));
            dto.setMake(rs.getString("make"));
            dto.setModel(rs.getString("model"));
            dto.setColor(rs.getString("color"));
            dto.setStartTime(rs.getObject("start_time", OffsetDateTime.class));
            dto.setEndTime(rs.getObject("end_time", OffsetDateTime.class));
            dto.setOwnerName(rs.getString("owner_name"));

            long uid = rs.getLong("user_id");
            dto.setUserId(rs.wasNull() ? 0L : uid);

            return dto;
        }
    };

    public List<ParkedVehicleDTO> findParked(Long lotId, String color, String sort, String order) {
        StringBuilder sql = new StringBuilder("""
            SELECT
                s.id AS reservation_id,
                s.spot_id,
                ps.lot_id,
                uv.license_plate,
                uv.make_name AS make,
                uv.model_name AS model,
                uv.color,
                s.check_in_time AS start_time,
                s.check_out_time AS end_time,
                s.user_id,
                u.full_name AS owner_name
            FROM sessions s
            JOIN parking_spots ps ON ps.id = s.spot_id
            LEFT JOIN user_vehicle uv ON uv.id = s.vehicle_id
            LEFT JOIN users u ON u.user_id = s.user_id
            WHERE s.check_out_time IS NULL
        """);

        List<Object> params = new ArrayList<>();

        if (lotId != null) {
            sql.append(" AND ps.lot_id = ? ");
            params.add(lotId);
        }
        if (color != null && !color.isBlank()) {
            sql.append(" AND LOWER(uv.color) = LOWER(?) ");
            params.add(color.trim());
        }

        Map<String, String> sortCols = Map.of(
                "spotId", "s.spot_id",
                "licensePlate", "uv.license_plate",
                "startTime", "s.check_in_time",
                "endTime", "s.check_out_time",
                "lotId", "ps.lot_id",
                "make", "uv.make_name",
                "model", "uv.model_name",
                "color", "uv.color"
        );
        String sortCol = sortCols.getOrDefault(sort, "s.check_in_time");
        String sortDir = "DESC".equalsIgnoreCase(order) ? "DESC" : "ASC";

        sql.append(" ORDER BY ").append(sortCol).append(" ").append(sortDir);
        return jdbcTemplate.query(sql.toString(), MAPPER, params.toArray());
    }

    public List<ParkedVehicleDTO> findActiveByLot(long lotId) {
        return findParked(lotId, null, "startTime", "DESC");
    }
}
