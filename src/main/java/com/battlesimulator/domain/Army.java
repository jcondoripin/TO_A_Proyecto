package com.battlesimulator.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Army {
  private final List<IWarrior> warriors = new ArrayList<>();
  private boolean hasFought = false;
  private String id;
  private Clan clan;

  public void addWarrior(IWarrior warrior) {
    warriors.add(warrior);
  }

  public List<IWarrior> getWarriors() {
    return warriors.stream().filter(IWarrior::isAlive).collect(Collectors.toCollection(ArrayList::new));
  }

  public List<IWarrior> getAllWarriors() {
    return new ArrayList<>(warriors);
  }

  public boolean isDefeated() {
    return getWarriors().isEmpty();
  }

  public int getLevel() {
    List<IWarrior> alive = getWarriors();
    int totalHealth = alive.stream().mapToInt(IWarrior::getHealth).sum();
    return alive.size() * 10 + totalHealth / 10;
  }

  public void setHasFought(boolean fought) {
    this.hasFought = fought;
  }

  public boolean hasFought() {
    return hasFought;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

  public void setClan(Clan clan) {
    this.clan = clan;
  }

  public Clan getClan() {
    return clan;
  }
}