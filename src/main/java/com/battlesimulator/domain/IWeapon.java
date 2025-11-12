package com.battlesimulator.domain;

public interface IWeapon {
  int attack();

  Element getElement();

  void upgrade();

  int getLevel();

  String getType();

  String getName();

  void reset();
}