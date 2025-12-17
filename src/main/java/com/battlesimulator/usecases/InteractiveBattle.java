package com.battlesimulator.usecases;

import com.battlesimulator.domain.*;
import java.util.*;

public class InteractiveBattle {
  private static final int GRID_SIZE = 12;
  private final Army army1;
  private final Army army2;
  private Army currentTurnArmy;
  private final Map<Position, IWarrior> gridPositions = new HashMap<>();
  private final List<String> logs = new ArrayList<>();
  private Army winner;
  private long randomSeed = System.currentTimeMillis(); // Semilla para sincronización

  public InteractiveBattle(Army army1, Army army2) {
    this.army1 = army1;
    this.army2 = army2;
  }
  
  public void setRandomSeed(long seed) {
    this.randomSeed = seed;
  }
  
  public long getRandomSeed() {
    return randomSeed;
  }

  public Army getArmy1() {
    return army1;
  }

  public Army getArmy2() {
    return army2;
  }

  public void initBattle() {
    gridPositions.clear();
    logs.clear();
    
    // Usar Random con semilla fija para que ambos jugadores tengan las mismas posiciones
    Random rand = new Random(randomSeed);
    
    List<IWarrior> allWarriors = new ArrayList<>();
    allWarriors.addAll(army1.getWarriors());
    allWarriors.addAll(army2.getWarriors());
    List<Position> positions = new ArrayList<>();
    for (int r = 0; r < GRID_SIZE; r++) {
      for (int c = 0; c < GRID_SIZE; c++) {
        positions.add(new Position(r, c));
      }
    }
    
    // Usar la misma semilla para shuffle
    Collections.shuffle(allWarriors, rand);
    Collections.shuffle(positions, rand);
    
    int idx = 0;
    for (IWarrior w : allWarriors) {
      if (idx >= positions.size())
        break;
      Position p = positions.get(idx++);
      w.setPosition(p);
      gridPositions.put(p, w);
    }
    currentTurnArmy = army1;
    addLog("Batalla iniciada entre " + army1.getId() + " (" + army1.getClan().getName() + ") y " + army2.getId() + " ("
        + army2.getClan().getName() + "). Turno inicial: " + currentTurnArmy.getId());
    winner = null;
  }

  public DamageReport performAttack(IWarrior attacker, IWarrior target) {
    Army enemyArmy = currentTurnArmy == army1 ? army2 : army1;
    if (!currentTurnArmy.getWarriors().contains(attacker) || !enemyArmy.getWarriors().contains(target)
        || !attacker.isAlive() || !target.isAlive()) {
      throw new IllegalArgumentException("Ataque inválido: turno o guerreros incorrectos.");
    }
    DamageReport dr = attacker.attack(target);
    if (dr.isKilled()) {
      Position targetPos = target.getPosition();
      Position attackerPos = attacker.getPosition();
      
      System.out.println("[LOCAL] Antes de remove - targetPos: " + targetPos + ", attackerPos: " + attackerPos);
      System.out.println("[LOCAL] Grid contiene targetPos? " + gridPositions.containsKey(targetPos));
      System.out.println("[LOCAL] Grid contiene attackerPos? " + gridPositions.containsKey(attackerPos));
      
      // Eliminar ambos del grid
      IWarrior removedTarget = gridPositions.remove(targetPos);
      IWarrior removedAttacker = gridPositions.remove(attackerPos);
      
      System.out.println("[LOCAL] Removido de targetPos: " + (removedTarget != null ? removedTarget.getId() : "null"));
      System.out.println("[LOCAL] Removido de attackerPos: " + (removedAttacker != null ? removedAttacker.getId() : "null"));
      
      // Limpiar la posición del guerrero muerto
      target.setPosition(null);
      // Mover atacante a la posición del objetivo
      attacker.setPosition(targetPos);
      gridPositions.put(targetPos, attacker);
      
      System.out.println("[LOCAL] Kill completado: " + target.getId() + " eliminado, " + attacker.getId() + " ahora en " + targetPos);
    }
    addLog(generateLogLine(attacker, target, dr));
    nextTurn();
    checkBattleOver();
    return dr;
  }

  private String generateLogLine(IWarrior attacker, IWarrior target, DamageReport dr) {
    String weaponStr = attacker.getWeapon().getName();
    String multStr = dr.getMultiplier() != 1.0 ? " x" + String.format("%.1f", dr.getMultiplier()) : "";
    int healthBefore = target.getHealth() + dr.getFinalDamage();
    String killedStr = dr.isKilled() ? ". ¡Muerto! " + attacker.getId() + " ocupa su posición." : "";
    return attacker.getId() + " (" + weaponStr + ") ataca a " + target.getId() + ". Daño base: " + dr.getBaseDamage()
        + multStr + " = " + dr.getEffectiveDamage()
        + ". Absorbido por escudo: " + dr.getAbsorbed() + ". Daño final: " + dr.getFinalDamage()
        + ". " + target.getId() + ": " + healthBefore + " -> " + target.getHealth() + killedStr;
  }

  public void nextTurn() {
    currentTurnArmy = (currentTurnArmy == army1 ? army2 : army1);
  }

  private void checkBattleOver() {
    if (army1.isDefeated()) {
      winner = army2;
    } else if (army2.isDefeated()) {
      winner = army1;
    }
  }

  public IWarrior getWarriorAt(Position p) {
    return gridPositions.get(p);
  }

  public boolean isOwnWarrior(IWarrior w) {
    return currentTurnArmy.getWarriors().contains(w);
  }

  public List<IWarrior> getCurrentWarriors() {
    return currentTurnArmy.getWarriors();
  }

  public Army getCurrentTurnArmy() {
    return currentTurnArmy;
  }

  public boolean isBattleOver() {
    return winner != null;
  }

  public Army getWinner() {
    return winner;
  }

  public List<String> getLogs() {
    return new ArrayList<>(logs);
  }

  private void addLog(String line) {
    logs.add(line);
  }

  public String getTurnDescription() {
    return "Turno: Ejército " + currentTurnArmy.getId() + " (" + currentTurnArmy.getClan().getName() + ")";
  }
  
  public IWarrior findWarriorById(String warriorId) {
    for (IWarrior w : army1.getAllWarriors()) {
      if (w.getId().equals(warriorId)) {
        return w;
      }
    }
    for (IWarrior w : army2.getAllWarriors()) {
      if (w.getId().equals(warriorId)) {
        return w;
      }
    }
    return null;
  }
  
  // Método para sincronizar movimiento de guerrero (usado en multijugador)
  public void moveWarrior(IWarrior warrior, Position newPosition) {
    Position oldPos = warrior.getPosition();
    if (oldPos != null) {
      gridPositions.remove(oldPos);
    }
    warrior.setPosition(newPosition);
    gridPositions.put(newPosition, warrior);
  }
  
  // Método para eliminar un guerrero muerto del grid
  public void removeFromGrid(Position pos) {
    if (pos != null) {
      IWarrior removed = gridPositions.remove(pos);
      if (removed != null) {
        System.out.println("[BATTLE] Eliminado del grid: " + removed.getId() + " de posición " + pos.row + "," + pos.col);
      }
    }
  }
  
  // Método para verificar si la batalla terminó (para sincronización)
  public void checkBattleEnd() {
    checkBattleOver();
  }
}