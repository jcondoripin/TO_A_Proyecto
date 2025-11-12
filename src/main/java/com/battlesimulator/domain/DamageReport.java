package com.battlesimulator.domain;

public class DamageReport {
  private final int baseDamage;
  private final double multiplier;
  private final int effectiveDamage;
  private final int absorbed;
  private final int finalDamage;
  private final boolean killed;

  public DamageReport(int baseDamage, double multiplier, int effectiveDamage, int absorbed, int finalDamage,
      boolean killed) {
    this.baseDamage = baseDamage;
    this.multiplier = multiplier;
    this.effectiveDamage = effectiveDamage;
    this.absorbed = absorbed;
    this.finalDamage = finalDamage;
    this.killed = killed;
  }

  public int getBaseDamage() {
    return baseDamage;
  }

  public double getMultiplier() {
    return multiplier;
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
}