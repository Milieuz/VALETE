package com.techelevator.dao;

import com.techelevator.model.vehicleapi.Make;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcMakeDao {
    private final JdbcTemplate jdbcTemplate;

    public JdbcMakeDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insertMake(Make make) {
        String sql = "INSERT INTO vehicle_make (nhtsa_make_id, make_name) VALUES (?, ?) ON CONFLICT (nhtsa_make_id) DO NOTHING";
        jdbcTemplate.update(sql, make.getMakeId(), make.getMakeName());
    }
}