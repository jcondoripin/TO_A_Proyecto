# Sistema de Autenticación y Estadísticas - Battle Simulator

## Nuevas Funcionalidades Implementadas

### 1. **Sistema de Login/Registro**
- Registro de usuarios con username y contraseña
- Login seguro con hash de contraseñas (BCrypt)
- Gestión de sesión durante el juego

### 2. **Base de Datos SQLite**
- Base de datos local embebida (no requiere instalación adicional)
- Archivo: `battlesimulator.db` (se crea automáticamente)
- Tablas: `users` y `stats`

### 3. **Estadísticas Persistentes**
- Contador de victorias
- Contador de derrotas
- Total de batallas jugadas
- Tasa de victoria (win rate)

## Estructura de Base de Datos

### Tabla `users`
```sql
CREATE TABLE users (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  username TEXT UNIQUE NOT NULL,
  password_hash TEXT NOT NULL,
  created_at TEXT NOT NULL
)
```

### Tabla `stats`
```sql
CREATE TABLE stats (
  user_id INTEGER PRIMARY KEY,
  wins INTEGER DEFAULT 0,
  losses INTEGER DEFAULT 0,
  total_battles INTEGER DEFAULT 0,
  FOREIGN KEY (user_id) REFERENCES users(id)
)
```

## Flujo de Uso

### Primera Vez
1. **Iniciar la aplicación**
2. **Pantalla de Login aparece automáticamente**
3. **Hacer clic en "Registrarse"**
4. **Ingresar:**
   - Usuario (mínimo 3 caracteres)
   - Contraseña (mínimo 4 caracteres)
   - Confirmar contraseña
5. **Cuenta creada y sesión iniciada automáticamente**

### Usuarios Existentes
1. **Iniciar la aplicación**
2. **Pantalla de Login aparece automáticamente**
3. **Ingresar usuario y contraseña**
4. **Hacer clic en "Iniciar Sesión"**

### Durante el Juego
- **Ver Estadísticas**: Botón "Ver Estadísticas" en menú principal
- **Cerrar Sesión**: Botón "Cerrar Sesión" para cambiar de usuario
- **Las estadísticas se guardan automáticamente** al terminar cada guerra

## Seguridad

### Hash de Contraseñas
- **BCrypt** con salt automático
- Las contraseñas **NUNCA** se guardan en texto plano
- Hash de una sola vía (no reversible)

### Validaciones
- Username único (no puede haber duplicados)
- Longitud mínima de usuario: 3 caracteres
- Longitud mínima de contraseña: 4 caracteres
- Confirmación de contraseña en registro

## Archivos Nuevos Creados

### Package `database`
```
src/main/java/com/battlesimulator/database/
├── DatabaseManager.java      - Gestor principal de BD
├── User.java                  - Modelo de usuario
└── UserStats.java             - Modelo de estadísticas
```

### UI Nuevos Diálogos
```
src/main/java/com/battlesimulator/ui/
├── LoginDialog.java           - Pantalla de login
└── RegisterDialog.java        - Pantalla de registro
```

### Modificaciones
- `MainFrame.java` - Agregado login automático y gestión de usuario
- `WarController.java` - Guarda estadísticas al terminar guerra
- `MultiplayerWarController.java` - Guarda estadísticas en modo multijugador
- `pom.xml` - Agregadas dependencias SQLite y BCrypt

## Dependencias Agregadas

### SQLite JDBC (3.43.0.0)
```xml
<dependency>
    <groupId>org.xerial</groupId>
    <artifactId>sqlite-jdbc</artifactId>
    <version>3.43.0.0</version>
</dependency>
```
**Función**: Base de datos embebida sin servidor

### jBCrypt (0.4)
```xml
<dependency>
    <groupId>org.mindrot</groupId>
    <artifactId>jbcrypt</artifactId>
    <version>0.4</version>
</dependency>
```
**Función**: Hash seguro de contraseñas

## Características del Sistema

### Modo Local
- Al ganar una guerra: se registra **1 victoria**
- Al perder una guerra: se registra **1 derrota**
- El jugador siempre controla el **clan1** (primer clan configurado)

### Modo Multijugador
- **Host** controla clan1
- **Cliente** controla clan2
- Las estadísticas se guardan según quién gana:
  - Host gana si clan1 gana
  - Cliente gana si clan2 gana

