package com.battlesimulator.domain;

public class MeleeWarrior extends Warrior {
  public MeleeWarrior(String name, int health, int shield, int strength, Element element, IWeapon weapon) {
    super(name, health, shield, strength, element, weapon);
  }

  @Override
  protected int calculateBaseDamage() {
    return weapon.attack() + strength;
  }
}