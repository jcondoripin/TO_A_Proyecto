package com.battlesimulator.usecases;

import com.battlesimulator.database.DatabaseManager;
import com.battlesimulator.database.User;
import com.battlesimulator.domain.*;
import com.battlesimulator.network.GameClient;
import com.battlesimulator.network.Message;
import com.battlesimulator.ui.MultiplayerBattleFrame;
import com.battlesimulator.ui.UITheme;
import java.awt.*;
import java.sql.SQLException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.*;

public class MultiplayerWarController extends JFrame {
  private final Clan clan1;
  private final Clan clan2;
  private final GameClient client;
  private final boolean isHost;
  private final JTextArea warLogArea = new JTextArea();
  private final JLabel statusLabel1 = new JLabel();
  private final JLabel statusLabel2 = new JLabel();
  private final DefaultListModel<String> battleListModel = new DefaultListModel<>();
  private final JList<String> battleList = new JList<>(battleListModel);
  private final List<InteractiveBattle> allBattles = new ArrayList<>();
  private final Map<InteractiveBattle, String> battleWinners = new HashMap<>();
  private final Map<InteractiveBattle, MultiplayerBattleFrame> battleFrames = new HashMap<>();
  private final JTextArea chatArea = new JTextArea();
  private final JTextField chatInput = new JTextField();
  private final User currentUser;
  private final String myUsername;

  public MultiplayerWarController(Clan clan1, Clan clan2, GameClient client, boolean isHost) {
    this(clan1, clan2, client, isHost, null, "Jugador");
  }
  
  public MultiplayerWarController(Clan clan1, Clan clan2, GameClient client, boolean isHost, User currentUser, String username) {
    this.clan1 = clan1;
    this.clan2 = clan2;
    this.client = client;
    this.isHost = isHost;
    this.currentUser = currentUser;
    this.myUsername = username != null ? username : "Jugador";
    initUI();
    resetClans();
    if (isHost) {
      initializeBattles();
    }
    setupNetworkListeners();
  }

