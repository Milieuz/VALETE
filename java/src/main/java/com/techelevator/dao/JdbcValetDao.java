package com.techelevator.dao;

import com.techelevator.model.auth.User;
import com.techelevator.model.parking.Session;
import com.techelevator.model.userprofile.UserVehicle;
import com.techelevator.model.valet.ValetDetails;
import com.techelevator.model.valet.ValetTicketSummary;
import com.techelevator.model.valet.ValetVehicle;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Component
public class JdbcValetDao implements ValetDao {

    private final JdbcTemplate jdbc;

    public JdbcValetDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean isLotFull() {
        Integer free = jdbc.queryForObject(
                "SELECT COUNT(*) FROM parking_spots WHERE is_available = TRUE", Integer.class);
        return free == null || free == 0;
    }

    @Override
    public Spot pickFreeSpotOrThrow() {
        return jdbc.query(
                "SELECT id, spot_number FROM parking_spots WHERE is_available = TRUE ORDER BY spot_number LIMIT 1",
                (rs, i) -> new Spot(rs.getInt("id"), rs.getInt("spot_number"))
        ).stream().findFirst().orElseThrow(() -> new RuntimeException("Lot is full"));
    }

    @Override
    public void markSpotAvailability(int spotId, boolean isAvailable) {
        jdbc.update("UPDATE parking_spots SET is_available = ? WHERE id = ?", isAvailable, spotId);
    }

    @Override
    public User upsertOwner(String fullName, String phone) {
        Integer userId = jdbc.query(
                "SELECT user_id FROM users WHERE phone_number = ? LIMIT 1",
                (rs, i) -> rs.getInt("user_id"), phone
        ).stream().findFirst().orElse(null);

        if (userId == null) {
            KeyHolder kh = new GeneratedKeyHolder();
            jdbc.update(con -> {
                PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO users (username, password_hash, role, full_name, phone_number) " +
                                "VALUES (?, ?, ?, ?, ?) RETURNING user_id",
                        new String[]{"user_id"}
                );
                ps.setString(1, "patron_" + phone.replaceAll("\\D", ""));
                ps.setString(2, "{noop}");
                ps.setString(3, "ROLE_PATRON");
                ps.setString(4, fullName);
                ps.setString(5, phone);
                return ps;
            }, kh);
            userId = Objects.requireNonNull(kh.getKey()).intValue();
        } else {
            jdbc.update(
                    "UPDATE users SET full_name = COALESCE(?, full_name), phone_number = COALESCE(?, phone_number) WHERE user_id = ?",
                    fullName, phone, userId
            );
        }

