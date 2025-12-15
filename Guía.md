# Guía Técnica Completa - Simulador de Batallas de Clanes

## 1. VISIÓN GENERAL DEL PROYECTO

### 1.1 Propósito
Este proyecto es un **Simulador de Batallas de Clanes** desarrollado en Java que simula guerras estratégicas entre dos clanes. Cada clan puede tener múltiples ejércitos y cada ejército está compuesto por guerreros de diferentes tipos que combaten en una cuadrícula táctica.

### 1.2 Información Técnica Básica
- **Lenguaje**: Java 17
- **Build Tool**: Maven
- **Framework UI**: Java Swing
- **Arquitectura**: Modelo-Vista-Controlador con separación por capas
- **Group ID**: `com.battlesimulator`
- **Artifact ID**: `to_battle_simulator`
- **Versión**: 1.0-SNAPSHOT

### 1.3 Características Principales
- Sistema de batallas por turnos en cuadrícula 12x12
- Tres tipos de guerreros: Melee (Cuerpo a cuerpo), Ranged (Distancia), Magic (Mágico)
- Sistema de elementos con ventajas/desventajas (Fuego, Hielo, Agua, Tierra)
- Tres tipos de armas correspondientes: Espada, Arco, Varita Mágica
- Sistema de escudo y puntos de vida
- Batallas paralelas independientes entre ejércitos
- Interfaz gráfica interactiva con visualización en tiempo real
- Sistema de mejora de armas al derrotar enemigos

---

## 2. ARQUITECTURA DEL PROYECTO

### 2.1 Estructura de Paquetes

```
com.battlesimulator/
├── Main.java                          # Punto de entrada de la aplicación
├── domain/                            # Capa de dominio - Entidades del negocio
│   ├── IWarrior.java                  # Interfaz de guerrero
│   ├── Warrior.java                   # Clase abstracta base de guerrero
│   ├── MeleeWarrior.java              # Guerrero cuerpo a cuerpo
│   ├── RangedWarrior.java             # Guerrero a distancia
│   ├── MagicWarrior.java              # Guerrero mágico
│   ├── IWeapon.java                   # Interfaz de arma
│   ├── Weapon.java                    # Clase abstracta base de arma
│   ├── MeleeWeapon.java               # Arma cuerpo a cuerpo (Espada)
│   ├── RangedWeapon.java              # Arma a distancia (Arco)
│   ├── MagicWeapon.java               # Arma mágica (Varita)
│   ├── Element.java                   # Enum de elementos
│   ├── Army.java                      # Ejército (colección de guerreros)
│   ├── Clan.java                      # Clan (colección de ejércitos)
│   ├── Position.java                  # Posición en cuadrícula
│   └── DamageReport.java              # Reporte de daño de un ataque
├── usecases/                          # Capa de casos de uso - Lógica de negocio
│   ├── InteractiveBattle.java         # Controlador de batalla interactiva
│   ├── WarController.java             # Controlador de guerra
│   ├── Battle.java                    # [Comentado] Simulador de batalla antigua
│   └── War.java                       # [Comentado] Simulador de guerra antigua
└── ui/                                # Capa de presentación - Interfaz de usuario
    ├── MainFrame.java                 # Ventana principal
    ├── ConfigDialog.java              # Diálogo de configuración de clanes
    └── BattleFrame.java               # Ventana de batalla individual
```

### 2.2 Patrón Arquitectónico
El proyecto sigue una **arquitectura en capas** con separación clara de responsabilidades:

1. **Capa de Dominio (domain)**: Contiene las entidades del negocio y reglas fundamentales
2. **Capa de Casos de Uso (usecases)**: Contiene la lógica de aplicación y orquestación
3. **Capa de Presentación (ui)**: Contiene la interfaz gráfica y lógica de interacción

### 2.3 Patrones de Diseño Utilizados

#### 2.3.1 Strategy Pattern
Usado en el cálculo de daño base según tipo de guerrero:
- `MeleeWarrior`: `daño = arma.attack() + strength`
- `RangedWarrior`: `daño = arma.attack() + (strength / 2)`
- `MagicWarrior`: `daño = arma.attack() + strength`

#### 2.3.2 Template Method Pattern
`Warrior` es una clase abstracta que define el flujo de ataque, delegando cálculo específico a subclases:
```java
public abstract class Warrior implements IWarrior {
    public DamageReport attack(IWarrior target) {
        int baseDamage = calculateBaseDamage(); // Template method
        return target.takeDamage(baseDamage, weapon.getElement());
    }
    
    protected abstract int calculateBaseDamage(); // Implementado por subclases
}
```

#### 2.3.3 Observer Pattern (Implícito)
`WarController` observa las batallas y reacciona cuando finalizan:
```java
public void battleFinished(InteractiveBattle battle) {
    // Actualiza estado, empareja nuevas batallas
}
```

