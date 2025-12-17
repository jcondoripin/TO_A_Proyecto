package com.battlesimulator.domain;

public class DamageReport {
  private final int baseDamage;
  private final double elementMultiplier;
  private final double distanceMultiplier;
  private final int effectiveDamage;
  private final int absorbed;
  private final int finalDamage;
  private final boolean killed;
  private final int distance;
  private final CombatRules.AttackEfficiency efficiency;

  // Constructor completo con distancia
  public DamageReport(int baseDamage, double elementMultiplier, double distanceMultiplier, 
                      int effectiveDamage, int absorbed, int finalDamage, boolean killed,
                      int distance, CombatRules.AttackEfficiency efficiency) {
    this.baseDamage = baseDamage;
    this.elementMultiplier = elementMultiplier;
    this.distanceMultiplier = distanceMultiplier;
    this.effectiveDamage = effectiveDamage;
    this.absorbed = absorbed;
    this.finalDamage = finalDamage;
    this.killed = killed;
    this.distance = distance;
    this.efficiency = efficiency;
  }
  
  // Constructor de compatibilidad (sin distancia)
  public DamageReport(int baseDamage, double multiplier, int effectiveDamage, int absorbed, int finalDamage,
      boolean killed) {
    this(baseDamage, multiplier, 1.0, effectiveDamage, absorbed, finalDamage, killed, 0, CombatRules.AttackEfficiency.NORMAL);
  }

  public int getBaseDamage() {
    return baseDamage;
  }

  public double getMultiplier() {
    return elementMultiplier * distanceMultiplier;
  }
  
  public double getElementMultiplier() {
    return elementMultiplier;
  }
  
  public double getDistanceMultiplier() {
    return distanceMultiplier;
  }

  public int getEffectiveDamage() {
    return effectiveDamage;
  }

  public int getAbsorbed() {
    return absorbed;
  }

  public int getFinalDamage() {
    return finalDamage;
  }

  public boolean isKilled() {
    return killed;
  }
  
  public int getDistance() {
    return distance;
  }
  
  public CombatRules.AttackEfficiency getEfficiency() {
    return efficiency;
  }
}