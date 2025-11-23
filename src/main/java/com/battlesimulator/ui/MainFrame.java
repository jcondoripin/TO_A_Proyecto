package com.battlesimulator.ui;

import com.battlesimulator.database.DatabaseManager;
import com.battlesimulator.database.User;
import com.battlesimulator.database.UserStats;
import com.battlesimulator.domain.Clan;
import com.battlesimulator.network.GameClient;
import com.battlesimulator.network.GameServer;
import com.battlesimulator.network.Message;
import com.battlesimulator.usecases.MultiplayerWarController;
import com.battlesimulator.usecases.WarController;
import java.awt.*;
import java.sql.SQLException;
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

  public MainFrame() {
    setTitle("BATALLA DE CLANES");
    setSize(700, 550);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLayout(new BorderLayout());
    UITheme.styleFrame(this);
    
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
        public void onMessageReceived(Message message) {}
        
        @Override
        public void onConnected(String playerId) {
          SwingUtilities.invokeLater(() -> {
            statusLabel.setText("Host - ID: " + playerId + " | Esperando jugador...");
            statusLabel.setForeground(UITheme.ACCENT_GOLD);
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
      
      if (client.connect("localhost", 5555)) {
        isMultiplayer = true;
        isHost = true;
        hostBtn.setEnabled(false);
        joinBtn.setEnabled(false);
        JOptionPane.showMessageDialog(this, 
          "Servidor creado. Esperando que otro jugador se conecte a tu IP en el puerto 5555",
          "Servidor Activo", JOptionPane.INFORMATION_MESSAGE);
      }
    } catch (Exception e) {
      JOptionPane.showMessageDialog(this, "Error al crear servidor: " + e.getMessage(), 
        "Error", JOptionPane.ERROR_MESSAGE);
    }
  }
  
  private void joinGame() {
    String host = JOptionPane.showInputDialog(this, "Ingresa la IP del host:", "localhost");
    if (host != null && !host.trim().isEmpty()) {
      client = new GameClient(new GameClient.MessageListener() {
        @Override
        public void onMessageReceived(Message message) {}
        
        @Override
        public void onConnected(String playerId) {
          SwingUtilities.invokeLater(() -> {
            statusLabel.setText("Conectado - ID: " + playerId);
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
        JOptionPane.showMessageDialog(this, "Conectado exitosamente!", 
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
    }
    if (server != null) {
      server.stop();
    }
    isMultiplayer = false;
    isHost = false;
    hostBtn.setEnabled(true);
    joinBtn.setEnabled(true);
    statusLabel.setText("Modo: Local");
  }
  
  private void startWar() {
    if (clan1 != null && clan2 != null) {
      if (isMultiplayer && client != null) {
        new MultiplayerWarController(clan1, clan2, client, isHost, currentUser).setVisible(true);
      } else {
        new WarController(clan1, clan2, currentUser).setVisible(true);
      }
    }
  }

  public void setClans(Clan c1, Clan c2) {
    this.clan1 = c1;
    this.clan2 = c2;
    startBtn.setEnabled(true);
    JOptionPane.showMessageDialog(this, "Clanes configurados correctamente. Puedes iniciar la guerra.");
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
      currentUser = null;
      userLabel.setText("Invitado");
      statsBtn.setEnabled(false);
      logoutBtn.setEnabled(false);
      JOptionPane.showMessageDialog(this, 
        "Sesión cerrada. Inicia sesión para guardar tus estadísticas.",
        "Sesión Cerrada", JOptionPane.INFORMATION_MESSAGE);
    }
  }
  
  public User getCurrentUser() {
    return currentUser;
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