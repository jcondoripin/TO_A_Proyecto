package com.battlesimulator.domain;

public class MagicWeapon extends Weapon {
  public MagicWeapon(int damage, Element element) {
    super(damage, element);
  }

  @Override
  public String getType() {
    return "Varita mágica";
  }

  @Override
  public int attack() {
    return damage * level + 10;
  }
}