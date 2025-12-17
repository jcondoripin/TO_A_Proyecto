package com.battlesimulator.domain;

/**
 * Define las reglas de combate basadas en distancia y tipo de guerrero.
 * 
 * MELEE (Cuerpo a cuerpo):
 * - Rango óptimo: 1-2 casillas (daño x1.5)
 * - Rango normal: 3 casillas (daño x1.0)
 * - Rango penalizado: 4+ casillas (daño x0.3, no puede atacar más allá de 5)
 * 
 * RANGED (Arquero):
 * - Rango óptimo: 5-8 casillas (daño x1.5)
 * - Rango normal: 3-4 casillas (daño x1.0)
 * - Rango penalizado: 1-2 casillas (daño x0.4, vulnerabilidad en cuerpo a cuerpo)
 * - Rango máximo: 10 casillas
 * 
 * MAGIC (Mago):
 * - Rango óptimo: 3-5 casillas (daño x1.4)
 * - Rango normal: 2 o 6-7 casillas (daño x1.0)
 * - Rango penalizado: 1 casilla (daño x0.5, concentración interrumpida)
 * - Rango máximo: 8 casillas
 * - Bonus: Puede atacar en área (daño reducido a adyacentes) - futuro
 */
public class CombatRules {
  
  // Rangos de ataque máximos
  public static final int MELEE_MAX_RANGE = 5;
  public static final int RANGED_MAX_RANGE = 10;
  public static final int MAGIC_MAX_RANGE = 8;
  
  // Rangos óptimos
  public static final int MELEE_OPTIMAL_MIN = 1;
  public static final int MELEE_OPTIMAL_MAX = 2;
  
  public static final int RANGED_OPTIMAL_MIN = 5;
  public static final int RANGED_OPTIMAL_MAX = 8;
  
  public static final int MAGIC_OPTIMAL_MIN = 3;
  public static final int MAGIC_OPTIMAL_MAX = 5;
  
  /**
   * Calcula la distancia Manhattan entre dos posiciones.
   */
  public static int calculateDistance(Position from, Position to) {
    if (from == null || to == null) return Integer.MAX_VALUE;
    return Math.abs(from.row - to.row) + Math.abs(from.col - to.col);
  }
  
  /**
   * Calcula la distancia Euclidiana (más realista para combate).
   */
  public static double calculateEuclideanDistance(Position from, Position to) {
    if (from == null || to == null) return Double.MAX_VALUE;
    int dx = from.row - to.row;
    int dy = from.col - to.col;
    return Math.sqrt(dx * dx + dy * dy);
  }
  
  /**
   * Obtiene el rango máximo de ataque para un tipo de guerrero.
   */
  public static int getMaxRange(String warriorType) {
    return switch (warriorType.toLowerCase()) {
      case "melee" -> MELEE_MAX_RANGE;
      case "ranged" -> RANGED_MAX_RANGE;
      case "magic" -> MAGIC_MAX_RANGE;
      default -> 3;
    };
  }
  
  /**
   * Verifica si un ataque está dentro del rango permitido.
   */
  public static boolean isInRange(IWarrior attacker, IWarrior target) {
    int distance = calculateDistance(attacker.getPosition(), target.getPosition());
    int maxRange = getMaxRange(attacker.getWarriorType());
    return distance <= maxRange && distance > 0;
  }
  
  /**
   * Calcula el multiplicador de daño basado en la distancia y tipo de guerrero.
   */
  public static double getDistanceMultiplier(IWarrior attacker, IWarrior target) {
    int distance = calculateDistance(attacker.getPosition(), target.getPosition());
    String type = attacker.getWarriorType().toLowerCase();
    
    return switch (type) {
      case "melee" -> getMeleeMultiplier(distance);
      case "ranged" -> getRangedMultiplier(distance);
      case "magic" -> getMagicMultiplier(distance);
      default -> 1.0;
    };
  }
  
