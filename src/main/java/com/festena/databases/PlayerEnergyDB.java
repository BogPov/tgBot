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

import java.util.ArrayList;
import java.util.List;

@Component
public class PlayerEnergyDB implements IPlayerEnergyDataBase {

    @Value("${db.url}")
    private String dirtyUrl;

    @Value("${db.username}")
    private String username;

    @Value("${db.password}")
    private String password;

    private String url;
    public static final int DEFAULT_ENERGY = 10;

    @PostConstruct
    private void init() {
        this.url = "jdbc:" + dirtyUrl;
        createTable();
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    private void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS energy_amount (" +
                "chatId BIGINT PRIMARY KEY," +
                "energy INT DEFAULT 0" +
                ")";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to create table energy_amount", e);
        }
    }

    @Override
    public List<Long> getAllPlayers() {
        String sql = "SELECT chatId FROM energy_amount";
        List<Long> chatIds = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                chatIds.add(rs.getLong("chatId"));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to get all players from energy_amount", e);
        }

        return chatIds;
    }

    @Override
    public int getPlayerEnergy(long chatId) {
        String sql = "SELECT energy FROM energy_amount WHERE chatId = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, chatId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("energy");
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to get player energy, chatId=" + chatId, e);
        }

        return 0;
    }

    @Override
    public void addEnergyToPlayer(long chatId, int delta) {
        String sql = "INSERT INTO energy_amount (chatId, energy) " +
                "VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE energy = energy + VALUES(energy)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, chatId);
            ps.setInt(2, delta);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to add energy, chatId=" + chatId + ", delta=" + delta, e);
        }
    }

    @Override
    public void addPlayerToDB(long chatId) {
        String sql = "INSERT IGNORE INTO energy_amount (chatId, energy) VALUES (?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, chatId);
            ps.setInt(2, DEFAULT_ENERGY);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to add player to energy DB, chatId=" + chatId, e);
        }
    }

    private boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null;
        } catch (SQLException e) {
            return false;
        }
    }
}