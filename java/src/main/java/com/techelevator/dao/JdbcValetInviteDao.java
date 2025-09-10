package com.techelevator.dao;

import com.techelevator.exception.DaoException;
import com.techelevator.model.valet.ValetInvite;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
public class JdbcValetInviteDao implements ValetInviteDao{
    private final JdbcTemplate jdbcTemplate;

    public JdbcValetInviteDao(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }
    @Override
    public int makeNewInvite(String valetSecretHash, Instant expirationTime, int useLimit, int creatorId) {
        String sql = "INSERT INTO valet_invite (valet_secret_hash, expiration_time, use_limit, use_count, is_revoked, " +
                "creator) " + "VALUES(?,?,?,0,FALSE,?) RETURNING id;";

        int id = jdbcTemplate.queryForObject(sql,
                int.class, valetSecretHash, Timestamp.from(expirationTime), useLimit, creatorId);

        if(id == 0) {
            throw new DaoException("Failed to make a new valet invite (invalid id).");
        }
        return id;
    }

    @Override
    public ValetInvite getActiveInviteById(int id) {
        String sql = "SELECT id, valet_secret_hash, expiration_time, use_limit, use_count, is_revoked, creation_time "
                + "FROM valet_invite "
                + "WHERE id = ? AND is_revoked = FALSE AND now() < expiration_time AND use_count < use_limit;";

        SqlRowSet inviteRow = jdbcTemplate.queryForRowSet(sql, id);

        if(inviteRow.next()){
            return mapRowToInvite(inviteRow);
        }

        return null;
    }

    @Override
    public boolean markUsed(int id) {
        String sql = "UPDATE valet_invite SET use_count = use_count + 1 "
                + "WHERE id = ? AND is_revoked = FALSE AND now() < expiration_time AND use_count < use_limit;";

        int numRowsAffected = jdbcTemplate.update(sql, id);
        return numRowsAffected == 1;
    }

    @Override
    public void revokeInvite(int id) {
        String sql = "UPDATE valet_invite SET is_revoked = TRUE "
                + "WHERE id = ?;";

        jdbcTemplate.update(sql, id);
    }

    @Override
    public int deleteInvite(int id){
        String sql = "DELETE FROM valet_invite WHERE id=?;";
        int numRowsAffected = jdbcTemplate.update(sql, id);
        return numRowsAffected;
    }

    @Override
    public int flushExpiredInvite() {
        String sql = "DELETE FROM valet_invite "
                + "WHERE (now() >= expiration_time) OR (use_count >= use_limit);";
        int numRowsAffected = jdbcTemplate.update(sql);
        return numRowsAffected;
    }

    @Override
    public List<ValetInvite> findAllInvites(boolean isActive){
        List<ValetInvite> allInvitesList = new ArrayList<>();
        String baseSql = "SELECT id, valet_secret_hash, expiration_time, use_limit, use_count, is_revoked, creator, " +
                "creation_time FROM valet_invite ";
        String sql = isActive ? baseSql + "WHERE is_revoked = FALSE AND now() < expiration_time AND use_count < use_limit " +
                "ORDER BY creation_time DESC;"
                : baseSql + "ORDER BY creation_time DESC";

        SqlRowSet inviteRows = jdbcTemplate.queryForRowSet(sql);
        while(inviteRows.next()){
            allInvitesList.add(mapRowToInvite(inviteRows));
        }
        return allInvitesList;
    }

    private ValetInvite mapRowToInvite(SqlRowSet inviteRow){
        ValetInvite vInvite = new ValetInvite();
        vInvite.setId(inviteRow.getInt("id"));
        vInvite.setValetSecretHash(inviteRow.getString("valet_secret_hash"));
        vInvite.setExpirationTime(inviteRow.getTimestamp("expiration_time").toInstant());
        vInvite.setUseLimit(inviteRow.getInt("use_limit"));
        vInvite.setUseCount(inviteRow.getInt("use_count"));
        vInvite.setIsRevoked(inviteRow.getBoolean("is_revoked"));

        int creator = inviteRow.getInt("creator");
        vInvite.setCreator(inviteRow.wasNull() ? 0 : creator);

        Timestamp creationTime = inviteRow.getTimestamp("creation_time");
        vInvite.setCreationTime(creationTime != null? creationTime.toInstant() : null);
        return vInvite;
    }
}
