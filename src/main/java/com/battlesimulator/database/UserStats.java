package com.battlesimulator.database;

public class UserStats {
  private final int userId;
  private int wins;
  private int losses;
  private int totalBattles;
  
  public UserStats(int userId, int wins, int losses, int totalBattles) {
    this.userId = userId;
    this.wins = wins;
    this.losses = losses;
    this.totalBattles = totalBattles;
  }
  
  public int getUserId() {
    return userId;
  }
  
  public int getWins() {
    return wins;
  }
  
  public int getLosses() {
    return losses;
  }
  
  public int getTotalBattles() {
    return totalBattles;
  }
  
  public double getWinRate() {
    if (totalBattles == 0) return 0.0;
    return (double) wins / totalBattles * 100.0;
  }
  
  @Override
  public String toString() {
    return String.format("Victorias: %d | Derrotas: %d | Total: %d | Tasa de Victoria: %.1f%%",
      wins, losses, totalBattles, getWinRate());
  }
}