#### 2.3.4 MVC Pattern
- **Model**: Clases en `domain/`
- **View**: Clases en `ui/`
- **Controller**: `WarController`, `InteractiveBattle`

---

## 3. MODELO DE DOMINIO

### 3.1 Jerarquía de Entidades

```
Clan
  └── Army (1-3 ejércitos)
        └── IWarrior (5-30 guerreros)
              └── IWeapon (1 arma)
                    └── Element (1 elemento)
```

### 3.2 Entidades Principales

#### 3.2.1 Clan
Representa una facción en la guerra.

**Atributos:**
- `name: String` - Nombre del clan
- `armies: List<Army>` - Lista de ejércitos (máximo 3)
- `color: Color` - Color de identificación en UI

**Métodos clave:**
- `addArmy(Army)` - Añade ejército (máx 3)
- `getActiveArmies()` - Retorna ejércitos no derrotados
- `isDefeated()` - True si no tiene ejércitos activos

**Reglas de negocio:**
- Un clan puede tener entre 1 y 3 ejércitos
- Un clan está derrotado cuando todos sus ejércitos son derrotados

#### 3.2.2 Army
Representa un grupo de combate dentro de un clan.

**Atributos:**
- `warriors: List<IWarrior>` - Lista de guerreros
- `hasFought: boolean` - Flag de combate en ronda actual
- `id: String` - Identificador (A, B, C)
- `clan: Clan` - Clan al que pertenece

**Métodos clave:**
- `addWarrior(IWarrior)` - Añade guerrero al ejército
- `getWarriors()` - Retorna solo guerreros vivos
- `getAllWarriors()` - Retorna todos los guerreros (vivos y muertos)
- `isDefeated()` - True si no tiene guerreros vivos
- `getLevel()` - Calcula nivel: `guerreros_vivos * 10 + salud_total / 10`

**Reglas de negocio:**
- Un ejército está derrotado cuando todos sus guerreros mueren
- El nivel del ejército determina prioridad en emparejamiento

#### 3.2.3 IWarrior (Interfaz) / Warrior (Clase Abstracta)
Representa un combatiente individual.

**Atributos:**
- `name: String` - Nombre del guerrero
- `health: int` - Puntos de vida actuales
- `initialHealth: int` - Puntos de vida iniciales
- `shield: int` - Puntos de escudo actuales
- `initialShield: int` - Puntos de escudo iniciales
- `strength: int` - Fuerza base (5-19)
- `element: Element` - Elemento del guerrero
- `weapon: IWeapon` - Arma equipada
- `defeats: int` - Número de enemigos derrotados
- `position: Position` - Posición en cuadrícula
- `armyId: String` - ID del ejército
- `number: int` - Número dentro del ejército
- `clan: Clan` - Clan al que pertenece

**Métodos clave:**
- `attack(IWarrior target): DamageReport` - Ataca a un objetivo
- `takeDamage(int damage, Element element): DamageReport` - Recibe daño
- `calculateBaseDamage(): int` - Calcula daño base (abstracto)
- `isAlive(): boolean` - True si health > 0
- `reset()` - Reinicia estado a valores iniciales

**Implementaciones:**
1. **MeleeWarrior**: `baseDamage = weapon.attack() + strength`
2. **RangedWarrior**: `baseDamage = weapon.attack() + (strength / 2)`
3. **MagicWarrior**: `baseDamage = weapon.attack() + strength`

#### 3.2.4 IWeapon (Interfaz) / Weapon (Clase Abstracta)
Representa un arma equipada por guerrero.

**Atributos:**
- `damage: int` - Daño base actual
- `initialDamage: int` - Daño base inicial (10-24)
- `element: Element` - Elemento del arma
- `level: int` - Nivel del arma (empieza en 1)

**Métodos clave:**
- `attack(): int` - Calcula daño del arma (abstracto)
- `upgrade()` - Aumenta nivel y daño (+5)
- `reset()` - Reinicia a estado inicial

**Implementaciones:**
1. **MeleeWeapon (Espada)**: `attack = damage * level`
2. **RangedWeapon (Arco)**: `attack = (damage * level * 0.8)`
3. **MagicWeapon (Varita)**: `attack = damage * level + 10`

**Reglas de negocio:**
- Las armas mejoran cuando su portador mata a un enemigo
- Cada mejora incrementa el nivel en 1 y el daño en 5

#### 3.2.5 Element (Enum)
Representa los elementos mágicos.

**Valores:**
- `NONE` - Sin elemento
- `FIRE` - Fuego
- `ICE` - Hielo
- `WATER` - Agua
- `EARTH` - Tierra

**Métodos clave:**
- `getDamageMultiplier(Element target): double` - Calcula multiplicador de daño

**Matriz de Ventajas:**
```
FIRE vs ICE   → 1.5x (ventaja)
ICE vs FIRE   → 1.5x (ventaja)
WATER vs FIRE → 1.5x (ventaja)
FIRE vs WATER → 0.5x (desventaja)
Otros casos   → 1.0x (neutral)
```