        User u = new User();
        u.setId(userId);
        u.setFullName(fullName);
        u.setPhoneNumber(phone);
        return u;
    }

    @Override
    public UserVehicle upsertVehicle(int userId, ValetVehicle v) {
        Integer id = jdbc.query(
                "SELECT id FROM user_vehicle WHERE license_plate = ? LIMIT 1",
                (rs, i) -> rs.getInt("id"), v.licensePlate()
        ).stream().findFirst().orElse(null);

        if (id == null) {
            KeyHolder kh = new GeneratedKeyHolder();
            jdbc.update(con -> {
                PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO user_vehicle (user_id, make_name, model_name, color, license_plate, vin) " +
                                "VALUES (?, ?, ?, ?, ?, ?) RETURNING id",
                        new String[]{"id"}
                );
                ps.setInt(1, userId);
                ps.setString(2, v.makeName());
                ps.setString(3, v.modelName());
                ps.setString(4, v.color());
                ps.setString(5, v.licensePlate());
                ps.setString(6, v.vin());
                return ps;
            }, kh);
            id = Objects.requireNonNull(kh.getKey()).intValue();
        } else {
            jdbc.update(
                    "UPDATE user_vehicle SET user_id = ?, make_name = ?, model_name = ?, color = ?, vin = ? WHERE id = ?",
                    userId, v.makeName(), v.modelName(), v.color(), v.vin(), id
            );
        }

        UserVehicle uv = new UserVehicle();
        uv.setId(id);
        uv.setUserId(userId);
        uv.setMakeName(v.makeName());
        uv.setModelName(v.modelName());
        uv.setColor(v.color());
        uv.setLicensePlate(v.licensePlate());
        uv.setVin(v.vin());
        return uv;
    }

    @Override
    public Session checkIn(int userId, int spotId) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO sessions (user_id, spot_id, check_in_time) VALUES (?, ?, ?) RETURNING id",
                    new String[]{"id"}
            );
            ps.setInt(1, userId);
            ps.setInt(2, spotId);
            ps.setTimestamp(3, Timestamp.from(Instant.now()));
            return ps;
        }, kh);
        int id = Objects.requireNonNull(kh.getKey()).intValue();

        Session s = new Session();
        s.setSessionId(id); // fixed from setId(...)
        s.setUserId(userId);
        s.setSpotId(spotId);
        return s;
    }

    @Override
    public List<ValetTicketSummary> listParked(String q, String sortBy, String dir) {
        enum SortBy { SPOT, OWNER, PLATE, CHECKIN }
        enum Dir { ASC, DESC}

        SortBy orderCategory;
        Dir orderOrientation;
        try {
            orderCategory = SortBy.valueOf((sortBy == null ? "SPOT" : sortBy).toUpperCase());
        }
        catch (Exception e) {
            orderCategory = SortBy.SPOT;
        }
        orderOrientation = "DESC".equalsIgnoreCase(dir)? Dir.DESC : Dir.ASC;
        String order =
                switch (orderCategory) {
                    case OWNER -> "u.full_name";
                    case PLATE -> "uv.license_plate";
                    case CHECKIN -> "s.check_in_time";
                    case SPOT -> "ps.spot_number";
                } + " " + (orderOrientation == Dir.DESC ? "DESC" : "ASC");
               /* switch ((sortBy == null ? "spot" : sortBy) + ":" + (dir == null ? "asc" : dir)) {
                    case "owner:desc" -> "u.full_name DESC";
                    case "owner:asc" -> "u.full_name ASC";
                    case "plate:desc" -> "uv.license_plate DESC";
                    case "plate:asc" -> "uv.license_plate ASC";
                    case "checkin:desc" -> "s.check_in_time DESC";
                    case "checkin:asc" -> "s.check_in_time ASC";
                    case "spot:desc" -> "ps.spot_number DESC";
                    default -> "ps.spot_number ASC";
                };*/

        String like = (q == null || q.isBlank()) ? "%" : "%" + q + "%";
        boolean noFilter = "%".equals(like);

        String baseSql = """
            SELECT s.id AS session_id,
                   u.full_name,
                   u.phone_number,
                   ps.spot_number,
                   s.check_in_time,
                   uv.license_plate,
                   uv.make_name,
                   uv.model_name,
                   uv.color
            FROM sessions s
            JOIN users u ON u.user_id = s.user_id
            JOIN parking_spots ps ON ps.id = s.spot_id
            LEFT JOIN LATERAL (
                SELECT uv.*
                FROM user_vehicle uv
                WHERE uv.user_id = u.user_id
                ORDER BY uv.created_at DESC
                LIMIT 1
            ) uv ON TRUE
            WHERE s.check_out_time IS NULL
              AND (
                   ?::boolean OR
                   u.full_name ILIKE ? OR
                   COALESCE(uv.license_plate,'') ILIKE ? OR
                   COALESCE(uv.make_name,'') ILIKE ? OR
                   COALESCE(uv.model_name,'') ILIKE ?
              )
              """;
        String sql = baseSql + " ORDER BY " + order;

        return jdbc.query(sql, (rs, i) -> {
            ValetTicketSummary v = new ValetTicketSummary();
            v.ticketId = rs.getInt("session_id");
            v.ticketNumber = "VL-" + v.ticketId;
            v.ownerName = rs.getString("full_name");
            v.licensePlate = rs.getString("license_plate");
            v.makeName = rs.getString("make_name");
            v.modelName = rs.getString("model_name");
            v.color = rs.getString("color");
            v.spotNumber = rs.getInt("spot_number");
            v.status = "PARKED";
            Timestamp inTs = rs.getTimestamp("check_in_time");
            v.checkInTs = (inTs != null ? inTs.toInstant().toString() : null);
            return v;
        }, noFilter, like, like, like, like);
    }

    @Override
    public ValetDetails getDetails(int sessionId) {
        String sql = """
            SELECT s.id, s.check_in_time, s.check_out_time, s.total_charged,
                   u.full_name, u.phone_number, ps.spot_number,
                   uv.license_plate, uv.make_name, uv.model_name, uv.color
            FROM sessions s
            JOIN users u ON u.user_id = s.user_id
            JOIN parking_spots ps ON ps.id = s.spot_id
            LEFT JOIN LATERAL (
                SELECT uv.*
                FROM user_vehicle uv
                WHERE uv.user_id = u.user_id
                ORDER BY uv.created_at DESC
                LIMIT 1
            ) uv ON TRUE
            WHERE s.id = ?
        """;
        return jdbc.query(sql, rs -> {
            if (!rs.next()) return null;

            ValetDetails d = new ValetDetails();
            d.ticketId = rs.getInt("id");
            d.ticketNumber = "VL-" + d.ticketId;
            d.ownerName = rs.getString("full_name");
            d.phoneNumber = rs.getString("phone_number");
            d.spotNumber = rs.getInt("spot_number");
            d.licensePlate = rs.getString("license_plate");
            d.makeName = rs.getString("make_name");
            d.modelName = rs.getString("model_name");
            d.color = rs.getString("color");

            Timestamp inTs = rs.getTimestamp("check_in_time");
            Timestamp outTs = rs.getTimestamp("check_out_time");
            d.checkInTs = (inTs != null ? inTs.toInstant().toString() : null);
            d.checkOutTs = (outTs != null ? outTs.toInstant().toString() : null);

            Instant in = (inTs != null ? inTs.toInstant() : Instant.now());
            Instant end = (outTs != null ? outTs.toInstant() : Instant.now());
            long minutes = Duration.between(in, end).toMinutes();
            d.minutesParked = Math.max(0, minutes);

            BigDecimal totalCharged = (BigDecimal) rs.getObject("total_charged");
            d.amountDueCents = (totalCharged == null ? 0 : totalCharged.movePointRight(2).longValue());
            d.status = (outTs == null ? "PARKED" : "PICKED_UP");
            return d;
        }, sessionId);
    }

    @Override
    public void createPickupRequest(int sessionId /* int customerId*/, String notes) {
        /*jdbc.update(
                "INSERT INTO notifications (session_id, customer_id, requested_time, status, notes) VALUES (?, ?, now(), ?, ?)",
                sessionId, customerId, "REQUESTED", notes
        );*/
        // Check to make sure session for which pickup request is to be opened is active.
        Integer open = jdbc.queryForObject(
                "SELECT COUNT(*) FROM sessions WHERE id = ? AND check_out_time IS NULL",
                Integer.class, sessionId
        );
        // Exception thrown for when retrieval fails or the session is inactive.
        if(open == null || open == 0){
            throw new RuntimeException("Session is not active.");
        }

        // Extract the user id of the patron in the active session
        Integer customerId = jdbc.queryForObject(
                "SELECT user_id FROM sessions WHERE id = ?",
                Integer.class, sessionId
        );

        // Exception thrown for user retrieval failure
        if(customerId == null){
            throw new RuntimeException("No user found for session " + sessionId);
        }

        // Ruling out existing pickup request
        Integer exists = jdbc.queryForObject(
                "SELECT COUNT(*) FROM notifications " +
                        "WHERE session_id = ? AND status = 'REQUESTED' AND picked_up_time IS NULL",
                        Integer.class, sessionId
        );
        if(exists != null && exists > 0) {
            return;
        }
        // Now, insert the pickup request entry into notifications
        jdbc.update(
                "INSERT INTO notifications (session_id, customer_id, requested_time, status, notes) VALUES (?, ?, now(), ?, ?)",
                sessionId, customerId, "REQUESTED", notes
        );

    }

    @Override
    public List<ValetTicketSummary> listPickupRequests() {
        String sql = """
            SELECT n.session_id, u.full_name, uv.license_plate, uv.make_name, uv.model_name, uv.color,
                   ps.spot_number, s.check_in_time
            FROM notifications n
            JOIN sessions s ON s.id = n.session_id AND s.check_out_time IS NULL
            JOIN users u ON u.user_id = s.user_id
            JOIN parking_spots ps ON ps.id = s.spot_id
            LEFT JOIN LATERAL (
                SELECT uv.* FROM user_vehicle uv
                WHERE uv.user_id = u.user_id ORDER BY uv.created_at DESC LIMIT 1
            ) uv ON TRUE
            WHERE n.status = 'REQUESTED' AND n.picked_up_time IS NULL
            ORDER BY n.requested_time ASC
        """;
        return jdbc.query(sql, (rs, i) -> {
            ValetTicketSummary v = new ValetTicketSummary();
            v.ticketId = rs.getInt("session_id");
            v.ticketNumber = "VL-" + v.ticketId;
            v.ownerName = rs.getString("full_name");
            v.licensePlate = rs.getString("license_plate");
            v.makeName = rs.getString("make_name");
            v.modelName = rs.getString("model_name");
            v.color = rs.getString("color");
            v.spotNumber = rs.getInt("spot_number");
            v.status = "REQUESTED";
            Timestamp inTs = rs.getTimestamp("check_in_time");
            v.checkInTs = (inTs != null ? inTs.toInstant().toString() : null);
            return v;
        });
    }

    @Override
    public ValetDetails checkout(int sessionId, BigDecimal hourlyRate) {
        ValetDetails d = getDetails(sessionId);
        if (d == null) throw new RuntimeException("Session not found");

        long minutes = d.minutesParked;
        long hours = (minutes + 59) / 60;
        BigDecimal total = hourlyRate.multiply(BigDecimal.valueOf(hours));

        jdbc.update(
                "UPDATE sessions SET check_out_time = now(), total_charged = ? WHERE id = ?",
                total, sessionId
        );

        Integer spotId = jdbc.queryForObject(
                "SELECT spot_id FROM sessions WHERE id = ?", Integer.class, sessionId
        );
        if (spotId != null) markSpotAvailability(spotId, true);

        jdbc.update(
                "UPDATE notifications SET picked_up_time = now(), status = 'PICKED_UP' WHERE session_id = ? AND picked_up_time IS NULL",
                sessionId
        );

        ValetDetails out = getDetails(sessionId);
        out.amountDueCents = total.movePointRight(2).longValue();
        out.status = "PICKED_UP";
        return out;
    }

    public record Spot(int id, int number) {}
}
