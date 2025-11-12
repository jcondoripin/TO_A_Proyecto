package com.battlesimulator.domain;

public abstract class Weapon implements IWeapon {
  protected int damage;
  protected int initialDamage;
  protected Element element;
  protected int level = 1;

  public Weapon(int damage, Element element) {
    this.initialDamage = damage;
    this.damage = damage;
    this.element = element;
  }

  @Override
  public Element getElement() {
    return element;
  }

  @Override
  public void upgrade() {
    level++;
    damage += 5;
  }

  @Override
  public int getLevel() {
    return level;
  }

  @Override
  public String getName() {
    return getType() + " de " + element.toString().toLowerCase();
  }

  @Override
  public void reset() {
    level = 1;
    damage = initialDamage;
  }
}