#### 3.2.6 Position
Representa una posición en la cuadrícula de batalla.

**Atributos:**
- `row: int` - Fila (0-11)
- `col: int` - Columna (0-11)

**Métodos:**
- `equals(Object)` - Comparación por valor
- `hashCode()` - Hash para uso en colecciones

#### 3.2.7 DamageReport
Reporte inmutable de un ataque realizado.

**Atributos:**
- `baseDamage: int` - Daño calculado por atacante
- `multiplier: double` - Multiplicador elemental aplicado
- `effectiveDamage: int` - Daño después del multiplicador
- `absorbed: int` - Daño absorbido por escudo
- `finalDamage: int` - Daño real a salud
- `killed: boolean` - Si el objetivo murió

---

## 4. MECÁNICAS DE COMBATE

### 4.1 Flujo de Ataque
```
1. Atacante calcula daño base según su tipo
2. Se aplica multiplicador elemental
3. El escudo del objetivo absorbe daño
4. El daño restante reduce la salud
5. Si salud <= 0, el objetivo muere
6. Si el objetivo muere:
   - El arma del atacante mejora
   - El atacante se mueve a la posición del objetivo
```

### 4.2 Cálculo Detallado de Daño

```java
// 1. Daño base (varía según tipo de guerrero)
int baseDamage = calculateBaseDamage();

// 2. Multiplicador elemental
double multiplier = attackElement.getDamageMultiplier(targetElement);

// 3. Daño efectivo
int effectiveDamage = (int)(baseDamage * multiplier);

// 4. Absorción de escudo
int absorbed = Math.min(shield, effectiveDamage);
int finalDamage = effectiveDamage - absorbed;

// 5. Aplicación de daño
shield -= absorbed;
health -= finalDamage;

// 6. Verificación de muerte
boolean killed = (health <= 0);
if (killed) {
    health = 0;
    weapon.upgrade(); // Arma del atacante mejora
}
```

### 4.3 Sistema de Turnos

**En InteractiveBattle:**
1. La batalla comienza con el ejército 1
2. El jugador selecciona un guerrero de su turno actual
3. El jugador selecciona un guerrero enemigo como objetivo
4. Se ejecuta el ataque
5. El turno cambia al otro ejército
6. Se repite hasta que un ejército es derrotado

### 4.4 Mecánica de Posicionamiento

**Inicialización:**
- Cuadrícula de 12x12 (144 posiciones)
- Los guerreros se colocan aleatoriamente al inicio
- Cada posición puede tener máximo 1 guerrero

**Movimiento:**
- Los guerreros no se mueven voluntariamente
- Al matar a un enemigo, el atacante ocupa su posición
- Esto permite que el guerrero se acerque a otros enemigos

---

## 5. SISTEMA DE GUERRA

### 5.1 Gestión de Batallas Paralelas

`WarController` orquesta múltiples batallas simultáneas:

**Algoritmo de Emparejamiento:**
```java
1. Mientras ambos clanes tengan ejércitos activos:
   a. Seleccionar un ejército de cada clan
   b. Crear una batalla entre ellos
   c. Marcar ejércitos como "en combate"
   d. Añadir batalla a la lista
2. Las batallas se resuelven de forma independiente
3. Al finalizar una batalla:
   a. Se registra el ganador
   b. Se intentan emparejar nuevas batallas
   c. Si un clan no tiene más ejércitos, gana el otro
```

**Criterio de Selección de Ejército:**
```java
1. Filtrar ejércitos que no han peleado en esta ronda
2. Si hay ejércitos frescos, usar solo esos
3. Ordenar por nivel (descendente)
4. Seleccionar aleatoriamente entre los 3 mejores
```

### 5.2 Estados de Batalla

1. **No iniciada**: Batalla creada pero no comenzada
2. **En curso**: Batalla activa, esperando acciones del jugador
3. **Finalizada**: Un ejército ha sido derrotado

### 5.3 Condiciones de Victoria

**A nivel de batalla:**
- Un ejército gana cuando todos los guerreros enemigos mueren

**A nivel de guerra:**
- Un clan gana cuando el otro no tiene ejércitos activos
- Un clan sin ejércitos activos está completamente derrotado

---

## 6. INTERFAZ DE USUARIO

### 6.1 MainFrame (Ventana Principal)

**Responsabilidades:**
- Punto de entrada de la aplicación
- Botón para abrir configuración de clanes
- Botón para iniciar guerra (habilitado tras configurar)

**Flujo:**
```
Usuario → Configurar Clanes → ConfigDialog
       → Iniciar Guerra → WarController
```

### 6.2 ConfigDialog (Diálogo de Configuración)