  private void initUI() {
    setTitle("GUERRA MULTIJUGADOR: " + clan1.getName() + " vs " + clan2.getName());
    setSize(1100, 750);
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setLayout(new BorderLayout());
    UITheme.styleFrame(this);
    
    // Panel superior
    JPanel headerPanel = new JPanel(new BorderLayout());
    UITheme.stylePanel(headerPanel);
    headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));
    
    JLabel titleLabel = new JLabel("GUERRA MULTIJUGADOR", SwingConstants.CENTER);
    UITheme.styleTitleLabel(titleLabel);
    headerPanel.add(titleLabel, BorderLayout.NORTH);

    JPanel statusPanel = new JPanel(new GridLayout(1, 2, 20, 0));
    UITheme.stylePanel(statusPanel);
    statusPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
    
    JPanel clan1Panel = UITheme.createTitledPanel("🛡️ " + clan1.getName() + (isHost ? " (TÚ)" : ""));
    clan1Panel.setLayout(new BorderLayout());
    statusLabel1.setHorizontalAlignment(SwingConstants.CENTER);
    UITheme.styleLabel(statusLabel1);
    clan1Panel.add(statusLabel1, BorderLayout.CENTER);
    statusPanel.add(clan1Panel);
    
    JPanel clan2Panel = UITheme.createTitledPanel("🛡️ " + clan2.getName() + (!isHost ? " (TÚ)" : ""));
    clan2Panel.setLayout(new BorderLayout());
    statusLabel2.setHorizontalAlignment(SwingConstants.CENTER);
    UITheme.styleLabel(statusLabel2);
    clan2Panel.add(statusLabel2, BorderLayout.CENTER);
    statusPanel.add(clan2Panel);
    
    headerPanel.add(statusPanel, BorderLayout.CENTER);
    add(headerPanel, BorderLayout.NORTH);

    // Split principal con mejor diseño
    JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    mainSplit.setBackground(UITheme.PRIMARY_DARK);
    
    // Izquierda: logs y batallas
    JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
    UITheme.stylePanel(leftPanel);
    leftPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    
    JPanel logPanel = UITheme.createTitledPanel("📜 Registro de Guerra");
    logPanel.setLayout(new BorderLayout());
    UITheme.styleTextArea(warLogArea);
    warLogArea.setEditable(false);
    JScrollPane logScroll = new JScrollPane(warLogArea);
    logScroll.setBorder(BorderFactory.createLineBorder(UITheme.ACCENT_GOLD, 1));
    logPanel.add(logScroll, BorderLayout.CENTER);
    leftPanel.add(logPanel, BorderLayout.CENTER);
    
    JPanel battlePanel = UITheme.createTitledPanel("Batallas (Clic para ver)");
    battlePanel.setLayout(new BorderLayout());
    battlePanel.setPreferredSize(new Dimension(0, 120));
    
    battleList.setBackground(UITheme.SECONDARY_DARK);
    battleList.setForeground(UITheme.TEXT_LIGHT);
    battleList.setFont(UITheme.NORMAL_FONT);
    battleList.setSelectionBackground(UITheme.ACCENT_GOLD);
    battleList.setSelectionForeground(UITheme.PRIMARY_DARK);
    battleList.addListSelectionListener(e -> {
      if (!e.getValueIsAdjusting()) {
        int index = battleList.getSelectedIndex();
        if (index >= 0) {
          openBattle(index);
        }
      }
    });
    JScrollPane battleScroll = new JScrollPane(battleList);
    battleScroll.setBorder(BorderFactory.createLineBorder(UITheme.ACCENT_GOLD, 1));
    battlePanel.add(battleScroll, BorderLayout.CENTER);
    leftPanel.add(battlePanel, BorderLayout.SOUTH);
    
    mainSplit.setLeftComponent(leftPanel);
    
    // Derecha: chat
    JPanel chatPanel = UITheme.createTitledPanel("💬 Chat en Vivo");
    chatPanel.setLayout(new BorderLayout(5, 5));
    chatPanel.setBorder(BorderFactory.createCompoundBorder(
      chatPanel.getBorder(),
      BorderFactory.createEmptyBorder(5, 5, 5, 5)
    ));
    
    UITheme.styleTextArea(chatArea);
    chatArea.setEditable(false);
    JScrollPane chatScroll = new JScrollPane(chatArea);
    chatScroll.setBorder(BorderFactory.createLineBorder(UITheme.ACCENT_GOLD, 1));
    chatPanel.add(chatScroll, BorderLayout.CENTER);
    
    JPanel chatInputPanel = new JPanel(new BorderLayout(5, 5));
    UITheme.stylePanel(chatInputPanel);
    UITheme.styleTextField(chatInput);
    chatInput.addActionListener(e -> sendChat());
    chatInputPanel.add(chatInput, BorderLayout.CENTER);
    
    JButton sendBtn = new JButton("Enviar");
    sendBtn.addActionListener(e -> sendChat());
    UITheme.styleButton(sendBtn);
    chatInputPanel.add(sendBtn, BorderLayout.EAST);
    chatPanel.add(chatInputPanel, BorderLayout.SOUTH);
    
    mainSplit.setRightComponent(chatPanel);
    mainSplit.setDividerLocation(700);
    
    add(mainSplit, BorderLayout.CENTER);
    updateStatus();
  }
  
  private void sendChat() {
    String text = chatInput.getText().trim();
    if (!text.isEmpty() && client != null) {
      Message msg = new Message(Message.Type.CHAT, text);
      msg.setUsername(myUsername);
      client.sendMessage(msg);
      chatInput.setText("");
      // Mostrar mi propio mensaje
      addChatMessage(myUsername + ": " + text);
    }
  }
  
  private void setupNetworkListeners() {
    // Los mensajes de red son manejados por el listener en MainFrame
    // que debe tener una referencia a este controller y llamar handleNetworkMessage
  }
  
  public void handleNetworkMessage(Message message) {
    SwingUtilities.invokeLater(() -> {
      System.out.println("[" + (isHost ? "HOST" : "CLIENTE") + "] Recibido mensaje: " + message.getType() + " playerId=" + message.getPlayerId());
      
      switch (message.getType()) {
        case ATTACK:
          // Ignorar mis propios ataques (el servidor hace broadcast a todos)
          String senderPlayerId = message.getPlayerId();
          String myPlayerId = client != null ? client.getPlayerId() : null;
          System.out.println("[" + (isHost ? "HOST" : "CLIENTE") + "] ATTACK - senderPlayerId=" + senderPlayerId + ", myPlayerId=" + myPlayerId);
          if (senderPlayerId != null && senderPlayerId.equals(myPlayerId)) {
            System.out.println("[" + (isHost ? "HOST" : "CLIENTE") + "] Ignorando mi propio ataque");
            break;
          }
          System.out.println("[" + (isHost ? "HOST" : "CLIENTE") + "] Procesando ataque remoto");
          // Parsear el índice de batalla del mensaje
          handleRemoteAttack(message.getData());
          break;
        case CHAT:
          String username = message.getUsername() != null ? message.getUsername() : "Jugador";
          // No duplicar si es mi propio mensaje
          if (!username.equals(myUsername)) {
            addChatMessage(username + ": " + message.getData());
          }
          break;
        case WAR_END:
          JOptionPane.showMessageDialog(MultiplayerWarController.this,
            "¡La guerra ha terminado! Ganador: " + message.getData(),
            "Fin de la Guerra", JOptionPane.INFORMATION_MESSAGE);
          break;
        case BATTLE_END:
          warLogArea.append("Batalla finalizada. Ganador: " + message.getData() + "\n");
          break;
        case BATTLE_UPDATE:
          // Actualizar lista de batallas para el cliente
          System.out.println("[" + (isHost ? "HOST" : "CLIENTE") + "] Recibido BATTLE_UPDATE: " + message.getData());
          System.out.println("[" + (isHost ? "HOST" : "CLIENTE") + "] isHost=" + isHost + ", procesará: " + !isHost);
          if (!isHost) {
            handleBattleUpdate(message.getData());
          } else {
            System.out.println("[HOST] Ignorando BATTLE_UPDATE propio");
          }
          break;
        default:
          break;
      }
    });
  }

  private void resetClans() {
    resetClan(clan1);
    resetClan(clan2);
  }

  private void resetClan(Clan clan) {
    for (Army army : clan.getAllArmies()) {
      army.setHasFought(false);
      for (IWarrior w : army.getAllWarriors()) {
        w.reset();
      }
    }
  }

  private void initializeBattles() {
    battleListModel.clear();
    allBattles.clear();
    battleWinners.clear();
    battleFrames.clear();
    pairNewBattles(false); // false = no enviar a cliente durante inicialización
  }
  
  public void syncBattlesToClient() {
    // Enviar todas las batallas existentes al cliente
    if (client != null && isHost) {
      System.out.println("[HOST] Sincronizando " + allBattles.size() + " batallas al cliente");
      for (int i = 0; i < allBattles.size(); i++) {
        InteractiveBattle battle = allBattles.get(i);
        if (battle != null) {
          String battleData = serializeBattle(battle, i);
          System.out.println("[HOST] Enviando BATTLE_UPDATE: " + battleData);
          Message msg = new Message(Message.Type.BATTLE_UPDATE, battleData);
          client.sendMessage(msg);
        }
      }
    }
  }

  private void pairNewBattles() {
    pairNewBattles(true); // Por defecto sí enviar al cliente
  }
  
  private void pairNewBattles(boolean sendToClient) {
    while (!isWarOver() && !clan1.getActiveArmies().isEmpty() && !clan2.getActiveArmies().isEmpty()) {
      Army a1 = selectArmy(clan1);
      Army a2 = selectArmy(clan2);
      if (a1 == null || a2 == null)
        break;

      a1.setHasFought(true);
      a2.setHasFought(true);

      InteractiveBattle battle = new InteractiveBattle(a1, a2);
      battle.initBattle();
      String battleDesc = getBattleDescription(battle);
      battleListModel.addElement(battleDesc);
      allBattles.add(battle);
      
      // Sincronizar batalla completa al cliente solo si se solicita
      if (sendToClient && client != null) {
        String battleData = serializeBattle(battle, allBattles.size() - 1);
        System.out.println("[HOST] Enviando BATTLE_UPDATE: " + battleData);
        Message msg = new Message(Message.Type.BATTLE_UPDATE, battleData);
        client.sendMessage(msg);
      }
    }
    if (isWarOver()) {
      Clan winClan = clan1.isDefeated() ? clan2 : clan1;
      JOptionPane.showMessageDialog(this, "Ganador de la guerra: " + winClan.getName() + "!", "Victoria",
          JOptionPane.INFORMATION_MESSAGE);
      
      // Guardar resultado si hay usuario logueado
      if (currentUser != null) {
        saveWarResult(winClan);
      }
      
      // Notificar al otro jugador
      if (client != null) {
        Message msg = new Message(Message.Type.WAR_END, winClan.getName());
        client.sendMessage(msg);
      }
    }
  }
  
  private void saveWarResult(Clan winClan) {
    try {
      DatabaseManager db = DatabaseManager.getInstance();
      // En multijugador, el host controla clan1
      if ((isHost && winClan == clan1) || (!isHost && winClan == clan2)) {
        db.recordWin(currentUser.getId());
        addChatMessage("¡Victoria registrada en tus estadísticas!");
      } else {
        db.recordLoss(currentUser.getId());
        addChatMessage("Derrota registrada en tus estadísticas.");
      }
    } catch (SQLException e) {
      System.err.println("Error al guardar estadísticas: " + e.getMessage());
    }
  }

  private String getBattleDescription(InteractiveBattle battle) {
    String winnerStr = battleWinners.getOrDefault(battle, "");
    if (!winnerStr.isEmpty()) {
      return "Batalla: " + battle.getArmy1().getId() + " vs " + battle.getArmy2().getId() + " - Ganador: " + winnerStr;
    }
    return "Batalla: " + battle.getArmy1().getId() + " vs " + battle.getArmy2().getId() + " - En curso";
  }

  private void openBattle(int index) {
    InteractiveBattle battle = getBattleByIndex(index);
    if (battle == null)
      return;

    MultiplayerBattleFrame frame = battleFrames.get(battle);
    // Verificar si el frame existe Y está usable (no disposed)
    if (frame == null || !frame.isDisplayable()) {
      // Determinar si este jugador controla el ejército 1 basándose en el clan
      boolean controlsArmy1 = determineControlsArmy1(battle);
      frame = new MultiplayerBattleFrame(battle, this, client, controlsArmy1);
      battleFrames.put(battle, frame);
    }
    frame.setVisible(true);
    frame.toFront();
    
    // Actualizar lista con indicador visual
    updateBattleListIndicators();
  }
  
  private boolean determineControlsArmy1(InteractiveBattle battle) {
    // El jugador controla army1 si su clan (del host) tiene ese ejército
    // Host controla clan1, Cliente controla clan2
    Army army1 = battle.getArmy1();
    if (isHost) {
      return clan1.getAllArmies().contains(army1);
    } else {
      return clan2.getAllArmies().contains(army1);
    }
  }

  public void battleFinished(InteractiveBattle battle) {
    if (battle.isBattleOver()) {
      Army winArmy = battle.getWinner();
      String winStr = winArmy.getClan().getName() + " (" + winArmy.getId() + ")";
      battleWinners.put(battle, winStr);
      int battleIndex = getBattleIndex(battle);
      if (battleIndex >= 0) {
        battleListModel.set(battleIndex, getBattleDescription(battle));
      }
      for (String log : battle.getLogs()) {
        warLogArea.append(log + "\n");
      }
      warLogArea.setCaretPosition(warLogArea.getDocument().getLength());
      
      if (isHost) {
        pairNewBattles();
      }
      updateStatus();
      
      // Notificar fin de batalla
      if (client != null) {
        Message msg = new Message(Message.Type.BATTLE_END, winArmy.getId());
        client.sendMessage(msg);
      }
    }
  }

  public int getBattleIndex(InteractiveBattle battle) {
    return allBattles.indexOf(battle);
  }

  private InteractiveBattle getBattleByIndex(int index) {
    if (index < allBattles.size()) {
      return allBattles.get(index);
    }
    return null;
  }

  private boolean isWarOver() {
    return clan1.isDefeated() || clan2.isDefeated();
  }

  private void updateStatus() {
    statusLabel1.setText(clan1.getName() + ": " + getArmyIds(clan1));
    statusLabel2.setText(clan2.getName() + ": " + getArmyIds(clan2));
  }

  private String getArmyIds(Clan clan) {
    StringBuilder sb = new StringBuilder();
    for (Army a : clan.getActiveArmies()) {
      sb.append(a.getId()).append(" ");
    }
    return sb.toString().trim();
  }

  private Army selectArmy(Clan clan) {
    List<Army> available = clan.getActiveArmies().stream().filter(a -> !a.hasFought())
        .collect(Collectors.toCollection(ArrayList::new));
    if (available.isEmpty())
      return null;
    Collections.sort(available, (a, b) -> Integer.compare(b.getLevel(), a.getLevel()));
    Random rand = new Random();
    int size = Math.min(3, available.size());
    return available.get(rand.nextInt(size));
  }
  
  public void addChatMessage(String message) {
    SwingUtilities.invokeLater(() -> {
      chatArea.append(message + "\n");
      chatArea.setCaretPosition(chatArea.getDocument().getLength());
    });
  }
  
  private String serializeBattle(InteractiveBattle battle, int index) {
    // Formato: index|clan1Army|army1Id|clan2Army|army2Id|seed|description
    // clan1Army/clan2Army indica de qué clan viene cada ejército (1 o 2)
    StringBuilder sb = new StringBuilder();
    sb.append(index).append("|");
    
    Army army1 = battle.getArmy1();
    Army army2 = battle.getArmy2();
    
    System.out.println("[HOST] Serializando batalla " + index + ": " + army1.getId() + " vs " + army2.getId());
    System.out.println("[HOST] Army1 clan: " + army1.getClan().getName() + ", Army2 clan: " + army2.getClan().getName());
    
    // Determinar de qué clan es cada ejército
    int clan1Index = clan1.getAllArmies().contains(army1) ? 1 : 2;
    int clan2Index = clan1.getAllArmies().contains(army2) ? 1 : 2;
    
    System.out.println("[HOST] clan1Index=" + clan1Index + ", clan2Index=" + clan2Index);
    
    sb.append(clan1Index).append("|");
    sb.append(army1.getId()).append("|");
    sb.append(clan2Index).append("|");
    sb.append(army2.getId()).append("|");
    sb.append(battle.getRandomSeed()).append("|"); // Agregar la semilla
    sb.append(getBattleDescription(battle));
    
    String result = sb.toString();
    System.out.println("[HOST] Resultado serialización: " + result);
    return result;
  }
  
  private void handleBattleUpdate(String data) {
    try {
      System.out.println("[CLIENTE] handleBattleUpdate llamado con data: " + data);
      String[] parts = data.split("\\|", 8);
      System.out.println("[CLIENTE] Parts divididos: " + parts.length);
      if (parts.length < 7) {
        System.err.println("[CLIENTE] Error: datos insuficientes, solo " + parts.length + " partes");
        return;
      }
      
      int index = Integer.parseInt(parts[0]);
      int clan1Index = Integer.parseInt(parts[1]);
      String army1Id = parts[2];
      int clan2Index = Integer.parseInt(parts[3]);
      String army2Id = parts[4];
      long seed = Long.parseLong(parts[5]); // Obtener la semilla
      String description = parts.length > 6 ? parts[6] : "";
      
      System.out.println("[CLIENTE] Buscando ejércitos: clan" + clan1Index + "." + army1Id + " y clan" + clan2Index + "." + army2Id);
      System.out.println("[CLIENTE] Usando semilla: " + seed);
      
      // Buscar los ejércitos en el clan correcto
      Army a1 = findArmyById(clan1Index == 1 ? clan1 : clan2, army1Id);
      Army a2 = findArmyById(clan2Index == 1 ? clan1 : clan2, army2Id);
      
      System.out.println("[CLIENTE] Ejércitos encontrados: a1=" + (a1 != null ? a1.getId() : "null") + ", a2=" + (a2 != null ? a2.getId() : "null"));
      
      if (a1 != null && a2 != null) {
        // Marcar ejércitos como usados
        a1.setHasFought(true);
        a2.setHasFought(true);
        
        // Crear la batalla en el cliente con la misma semilla
        InteractiveBattle battle = new InteractiveBattle(a1, a2);
        battle.setRandomSeed(seed); // Usar la misma semilla que el host
        battle.initBattle();
        
        // Asegurar que el índice sea correcto
        while (allBattles.size() <= index) {
          allBattles.add(null);
        }
        allBattles.set(index, battle);
        
        // Actualizar la lista visual
        while (battleListModel.size() <= index) {
          battleListModel.addElement("");
        }
        battleListModel.set(index, description);
        
        System.out.println("[CLIENTE] Batalla creada en índice " + index + ": " + description);
        System.out.println("[CLIENTE] Total batallas ahora: " + allBattles.size());
        
        // Imprimir posiciones para debug
        for (IWarrior w : a1.getAllWarriors()) {
          System.out.println("[CLIENTE] Guerrero " + w.getId() + " en posición " + w.getPosition());
        }
      } else {
        System.err.println("[CLIENTE] No se pudieron encontrar los ejércitos");
      }
    } catch (Exception e) {
      e.printStackTrace();
      System.err.println("Error al procesar actualización de batalla: " + e.getMessage());
    }
  }
  
  private Army findArmyById(Clan clan, String armyId) {
    for (Army army : clan.getAllArmies()) {
      if (army.getId().equals(armyId)) {
        return army;
      }
    }
    return null;
  }
  
  private void handleRemoteAttack(String attackData) {
    try {
      System.out.println("[" + (isHost ? "HOST" : "CLIENTE") + "] handleRemoteAttack llamado con: " + attackData);
      String[] parts = attackData.split("\\|");
      if (parts.length < 7) {
        System.err.println("[ERROR] Datos de ataque incompletos: " + parts.length + " partes");
        return;
      }
      
      int battleIndex = Integer.parseInt(parts[0]);
      String remainingData = attackData.substring(attackData.indexOf("|") + 1);
      
      System.out.println("[" + (isHost ? "HOST" : "CLIENTE") + "] Buscando batalla con índice: " + battleIndex);
      
      // Obtener la batalla
      InteractiveBattle battle = getBattleByIndex(battleIndex);
      if (battle == null) {
        System.err.println("[ERROR] No se encontró batalla en índice " + battleIndex + ". Total batallas: " + allBattles.size());
        return;
      }
      
      System.out.println("[" + (isHost ? "HOST" : "CLIENTE") + "] Batalla encontrada. Turno actual: " + battle.getCurrentTurnArmy().getId());
      
      // Obtener o crear la ventana de batalla
      MultiplayerBattleFrame frame = battleFrames.get(battle);
      // Verificar si el frame existe Y está visible/usable
      boolean needsCreate = (frame == null || !frame.isDisplayable());
      
      if (needsCreate) {
        System.out.println("[" + (isHost ? "HOST" : "CLIENTE") + "] Creando nueva ventana de batalla");
        boolean controlsArmy1 = determineControlsArmy1(battle);
        frame = new MultiplayerBattleFrame(battle, this, client, controlsArmy1);
        battleFrames.put(battle, frame);
      }
      
      System.out.println("[" + (isHost ? "HOST" : "CLIENTE") + "] Enviando ataque a la ventana");
      
      // Enviar el ataque a la ventana
      frame.handleRemoteAttack(remainingData);
      
      // Siempre mostrar y traer al frente cuando llega un ataque
      frame.setVisible(true);
      frame.toFront();
      
      if (needsCreate) {
        // Notificar que se abrió automáticamente
        addChatMessage("[Sistema] Se abrió la batalla donde jugó tu oponente");
      }
      
      updateBattleListIndicators();
    } catch (Exception e) {
      e.printStackTrace();
      System.err.println("Error al procesar ataque remoto: " + e.getMessage());
    }
  }
  
  private void updateBattleListIndicators() {
    // Marcar batallas abiertas con indicador visual
    for (int i = 0; i < allBattles.size(); i++) {
      InteractiveBattle battle = allBattles.get(i);
      if (battle == null) continue;
      
      String baseDesc = getBattleDescription(battle);
      
      MultiplayerBattleFrame frame = battleFrames.get(battle);
      if (frame != null && frame.isVisible()) {
        if (!baseDesc.startsWith("▶ ")) {
          battleListModel.set(i, "▶ " + baseDesc);
        }
      } else {
        if (baseDesc.startsWith("▶ ")) {
          battleListModel.set(i, baseDesc.substring(2));
        }
      }
    }
  }
}
