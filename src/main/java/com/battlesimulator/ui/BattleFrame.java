package com.battlesimulator.ui;

import com.battlesimulator.domain.*;
import com.battlesimulator.usecases.InteractiveBattle;
import com.battlesimulator.usecases.WarController;
import java.awt.*;
import javax.swing.*;

public class BattleFrame extends JDialog {
  private static final int GRID_SIZE = 12;
  private final InteractiveBattle battleCtrl;
  private final WarController warController;
  private final JButton[][] gridButtons = new JButton[GRID_SIZE][GRID_SIZE];
  private IWarrior selectedAttacker;
  private final JTextArea logArea = new JTextArea();
  private final JLabel turnLabel = new JLabel();
  private final JLabel rangeInfoLabel = new JLabel();
  private Army winner;

  public BattleFrame(InteractiveBattle battleCtrl, WarController warController) {
    super(warController, "Batalla en cuadricula", false); // non-modal
    this.battleCtrl = battleCtrl;
    this.warController = warController;
    initUI();
    refreshGrid();
    updateTurnLabel();
    logArea.append("⚔️ Batalla iniciada. Selecciona un guerrero propio y luego un enemigo.\n");
    logArea.append("📏 Los rangos de ataque varían según el tipo de guerrero.\n\n");
  }

  private void initUI() {
    setSize(1500, 950);
    setLayout(new BorderLayout());
    getContentPane().setBackground(UITheme.PRIMARY_DARK);

    // Panel superior con turno e info de rango
    JPanel topPanel = new JPanel(new BorderLayout());
    UITheme.stylePanel(topPanel);
    topPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
    
    JLabel titleLabel = new JLabel("⚔️ CAMPO DE BATALLA ⚔️", SwingConstants.CENTER);
    UITheme.styleTitleLabel(titleLabel);
    topPanel.add(titleLabel, BorderLayout.NORTH);
    
    // Panel central con turno y rango
    JPanel infoPanel = new JPanel(new GridLayout(2, 1));
    infoPanel.setOpaque(false);
    
    turnLabel.setHorizontalAlignment(SwingConstants.CENTER);
    UITheme.styleLabel(turnLabel);
    turnLabel.setFont(UITheme.SUBTITLE_FONT);
    turnLabel.setForeground(UITheme.ACCENT_GOLD);
    infoPanel.add(turnLabel);
    
    rangeInfoLabel.setHorizontalAlignment(SwingConstants.CENTER);
    UITheme.styleLabel(rangeInfoLabel);
    rangeInfoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    rangeInfoLabel.setForeground(new Color(200, 200, 200));
    infoPanel.add(rangeInfoLabel);
    
    topPanel.add(infoPanel, BorderLayout.CENTER);
    
    add(topPanel, BorderLayout.NORTH);

    // Grid con mejor diseño
    JPanel gridContainer = new JPanel(new BorderLayout());
    UITheme.stylePanel(gridContainer);
    gridContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    
    // Leyenda de rangos
    JPanel legendPanel = createLegendPanel();
    gridContainer.add(legendPanel, BorderLayout.NORTH);
    
    JPanel gridPanel = new JPanel(new GridLayout(GRID_SIZE, GRID_SIZE, 2, 2));
    gridPanel.setBackground(UITheme.BATTLE_GRID_BORDER);
    gridPanel.setBorder(BorderFactory.createLineBorder(UITheme.ACCENT_GOLD, 3));
    
    for (int r = 0; r < GRID_SIZE; r++) {
      for (int c = 0; c < GRID_SIZE; c++) {
        JButton btn = new JButton();
        btn.setPreferredSize(new Dimension(68, 68));
        btn.setFont(new Font("Arial", Font.BOLD, 10));
        btn.setMargin(new Insets(2, 2, 2, 2));
        btn.setBackground(UITheme.BATTLE_GRID_EMPTY);
        btn.setBorder(BorderFactory.createLineBorder(UITheme.BATTLE_GRID_BORDER, 1));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        int finalR = r, finalC = c;
        btn.addActionListener(e -> handleButtonClick(finalR, finalC));
        gridButtons[r][c] = btn;
        gridPanel.add(btn);
      }
    }
    gridContainer.add(gridPanel, BorderLayout.CENTER);
    add(gridContainer, BorderLayout.WEST);

    // Panel de logs
    JPanel logPanel = UITheme.createTitledPanel("📜 Registro de Batalla");
    logPanel.setLayout(new BorderLayout());
    logPanel.setBorder(BorderFactory.createCompoundBorder(
      logPanel.getBorder(),
      BorderFactory.createEmptyBorder(10, 10, 10, 10)
    ));
    
    UITheme.styleTextArea(logArea);
    logArea.setEditable(false);
    JScrollPane scrollPane = new JScrollPane(logArea);
    scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.ACCENT_GOLD, 1));
    logPanel.add(scrollPane, BorderLayout.CENTER);
    
    add(logPanel, BorderLayout.CENTER);

    pack();
  }
  
  private JPanel createLegendPanel() {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
    panel.setOpaque(false);
    
    // Colores de rango
    panel.add(createLegendItem("🚶 Movimiento", new Color(0, 100, 200)));
    panel.add(createLegendItem("🎯 Óptimo", new Color(0, 200, 0)));
    panel.add(createLegendItem("✓ Normal", new Color(200, 200, 0)));
    panel.add(createLegendItem("⚠️ Penalizado", new Color(255, 140, 0)));
    panel.add(createLegendItem("✗ Fuera de rango", new Color(100, 100, 100)));
    
    return panel;
  }
  
  private JPanel createLegendItem(String text, Color color) {
    JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
    item.setOpaque(false);
    
    JPanel colorBox = new JPanel();
    colorBox.setPreferredSize(new Dimension(16, 16));
    colorBox.setBackground(color);
    colorBox.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
    item.add(colorBox);
    
    JLabel label = new JLabel(text);
    label.setForeground(Color.WHITE);
    label.setFont(new Font("Segoe UI", Font.PLAIN, 11));
    item.add(label);
    
    return item;
  }

  private void handleButtonClick(int row, int col) {
    Position p = new Position(row, col);
    IWarrior clicked = battleCtrl.getWarriorAt(p);
    
    // Click en casilla vacía
    if (clicked == null) {
      if (selectedAttacker != null) {
        // Intentar mover a la casilla vacía
        if (battleCtrl.isValidMoveTarget(selectedAttacker, p)) {
          try {
            Position oldPos = selectedAttacker.getPosition();
            battleCtrl.performMove(selectedAttacker, p);
            logArea.append("🚶 " + selectedAttacker.getId() + " se movió de (" + 
                          oldPos.row + "," + oldPos.col + ") a (" + p.row + "," + p.col + ")\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
            selectedAttacker = null;
            rangeInfoLabel.setText("");
            refreshGrid();
            updateTurnLabel();
          } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
          }
        } else {
          // Deseleccionar si no es casilla válida para mover
          selectedAttacker = null;
          rangeInfoLabel.setText("");
          refreshGrid();
        }
      }
      return;
    }
    
    // Guerrero muerto - ignorar
    if (!clicked.isAlive()) {
      return;
    }

    if (battleCtrl.isOwnWarrior(clicked)) {
      // Seleccionar atacante propio
      selectedAttacker = clicked;
      refreshGrid();
      updateRangeInfo(clicked);
      logArea.append("▶ Seleccionado: " + clicked.getId() + " (" + clicked.getWarriorType().toUpperCase() + 
                    ") - Click en enemigo para atacar, casilla vacía adyacente para mover\n");
    } else {
      // Intentar atacar enemigo
      if (selectedAttacker != null) {
        // Verificar si está en rango
        if (!CombatRules.isInRange(selectedAttacker, clicked)) {
          int distance = CombatRules.calculateDistance(selectedAttacker.getPosition(), clicked.getPosition());
          int maxRange = CombatRules.getMaxRange(selectedAttacker.getWarriorType());
          JOptionPane.showMessageDialog(this, 
            "¡Fuera de rango!\n\n" +
            "Distancia: " + distance + " casillas\n" +
            "Rango máximo de " + selectedAttacker.getWarriorType().toUpperCase() + ": " + maxRange + " casillas\n\n" +
            "💡 Tip: Puedes mover tu guerrero 1 casilla haciendo click en una casilla vacía adyacente.",
            "Ataque Inválido", JOptionPane.WARNING_MESSAGE);
          return;
        }
        
        try {
          Position attPos = selectedAttacker.getPosition();
          Position tarPos = clicked.getPosition();
          IWarrior attacker = selectedAttacker; // Guardar referencia
          DamageReport dr = battleCtrl.performAttack(selectedAttacker, clicked);
          
          // Efecto visual según eficiencia
          flashAttackEffect(attPos, tarPos, dr);
          
          logArea.append(generateLogLine(attacker, clicked, dr) + "\n");
          logArea.setCaretPosition(logArea.getDocument().getLength());
          selectedAttacker = null;
          rangeInfoLabel.setText("");
          refreshGrid();
          updateTurnLabel();
          
          if (battleCtrl.isBattleOver()) {
            JOptionPane.showMessageDialog(this,
                "🏆 ¡Victoria!\n\nGanador: Ejército " + battleCtrl.getWinner().getId() + 
                "\nClan: " + battleCtrl.getWinner().getClan().getName(),
                "Batalla Terminada", JOptionPane.INFORMATION_MESSAGE);
            winner = battleCtrl.getWinner();
            warController.battleFinished(battleCtrl);
            dispose();
          }
        } catch (IllegalArgumentException ex) {
          JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
      }
    }
  }
  
  private void updateRangeInfo(IWarrior warrior) {
    String type = warrior.getWarriorType();
    String info = switch (type.toLowerCase()) {
      case "melee" -> "⚔️ MELEE - Óptimo: 1-2 | Normal: 3 | Penalizado: 4-5 | Máx: 5";
      case "ranged" -> "🏹 RANGED - Penalizado: 1-2 | Normal: 3-4 | Óptimo: 5-8 | Máx: 10";
      case "magic" -> "✨ MAGIC - Penalizado: 1 | Normal: 2,6-7 | Óptimo: 3-5 | Máx: 8";
      default -> "Rango desconocido";
    };
    rangeInfoLabel.setText(info);
  }

  private String generateLogLine(IWarrior att, IWarrior tar, DamageReport dr) {
    String attType = att.getWarriorType().toUpperCase();
    String tarType = tar.getWarriorType().toUpperCase();
    Position attPos = att.getPosition();
    Position tarPos = tar.getPosition();
    String weaponStr = att.getWeapon().getName();
    
    String effIcon = switch (dr.getEfficiency()) {
      case OPTIMAL -> "🎯";
      case NORMAL -> "✓";
      case PENALIZED -> "⚠️";
      case OUT_OF_RANGE -> "✗";
    };
    
    String elemMult = dr.getElementMultiplier() != 1.0 ? String.format(" Elem:x%.1f", dr.getElementMultiplier()) : "";
    String distMult = dr.getDistanceMultiplier() != 1.0 ? String.format(" Dist:x%.1f", dr.getDistanceMultiplier()) : "";
    
    int healthBefore = tar.getHealth() + dr.getFinalDamage();
    String killedStr = dr.isKilled() ? " 💀 ¡ELIMINADO!" : "";
    
    // Formato: [ATACANTE] Tipo en (x,y) usó Arma → [OBJETIVO] Tipo en (x,y) | Daño | Resultado
    return String.format("%s %s [%s] en (%d,%d) usó %s → %s [%s] en (%d,%d) | Dist:%d%s%s | Daño:%d→%d | HP:%d→%d%s",
        effIcon,
        att.getId(), attType, attPos != null ? attPos.row : -1, attPos != null ? attPos.col : -1,
        weaponStr,
        tar.getId(), tarType, tarPos != null ? tarPos.row : -1, tarPos != null ? tarPos.col : -1,
        dr.getDistance(), elemMult, distMult,
        dr.getBaseDamage(), dr.getFinalDamage(),
        healthBefore, tar.getHealth(),
        killedStr);
  }

  private void refreshGrid() {
    ImageResources imgRes = ImageResources.getInstance();
    
    for (int r = 0; r < GRID_SIZE; r++) {
      for (int c = 0; c < GRID_SIZE; c++) {
        Position p = new Position(r, c);
        IWarrior w = battleCtrl.getWarriorAt(p);
        JButton btn = gridButtons[r][c];
        
        // Calcular color de rango si hay atacante seleccionado
        Color rangeColor = null;
        if (selectedAttacker != null && w == null) {
          rangeColor = getRangeColor(selectedAttacker, p);
        }
        
        if (w == null) {
          btn.setText("");
          btn.setIcon(null);
          if (rangeColor != null) {
            btn.setBackground(rangeColor);
            btn.setBorder(BorderFactory.createLineBorder(rangeColor.brighter(), 1));
          } else {
            btn.setBackground(UITheme.BATTLE_GRID_EMPTY);
            btn.setBorder(BorderFactory.createLineBorder(UITheme.BATTLE_GRID_BORDER, 1));
          }
          btn.setToolTipText(null);
        } else {
          // Obtener icono con color del clan
          Color clanColor = w.getClan().getColor();
          String warriorType = w.getWarriorType();
          ImageIcon icon = imgRes.getScaledIcon(warriorType, clanColor, 48, 48);
          
          btn.setIcon(icon);
          btn.setText("<html><center>" + w.getId() + "<br>HP:" + w.getHealth() + "</center></html>");
          btn.setHorizontalTextPosition(SwingConstants.CENTER);
          btn.setVerticalTextPosition(SwingConstants.BOTTOM);
          btn.setFont(new Font("Arial", Font.BOLD, 9));
          
          // Color de fondo
          Color bg = clanColor.darker().darker();
          
          if (selectedAttacker == w) {
            // Guerrero seleccionado
            bg = UITheme.ACCENT_GOLD;
            btn.setBorder(BorderFactory.createLineBorder(Color.YELLOW, 3));
          } else if (selectedAttacker != null && !battleCtrl.isOwnWarrior(w)) {
            // Enemigo - mostrar indicador de rango
            Color enemyRangeColor = getEnemyRangeIndicator(selectedAttacker, w);
            btn.setBorder(BorderFactory.createLineBorder(enemyRangeColor, 3));
          } else {
            btn.setBorder(BorderFactory.createLineBorder(UITheme.BATTLE_GRID_BORDER, 1));
          }
          btn.setBackground(bg);
          
          // Color del texto
          Color fg = getContrastColor(bg);
          btn.setForeground(fg);
          
          // Tooltip detallado con info de rango
          String tooltip = createWarriorTooltip(w);
          btn.setToolTipText(tooltip);
        }
      }
    }
  }
  
  private Color getRangeColor(IWarrior attacker, Position targetPos) {
    int distance = CombatRules.calculateDistance(attacker.getPosition(), targetPos);
    String type = attacker.getWarriorType();
    int maxRange = CombatRules.getMaxRange(type);
    
    // Casillas de movimiento (distancia 1) - azul
    if (distance == 1) {
      return new Color(0, 100, 200); // Azul para movimiento
    }
    
    if (distance == 0 || distance > maxRange) {
      return null; // Fuera de rango o misma posición
    }
    
    if (CombatRules.isOptimalRange(type, distance)) {
      return new Color(0, 100, 0, 150); // Verde oscuro semi-transparente
    } else if (CombatRules.isPenalizedRange(type, distance)) {
      return new Color(150, 75, 0, 150); // Naranja oscuro semi-transparente
    } else {
      return new Color(100, 100, 0, 150); // Amarillo oscuro semi-transparente
    }
  }
  
  private Color getEnemyRangeIndicator(IWarrior attacker, IWarrior target) {
    if (!CombatRules.isInRange(attacker, target)) {
      return new Color(150, 150, 150); // Gris - fuera de rango
    }
    
    CombatRules.AttackEfficiency eff = CombatRules.getAttackEfficiency(attacker, target);
    return switch (eff) {
      case OPTIMAL -> new Color(0, 255, 0);      // Verde brillante
      case NORMAL -> new Color(255, 255, 0);     // Amarillo
      case PENALIZED -> new Color(255, 140, 0);  // Naranja
      case OUT_OF_RANGE -> new Color(150, 150, 150); // Gris
    };
  }
  
  private String createWarriorTooltip(IWarrior w) {
    String type = w.getWarriorType();
    String weaponName = w.getWeapon().getName();
    
    String rangeInfo = switch (type.toLowerCase()) {
      case "melee" -> "Rango: 1-5 (Óptimo: 1-2)";
      case "ranged" -> "Rango: 1-10 (Óptimo: 5-8)";
      case "magic" -> "Rango: 1-8 (Óptimo: 3-5)";
      default -> "Rango: ???";
    };
    
    // Si hay atacante seleccionado, mostrar info de ataque
    String attackInfo = "";
    if (selectedAttacker != null && selectedAttacker != w && !battleCtrl.isOwnWarrior(w)) {
      int distance = CombatRules.calculateDistance(selectedAttacker.getPosition(), w.getPosition());
      double mult = CombatRules.getDistanceMultiplier(selectedAttacker, w);
      CombatRules.AttackEfficiency eff = CombatRules.getAttackEfficiency(selectedAttacker, w);
      attackInfo = String.format(
        "<br><br><b style='color:#FFD700'>--- ATAQUE ---</b><br>" +
        "Distancia: %d casillas<br>" +
        "Eficiencia: %s<br>" +
        "Multiplicador: x%.1f",
        distance, eff.getDescription(), mult
      );
    }
    
    return String.format(
      "<html><b style='color:#FFD700'>%s</b><br>" +
      "<b>Tipo:</b> %s<br>" +
      "<b>Salud:</b> %d<br>" +
      "<b>Escudo:</b> %d<br>" +
      "<b>Fuerza:</b> %d<br>" +
      "<b>Arma:</b> %s<br>" +
      "<b>%s</b>%s</html>",
      w.getId(),
      type.toUpperCase(),
      w.getHealth(),
      w.getShield(),
      w.getStrength(),
      weaponName,
      rangeInfo,
      attackInfo
    );
  }

  private Color getContrastColor(Color bg) {
    int r = bg.getRed();
    int g = bg.getGreen();
    int b = bg.getBlue();
    double brightness = (r * 299 + g * 587 + b * 114) / 1000.0;
    return brightness > 128 ? Color.BLACK : Color.WHITE;
  }

  private void updateTurnLabel() {
    turnLabel.setText("🎮 Turno: Ejército " + battleCtrl.getCurrentTurnArmy().getId() + 
                      " (" + battleCtrl.getCurrentTurnArmy().getClan().getName() + ")");
  }
  
  private void flashAttackEffect(Position attacker, Position target, DamageReport dr) {
    if (attacker == null || target == null) return;
    
    JButton attackerBtn = gridButtons[attacker.row][attacker.col];
    JButton targetBtn = gridButtons[target.row][target.col];
    
    Color originalAttackerBg = attackerBtn.getBackground();
    Color originalTargetBg = targetBtn.getBackground();
    
    // Color según eficiencia del ataque
    Color attackColor = switch (dr.getEfficiency()) {
      case OPTIMAL -> new Color(0, 255, 0);
      case NORMAL -> Color.YELLOW;
      case PENALIZED -> new Color(255, 140, 0);
      case OUT_OF_RANGE -> Color.GRAY;
    };
    
    Timer timer = new Timer(80, null);
    final int[] count = {0};
    
    timer.addActionListener(e -> {
      if (count[0] % 2 == 0) {
        attackerBtn.setBackground(attackColor);
        targetBtn.setBackground(dr.isKilled() ? Color.RED : new Color(255, 100, 100));
      } else {
        attackerBtn.setBackground(originalAttackerBg);
        targetBtn.setBackground(originalTargetBg);
      }
      count[0]++;
      
      if (count[0] >= 6) {
        timer.stop();
        refreshGrid();
      }
    });
    
    timer.start();
  }

  public Army getWinner() {
    return winner;
  }
}