package com.battlesimulator.domain;

public class MagicWarrior extends Warrior {
  public MagicWarrior(String name, int health, int shield, int strength, Element element, IWeapon weapon) {
    super(name, health, shield, strength, element, weapon);
  }

  @Override
  protected int calculateBaseDamage() {
    return weapon.attack() + strength;
  }
}