**Parámetros configurables por clan:**
- Nombre del clan
- Color de identificación
- Número de ejércitos (1-3)
- Número de guerreros por ejército (5-30)
- Generación aleatoria de guerreros (checkbox)

**Generación Aleatoria:**
- Tipo de guerrero: 40% Melee, 30% Ranged, 30% Magic
- Elemento: Aleatorio entre FIRE, ICE, WATER, EARTH
- Daño de arma: 10-24
- Salud: 70-119
- Escudo: 5-29
- Fuerza: 5-19

### 6.3 WarController (Ventana de Control de Guerra)

**Componentes:**
1. **Panel superior**: Estado de ambos clanes (ejércitos activos)
2. **Panel central**: Log de guerra (texto)
3. **Panel inferior**: Lista de batallas

**Interacciones:**
- Click en batalla abre `BattleFrame`
- Se muestra el ganador de cada batalla
- Se actualiza automáticamente cuando finalizan batallas

### 6.4 BattleFrame (Ventana de Batalla Individual)

**Componentes:**
1. **Panel superior**: Indicador de turno actual
2. **Panel izquierdo**: Cuadrícula 12x12 de botones
3. **Panel derecho**: Log de batalla

**Flujo de Interacción:**
```
1. Turno de Ejército A:
   a. Usuario click en guerrero de A → Se selecciona (fondo amarillo)
   b. Usuario click en guerrero de B → Se ejecuta ataque
   c. Se actualiza UI y cambia turno

2. Turno de Ejército B:
   a. Usuario click en guerrero de B → Se selecciona
   b. Usuario click en guerrero de A → Se ejecuta ataque
   c. Se actualiza UI y cambia turno

3. Repetir hasta que un ejército es eliminado
```

**Visualización de Guerreros:**
- **Color de fondo**: Color del clan
- **Color de fondo amarillo**: Guerrero seleccionado como atacante
- **Color gris oscuro**: Casilla vacía
- **Texto**: ID del guerrero (ej: "A5", "B12")
- **Tooltip**: `HP:XX Sh:YY Str:ZZ Arma`

**Contraste de Texto:**
- Negro si el fondo es claro (brightness > 128)
- Blanco si el fondo es oscuro (brightness ≤ 128)

---

## 7. FLUJO DE DATOS

### 7.1 Inicialización de Guerra

```
1. Usuario configura clanes en ConfigDialog
   ↓
2. Se crean objetos Clan con ejércitos y guerreros
   ↓
3. MainFrame recibe los clanes y habilita botón "Iniciar Guerra"
   ↓
4. Usuario presiona "Iniciar Guerra"
   ↓
5. Se crea WarController con ambos clanes
   ↓
6. WarController reinicia estados de clanes
   ↓
7. WarController empareja batallas iniciales
   ↓
8. Se crean objetos InteractiveBattle
   ↓
9. Se inicializan posiciones aleatorias en cuadrículas
```

### 7.2 Flujo de Batalla Individual

```
1. Usuario abre BattleFrame desde lista en WarController
   ↓
2. BattleFrame renderiza cuadrícula con guerreros
   ↓
3. Usuario selecciona atacante (de su turno)
   ↓
4. Usuario selecciona objetivo (del turno enemigo)
   ↓
5. InteractiveBattle.performAttack(atacante, objetivo)
   ↓
6. Se calcula daño y se aplica
   ↓
7. Se genera DamageReport
   ↓
8. Si objetivo muere: atacante ocupa su posición, arma mejora
   ↓
9. Se registra log de ataque
   ↓
10. Cambia turno al otro ejército
   ↓
11. Se verifica si la batalla terminó
   ↓
12. Si terminó: notifica a WarController
   ↓
13. WarController intenta emparejar nuevas batallas
   ↓
14. Si un clan no tiene más ejércitos: FIN DE GUERRA
```

### 7.3 Diagrama de Flujo de Ataque

```
┌─────────────────────────┐
│ Usuario selecciona      │
│ atacante y objetivo     │
└───────────┬─────────────┘
            ↓
┌─────────────────────────┐
│ InteractiveBattle       │
│ valida turno y estado   │
└───────────┬─────────────┘
            ↓
┌─────────────────────────┐
│ Warrior.attack()        │
│ calcula daño base       │
└───────────┬─────────────┘
            ↓
┌─────────────────────────┐
│ target.takeDamage()     │
│ aplica multiplicador    │
│ absorbe con escudo      │
│ reduce salud            │
└───────────┬─────────────┘
            ↓
┌─────────────────────────┐
│ ¿Objetivo muere?        │
└───────────┬─────────────┘
            ↓
       ┌────┴────┐
       │   SÍ    │   NO
       ↓         ↓
┌─────────┐  ┌──────────┐
│ Arma    │  │ Continúa │
│ mejora  │  │ batalla  │
│         │  │          │
│ Atacante│  └──────────┘
│ se mueve│
└─────────┘
       ↓
┌─────────────────────────┐
│ Cambia turno            │
└───────────┬─────────────┘
            ↓
┌─────────────────────────┐
│ ¿Ejército derrotado?    │
└───────────┬─────────────┘
            ↓
       ┌────┴────┐
       │   SÍ    │   NO
       ↓         ↓
┌─────────┐  ┌──────────┐
│ Batalla │  │ Siguiente│
│ termina │  │ turno    │
└─────────┘  └──────────┘
```

