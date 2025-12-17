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
    
    // Calcular distancia y multiplicador de distancia
    int distance = CombatRules.calculateDistance(this.position, target.getPosition());
    double distanceMultiplier = CombatRules.getDistanceMultiplier(this, target);
    CombatRules.AttackEfficiency efficiency = CombatRules.getAttackEfficiency(this, target);
    
    // Aplicar daño con multiplicador de distancia
    return target.takeDamageWithDistance(baseDamage, weapon.getElement(), distanceMultiplier, distance, efficiency);
  }

  protected abstract int calculateBaseDamage();

  @Override
  public DamageReport takeDamage(int baseDamage, Element attackElement) {
    // Método de compatibilidad - sin modificador de distancia
    return takeDamageWithDistance(baseDamage, attackElement, 1.0, 0, CombatRules.AttackEfficiency.NORMAL);
  }
  
  @Override
  public DamageReport takeDamageWithDistance(int baseDamage, Element attackElement, 
                                              double distanceMultiplier, int distance,
                                              CombatRules.AttackEfficiency efficiency) {
    double elementMultiplier = attackElement.getDamageMultiplier(this.element);
    
    // Aplicar ambos multiplicadores: elemento y distancia
    double totalMultiplier = elementMultiplier * distanceMultiplier;
    int effectiveDamage = (int) (baseDamage * totalMultiplier);
    
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
    return new DamageReport(baseDamage, elementMultiplier, distanceMultiplier, 
                           effectiveDamage, absorbed, finalDamage, killed, distance, efficiency);
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