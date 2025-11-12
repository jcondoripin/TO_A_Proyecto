package com.battlesimulator.ui;

import com.battlesimulator.domain.*;
import com.battlesimulator.usecases.InteractiveBattle;
import java.awt.*;
import javax.swing.*;

public class BattleFrame extends JDialog {
  private static final int GRID_SIZE = 12;
  private final InteractiveBattle battleCtrl;
  private final JButton[][] gridButtons = new JButton[GRID_SIZE][GRID_SIZE];
  private IWarrior selectedAttacker;
  private final JTextArea logArea = new JTextArea();
  private final JLabel turnLabel = new JLabel();
  private Army winner;

  public BattleFrame(InteractiveBattle battleCtrl, JFrame parent) {
    super(parent, "Batalla en cuadricula", true);
    this.battleCtrl = battleCtrl;
    initUI();
    battleCtrl.initBattle();
    refreshGrid();
    updateTurnLabel();
    logArea.append("Batalla iniciada. Selecciona guerrero y enemigo.\n");
  }

  private void initUI() {
    setSize(1200, 900); // Aumentar tamaño de ventana para acomodar botones más grandes
    setLayout(new BorderLayout());

    // Turn label
    add(turnLabel, BorderLayout.NORTH);

    // Grid left
    JPanel gridPanel = new JPanel(new GridLayout(GRID_SIZE, GRID_SIZE, 1, 1));
    for (int r = 0; r < GRID_SIZE; r++) {
      for (int c = 0; c < GRID_SIZE; c++) {
        JButton btn = new JButton();
        btn.setPreferredSize(new Dimension(48, 48));
        btn.setFont(new Font("Arial", Font.BOLD, 18));
        btn.setMargin(new Insets(2, 2, 2, 2));
        int finalR = r, finalC = c;
        btn.addActionListener(e -> handleButtonClick(finalR, finalC));
        gridButtons[r][c] = btn;
        gridPanel.add(btn);
      }
    }
    add(gridPanel, BorderLayout.WEST);

    // Logs right
    logArea.setEditable(false);
    add(new JScrollPane(logArea), BorderLayout.CENTER);

    pack();
  }

  private void handleButtonClick(int row, int col) {
    Position p = new Position(row, col);
    IWarrior clicked = battleCtrl.getWarriorAt(p);
    if (clicked == null || !clicked.isAlive())
      return;

    if (battleCtrl.isOwnWarrior(clicked)) {
      selectedAttacker = clicked;
      refreshGrid();
      logArea.append("Atacante: " + clicked.getId() + "\n");
    } else {
      if (selectedAttacker != null) {
        try {
          DamageReport dr = battleCtrl.performAttack(selectedAttacker, clicked);
          logArea.append(generateLogLine(selectedAttacker, clicked, dr) + "\n");
          logArea.setCaretPosition(logArea.getDocument().getLength());
          selectedAttacker = null;
          refreshGrid();
          updateTurnLabel();
          if (battleCtrl.isBattleOver()) {
            JOptionPane.showMessageDialog(this,
                "Ganador batalla: Ej. " + battleCtrl.getWinner().getId() + " ("
                    + battleCtrl.getWinner().getClan().getName() + ")",
                "Victoria batalla", JOptionPane.INFORMATION_MESSAGE);
            winner = battleCtrl.getWinner();
            dispose();
          }
        } catch (IllegalArgumentException ex) {
          JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
      }
    }
  }

  private String generateLogLine(IWarrior att, IWarrior tar, DamageReport dr) {
    String weaponStr = att.getWeapon().getName();
    String multStr = dr.getMultiplier() != 1.0 ? "x" + String.format("%.1f", dr.getMultiplier()) : "";
    int healthBefore = tar.getHealth() + dr.getFinalDamage();
    String killedStr = dr.isKilled() ? ". Muerto. " + att.getId() + " ocupa pos." : "";
    return att.getId() + "(" + weaponStr + ") ataca " + tar.getId() + ". Dano: " + dr.getBaseDamage() + multStr + "="
        + dr.getEffectiveDamage()
        + ". Abs: " + dr.getAbsorbed() + ". Fin: " + dr.getFinalDamage()
        + ". " + tar.getId() + ": " + healthBefore + ">" + tar.getHealth() + killedStr;
  }

  private void refreshGrid() {
    for (int r = 0; r < GRID_SIZE; r++) {
      for (int c = 0; c < GRID_SIZE; c++) {
        Position p = new Position(r, c);
        IWarrior w = battleCtrl.getWarriorAt(p);
        JButton btn = gridButtons[r][c];
        if (w == null) {
          btn.setText("");
          btn.setBackground(Color.GRAY.darker());
          btn.setToolTipText(null);
        } else {
          btn.setText(w.getId());
          Color bg = w.getClan().getColor();
          if (selectedAttacker == w) {
            bg = Color.YELLOW;
          }
          btn.setBackground(bg);
          btn.setToolTipText(String.format("HP:%d Sh:%d Str:%d %s", w.getHealth(), w.getShield(), w.getStrength(),
              w.getWeapon().getName()));
        }
      }
    }
  }

  private void updateTurnLabel() {
    turnLabel.setText(battleCtrl.getTurnDescription());
  }

  public Army getWinner() {
    return winner;
  }
}