---

## 8. CONFIGURACIÓN Y EJECUCIÓN

### 8.1 Requisitos del Sistema

**Software necesario:**
- Java Development Kit (JDK) 17 o superior
- Apache Maven 3.6+ (para compilar)
- Sistema operativo con soporte para Java Swing (Windows, Linux, macOS)

### 8.2 Compilación

```bash
# Compilar el proyecto
mvn clean compile

# Empaquetar en JAR
mvn clean package

# El JAR se generará en: target/to_battle_simulator-1.0-SNAPSHOT.jar
```

### 8.3 Ejecución

**Desde IDE:**
```
Ejecutar Main.java como aplicación Java
```

**Desde línea de comandos:**
```bash
# Opción 1: Ejecutar clase Main directamente
mvn exec:java -Dexec.mainClass="com.battlesimulator.Main"

# Opción 2: Ejecutar JAR compilado
java -jar target/to_battle_simulator-1.0-SNAPSHOT.jar
```

### 8.4 Estructura del POM

```xml
<project>
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.battlesimulator</groupId>
    <artifactId>to_battle_simulator</artifactId>
    <version>1.0-SNAPSHOT</version>
    
    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
    </properties>
</project>
```

**Dependencias:**
- No requiere dependencias externas
- Usa solo bibliotecas estándar de Java (java.util, javax.swing, java.awt)

---

## 9. DETALLES DE IMPLEMENTACIÓN

### 9.1 Manejo de Estado

**Estado mutable:**
- `Warrior.health` - Cambia durante combate
- `Warrior.shield` - Se reduce al recibir daño
- `Weapon.level` - Aumenta al derrotar enemigos
- `Army.hasFought` - Flag de ronda actual

**Reset de estado:**
Todos los guerreros y armas pueden reiniciarse a valores iniciales:
```java
warrior.reset(); // Restaura health, shield, position
weapon.reset();  // Restaura level y damage
```

### 9.2 Gestión de Colecciones

**Listas de guerreros:**
```java
// Todos los guerreros (incluyendo muertos)
List<IWarrior> allWarriors = army.getAllWarriors();

// Solo guerreros vivos
List<IWarrior> aliveWarriors = army.getWarriors();
// Implementación:
warriors.stream()
    .filter(IWarrior::isAlive)
    .collect(Collectors.toCollection(ArrayList::new));
```

**Mapa de posiciones:**
```java
Map<Position, IWarrior> gridPositions;
// Permite búsqueda O(1) de guerrero en posición
```

### 9.3 Concurrencia y Thread Safety

**Modelo de threading:**
- La UI se ejecuta en el Event Dispatch Thread (EDT) de Swing
- `SwingUtilities.invokeLater()` en Main.java asegura esto
- Las batallas son secuenciales (por turnos)
- No hay acceso concurrente a datos compartidos

### 9.4 Validaciones

**En InteractiveBattle.performAttack():**
```java
// Validar turno correcto
if (!currentTurnArmy.getWarriors().contains(attacker))
    throw new IllegalArgumentException("No es el turno de este guerrero");

// Validar objetivo enemigo
if (!enemyArmy.getWarriors().contains(target))
    throw new IllegalArgumentException("Objetivo inválido");

// Validar que estén vivos
if (!attacker.isAlive() || !target.isAlive())
    throw new IllegalArgumentException("Guerreros deben estar vivos");
```

### 9.5 Inmutabilidad

**Clases inmutables:**
- `Position` - Atributos final
- `DamageReport` - Todos los atributos final
- `Element` - Enum (inherentemente inmutable)

**Ventajas:**
- Thread-safe por diseño
- Pueden usarse como claves de mapa
- Previenen bugs por modificación accidental

---

## 10. EXTENSIBILIDAD

### 10.1 Añadir Nuevo Tipo de Guerrero

```java
// 1. Crear nueva clase que extiende Warrior
public class AssassinWarrior extends Warrior {
    public AssassinWarrior(String name, int health, int shield, 
                          int strength, Element element, IWeapon weapon) {
        super(name, health, shield, strength, element, weapon);
    }
    
    @Override
    protected int calculateBaseDamage() {
        // Definir fórmula única
        return weapon.attack() * 2 + strength / 2;
    }
}

// 2. Añadir lógica de creación en ConfigDialog
```

### 10.2 Añadir Nuevo Tipo de Arma

