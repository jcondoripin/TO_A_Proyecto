package com.battlesimulator.domain;

public enum Element {
  NONE, FIRE, ICE, WATER, EARTH;

  @Override
  public String toString() {
    return switch (this) {
      case NONE -> "ninguno";
      case FIRE -> "fuego";
      case ICE -> "hielo";
      case WATER -> "agua";
      case EARTH -> "tierra";
    };
  }

  public double getDamageMultiplier(Element targetElement) {
    if (this == FIRE && targetElement == ICE)
      return 1.5;
    if (this == ICE && targetElement == FIRE)
      return 1.5;
    if (this == WATER && targetElement == FIRE)
      return 1.5;
    if (this == FIRE && targetElement == WATER)
      return 0.5;
    return 1.0;
  }

  public double getShieldMultiplier(Element attackElement) {
    return 1.0 / getDamageMultiplier(attackElement);
  }
}