  private static double getMeleeMultiplier(int distance) {
    if (distance <= 0) return 0;
    if (distance <= 2) return 1.5;      // Óptimo: cuerpo a cuerpo
    if (distance <= 3) return 1.0;      // Normal
    if (distance <= 5) return 0.3;      // Penalizado: muy lejos para melee
    return 0;                            // Fuera de rango
  }
  
  private static double getRangedMultiplier(int distance) {
    if (distance <= 0) return 0;
    if (distance <= 2) return 0.4;      // Penalizado: muy cerca, no puede apuntar bien
    if (distance <= 4) return 1.0;      // Normal
    if (distance <= 8) return 1.5;      // Óptimo: distancia perfecta para arquero
    if (distance <= 10) return 0.8;     // Un poco lejos
    return 0;                            // Fuera de rango
  }
  
  private static double getMagicMultiplier(int distance) {
    if (distance <= 0) return 0;
    if (distance == 1) return 0.5;      // Penalizado: muy cerca, concentración interrumpida
    if (distance == 2) return 1.0;      // Normal
    if (distance <= 5) return 1.4;      // Óptimo: distancia media para canalizar magia
    if (distance <= 7) return 1.0;      // Normal
    if (distance <= 8) return 0.7;      // Lejos
    return 0;                            // Fuera de rango
  }
  
  /**
   * Obtiene la categoría de eficiencia del ataque.
   */
  public static AttackEfficiency getAttackEfficiency(IWarrior attacker, IWarrior target) {
    double multiplier = getDistanceMultiplier(attacker, target);
    
    if (multiplier <= 0) return AttackEfficiency.OUT_OF_RANGE;
    if (multiplier >= 1.4) return AttackEfficiency.OPTIMAL;
    if (multiplier >= 1.0) return AttackEfficiency.NORMAL;
    return AttackEfficiency.PENALIZED;
  }
  
  /**
   * Obtiene una descripción del rango para mostrar en UI.
   */
  public static String getRangeDescription(String warriorType) {
    return switch (warriorType.toLowerCase()) {
      case "melee" -> "⚔️ Melee: Óptimo 1-2, Max 5";
      case "ranged" -> "🏹 Ranged: Óptimo 5-8, Max 10";
      case "magic" -> "✨ Magic: Óptimo 3-5, Max 8";
      default -> "Rango desconocido";
    };
  }
  
  /**
   * Verifica si una posición está en el rango óptimo.
   */
  public static boolean isOptimalRange(String warriorType, int distance) {
    return switch (warriorType.toLowerCase()) {
      case "melee" -> distance >= MELEE_OPTIMAL_MIN && distance <= MELEE_OPTIMAL_MAX;
      case "ranged" -> distance >= RANGED_OPTIMAL_MIN && distance <= RANGED_OPTIMAL_MAX;
      case "magic" -> distance >= MAGIC_OPTIMAL_MIN && distance <= MAGIC_OPTIMAL_MAX;
      default -> false;
    };
  }
  
  /**
   * Verifica si una posición está penalizada.
   */
  public static boolean isPenalizedRange(String warriorType, int distance) {
    double mult = switch (warriorType.toLowerCase()) {
      case "melee" -> getMeleeMultiplier(distance);
      case "ranged" -> getRangedMultiplier(distance);
      case "magic" -> getMagicMultiplier(distance);
      default -> 1.0;
    };
    return mult > 0 && mult < 1.0;
  }
  
  /**
   * Enum para categorías de eficiencia de ataque.
   */
  public enum AttackEfficiency {
    OPTIMAL("🎯 ¡Óptimo!", 0x00FF00),      // Verde brillante
    NORMAL("✓ Normal", 0xFFFF00),          // Amarillo
    PENALIZED("⚠️ Penalizado", 0xFF8800),   // Naranja
    OUT_OF_RANGE("✗ Fuera de rango", 0xFF0000); // Rojo
    
    private final String description;
    private final int colorCode;
    
    AttackEfficiency(String description, int colorCode) {
      this.description = description;
      this.colorCode = colorCode;
    }
    
    public String getDescription() {
      return description;
    }
    
    public int getColorCode() {
      return colorCode;
    }
  }
}
