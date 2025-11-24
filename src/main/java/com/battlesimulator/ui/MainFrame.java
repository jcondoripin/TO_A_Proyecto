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
    // Formato: nombre1|color1_r_g_b|numArmies1|numWarriorsPerArmy1;nombre2|color2_r_g_b|numArmies2|numWarriorsPerArmy2
    StringBuilder sb = new StringBuilder();
    sb.append(c1.getName()).append("|");
    sb.append(c1.getColor().getRed()).append("_").append(c1.getColor().getGreen()).append("_").append(c1.getColor().getBlue()).append("|");
    sb.append(c1.getAllArmies().size()).append("|");
    sb.append(c1.getAllArmies().get(0).getAllWarriors().size());
    sb.append(";");
    sb.append(c2.getName()).append("|");
    sb.append(c2.getColor().getRed()).append("_").append(c2.getColor().getGreen()).append("_").append(c2.getColor().getBlue()).append("|");
    sb.append(c2.getAllArmies().size()).append("|");
    sb.append(c2.getAllArmies().get(0).getAllWarriors().size());
    return sb.toString();
  }
  
  private void handleClanConfig(String clanData) {
    try {
      String[] clans = clanData.split(";");
      if (clans.length != 2) return;
      
      // Parsear clan 1
      String[] c1Parts = clans[0].split("\\|");
      String name1 = c1Parts[0];
      String[] rgb1 = c1Parts[1].split("_");
      Color color1 = new Color(Integer.parseInt(rgb1[0]), Integer.parseInt(rgb1[1]), Integer.parseInt(rgb1[2]));
      int armies1 = Integer.parseInt(c1Parts[2]);
      int warriors1 = Integer.parseInt(c1Parts[3]);
      
      // Parsear clan 2
      String[] c2Parts = clans[1].split("\\|");
      String name2 = c2Parts[0];
      String[] rgb2 = c2Parts[1].split("_");
      Color color2 = new Color(Integer.parseInt(rgb2[0]), Integer.parseInt(rgb2[1]), Integer.parseInt(rgb2[2]));
      int armies2 = Integer.parseInt(c2Parts[2]);
      int warriors2 = Integer.parseInt(c2Parts[3]);
      
      // Crear clanes usando la lógica de ConfigDialog
      clan1 = createClanFromConfig(name1, color1, armies1, warriors1);
      clan2 = createClanFromConfig(name2, color2, armies2, warriors2);
      
      SwingUtilities.invokeLater(() -> {
        statusLabel.setText("Configuración recibida del host: " + name1 + " vs " + name2);
        statusLabel.setForeground(Color.GREEN);
      });
    } catch (Exception e) {
      e.printStackTrace();
      JOptionPane.showMessageDialog(this,
        "Error al recibir configuración del host: " + e.getMessage(),
        "Error", JOptionPane.ERROR_MESSAGE);
    }
  }
  
  private Clan createClanFromConfig(String name, Color color, int numArmies, int numWarriorsPerArmy) {
    Clan clan = new Clan(name);
    clan.setColor(color);
    Random rand = new Random();
    
    for (int i = 0; i < numArmies; i++) {
      Army army = new Army();
      army.setId(String.valueOf((char) ('A' + i)));
      army.setClan(clan);
      
      for (int j = 1; j <= numWarriorsPerArmy; j++) {
        double typeRand = rand.nextDouble();
        Element elem = Element.values()[1 + rand.nextInt(4)];
        int wDmg = 10 + rand.nextInt(15);
        
        IWeapon weapon;
        if (typeRand < 0.4) {
          weapon = new MeleeWeapon(wDmg, elem);
        } else if (typeRand < 0.7) {
          weapon = new RangedWeapon(wDmg, elem);
        } else {
          weapon = new MagicWeapon(wDmg, elem);
        }
        
        int h = 70 + rand.nextInt(50);
        int s = 5 + rand.nextInt(25);
        int str = 5 + rand.nextInt(15);
        String wName = army.getId() + j;
        
        IWarrior warrior;
        if (weapon instanceof MeleeWeapon) {
          warrior = new MeleeWarrior(wName, h, s, str, elem, weapon);
        } else if (weapon instanceof RangedWeapon) {
          warrior = new RangedWarrior(wName, h, s, str, elem, weapon);
        } else {
          warrior = new MagicWarrior(wName, h, s, str, elem, weapon);
        }
        
        warrior.setArmyId(army.getId());
        warrior.setNumber(j);
        warrior.setClan(clan);
        army.addWarrior(warrior);
      }
      clan.addArmy(army);
    }
    return clan;
  }
  
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