```java
// 1. Crear nueva clase que extiende Weapon
public class StaffWeapon extends Weapon {
    public StaffWeapon(int damage, Element element) {
        super(damage, element);
    }
    
    @Override
    public String getType() {
        return "Bastón";
    }
    
    @Override
    public int attack() {
        return damage * level + (level * 5);
    }
}

// 2. Actualizar ConfigDialog para generar esta arma
```

### 10.3 Añadir Nuevo Elemento

```java
// 1. Añadir valor al enum Element
public enum Element {
    NONE, FIRE, ICE, WATER, EARTH, LIGHTNING; // Nuevo elemento
    
    @Override
    public String toString() {
        return switch (this) {
            // ... casos existentes
            case LIGHTNING -> "rayo";
        };
    }
    
    public double getDamageMultiplier(Element targetElement) {
        // Añadir nuevas interacciones
        if (this == LIGHTNING && targetElement == WATER) return 2.0;
        if (this == LIGHTNING && targetElement == EARTH) return 0.5;
        // ... resto de lógica
        return 1.0;
    }
}
```

### 10.4 Añadir Habilidades Especiales

```java
// Añadir a interfaz IWarrior
public interface IWarrior {
    // ... métodos existentes
    void useSpecialAbility(IWarrior target);
    boolean canUseSpecialAbility();
}

// Implementar en Warrior
@Override
public void useSpecialAbility(IWarrior target) {
    // Lógica base, puede ser sobrescrita
}

// Sobrescribir en guerrero específico
public class MagicWarrior extends Warrior {
    private int mana = 100;
    
    @Override
    public void useSpecialAbility(IWarrior target) {
        if (mana >= 30) {
            // Ejecutar habilidad mágica especial
            mana -= 30;
        }
    }
    
    @Override
    public boolean canUseSpecialAbility() {
        return mana >= 30;
    }
}
```

---

## 11. LIMITACIONES Y CONSIDERACIONES

### 11.1 Limitaciones Actuales

1. **Tamaño de cuadrícula fijo**: 12x12 no configurable
2. **Máximo de ejércitos por clan**: 3 ejércitos
3. **Sin persistencia**: No se guardan partidas
4. **Sin IA**: Todas las acciones son manuales
5. **Sin red**: Solo juego local
6. **Batallas secuenciales**: Aunque múltiples batallas existen, se juegan una a la vez

### 11.2 Posibles Mejoras

1. **Sistema de guardado/carga**:
   - Serializar estado de guerra
   - Guardar configuraciones de clanes

2. **Inteligencia Artificial**:
   - Modo de batalla automática
   - Diferentes niveles de dificultad
   - Estrategias de ataque inteligentes

3. **Mejoras de UI**:
   - Animaciones de ataques
   - Efectos visuales de elementos
   - Sonidos de combate
   - Vista previa de rango de ataque

4. **Sistema de progresión**:
   - Experiencia de guerreros
   - Desbloqueo de habilidades
   - Árbol de mejoras

5. **Modo multijugador**:
   - Red local
   - Servidor dedicado
   - Matchmaking

### 11.3 Consideraciones de Rendimiento

**Rendimiento actual:**
- Muy eficiente para el tamaño de problema
- No hay operaciones costosas en loop principal
- La UI es responsiva

**Potenciales cuellos de botella:**
- Si se aumenta tamaño de cuadrícula significativamente
- Si se añaden muchas más batallas simultáneas
- Renderizado de UI con muchos elementos visuales

---

## 12. GLOSARIO TÉCNICO

| Término | Definición |
|---------|------------|
| **Clan** | Facción que agrupa múltiples ejércitos |
| **Ejército (Army)** | Unidad táctica compuesta por guerreros |
| **Guerrero (Warrior)** | Unidad de combate individual |
| **Elemento (Element)** | Tipo mágico que afecta daño (Fuego, Hielo, Agua, Tierra) |
| **Arma (Weapon)** | Equipamiento que determina daño base |
| **Escudo (Shield)** | Puntos que absorben daño antes de afectar salud |
| **Daño Base (Base Damage)** | Daño calculado antes de multiplicadores |
| **Daño Efectivo (Effective Damage)** | Daño después de aplicar multiplicador elemental |
| **Daño Final (Final Damage)** | Daño real aplicado a salud después de escudo |
| **Multiplicador Elemental** | Factor de daño por ventaja/desventaja de elementos |
| **Nivel de Ejército** | Métrica que combina cantidad y salud de guerreros |
| **Nivel de Arma** | Indicador de mejoras acumuladas del arma |
| **Turno** | Fase en la que un ejército puede atacar |
| **Batalla** | Combate entre dos ejércitos en una cuadrícula |
| **Guerra** | Conflicto general entre dos clanes con múltiples batallas |
| **Posición** | Coordenada (fila, columna) en la cuadrícula 12x12 |
| **Ronda** | Ciclo completo de emparejamiento de batallas |

---

