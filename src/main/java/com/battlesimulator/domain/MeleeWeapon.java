package com.battlesimulator.domain;

public class MeleeWeapon extends Weapon {
  public MeleeWeapon(int damage, Element element) {
    super(damage, element);
  }

  @Override
  public String getType() {
    return "Espada";
  }

  @Override
  public int attack() {
    return damage * level;
  }
}