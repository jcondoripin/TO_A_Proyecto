package com.battlesimulator.usecases;

import com.battlesimulator.domain.*;
import com.battlesimulator.ui.BattleFrame;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import javax.swing.*;

public class WarController extends JFrame {
  private final Clan clan1;
  private final Clan clan2;
  private final JTextArea warLogArea = new JTextArea();
  private final JLabel statusLabel1 = new JLabel();
  private final JLabel statusLabel2 = new JLabel();
  private final JButton nextBattleBtn = new JButton("Siguiente Batalla");

  public WarController(Clan clan1, Clan clan2) {
    this.clan1 = clan1;
    this.clan2 = clan2;
    initUI();
    resetClans();
    startNextBattle();
  }

  private void initUI() {
    setTitle("Control de Guerra - " + clan1.getName() + " vs " + clan2.getName());
    setSize(600, 400);
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

    nextBattleBtn.addActionListener((ActionEvent e) -> {
        startNextBattle();
    });
    add(nextBattleBtn, BorderLayout.SOUTH);

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

  private void startNextBattle() {
    if (isWarOver()) {
      Clan winClan = clan1.isDefeated() ? clan2 : clan1;
      JOptionPane.showMessageDialog(this, "¡Ganador de la guerra: " + winClan.getName() + "!", "Victoria",
          JOptionPane.INFORMATION_MESSAGE);
      return;
    }

    Army a1 = selectArmy(clan1);
    Army a2 = selectArmy(clan2);
    if (a1 == null || a2 == null) {
      startNextBattle(); // retry
      return;
    }

    InteractiveBattle battleCtrl = new InteractiveBattle(a1, a2);
    battleCtrl.initBattle();

    BattleFrame battleFrame = new BattleFrame(battleCtrl, this);
    battleFrame.setVisible(true);

    for (String log : battleCtrl.getLogs()) {
      warLogArea.append(log + "\n");
    }
    warLogArea.setCaretPosition(warLogArea.getDocument().getLength());

    updateStatus();
    startNextBattle();
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
    List<Army> available = clan.getActiveArmies();
    List<Army> fresh = available.stream().filter(a -> !a.hasFought()).collect(Collectors.toCollection(ArrayList::new));
    if (!fresh.isEmpty()) {
      available = fresh;
    }
    if (available.isEmpty())
      return null;
    Collections.sort(available, (a, b) -> Integer.compare(b.getLevel(), a.getLevel()));
    Random rand = new Random();
    int size = Math.min(3, available.size());
    return available.get(rand.nextInt(size));
  }
}