## 13. DIAGRAMAS DE RELACIONES

### 13.1 Diagrama de Clases Simplificado

```
┌─────────────┐
│    Clan     │
├─────────────┤
│ - name      │
│ - armies[]  │
│ - color     │
└──────┬──────┘
       │ 1
       │ contiene
       │ 1..3
       ↓
┌─────────────┐
│    Army     │
├─────────────┤
│ - id        │
│ - warriors[]│
│ - hasFought │
│ - clan      │
└──────┬──────┘
       │ 1
       │ contiene
       │ 5..30
       ↓
┌─────────────┐        ┌──────────────┐
│  IWarrior   │◄───────│   Warrior    │
│  (interface)│        │  (abstract)  │
└─────────────┘        └──────┬───────┘
                              │ extienden
              ┌───────────────┼───────────────┐
              ↓               ↓               ↓
      ┌────────────┐  ┌─────────────┐ ┌────────────┐
      │MeleeWarrior│  │RangedWarrior│ │MagicWarrior│
      └────────────┘  └─────────────┘ └────────────┘
              │               │               │
              │ equipado con  │               │
              │      1        │               │
              ↓               ↓               ↓
         ┌──────────┐    ┌──────────┐   ┌──────────┐
         │ IWeapon  │◄───│  Weapon  │   │          │
         │(interface)│    │(abstract)│   │          │
         └──────────┘    └─────┬────┘   │          │
                               │ extienden          │
              ┌────────────────┼────────────────┐   │
              ↓                ↓                ↓   │
      ┌────────────┐  ┌─────────────┐ ┌────────────┐
      │MeleeWeapon │  │RangedWeapon │ │MagicWeapon │
      └────────────┘  └─────────────┘ └────────────┘
              │                │                │
              │ tiene elemento │                │
              └────────────────┼────────────────┘
                               ↓
                        ┌──────────┐
                        │ Element  │
                        │  (enum)  │
                        └──────────┘
```

### 13.2 Diagrama de Secuencia: Ataque

```
Usuario → BattleFrame → InteractiveBattle → Warrior(A) → Warrior(B) → DamageReport
   │          │                │                │            │              │
   │ click    │                │                │            │              │
   ├─────────→│                │                │            │              │
   │          │ performAttack  │                │            │              │
   │          ├───────────────→│                │            │              │
   │          │                │ attack(B)      │            │              │
   │          │                ├───────────────→│            │              │
   │          │                │                │ takeDamage │              │
   │          │                │                ├───────────→│              │
   │          │                │                │            │ create DR   │
   │          │                │                │            ├────────────→│
   │          │                │                │            │←────────────┤
   │          │                │                │←───────────┤              │
   │          │                │←───────────────┤            │              │
   │          │                │ update grid    │            │              │
   │          │                │ next turn      │            │              │
   │          │←───────────────┤                │            │              │
   │          │ refresh UI     │                │            │              │
   │←─────────┤                │                │            │              │
```

---

## 14. CASOS DE USO DETALLADOS

### 14.1 Caso de Uso: Configurar y Comenzar Guerra

**Actor**: Usuario

**Precondiciones**: 
- Aplicación iniciada
- MainFrame visible

**Flujo principal**:
1. Usuario hace click en "Configurar Clanes"
2. Sistema muestra ConfigDialog
3. Usuario ingresa nombre para Clan 1
4. Usuario selecciona color para Clan 1
5. Usuario configura número de ejércitos (1-3)
6. Usuario configura número de guerreros por ejército (5-30)
7. Usuario repite pasos 3-6 para Clan 2
8. Usuario hace click en "Configurar"
9. Sistema genera guerreros aleatorios según configuración
10. Sistema asigna armas y elementos aleatorios
11. Sistema asigna ID a ejércitos (A, B, C)
12. Sistema asigna números a guerreros (1-30)
13. Sistema cierra ConfigDialog
14. Sistema habilita botón "Iniciar Guerra"
15. Usuario hace click en "Iniciar Guerra"
16. Sistema crea WarController
17. Sistema empareja batallas iniciales
18. Sistema muestra ventana de control de guerra

**Postcondiciones**:
- Dos clanes configurados con ejércitos y guerreros
- Batallas iniciales emparejadas
- WarController visible con lista de batallas

### 14.2 Caso de Uso: Ejecutar una Batalla

**Actor**: Usuario

**Precondiciones**:
- Guerra iniciada
- Al menos una batalla disponible

**Flujo principal**:
1. Usuario hace click en batalla de la lista
2. Sistema abre BattleFrame con cuadrícula
3. Sistema coloca guerreros en posiciones aleatorias
4. Sistema muestra turno actual (Ejército A o B)
5. Usuario hace click en guerrero de su turno (atacante)
6. Sistema marca guerrero con fondo amarillo
7. Usuario hace click en guerrero enemigo (objetivo)
8. Sistema valida que es turno correcto y guerreros válidos
9. Sistema calcula daño base del atacante
10. Sistema aplica multiplicador elemental
11. Sistema reduce escudo del objetivo
12. Sistema reduce salud del objetivo
13. Sistema verifica si objetivo murió
14. Si murió:
    a. Sistema mejora arma del atacante
    b. Sistema mueve atacante a posición del objetivo
