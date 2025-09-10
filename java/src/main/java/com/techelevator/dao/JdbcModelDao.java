package com.techelevator.dao;

import com.techelevator.model.vehicleapi.Model;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcModelDao {
    private final JdbcTemplate jdbcTemplate;

    public JdbcModelDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insertModel(Model model) {
        String sql = "INSERT INTO vehicle_model (nhtsa_make_id, model_name) VALUES (?, ?)";
        jdbcTemplate.update(sql, model.getMakeId(), model.getModelId());
    }
}