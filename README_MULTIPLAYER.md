# Battle Simulator - Modo Multijugador

## Descripción
Sistema de simulación de batallas de clanes convertido a **modo multijugador en red**. Ahora dos jugadores pueden enfrentarse en tiempo real, cada uno controlando sus propios ejércitos.

## Nuevas Características Multijugador

### Conectividad de Red
- **Modo Host**: Crea un servidor y espera a que otro jugador se conecte
- **Modo Cliente**: Únete a una partida existente mediante la IP del host
- **Modo Local**: Mantiene la funcionalidad original de jugar solo

### Sistema de Chat
- Chat en tiempo real durante las batallas
- Comunicación entre jugadores

### Sincronización de Batallas
- Turnos alternados entre jugadores
- Sincronización automática de ataques
- Actualización en tiempo real del estado del juego

### Control de Guerra
- Cada jugador controla sus propios ejércitos
- Visualización clara del turno actual
- Indicadores visuales de qué ejércitos controlas

## Cómo Usar

### Iniciar una Partida como Host:
1. Ejecuta la aplicación
2. Haz clic en "Crear Partida (Host)"
3. El servidor se iniciará automáticamente
4. Comparte tu dirección IP con el otro jugador
5. Espera a que el otro jugador se conecte
6. Configura tus clanes
7. ¡Inicia la guerra!

### Unirse a una Partida:
1. Ejecuta la aplicación
2. Haz clic en "Unirse a Partida"
3. Ingresa la IP del host
4. Espera la confirmación de conexión
5. Configura tus clanes
6. ¡Inicia la guerra!

### Modo Local (Original):
1. Ejecuta la aplicación
2. Haz clic en "Modo Local"
3. Configura ambos clanes
4. Inicia la guerra como antes

## Estructura de Archivos Nuevos

### Red y Comunicación
- `network/Message.java` - Protocolo de mensajes
- `network/GameServer.java` - Servidor de juegos
- `network/GameClient.java` - Cliente de red

### Controladores Multijugador
- `usecases/MultiplayerWarController.java` - Control de guerra en red
- `ui/MultiplayerBattleFrame.java` - Interfaz de batalla multijugador

### Modificaciones
- `ui/MainFrame.java` - Actualizado con opciones multijugador
- `pom.xml` - Agregada dependencia Jackson para JSON

## Requisitos Técnicos

### Dependencias
- Java 17 o superior
- Jackson Databind 2.15.2 (para serialización JSON)

### Configuración de Red
- Puerto por defecto: **5555**
- Asegúrate de que el firewall permita conexiones en este puerto
- Para jugar en red local, ambos jugadores deben estar en la misma red

### Compilar el Proyecto
```bash
mvn clean compile
```

### Ejecutar
```bash
mvn exec:java -Dexec.mainClass="com.battlesimulator.Main"
```

## Mecánicas de Juego Multijugador

### Turnos
- El **Host** controla el **Ejército 1** (primer clan)
- El **Cliente** controla el **Ejército 2** (segundo clan)
- Los turnos se alternan automáticamente
- Solo puedes atacar en tu turno

### Batallas
- Cada batalla es independiente
- Selecciona tu guerrero atacante
- Selecciona el enemigo objetivo
- El ataque se sincroniza automáticamente con el otro jugador

### Finalización
- La guerra termina cuando un clan pierde todos sus ejércitos
- Ambos jugadores son notificados del ganador

## Solución de Problemas

### No puedo conectarme al servidor
- Verifica que el host haya iniciado el servidor
- Confirma que la IP sea correcta
- Revisa el firewall y puerto 5555

### El juego se congela
- Verifica tu conexión de red
- Asegúrate de que ambos jugadores estén conectados

### Los ataques no se sincronizan
- Espera tu turno (indicador verde)
- Verifica que ambos clientes estén conectados al servidor

## Notas de Desarrollo

### Arquitectura de Red
- Servidor basado en sockets TCP
- Comunicación mediante mensajes JSON
- Listeners para eventos asíncronos

### Protocolo de Mensajes
- `CONNECT` / `DISCONNECT` - Conexión
- `CLAN_CONFIG` - Configuración de clanes
- `START_WAR` - Inicio de guerra
- `ATTACK` - Ataque de guerrero
- `BATTLE_UPDATE` / `BATTLE_END` - Estado de batalla
- `WAR_STATUS` / `WAR_END` - Estado de guerra
- `CHAT` - Mensajes de chat

