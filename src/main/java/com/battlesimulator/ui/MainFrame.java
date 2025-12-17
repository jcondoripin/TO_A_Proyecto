package com.battlesimulator.ui;

import com.battlesimulator.database.DatabaseManager;
import com.battlesimulator.database.User;
import com.battlesimulator.database.UserStats;
import com.battlesimulator.domain.*;
import com.battlesimulator.network.GameClient;
import com.battlesimulator.network.GameServer;
import com.battlesimulator.network.Message;
import com.battlesimulator.usecases.MultiplayerWarController;
import com.battlesimulator.usecases.WarController;
import java.awt.*;
import java.sql.SQLException;
import java.util.Random;
import javax.swing.*;

public class MainFrame extends JFrame {
  private Clan clan1;
  private Clan clan2;
  private final JButton configBtn;
  private final JButton startBtn;
  private final JButton hostBtn;
  private final JButton joinBtn;
  private final JLabel statusLabel;
  private final JLabel userLabel;
  private final JButton statsBtn;
  private final JButton logoutBtn;
  
  private GameServer server;
  private GameClient client;
  private boolean isMultiplayer = false;
  private boolean isHost = false;
  private User currentUser;
  private MultiplayerWarController activeMultiplayerController;
  @SuppressWarnings("unused")
  private String myPlayerId;
  private String myUsername;

