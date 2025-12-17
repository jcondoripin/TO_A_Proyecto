package com.battlesimulator.domain;

public abstract class Warrior implements IWarrior {
  protected String name;
  protected int health;
  protected int initialHealth;
  protected int shield;
  protected int initialShield;
  protected int strength;
  protected Element element;
  protected IWeapon weapon;
  protected int defeats = 0;
  protected Position position;
  protected String armyId;
  protected int number;
  protected Clan clan;

  public Warrior(String name, int health, int shield, int strength, Element element, IWeapon weapon) {
    this.name = name;
    this.initialHealth = health;
    this.health = health;
    this.initialShield = shield;
    this.shield = shield;
    this.strength = strength;
    this.element = element;
    this.weapon = weapon;
  }

  @Override
  public DamageReport attack(IWarrior target) {
    int baseDamage = calculateBaseDamage();
    return target.takeDamage(baseDamage, weapon.getElement());
  }

  protected abstract int calculateBaseDamage();

  @Override
  public DamageReport takeDamage(int baseDamage, Element attackElement) {
    double multiplier = attackElement.getDamageMultiplier(this.element);
    int effectiveDamage = (int) (baseDamage * multiplier);
    int absorbed = Math.min(shield, effectiveDamage);
    int finalDamage = effectiveDamage - absorbed;
    shield -= absorbed;
    health -= finalDamage;
    boolean killed = health <= 0;
    if (killed) {
      health = 0;
      defeats++;
      weapon.upgrade();
    }
    return new DamageReport(baseDamage, multiplier, effectiveDamage, absorbed, finalDamage, killed);
  }

  @Override
  public boolean isAlive() {
    return health > 0;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public int getHealth() {
    return health;
  }

  @Override
  public IWeapon getWeapon() {
    return weapon;
  }

  @Override
  public Position getPosition() {
    return position;
  }

  @Override
  public void setPosition(Position position) {
    this.position = position;
  }

  @Override
  public String getId() {
    return armyId + number;
  }

  @Override
  public Clan getClan() {
    return clan;
  }

  @Override
  public void setArmyId(String armyId) {
    this.armyId = armyId;
  }

  @Override
  public int getNumber() {
    return this.number;
  }

  @Override
  public void setNumber(int number) {
    this.number = number;
  }

  @Override
  public void setClan(Clan clan) {
    this.clan = clan;
  }

  @Override
  public int getStrength() {
    return strength;
  }

  @Override
  public int getShield() {
    return shield;
  }

  @Override
  public void reset() {
    health = initialHealth;
    shield = initialShield;
    defeats = 0;
    weapon.reset();
    position = null;
  }
  
  @Override
  public void setHealth(int health) {
    this.health = Math.max(0, health);
  }
}