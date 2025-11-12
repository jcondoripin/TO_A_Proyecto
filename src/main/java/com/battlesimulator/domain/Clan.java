package com.battlesimulator.domain;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Clan {
  private final String name;
  private final List<Army> armies = new ArrayList<>();
  private Color color = Color.WHITE;

  public Clan(String name) {
    this.name = name;
  }

  public void addArmy(Army army) {
    if (armies.size() < 3) {
      armies.add(army);
    }
  }

  public List<Army> getActiveArmies() {
    return armies.stream().filter(a -> !a.isDefeated()).collect(Collectors.toCollection(ArrayList::new));
  }

  public List<Army> getAllArmies() {
    return new ArrayList<>(armies);
  }

  public boolean isDefeated() {
    return getActiveArmies().isEmpty();
  }

  public String getName() {
    return name;
  }

  public void setColor(Color color) {
    this.color = color;
  }

  public Color getColor() {
    return color;
  }
}