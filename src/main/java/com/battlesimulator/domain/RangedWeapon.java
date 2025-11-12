package com.battlesimulator.domain;

public class RangedWeapon extends Weapon {
  public RangedWeapon(int damage, Element element) {
    super(damage, element);
  }

  @Override
  public String getType() {
    return "Arco";
  }

  @Override
  public int attack() {
    return (int) (damage * level * 0.8);
  }
}