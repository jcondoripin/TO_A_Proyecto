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
  
  public int getGridSize() {
    return GRID_SIZE;
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
    
    // Guardar posiciones ANTES del ataque
    Position attackerPos = attacker.getPosition();
    Position targetPos = target.getPosition();
    
    // Realizar el ataque
    DamageReport dr = attacker.attack(target);
    
    // Si el objetivo murió, mover el atacante a su posición
    if (dr.isKilled()) {
      System.out.println("[BATTLE] Kill detectado: " + attacker.getId() + " mató a " + target.getId());
      System.out.println("[BATTLE] attackerPos=" + attackerPos + ", targetPos=" + targetPos);
      
      // 1. Remover atacante de su posición actual
      gridPositions.remove(attackerPos);
      
      // 2. Remover objetivo muerto del grid
      gridPositions.remove(targetPos);
      
      // 3. Limpiar posición del muerto
      target.setPosition(null);
      
      // 4. Mover atacante a la posición del objetivo
      attacker.setPosition(targetPos);
      gridPositions.put(targetPos, attacker);
      
      System.out.println("[BATTLE] " + attacker.getId() + " movido de " + attackerPos + " a " + targetPos);
      System.out.println("[BATTLE] Grid ahora tiene en targetPos: " + (gridPositions.get(targetPos) != null ? gridPositions.get(targetPos).getId() : "null"));
    }
    
    addLog(generateLogLine(attacker, target, dr));
    nextTurn();
    checkBattleOver();
    return dr;
  }
  
  /**
   * Mueve un guerrero 1 casilla (alternativa a atacar).
   * @return true si el movimiento fue exitoso
   */
  public boolean performMove(IWarrior warrior, Position newPosition) {
    // Verificar que es el turno correcto
    if (!currentTurnArmy.getWarriors().contains(warrior) || !warrior.isAlive()) {
      throw new IllegalArgumentException("Movimiento inválido: no es tu turno o guerrero incorrecto.");
    }
    
    Position currentPos = warrior.getPosition();
    if (currentPos == null) {
      throw new IllegalArgumentException("El guerrero no tiene posición.");
    }
    
    // Verificar que el movimiento es de máximo 1 casilla (distancia Chebyshev - permite diagonal)
    int rowDiff = Math.abs(currentPos.row - newPosition.row);
    int colDiff = Math.abs(currentPos.col - newPosition.col);
    if (rowDiff > 1 || colDiff > 1 || (rowDiff == 0 && colDiff == 0)) {
      throw new IllegalArgumentException("Solo puedes moverte 1 casilla (incluye diagonal).");
    }
    
    // Verificar que la posición destino está dentro del grid
    if (newPosition.row < 0 || newPosition.row >= GRID_SIZE || 
        newPosition.col < 0 || newPosition.col >= GRID_SIZE) {
      throw new IllegalArgumentException("Posición fuera del tablero.");
    }
    
    // Verificar que la casilla destino está vacía
    if (gridPositions.containsKey(newPosition)) {
      throw new IllegalArgumentException("La casilla está ocupada.");
    }
    
    // Realizar el movimiento
    gridPositions.remove(currentPos);
    warrior.setPosition(newPosition);
    gridPositions.put(newPosition, warrior);
    
    addLog("🚶 " + warrior.getId() + " se mueve de (" + currentPos.row + "," + currentPos.col + 
           ") a (" + newPosition.row + "," + newPosition.col + ")");
    
    System.out.println("[BATTLE] Movimiento: " + warrior.getId() + " de " + currentPos + " a " + newPosition);
    
    nextTurn();
    return true;
  }
  
  /**
   * Verifica si una posición es válida para moverse (adyacente y vacía).
   */
  public boolean isValidMoveTarget(IWarrior warrior, Position targetPos) {
    Position currentPos = warrior.getPosition();
    if (currentPos == null || targetPos == null) return false;
    
    // Verificar distancia = 1 (Chebyshev - permite diagonal)
    int rowDiff = Math.abs(currentPos.row - targetPos.row);
    int colDiff = Math.abs(currentPos.col - targetPos.col);
    if (rowDiff > 1 || colDiff > 1 || (rowDiff == 0 && colDiff == 0)) return false;
    
    // Verificar dentro del grid
    if (targetPos.row < 0 || targetPos.row >= GRID_SIZE || 
        targetPos.col < 0 || targetPos.col >= GRID_SIZE) return false;
    
    // Verificar casilla vacía
    return !gridPositions.containsKey(targetPos);
  }
  
  /**
   * Obtiene las posiciones válidas para mover un guerrero.
   */
  public List<Position> getValidMovePositions(IWarrior warrior) {
    List<Position> validPositions = new ArrayList<>();
    Position currentPos = warrior.getPosition();
    if (currentPos == null) return validPositions;
    
    // Las 8 direcciones (cardinales + diagonales)
    int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}, {-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
    
    for (int[] dir : directions) {
      Position newPos = new Position(currentPos.row + dir[0], currentPos.col + dir[1]);
      if (isValidMoveTarget(warrior, newPos)) {
        validPositions.add(newPos);
      }
    }
    
    return validPositions;
  }

  private String generateLogLine(IWarrior attacker, IWarrior target, DamageReport dr) {
    String attType = attacker.getWarriorType().toUpperCase();
    String tarType = target.getWarriorType().toUpperCase();
    Position attPos = attacker.getPosition();
    Position tarPos = target.getPosition();
    String weaponStr = attacker.getWeapon().getName();
    String multStr = dr.getMultiplier() != 1.0 ? " x" + String.format("%.1f", dr.getMultiplier()) : "";
    int healthBefore = target.getHealth() + dr.getFinalDamage();
    String killedStr = dr.isKilled() ? " ¡ELIMINADO! " + attacker.getId() + " ocupa su posición." : "";
    
    return String.format("%s [%s] en (%d,%d) usó %s → %s [%s] en (%d,%d) | Daño:%d%s→%d | HP:%d→%d%s",
        attacker.getId(), attType, attPos != null ? attPos.row : -1, attPos != null ? attPos.col : -1,
        weaponStr,
        target.getId(), tarType, tarPos != null ? tarPos.row : -1, tarPos != null ? tarPos.col : -1,
        dr.getBaseDamage(), multStr, dr.getFinalDamage(),
        healthBefore, target.getHealth(),
        killedStr);
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
  
  /**
   * Serializa las posiciones actuales de todos los guerreros.
   * Formato: warriorId:row:col;warriorId:row:col;...
   */
  public String serializePositions() {
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<Position, IWarrior> entry : gridPositions.entrySet()) {
      Position pos = entry.getKey();
      IWarrior w = entry.getValue();
      if (sb.length() > 0) sb.append(";");
      sb.append(w.getId()).append(":").append(pos.row).append(":").append(pos.col);
    }
    return sb.toString();
  }
  
  /**
   * Restaura las posiciones de todos los guerreros desde una cadena serializada.
   */
  public void deserializePositions(String positionsData) {
    gridPositions.clear();
    if (positionsData == null || positionsData.isEmpty()) return;
    
    String[] entries = positionsData.split(";");
    for (String entry : entries) {
      String[] parts = entry.split(":");
      if (parts.length != 3) continue;
      
      String warriorId = parts[0];
      int row = Integer.parseInt(parts[1]);
      int col = Integer.parseInt(parts[2]);
      
      IWarrior warrior = findWarriorById(warriorId);
      if (warrior != null) {
        Position pos = new Position(row, col);
        warrior.setPosition(pos);
        gridPositions.put(pos, warrior);
      }
    }
    
    // Establecer turno inicial
    currentTurnArmy = army1;
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
        removed.setPosition(null);
        System.out.println("[BATTLE] Eliminado del grid: " + removed.getId() + " de posición " + pos.row + "," + pos.col);
      }
    }
  }
  
  // Método para verificar si la batalla terminó (para sincronización)
  public void checkBattleEnd() {
    checkBattleOver();
  }
}