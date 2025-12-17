package com.battlesimulator.domain;

public interface IWarrior {
  DamageReport attack(IWarrior target);

  DamageReport takeDamage(int damage, Element attackElement);
  
  // Método de daño con modificadores de distancia
  DamageReport takeDamageWithDistance(int damage, Element attackElement, 
                                       double distanceMultiplier, int distance,
                                       CombatRules.AttackEfficiency efficiency);

  boolean isAlive();

  String getName();

  int getHealth();

  IWeapon getWeapon();

  Position getPosition();

  void setPosition(Position position);

  String getId();

  Clan getClan();

  int getNumber();

  int getStrength();

  int getShield();

  void setArmyId(String armyId);

  void setNumber(int number);

  void setClan(Clan clan);

  void reset();
  
  // Para sincronización en multijugador - establece health directamente sin recalcular daño
  void setHealth(int health);
  
  default String getWarriorType() {
    String className = this.getClass().getSimpleName();
    if (className.contains("Melee")) return "melee";
    if (className.contains("Ranged")) return "ranged";
    if (className.contains("Magic")) return "magic";
    return "default";
  }
}