### Ventana de Estadísticas
Muestra:
- Nombre de usuario
- Victorias totales
- Derrotas totales
- Total de batallas
- **Tasa de victoria** (porcentaje)

## Uso de la Base de Datos

### Singleton Pattern
```java
DatabaseManager db = DatabaseManager.getInstance();
```

### Registrar Usuario
```java
User user = db.registerUser("username", "password");
```

### Login
```java
User user = db.loginUser("username", "password");
```

### Obtener Estadísticas
```java
UserStats stats = db.getUserStats(userId);
```

### Registrar Victoria
```java
db.recordWin(userId);
```

### Registrar Derrota
```java
db.recordLoss(userId);
```

## Ventajas del Sistema

### 1. **Auto-contenido**
- No requiere servidor de BD externo
- Archivo único: `battlesimulator.db`
- Portable con el proyecto

### 2. **Seguro**
- Contraseñas hasheadas con BCrypt
- Salt automático por contraseña
- Validaciones de entrada

### 3. **Persistente**
- Estadísticas guardadas permanentemente
- Sobrevive al cierre de la aplicación
- Historial completo de batallas

### 4. **Fácil de Usar**
- Login automático al iniciar
- Registro integrado en login
- Estadísticas guardadas automáticamente

## Compilar y Ejecutar

### Compilar
```bash
mvn clean compile
```

### Ejecutar
```bash
mvn exec:java -Dexec.mainClass="com.battlesimulator.Main"
```

### Archivo de BD
El archivo `battlesimulator.db` se creará automáticamente en el directorio raíz del proyecto la primera vez que se ejecute.

## Inspeccionar Base de Datos

### Con SQLite Browser (opcional)
1. Descargar [DB Browser for SQLite](https://sqlitebrowser.org/)
2. Abrir `battlesimulator.db`
3. Ver tablas y datos

### Consultas SQL Útiles
```sql
-- Ver todos los usuarios
SELECT * FROM users;

-- Ver estadísticas de todos
SELECT u.username, s.wins, s.losses, s.total_battles 
FROM users u 
JOIN stats s ON u.id = s.user_id;

-- Top 5 jugadores por victorias
SELECT u.username, s.wins, s.losses, 
       ROUND(CAST(s.wins AS FLOAT) / s.total_battles * 100, 2) as win_rate
FROM users u 
JOIN stats s ON u.id = s.user_id
ORDER BY s.wins DESC
LIMIT 5;
```

## Solución de Problemas

### "Usuario ya existe"
- El username debe ser único
- Intenta con otro nombre de usuario

### "Contraseña incorrecta"
- Verifica que la contraseña sea correcta
- Las contraseñas son case-sensitive

### Base de datos bloqueada
- Cierra otras instancias de la aplicación
- Verifica que no haya DB Browser abierto

### Estadísticas no se guardan
- Asegúrate de haber iniciado sesión
- Completa toda la guerra (no solo batallas individuales)
- Verifica que aparezca el mensaje de confirmación

## Ejemplo de Uso

### Escenario 1: Primer Usuario
```
1. Abrir aplicación
2. Clic en "Registrarse"
3. Usuario: "guerrero123"
4. Contraseña: "pass1234"
5. Confirmar: "pass1234"
6. Cuenta creada
7. Configurar clanes
8. Iniciar guerra
9. Ganar guerra
10. Victoria registrada: 1-0 (100%)
```

### Escenario 2: Usuario Existente
```
1. Abrir aplicación
2. Usuario: "guerrero123"
3. Contraseña: "pass1234"
4. Login exitoso
5. Clic en "Ver Estadísticas"
6. Ver: 1 victoria, 0 derrotas, 100% win rate
7. Jugar otra guerra
8. Perder
9. Derrota registrada: 1-1 (50%)
```

## Notas Técnicas

### Thread Safety
- `DatabaseManager` usa Singleton
- Una sola conexión compartida
- SQLite maneja concurrencia automáticamente

### Transacciones
- Cada operación es atómica
- No hay transacciones explícitas necesarias
- AUTOINCREMENT en ID de usuario

### Manejo de Errores
- `SQLException` capturada y mostrada al usuario
- Mensajes de error descriptivos
- Validaciones antes de operaciones BD

## Migración de Datos

Si necesitas resetear la BD:
```bash
# Eliminar archivo de BD
rm battlesimulator.db

# O renombrar para backup
mv battlesimulator.db battlesimulator.db.backup
```

