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

  public MultiplayerWarController(Clan clan1, Clan clan2, GameClient client, boolean isHost) {
    this(clan1, clan2, client, isHost, null);
  }
  
  public MultiplayerWarController(Clan clan1, Clan clan2, GameClient client, boolean isHost, User currentUser) {
    this.clan1 = clan1;
    this.clan2 = clan2;
    this.client = client;
    this.isHost = isHost;
    this.currentUser = currentUser;
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
      client.sendMessage(msg);
      chatInput.setText("");
    }
  }
  
  private void setupNetworkListeners() {
    // Ya configurado en MainFrame, pero aquí podríamos agregar listeners específicos
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
    pairNewBattles();
  }

  private void pairNewBattles() {
    while (!isWarOver() && !clan1.getActiveArmies().isEmpty() && !clan2.getActiveArmies().isEmpty()) {
      Army a1 = selectArmy(clan1);
      Army a2 = selectArmy(clan2);
      if (a1 == null || a2 == null)
        break;

      a1.setHasFought(true);
      a2.setHasFought(true);

      InteractiveBattle battle = new InteractiveBattle(a1, a2);
      battle.initBattle();
      battleListModel.addElement(getBattleDescription(battle));
      allBattles.add(battle);
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
    if (battle == null || battle.isBattleOver())
      return;

    MultiplayerBattleFrame frame = battleFrames.get(battle);
    if (frame == null || !frame.isVisible()) {
      // Determinar si este jugador controla el ejército 1 o 2
      boolean controlsArmy1 = isHost; // El host controla ejército 1
      frame = new MultiplayerBattleFrame(battle, this, client, controlsArmy1);
      battleFrames.put(battle, frame);
    }
    frame.setVisible(true);
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

  private int getBattleIndex(InteractiveBattle battle) {
    for (int i = 0; i < battleListModel.size(); i++) {
      if (battleListModel.get(i).contains(battle.getArmy1().getId() + " vs " + battle.getArmy2().getId())) {
        return i;
      }
    }
    return -1;
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
}