15. Sistema genera línea de log del ataque
16. Sistema cambia turno al otro ejército
17. Sistema actualiza UI (cuadrícula y log)
18. Si un ejército fue derrotado:
    a. Sistema muestra mensaje de victoria
    b. Sistema notifica a WarController
    c. Sistema cierra BattleFrame
19. Usuario repite pasos 5-17 hasta fin de batalla

**Postcondiciones**:
- Batalla completada con un ganador
- Estado de guerreros actualizado
- Log de batalla registrado
- WarController notificado

### 14.3 Caso de Uso: Completar una Guerra

**Actor**: Sistema (automático tras batallas)

**Precondiciones**:
- Guerra en curso
- Múltiples batallas definidas

**Flujo principal**:
1. Usuario completa una batalla
2. Sistema marca batalla como finalizada
3. Sistema intenta emparejar nuevas batallas
4. Sistema filtra ejércitos disponibles de ambos clanes
5. Si ambos clanes tienen ejércitos disponibles:
    a. Sistema selecciona ejércitos según criterio de nivel
    b. Sistema crea nueva batalla
    c. Sistema añade batalla a lista
    d. Volver a paso 3
6. Si algún clan no tiene ejércitos activos:
    a. Sistema determina clan ganador
    b. Sistema muestra mensaje de victoria final
    c. Sistema finaliza guerra

**Postcondiciones**:
- Todas las batallas resueltas
- Un clan declarado ganador
- Guerra completada

---

## 15. RESUMEN PARA PROCESAMIENTO POR IA

Este proyecto implementa un **simulador de batallas tácticas** con las siguientes características técnicas clave:

**Arquitectura**: Aplicación Java de escritorio con arquitectura en capas (Domain, UseCases, UI) y patrón MVC.

**Componentes principales**:
- **Dominio**: Clanes → Ejércitos → Guerreros → Armas → Elementos
- **Lógica**: InteractiveBattle (batallas por turnos), WarController (orquestación de guerra)
- **UI**: Swing con MainFrame, ConfigDialog, BattleFrame

**Mecánicas de combate**:
- Sistema por turnos en cuadrícula 12x12
- Cálculo de daño: `baseDamage × elementMultiplier - shield = finalDamage`
- Tres tipos de guerreros con fórmulas distintas
- Sistema de elementos con ventajas/desventajas
- Mejora de armas al derrotar enemigos

**Flujo de ejecución**:
1. Configuración de clanes y ejércitos
2. Emparejamiento automático de batallas
3. Combate por turnos manejado por usuario
4. Gestión paralela de múltiples batallas
5. Victoria cuando un clan pierde todos sus ejércitos

**Tecnologías**: Java 17, Maven, Swing, sin dependencias externas.

**Patrones de diseño**: Strategy (tipos de guerrero), Template Method (cálculo de daño), Observer (notificación de batallas), MVC (separación de capas).

El código está bien estructurado, es extensible y sigue principios SOLID. La arquitectura permite fácil adición de nuevos tipos de guerreros, armas, elementos o mecánicas de combate.

---

## 16. INFORMACIÓN ADICIONAL

### 16.1 Convenciones de Código

- **Nombrado de clases**: PascalCase (Ej: `MeleeWarrior`)
- **Nombrado de métodos**: camelCase (Ej: `calculateBaseDamage`)
- **Nombrado de constantes**: UPPER_SNAKE_CASE (Ej: `GRID_SIZE`)
- **Paquetes**: lowercase (Ej: `com.battlesimulator.domain`)

### 16.2 Comentarios en Código

- Clases `Battle.java` y `War.java` están comentadas (versión antigua)
- Código de producción está en `InteractiveBattle.java` y `WarController.java`
- Comentarios inline solo donde la lógica es compleja

### 16.3 Gestión de Errores

- Validaciones con `IllegalArgumentException` en operaciones críticas
- `JOptionPane` para mostrar errores al usuario
- No hay manejo explícito de excepciones de sistema (asume entorno estable)

### 16.4 Testing

- No hay tests unitarios en el proyecto actual
- Testing es manual a través de la UI
- Para añadir tests, se recomienda JUnit 5 con Mockito

---

**FIN DE LA GUÍA TÉCNICA**

Esta guía proporciona una descripción completa y técnica del proyecto Simulador de Batallas de Clanes, preparada para ser procesada y comprendida por sistemas de IA. Cubre arquitectura, implementación, flujos de datos, mecánicas de juego, y detalles de cada componente del sistema.
