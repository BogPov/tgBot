package com.festena.manager;

import java.sql.*;
import java.util.*;


public class DataBaseManager {
    private static String URL;
    private static String USERNAME;
    private static String PASSWORD;

    public static final String GOLD_KEY = "gold";
    public static final String FOOD_KEY = "food";
    public static final String REPUTATION_KEY = "respect";
    public static final String ARMY_KEY = "army";
    public static final String PEOPLE_KEY = "people";
    public static final String TECHNOLOGY_KEY = "technology";

    public DataBaseManager(){
        URL = System.getenv("DB_URL");
        USERNAME = System.getenv("DB_USERNAME");
        PASSWORD = System.getenv("DB_PASSWORD");
        this.createTable();
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }

    public void createTable() {
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
            e.printStackTrace();
        }
    }

    public boolean isPlayerExists(long chatId) {
        String sql = "SELECT COUNT(*) FROM players WHERE chatId = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, chatId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public void addPlayer(long chatId) {
        String sql = "INSERT IGNORE INTO players (chatId) VALUES (?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, chatId);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Map<String, Integer> getPlayerData(long chatId) {
        Map<String, Integer> data = new HashMap<>();
        String sql = "SELECT gold, people, respect, food, army, technology FROM players WHERE chatId = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, chatId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                data.put("gold", rs.getInt("gold"));
                data.put("people", rs.getInt("people"));
                data.put("respect", rs.getInt("respect"));
                data.put("food", rs.getInt("food"));
                data.put("army", rs.getInt("army"));
                data.put("technology", rs.getInt("technology"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return data;
    }

    public int getPlayerValue(long chatId, String field) {
        String sql = "SELECT " + field + " FROM players WHERE chatId = ?";
        int value = 0;

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, chatId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                value = rs.getInt(field);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return value;
    }

    public void updatePlayer(long chatId, int gold, int people, int respect,
                             int food, int army, int technology) {
        String sql = "UPDATE players SET gold = ?, people = ?, respect = ?, " +
                "food = ?, army = ?, technology = ? WHERE chatId = ?";

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
            e.printStackTrace();
        }
    }

    public void updatePlayerField(long chatId, String field, int value) {
        String sql = "UPDATE players SET " + field + " = ? WHERE chatId = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, value);
            pstmt.setLong(2, chatId);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Map<Long, Integer> getTopPlayers(int limit) {
        Map<Long, Integer> topPlayers = new LinkedHashMap<>(); // LinkedHashMap сохраняет порядок
        String sql = "SELECT chatId, gold FROM players ORDER BY gold DESC LIMIT ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                long chatId = rs.getLong("chatId");
                int gold = rs.getInt("gold");
                topPlayers.put(chatId, gold);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return topPlayers;
    }

    // Проверка соединения
    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null;
        } catch (SQLException e) {
            return false;
        }
    }
}
