package com.festena.databases;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Component
public class PlayersResDB implements IDataBase {

    @Value("${db.url}")
    private String url;

    @Value("${db.username}")
    private String username;

    @Value("${db.password}")
    private String password;

    public static final String GOLD_KEY = "gold";
    public static final String FOOD_KEY = "food";
    public static final String REPUTATION_KEY = "respect";
    public static final String ARMY_KEY = "army";
    public static final String PEOPLE_KEY = "people";
    public static final String TECHNOLOGY_KEY = "technology";

    private static final Set<String> ALLOWED_FIELDS = Set.of(
            GOLD_KEY, PEOPLE_KEY, REPUTATION_KEY, FOOD_KEY, ARMY_KEY, TECHNOLOGY_KEY
    );

    public PlayersResDB() {
    }

    @PostConstruct
    private void init() {
        createTable();
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    private void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS players (" +
                "chatId BIGINT PRIMARY KEY," +
                "gold INT DEFAULT 0," +
                "people INT DEFAULT 100," +
                "respect INT DEFAULT 0," +
                "food INT DEFAULT 100," +
                "army INT DEFAULT 0," +
                "technology INT DEFAULT 0)";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to create table players", e);
        }
    }

    private String requireAllowedField(String field) {
        if (field == null || !ALLOWED_FIELDS.contains(field)) {
            throw new IllegalArgumentException("Unsupported field: " + field);
        }
        return field;
    }

    @Override
    public boolean isPlayerExists(long chatId) {
        String sql = "SELECT COUNT(*) FROM players WHERE chatId = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, chatId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            throw new IllegalStateException("Failed to check player exists, chatId=" + chatId, e);
        }
    }

    @Override
    public void addPlayer(long chatId) {
        String sql = "INSERT IGNORE INTO players (chatId) VALUES (?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, chatId);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new IllegalStateException("Failed to add player, chatId=" + chatId, e);
        }
    }

    @Override
    public Map<String, Integer> getPlayerData(long chatId) {
        Map<String, Integer> data = new HashMap<>();
        String sql = "SELECT gold, people, respect, food, army, technology FROM players WHERE chatId = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, chatId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    data.put(GOLD_KEY, rs.getInt(GOLD_KEY));
                    data.put(PEOPLE_KEY, rs.getInt(PEOPLE_KEY));
                    data.put(REPUTATION_KEY, rs.getInt(REPUTATION_KEY));
                    data.put(FOOD_KEY, rs.getInt(FOOD_KEY));
                    data.put(ARMY_KEY, rs.getInt(ARMY_KEY));
                    data.put(TECHNOLOGY_KEY, rs.getInt(TECHNOLOGY_KEY));
                }
            }

        } catch (SQLException e) {
            throw new IllegalStateException("Failed to get player data, chatId=" + chatId, e);
        }

        return data;
    }

    @Override
    public int getPlayerValue(long chatId, String field) {
        String safeField = requireAllowedField(field);
        String sql = "SELECT " + safeField + " FROM players WHERE chatId = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, chatId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(safeField);
                }
            }

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Failed to get player value, chatId=" + chatId + ", field=" + safeField, e
            );
        }

        return 0;
    }

    @Override
    public void updatePlayer(long chatId,
                             int gold, int people, int respect,
                             int food, int army, int technology) {
        String sql = "UPDATE players SET gold = ?, people = ?, respect = ?, food = ?, army = ?, technology = ? WHERE chatId = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, gold);
            pstmt.setInt(2, people);
            pstmt.setInt(3, respect);
            pstmt.setInt(4, food);
            pstmt.setInt(5, army);
            pstmt.setInt(6, technology);
            pstmt.setLong(7, chatId);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new IllegalStateException("Failed to update player, chatId=" + chatId, e);
        }
    }

    @Override
    public void updatePlayerField(long chatId, String field, int value) {
        String safeField = requireAllowedField(field);
        String sql = "UPDATE players SET " + safeField + " = ? WHERE chatId = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, value);
            pstmt.setLong(2, chatId);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Failed to update player field, chatId=" + chatId + ", field=" + safeField, e
            );
        }
    }

    @Override
    public Map<Long, Integer> getTopPlayers(int limit) {
        Map<Long, Integer> topPlayers = new LinkedHashMap<>();
        String sql = "SELECT chatId, gold FROM players ORDER BY gold DESC LIMIT ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    long chatId = rs.getLong("chatId");
                    int gold = rs.getInt(GOLD_KEY);
                    topPlayers.put(chatId, gold);
                }
            }

        } catch (SQLException e) {
            throw new IllegalStateException("Failed to get top players, limit=" + limit, e);
        }

        return topPlayers;
    }

    private boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null;
        } catch (SQLException e) {
            return false;
        }
    }
}