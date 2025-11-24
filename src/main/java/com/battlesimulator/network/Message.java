package com.battlesimulator.network;

import java.io.Serializable;

public class Message implements Serializable {
  private static final long serialVersionUID = 1L;
  
  public enum Type {
    // Conexión
    CONNECT, DISCONNECT,
    // Configuración
    CLAN_CONFIG, START_WAR,
    // Batalla
    ATTACK, BATTLE_UPDATE, BATTLE_END,
    // Estado
    WAR_STATUS, WAR_END,
    // Sincronización
    SYNC_REQUEST, SYNC_RESPONSE,
    // Chat
    CHAT,
    // Turnos
    TURN_END
  }
  
  private Type type;
  private String data;
  private String playerId;
  private String username;
  private long timestamp;
  
  public Message() {
    this.timestamp = System.currentTimeMillis();
  }
  
  public Message(Type type, String data) {
    this.type = type;
    this.data = data;
    this.timestamp = System.currentTimeMillis();
  }
  
  public Message(Type type, String data, String playerId) {
    this.type = type;
    this.data = data;
    this.playerId = playerId;
    this.timestamp = System.currentTimeMillis();
  }
  
  public Type getType() {
    return type;
  }
  
  public void setType(Type type) {
    this.type = type;
  }
  
  public String getData() {
    return data;
  }
  
  public void setData(String data) {
    this.data = data;
  }
  
  public String getPlayerId() {
    return playerId;
  }
  
  public void setPlayerId(String playerId) {
    this.playerId = playerId;
  }
  
  public String getUsername() {
    return username;
  }
  
  public void setUsername(String username) {
    this.username = username;
  }
  
  public long getTimestamp() {
    return timestamp;
  }
  
  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }
}
