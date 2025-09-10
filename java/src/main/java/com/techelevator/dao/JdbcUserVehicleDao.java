package com.techelevator.dao;

import com.techelevator.exception.DaoException;
import com.techelevator.model.userprofile.UserVehicle;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class JdbcUserVehicleDao {

    private final JdbcTemplate jdbcTemplate;

    public JdbcUserVehicleDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public UserVehicle insert(UserVehicle v) {
        final String sql = """
            INSERT INTO user_vehicle (user_id, make_name, model_name, color, license_plate, vin)
            VALUES (?, ?, ?, ?, ?, ?)
            RETURNING id
        """;

        try {
            Integer id = jdbcTemplate.queryForObject(
                    sql,
                    Integer.class,
                    v.getUserId(),
                    v.getMakeName(),
                    v.getModelName(),
                    v.getColor(),
                    v.getLicensePlate(),
                    v.getVin()
            );
            v.setId(id);
            return v;
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }
    }

    public List<UserVehicle> findByUserId(int userId) {
        String sql = "SELECT id, user_id, make_name, model_name, color, license_plate, vin " +
                "FROM user_vehicle WHERE user_id = ? ORDER BY id DESC";
        try {
            return jdbcTemplate.query(sql, (rs, rowNum) -> {
                UserVehicle v = new UserVehicle();
                v.setId(rs.getInt("id"));
                v.setUserId(rs.getInt("user_id"));
                v.setMakeName(rs.getString("make_name"));
                v.setModelName(rs.getString("model_name"));
                v.setColor(rs.getString("color"));
                v.setLicensePlate(rs.getString("license_plate"));
                v.setVin(rs.getString("vin"));
                return v;
            }, userId);
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }
    }
}
