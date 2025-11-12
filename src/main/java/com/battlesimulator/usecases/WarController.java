package com.battlesimulator.usecases;

import com.battlesimulator.domain.*;
import com.battlesimulator.ui.BattleFrame;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.*;

public class WarController extends JFrame {
  private final Clan clan1;
  private final Clan clan2;
  private final JTextArea warLogArea = new JTextArea();
  private final JLabel statusLabel1 = new JLabel();
  private final JLabel statusLabel2 = new JLabel();
  private final DefaultListModel<String> battleListModel = new DefaultListModel<>();
  private final JList<String> battleList = new JList<>(battleListModel);
  private final List<InteractiveBattle> allBattles = new ArrayList<>();
  private final Map<InteractiveBattle, String> battleWinners = new HashMap<>();
  private final Map<InteractiveBattle, BattleFrame> battleFrames = new HashMap<>();

  public WarController(Clan clan1, Clan clan2) {
    this.clan1 = clan1;
    this.clan2 = clan2;
    initUI();
    resetClans();
    initializeBattles();
  }

  private void initUI() {
    setTitle("Control de Guerra - " + clan1.getName() + " vs " + clan2.getName());
    setSize(600, 600);
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setLayout(new BorderLayout());

    JPanel topPanel = new JPanel(new GridLayout(1, 2));
    statusLabel1.setFont(new Font("Arial", Font.BOLD, 14));
    statusLabel2.setFont(new Font("Arial", Font.BOLD, 14));
    topPanel.add(statusLabel1);
    topPanel.add(statusLabel2);
    add(topPanel, BorderLayout.NORTH);

    warLogArea.setEditable(false);
    add(new JScrollPane(warLogArea), BorderLayout.CENTER);

    // Battle list at bottom
    battleList.addListSelectionListener(e -> {
      if (!e.getValueIsAdjusting()) {
        int index = battleList.getSelectedIndex();
        if (index >= 0) {
          openBattle(index);
        }
      }
    });
    add(new JScrollPane(battleList), BorderLayout.SOUTH);

    updateStatus();
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

    BattleFrame frame = battleFrames.get(battle);
    if (frame == null || !frame.isVisible()) {
      frame = new BattleFrame(battle, this);
      battleFrames.put(battle, frame);
    }
    frame.setVisible(true);
  }

  public void battleFinished(InteractiveBattle battle) {
    if (battle.isBattleOver()) {
      Army winArmy = battle.getWinner();
      String winStr = winArmy.getClan().getName() + " (" + winArmy.getId() + ")";
      battleWinners.put(battle, winStr);
      // Update list item
      int battleIndex = getBattleIndex(battle);
      if (battleIndex >= 0) {
        battleListModel.set(battleIndex, getBattleDescription(battle));
      }
      // Log
      for (String log : battle.getLogs()) {
        warLogArea.append(log + "\n");
      }
      warLogArea.setCaretPosition(warLogArea.getDocument().getLength());
      // Check for new pairs
      pairNewBattles();
      updateStatus();
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
      if (!a.hasFought()) { // Solo no fought? No, all active
        sb.append(a.getId()).append(" ");
      }
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
}