  public MainFrame() {
    setTitle("BATALLA DE CLANES");
    setSize(700, 550);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLayout(new BorderLayout());
    UITheme.styleFrame(this);
    
    // Cerrar recursos al cerrar la ventana
    addWindowListener(new java.awt.event.WindowAdapter() {
      @Override
      public void windowClosing(java.awt.event.WindowEvent e) {
        if (server != null) {
          server.stop();
        }
        if (client != null) {
          client.disconnect();
        }
      }
    });
    
    // Panel superior con título épico
    JPanel headerPanel = new JPanel(new BorderLayout());
    UITheme.stylePanel(headerPanel);
    headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
    
    JLabel titleLabel = new JLabel("BATALLA DE CLANES", SwingConstants.CENTER);
    UITheme.styleTitleLabel(titleLabel);
    headerPanel.add(titleLabel, BorderLayout.NORTH);
    
    // Panel de usuario
    JPanel userPanel = new JPanel(new BorderLayout());
    UITheme.stylePanel(userPanel);
    userPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
    
    userLabel = new JLabel("Invitado", SwingConstants.LEFT);
    UITheme.styleLabel(userLabel);
    userLabel.setFont(UITheme.SUBTITLE_FONT);
    userPanel.add(userLabel, BorderLayout.WEST);
    
    JPanel userButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
    UITheme.stylePanel(userButtonsPanel);
    
    statsBtn = new JButton("Estadísticas");
    statsBtn.setEnabled(false);
    statsBtn.addActionListener(e -> showStats());
    UITheme.styleButton(statsBtn);
    userButtonsPanel.add(statsBtn);
    
    logoutBtn = new JButton("Cerrar Sesión");
    logoutBtn.setEnabled(false);
    logoutBtn.addActionListener(e -> logout());
    UITheme.styleButton(logoutBtn);
    userButtonsPanel.add(logoutBtn);
    
    userPanel.add(userButtonsPanel, BorderLayout.EAST);
    headerPanel.add(userPanel, BorderLayout.CENTER);
    add(headerPanel, BorderLayout.NORTH);
    
    // Panel principal con mejor diseño
    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
    UITheme.stylePanel(mainPanel);
    mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
    
    // Status label
    statusLabel = new JLabel("Modo: Local", SwingConstants.CENTER);
    statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    UITheme.styleLabel(statusLabel);
    statusLabel.setFont(UITheme.SUBTITLE_FONT);
    statusLabel.setForeground(UITheme.ACCENT_GOLD);
    mainPanel.add(statusLabel);
    mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

    // Sección Multijugador
    JPanel multiplayerSection = UITheme.createTitledPanel("Modo Multijugador");
    multiplayerSection.setLayout(new GridLayout(1, 2, 15, 0));
    multiplayerSection.setBorder(BorderFactory.createCompoundBorder(
      multiplayerSection.getBorder(),
      BorderFactory.createEmptyBorder(10, 10, 10, 10)
    ));
    multiplayerSection.setMaximumSize(new Dimension(600, 80));
    
    hostBtn = new JButton("Crear Partida (Host)");
    hostBtn.addActionListener(e -> hostGame());
    UITheme.styleButton(hostBtn);
    multiplayerSection.add(hostBtn);
    
    joinBtn = new JButton("Unirse a Partida");
    joinBtn.addActionListener(e -> joinGame());
    UITheme.styleButton(joinBtn);
    multiplayerSection.add(joinBtn);
    
    mainPanel.add(multiplayerSection);
    mainPanel.add(Box.createRigidArea(new Dimension(0, 25)));
    
    // Botones principales
    configBtn = new JButton("Configurar Clanes");
    configBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
    configBtn.addActionListener(e -> new ConfigDialog(this).setVisible(true));
    UITheme.stylePrimaryButton(configBtn);
    configBtn.setMaximumSize(new Dimension(300, 50));
    mainPanel.add(configBtn);
    mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));

    startBtn = new JButton("INICIAR GUERRA");
    startBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
    startBtn.setEnabled(false);
    startBtn.addActionListener(e -> startWar());
    UITheme.stylePrimaryButton(startBtn);
    startBtn.setMaximumSize(new Dimension(300, 60));
    startBtn.setFont(new Font("Serif", Font.BOLD, 18));
    mainPanel.add(startBtn);
    mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));
    
    JButton localBtn = new JButton("Cambiar a Modo Local");
    localBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
    localBtn.addActionListener(e -> switchToLocal());
    UITheme.styleButton(localBtn);
    localBtn.setMaximumSize(new Dimension(250, 45));
    mainPanel.add(localBtn);
    
    add(mainPanel, BorderLayout.CENTER);
    
    // Footer
    JPanel footerPanel = new JPanel();
    UITheme.stylePanel(footerPanel);
    JLabel footerLabel = new JLabel("Prepara tus ejércitos y conquista el campo de batalla", SwingConstants.CENTER);
    UITheme.styleLabel(footerLabel);
    footerLabel.setFont(UITheme.SMALL_FONT);
    footerLabel.setForeground(UITheme.ACCENT_GOLD.darker());
    footerPanel.add(footerLabel);
    add(footerPanel, BorderLayout.SOUTH);
  }
  
  private void hostGame() {
    try {
      // Cerrar servidor existente si hay uno
      if (server != null) {
        try {
          server.stop();
          Thread.sleep(500); // Esperar a que el puerto se libere
        } catch (Exception e) {
          // Ignorar errores al cerrar
        }
      }
      
      server = new GameServer(new GameServer.ServerListener() {
        @Override
        public void onPlayerConnected(String playerId) {
          SwingUtilities.invokeLater(() -> {
            statusLabel.setText("Jugadores conectados: " + server.getConnectedPlayers() + "/2");
          });
        }
        
        @Override
        public void onPlayerDisconnected(String playerId) {
          SwingUtilities.invokeLater(() -> {
            statusLabel.setText("Jugadores conectados: " + server.getConnectedPlayers() + "/2");
          });
        }
        
        @Override
        public void onClanConfigured(String playerId, Clan clan) {}
        
        @Override
        public void onWarStartRequested() {}
        
        @Override
        public void onAttackReceived(String playerId, String attackData) {}
        
        @Override
        public void onChatMessage(String playerId, String message) {}
      });
      
      server.start();
      
      // Auto-conectar como host
      client = new GameClient(new GameClient.MessageListener() {
        @Override
        public void onMessageReceived(Message message) {
          if (activeMultiplayerController != null) {
            activeMultiplayerController.handleNetworkMessage(message);
          }
        }
        
        @Override
        public void onConnected(String playerId) {
          myPlayerId = playerId;
          myUsername = currentUser != null ? currentUser.getUsername() : "Host";
          
          SwingUtilities.invokeLater(() -> {
            statusLabel.setText("Host - " + myUsername + " | Esperando jugador...");
            statusLabel.setForeground(UITheme.ACCENT_GOLD);
            
            // Cuando se conecta un jugador, notificar al host
            if (server != null && server.getConnectedPlayers() > 1) {
              statusLabel.setText("Host - " + myUsername + " | Jugador conectado!");
              JOptionPane.showMessageDialog(MainFrame.this,
                "¡Un jugador se ha unido!\nYa puedes configurar los clanes.",
                "Jugador Conectado", JOptionPane.INFORMATION_MESSAGE);
            }
          });
        }
        
        @Override
        public void onDisconnected() {
          SwingUtilities.invokeLater(() -> {
            statusLabel.setText("Desconectado");
            statusLabel.setForeground(Color.RED);
            isMultiplayer = false;
            isHost = false;
          });
        }
        
        @Override
        public void onError(String errorMessage) {
          SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(MainFrame.this,
              errorMessage,
              "Error de Conexión", JOptionPane.ERROR_MESSAGE);
            statusLabel.setText("Error de conexión");
            statusLabel.setForeground(Color.RED);
          });
        }
      });
      
      int actualPort = server.getActualPort();
      if (client.connect("localhost", actualPort)) {
        isMultiplayer = true;
        isHost = true;
        hostBtn.setEnabled(false);
        joinBtn.setEnabled(false);
        configBtn.setEnabled(true);
        JOptionPane.showMessageDialog(this, 
          "Servidor creado en puerto " + actualPort + ".\n" +
          "Esperando que otro jugador se conecte...\n" +
          "Como HOST, configura los clanes y inicia la guerra cuando estés listo.\n\n" +
          "Otros jugadores deben conectarse a localhost:" + actualPort,
          "Servidor Activo", JOptionPane.INFORMATION_MESSAGE);
      }
    } catch (Exception e) {
      e.printStackTrace();
      String errorMsg = e.getMessage();
      if (errorMsg.contains("Address already in use") || errorMsg.contains("bind")) {
        errorMsg = "El puerto está ocupado. Cierra otras instancias del juego o reinicia la aplicación.\n\nDetalles: " + errorMsg;
      }
      JOptionPane.showMessageDialog(this, "Error al crear servidor: " + errorMsg, 
        "Error", JOptionPane.ERROR_MESSAGE);
      
      // Limpiar servidor fallido
      if (server != null) {
        server.stop();
        server = null;
      }
    }
  }
  
  private void joinGame() {
    String host = JOptionPane.showInputDialog(this, "Ingresa la IP del host:", "localhost");
    if (host != null && !host.trim().isEmpty()) {
      client = new GameClient(new GameClient.MessageListener() {
        @Override
        public void onMessageReceived(Message message) {
          // Manejar mensajes específicos antes de pasar al controller
          if (message.getType() == Message.Type.CLAN_CONFIG && !isHost) {
            handleClanConfig(message.getData());
          } else if (message.getType() == Message.Type.START_WAR && !isHost) {
            // El cliente recibe la señal de inicio de guerra
            SwingUtilities.invokeLater(() -> {
              if (clan1 != null && clan2 != null) {
                activeMultiplayerController = new MultiplayerWarController(clan1, clan2, client, isHost, currentUser, myUsername);
                activeMultiplayerController.setVisible(true);
              }
            });
          }
          
          if (activeMultiplayerController != null) {
            activeMultiplayerController.handleNetworkMessage(message);
          }
        }
        
        @Override
        public void onConnected(String playerId) {
          myPlayerId = playerId;
          myUsername = currentUser != null ? currentUser.getUsername() : "Jugador";
          
          SwingUtilities.invokeLater(() -> {
            statusLabel.setText("Conectado - " + myUsername);
            statusLabel.setForeground(UITheme.ACCENT_GOLD);
          });
        }
        
        @Override
        public void onDisconnected() {
          SwingUtilities.invokeLater(() -> {
            statusLabel.setText("Desconectado");
            statusLabel.setForeground(Color.RED);
            isMultiplayer = false;
          });
        }
        
        @Override
        public void onError(String errorMessage) {
          SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(MainFrame.this,
              errorMessage,
              "Error de Conexión", JOptionPane.ERROR_MESSAGE);
            statusLabel.setText("Error de conexión");
            statusLabel.setForeground(Color.RED);
          });
        }
      });
      
      if (client.connect(host.trim(), 5555)) {
        isMultiplayer = true;
        isHost = false;
        hostBtn.setEnabled(false);
        joinBtn.setEnabled(false);
        configBtn.setEnabled(false);
        startBtn.setEnabled(false);
        JOptionPane.showMessageDialog(this, 
          "Conectado exitosamente al servidor!\n" +
          "Esperando que el host configure y comience la partida...", 
          "Conexión Exitosa", JOptionPane.INFORMATION_MESSAGE);
      } else {
        JOptionPane.showMessageDialog(this, "No se pudo conectar al servidor", 
          "Error", JOptionPane.ERROR_MESSAGE);
      }
    }
  }
  
  private void switchToLocal() {
    if (client != null) {
      client.disconnect();
      client = null;
    }
    if (server != null) {
      server.stop();
      server = null;
    }
    isMultiplayer = false;
    isHost = false;
    hostBtn.setEnabled(true);
    joinBtn.setEnabled(true);
    statusLabel.setText("Modo: Local");
    statusLabel.setForeground(UITheme.ACCENT_GOLD);
  }
  
  private void startWar() {
    if (clan1 != null && clan2 != null) {
      if (isMultiplayer && client != null) {
        // Si es host, enviar señal de inicio al cliente
        if (isHost && server != null) {
          Message msg = new Message(Message.Type.START_WAR, "START");
          server.broadcast(msg);
        }
        
        activeMultiplayerController = new MultiplayerWarController(clan1, clan2, client, isHost, currentUser, myUsername);
        activeMultiplayerController.setVisible(true);
        
        // Si es host, sincronizar batallas al cliente después de un pequeño delay
        // para asegurar que el cliente ya creó su controlador
        if (isHost) {
          SwingUtilities.invokeLater(() -> {
            try {
              Thread.sleep(500); // Esperar 500ms para que el cliente esté listo
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
            activeMultiplayerController.syncBattlesToClient();
          });
        }
      } else {
        new WarController(clan1, clan2, currentUser).setVisible(true);
      }
    }
  }

  public void setClans(Clan c1, Clan c2) {
    this.clan1 = c1;
    this.clan2 = c2;
    startBtn.setEnabled(true);
    
    // Si es host en multijugador, enviar configuración al cliente
    if (isMultiplayer && isHost && client != null && server != null) {
      // Serializar clanes y enviar al cliente
      try {
        String clanData = serializeClans(c1, c2);
        Message msg = new Message(Message.Type.CLAN_CONFIG, clanData);
        server.broadcast(msg);
        JOptionPane.showMessageDialog(this, 
          "Clanes configurados correctamente.\n" +
          "Configuración enviada al cliente.\n" +
          "Puedes iniciar la guerra cuando estés listo.");
      } catch (Exception e) {
        JOptionPane.showMessageDialog(this, 
          "Error al enviar configuración: " + e.getMessage(),
          "Error", JOptionPane.ERROR_MESSAGE);
      }
    } else {
      JOptionPane.showMessageDialog(this, "Clanes configurados correctamente. Puedes iniciar la guerra.");
    }
  }
  
  private void showLoginDialog() {
    LoginDialog loginDialog = new LoginDialog(this);
    loginDialog.setVisible(true);
    
    User user = loginDialog.getLoggedInUser();
    if (user != null) {
      currentUser = user;
      userLabel.setText("Usuario: " + user.getUsername());
      statsBtn.setEnabled(true);
      logoutBtn.setEnabled(true);
    }
  }
  
  private void showStats() {
    if (currentUser == null) return;
    
    try {
      DatabaseManager db = DatabaseManager.getInstance();
      UserStats stats = db.getUserStats(currentUser.getId());
      
      String message = String.format(
        "Estadísticas de %s\n\n" +
        "Victorias: %d\n" +
        "Derrotas: %d\n" +
        "Total de Batallas: %d\n" +
        "Tasa de Victoria: %.1f%%",
        currentUser.getUsername(),
        stats.getWins(),
        stats.getLosses(),
        stats.getTotalBattles(),
        stats.getWinRate()
      );
      
      JOptionPane.showMessageDialog(this, message, 
        "Estadísticas", JOptionPane.INFORMATION_MESSAGE);
    } catch (SQLException e) {
      JOptionPane.showMessageDialog(this, 
        "Error al cargar estadísticas: " + e.getMessage(),
        "Error", JOptionPane.ERROR_MESSAGE);
    }
  }
  
  private void logout() {
    int option = JOptionPane.showConfirmDialog(this,
      "¿Seguro que deseas cerrar sesión?",
      "Confirmar", JOptionPane.YES_NO_OPTION);
    
    if (option == JOptionPane.YES_OPTION) {
      // Desconectar de multijugador si está activo
      switchToLocal();
      
      // Reiniciar estado
      currentUser = null;
      userLabel.setText("Invitado");
      statsBtn.setEnabled(false);
      logoutBtn.setEnabled(false);
      clan1 = null;
      clan2 = null;
      startBtn.setEnabled(false);
      
      // Mostrar login nuevamente
      SwingUtilities.invokeLater(() -> showLoginDialog());
    }
  }
  
  public User getCurrentUser() {
    return currentUser;
  }
  
  private String serializeClans(Clan c1, Clan c2) {
    // Formato completo: serializa cada guerrero con todos sus datos
    // Formato: clan1Data;;clan2Data
    // Cada clan: nombre|r_g_b|army1Data~army2Data~...
    // Cada army: armyId:warrior1Data,warrior2Data,...
    // Cada warrior: type:health:shield:strength:element:weaponType:weaponDamage:weaponElement
    StringBuilder sb = new StringBuilder();
    sb.append(serializeClan(c1));
    sb.append(";;");
    sb.append(serializeClan(c2));
    return sb.toString();
  }
  
  private String serializeClan(Clan clan) {
    StringBuilder sb = new StringBuilder();
    sb.append(clan.getName()).append("|");
    sb.append(clan.getColor().getRed()).append("_").append(clan.getColor().getGreen()).append("_").append(clan.getColor().getBlue()).append("|");
    
    for (int i = 0; i < clan.getAllArmies().size(); i++) {
      Army army = clan.getAllArmies().get(i);
      if (i > 0) sb.append("~");
      sb.append(army.getId()).append(":");
      
      for (int j = 0; j < army.getAllWarriors().size(); j++) {
        IWarrior w = army.getAllWarriors().get(j);
        if (j > 0) sb.append(",");
        // type:health:shield:strength:element:weaponType:weaponDamage:weaponElement
        String wType = w.getWarriorType();
        IWeapon weapon = w.getWeapon();
        String weaponType = weapon.getType();
        sb.append(wType).append(":");
        sb.append(w.getHealth()).append(":");
        sb.append(w.getShield()).append(":");
        sb.append(w.getStrength()).append(":");
        sb.append(getWarriorElement(w).name()).append(":");
        sb.append(weaponType).append(":");
        sb.append(getWeaponDamage(weapon)).append(":");
        sb.append(weapon.getElement().name());
      }
    }
    return sb.toString();
  }
  
  private Element getWarriorElement(IWarrior w) {
    // Usar reflexión para obtener el elemento del guerrero
    try {
      java.lang.reflect.Field field = Warrior.class.getDeclaredField("element");
      field.setAccessible(true);
      return (Element) field.get(w);
    } catch (Exception e) {
      return Element.NONE;
    }
  }
  
  private int getWeaponDamage(IWeapon weapon) {
    try {
      java.lang.reflect.Field field = Weapon.class.getDeclaredField("initialDamage");
      field.setAccessible(true);
      return field.getInt(weapon);
    } catch (Exception e) {
      return 10;
    }
  }
  
  private void handleClanConfig(String clanData) {
    try {
      System.out.println("[CLIENTE] Recibiendo configuración de clanes:");
      System.out.println(clanData);
      
      String[] clans = clanData.split(";;");
      if (clans.length != 2) {
        System.err.println("[CLIENTE] Error: se esperaban 2 clanes, se recibieron " + clans.length);
        return;
      }
      
      // Deserializar clan 1
      clan1 = deserializeClan(clans[0]);
      // Deserializar clan 2
      clan2 = deserializeClan(clans[1]);
      
      System.out.println("[CLIENTE] Clanes deserializados:");
      System.out.println("  Clan1: " + clan1.getName() + " con " + clan1.getAllArmies().size() + " ejércitos");
      System.out.println("  Clan2: " + clan2.getName() + " con " + clan2.getAllArmies().size() + " ejércitos");
      
      // Verificar guerreros
      for (Army army : clan1.getAllArmies()) {
        System.out.println("    Ejército " + army.getId() + ": " + army.getAllWarriors().size() + " guerreros");
        for (IWarrior w : army.getAllWarriors()) {
          System.out.println("      - " + w.getId() + " (" + w.getWarriorType() + ") HP:" + w.getHealth());
        }
      }
      
      SwingUtilities.invokeLater(() -> {
        statusLabel.setText("Configuración recibida: " + clan1.getName() + " vs " + clan2.getName());
        statusLabel.setForeground(Color.GREEN);
      });
    } catch (Exception e) {
      e.printStackTrace();
      JOptionPane.showMessageDialog(this,
        "Error al recibir configuración del host: " + e.getMessage(),
        "Error", JOptionPane.ERROR_MESSAGE);
    }
  }
  
  private Clan deserializeClan(String clanData) {
    // Formato: nombre|r_g_b|army1Data~army2Data~...
    String[] parts = clanData.split("\\|", 3);
    String name = parts[0];
    String[] rgb = parts[1].split("_");
    Color color = new Color(Integer.parseInt(rgb[0]), Integer.parseInt(rgb[1]), Integer.parseInt(rgb[2]));
    
    Clan clan = new Clan(name);
    clan.setColor(color);
    
    if (parts.length > 2 && !parts[2].isEmpty()) {
      String[] armiesData = parts[2].split("~");
      for (String armyData : armiesData) {
        Army army = deserializeArmy(armyData, clan);
        clan.addArmy(army);
      }
    }
    
    return clan;
  }
  
  private Army deserializeArmy(String armyData, Clan clan) {
    // Formato: armyId:warrior1Data,warrior2Data,...
    String[] parts = armyData.split(":", 2);
    String armyId = parts[0];
    
    Army army = new Army();
    army.setId(armyId);
    army.setClan(clan);
    
    if (parts.length > 1 && !parts[1].isEmpty()) {
      String[] warriorsData = parts[1].split(",");
      int warriorNum = 1;
      for (String warriorData : warriorsData) {
        IWarrior warrior = deserializeWarrior(warriorData, armyId, warriorNum, clan);
        army.addWarrior(warrior);
        warriorNum++;
      }
    }
    
    return army;
  }
  
  private IWarrior deserializeWarrior(String warriorData, String armyId, int number, Clan clan) {
    // Formato: type:health:shield:strength:element:weaponType:weaponDamage:weaponElement
    String[] parts = warriorData.split(":");
    String type = parts[0];
    int health = Integer.parseInt(parts[1]);
    int shield = Integer.parseInt(parts[2]);
    int strength = Integer.parseInt(parts[3]);
    Element element = Element.valueOf(parts[4]);
    String weaponType = parts[5];
    int weaponDamage = Integer.parseInt(parts[6]);
    Element weaponElement = Element.valueOf(parts[7]);
    
    // Crear arma según tipo
    IWeapon weapon;
    switch (weaponType) {
      case "Espada":
        weapon = new MeleeWeapon(weaponDamage, weaponElement);
        break;
      case "Arco":
        weapon = new RangedWeapon(weaponDamage, weaponElement);
        break;
      case "Varita mágica":
        weapon = new MagicWeapon(weaponDamage, weaponElement);
        break;
      default:
        weapon = new MeleeWeapon(weaponDamage, weaponElement);
    }
    
    // Crear guerrero según tipo
    String wName = armyId + number;
    IWarrior warrior;
    switch (type) {
      case "melee":
        warrior = new MeleeWarrior(wName, health, shield, strength, element, weapon);
        break;
      case "ranged":
        warrior = new RangedWarrior(wName, health, shield, strength, element, weapon);
        break;
      case "magic":
        warrior = new MagicWarrior(wName, health, shield, strength, element, weapon);
        break;
      default:
        warrior = new MeleeWarrior(wName, health, shield, strength, element, weapon);
    }
    
    warrior.setArmyId(armyId);
    warrior.setNumber(number);
    warrior.setClan(clan);
    
    return warrior;
  }
  
  // Se eliminó createClanFromConfig - ahora usamos deserialización completa
  
  @Override
  public void setVisible(boolean visible) {
    if (visible && currentUser == null) {
      super.setVisible(true);
      SwingUtilities.invokeLater(this::showLoginDialog);
    } else {
      super.setVisible(visible);
    }
  }
}