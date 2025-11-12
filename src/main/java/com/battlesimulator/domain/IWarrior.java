package com.battlesimulator.domain;

public interface IWarrior {
  DamageReport attack(IWarrior target);

  DamageReport takeDamage(int damage, Element attackElement);

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
}