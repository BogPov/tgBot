package com.festena.databases;

import java.sql.Connection;
import java.sql.SQLException;

public interface IDataBase {
    public void createTable();

    public Connection getConnection() throws SQLException;

    public boolean testConnection();
}
