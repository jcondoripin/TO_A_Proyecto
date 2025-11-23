package com.battlesimulator.database;

import org.mindrot.jbcrypt.BCrypt;
import java.sql.*;
import java.time.LocalDateTime;

public class DatabaseManager {
  private static final String DB_URL = "jdbc:sqlite:battlesimulator.db";
  private static DatabaseManager instance;
  private Connection connection;
  
  private DatabaseManager() {
    initDatabase();
  }
  
  public static DatabaseManager getInstance() {
    if (instance == null) {
      instance = new DatabaseManager();
    }
    return instance;
  }
  
  private void initDatabase() {
    try {
      connection = DriverManager.getConnection(DB_URL);
      createTables();
      System.out.println("Base de datos inicializada correctamente");
    } catch (SQLException e) {
      System.err.println("Error al inicializar base de datos: " + e.getMessage());
      e.printStackTrace();
    }
  }
  
  private void createTables() throws SQLException {
    String createUsersTable = """
      CREATE TABLE IF NOT EXISTS users (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        username TEXT UNIQUE NOT NULL,
        password_hash TEXT NOT NULL,
        created_at TEXT NOT NULL
      )
    """;
    
    String createStatsTable = """
      CREATE TABLE IF NOT EXISTS stats (
        user_id INTEGER PRIMARY KEY,
        wins INTEGER DEFAULT 0,
        losses INTEGER DEFAULT 0,
        total_battles INTEGER DEFAULT 0,
        FOREIGN KEY (user_id) REFERENCES users(id)
      )
    """;
    
    try (Statement stmt = connection.createStatement()) {
      stmt.execute(createUsersTable);
      stmt.execute(createStatsTable);
    }
  }
  
  public User registerUser(String username, String password) throws SQLException {
    if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
      throw new IllegalArgumentException("Usuario y contraseña no pueden estar vacíos");
    }
    
    if (username.length() < 3) {
      throw new IllegalArgumentException("El usuario debe tener al menos 3 caracteres");
    }
    
    if (password.length() < 4) {
      throw new IllegalArgumentException("La contraseña debe tener al menos 4 caracteres");
    }
    
    String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());
    String sql = "INSERT INTO users (username, password_hash, created_at) VALUES (?, ?, ?)";
    
    try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      pstmt.setString(1, username.trim());
      pstmt.setString(2, passwordHash);
      pstmt.setString(3, LocalDateTime.now().toString());
      
      int affected = pstmt.executeUpdate();
      if (affected == 0) {
        throw new SQLException("Error al crear usuario");
      }
      
      try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
        if (generatedKeys.next()) {
          int userId = generatedKeys.getInt(1);
          
          // Crear stats iniciales
          String statsSql = "INSERT INTO stats (user_id, wins, losses, total_battles) VALUES (?, 0, 0, 0)";
          try (PreparedStatement statsStmt = connection.prepareStatement(statsSql)) {
            statsStmt.setInt(1, userId);
            statsStmt.executeUpdate();
          }
          
          return new User(userId, username.trim());
        }
      }
    } catch (SQLException e) {
      if (e.getMessage().contains("UNIQUE constraint failed")) {
        throw new SQLException("El nombre de usuario ya existe");
      }
      throw e;
    }
    
    throw new SQLException("Error al obtener ID del usuario creado");
  }
  
  public User loginUser(String username, String password) throws SQLException {
    if (username == null || username.trim().isEmpty() || password == null) {
      throw new IllegalArgumentException("Usuario y contraseña no pueden estar vacíos");
    }
    
    String sql = "SELECT id, username, password_hash FROM users WHERE username = ?";
    
    try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
      pstmt.setString(1, username.trim());
      
      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          int id = rs.getInt("id");
          String storedUsername = rs.getString("username");
          String passwordHash = rs.getString("password_hash");
          
          if (BCrypt.checkpw(password, passwordHash)) {
            return new User(id, storedUsername);
          } else {
            throw new SQLException("Contraseña incorrecta");
          }
        } else {
          throw new SQLException("Usuario no encontrado");
        }
      }
    }
  }
  
  public UserStats getUserStats(int userId) throws SQLException {
    String sql = "SELECT wins, losses, total_battles FROM stats WHERE user_id = ?";
    
    try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
      pstmt.setInt(1, userId);
      
      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          int wins = rs.getInt("wins");
          int losses = rs.getInt("losses");
          int totalBattles = rs.getInt("total_battles");
          return new UserStats(userId, wins, losses, totalBattles);
        }
      }
    }
    
    return new UserStats(userId, 0, 0, 0);
  }
  
  public void recordWin(int userId) throws SQLException {
    String sql = "UPDATE stats SET wins = wins + 1, total_battles = total_battles + 1 WHERE user_id = ?";
    
    try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
      pstmt.setInt(1, userId);
      pstmt.executeUpdate();
    }
  }
  
  public void recordLoss(int userId) throws SQLException {
    String sql = "UPDATE stats SET losses = losses + 1, total_battles = total_battles + 1 WHERE user_id = ?";
    
    try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
      pstmt.setInt(1, userId);
      pstmt.executeUpdate();
    }
  }
  
  public void close() {
    try {
      if (connection != null && !connection.isClosed()) {
        connection.close();
      }
    } catch (SQLException e) {
      System.err.println("Error al cerrar base de datos: " + e.getMessage());
    }
  }
}
