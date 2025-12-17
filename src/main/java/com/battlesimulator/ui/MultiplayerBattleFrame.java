package com.battlesimulator.ui;

import com.battlesimulator.domain.*;
import com.battlesimulator.network.GameClient;
import com.battlesimulator.network.Message;
import com.battlesimulator.usecases.InteractiveBattle;
import com.battlesimulator.usecases.MultiplayerWarController;
import java.awt.*;
import javax.swing.*;

public class MultiplayerBattleFrame extends JDialog {
  private static final int GRID_SIZE = 12;
  private final InteractiveBattle battleCtrl;
  private final MultiplayerWarController warController;
  private final GameClient client;
  private final boolean controlsArmy1; // true si controla ejército 1, false si controla ejército 2
  private final JButton[][] gridButtons = new JButton[GRID_SIZE][GRID_SIZE];
  private IWarrior selectedAttacker;
  private final JTextArea logArea = new JTextArea();
  private final JLabel turnLabel = new JLabel();
  private final JLabel rangeInfoLabel = new JLabel();
  private Army winner;

  public MultiplayerBattleFrame(InteractiveBattle battleCtrl, MultiplayerWarController warController, 
                                GameClient client, boolean controlsArmy1) {
    super(warController, "Batalla en cuadricula (Multijugador)", false);
    this.battleCtrl = battleCtrl;
    this.warController = warController;
    this.client = client;
    this.controlsArmy1 = controlsArmy1;
    initUI();
    setupNetworkListener();
    refreshGrid();
    updateTurnLabel();
    logArea.append("⚔️ Batalla multijugador iniciada.\n");
    logArea.append("📏 Los rangos de ataque varían según el tipo de guerrero.\n");
    logArea.append("🎮 Controlas: " + (controlsArmy1 ? "Ejército 1" : "Ejército 2") + "\n\n");
  }

  private void initUI() {
    setSize(1500, 950);
    setLayout(new BorderLayout());
    getContentPane().setBackground(UITheme.PRIMARY_DARK);

    // Panel superior con turno e info de rango
    JPanel topPanel = new JPanel(new BorderLayout());
    UITheme.stylePanel(topPanel);
    topPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
    
    JLabel titleLabel = new JLabel("⚔️ BATALLA MULTIJUGADOR ⚔️", SwingConstants.CENTER);
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
  
  private void setupNetworkListener() {
    // El cliente ya tiene un listener, aquí solo procesamos los mensajes de ataque
  }

  private void handleButtonClick(int row, int col) {
    // Solo permitir acciones si es el turno del jugador
    System.out.println("[BATTLE] Click en (" + row + "," + col + ") - isMyTurn: " + isMyTurn());
    if (!isMyTurn()) {
      JOptionPane.showMessageDialog(this, "No es tu turno!", "Espera", JOptionPane.WARNING_MESSAGE);
      return;
    }
    
    Position p = new Position(row, col);
    IWarrior clicked = battleCtrl.getWarriorAt(p);
    
    // Click en casilla vacía
    if (clicked == null || !clicked.isAlive()) {
      if (selectedAttacker != null) {
        // Intentar mover a casilla vacía (distancia 1)
        if (battleCtrl.isValidMoveTarget(selectedAttacker, p)) {
          try {
            Position oldPos = selectedAttacker.getPosition();
            IWarrior mover = selectedAttacker;
            battleCtrl.performMove(selectedAttacker, p);
            logArea.append("🚶 " + mover.getId() + " se movió de (" + 
                          oldPos.row + "," + oldPos.col + ") a (" + p.row + "," + p.col + ")\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
            
            // Enviar movimiento al otro jugador
            if (client != null) {
              int battleIndex = warController.getBattleIndex(battleCtrl);
              String moveData = String.format("%d|%s|%d|%d|%d|%d",
                battleIndex,
                mover.getId(),
                oldPos.row, oldPos.col,
                p.row, p.col);
              Message msg = new Message(Message.Type.MOVE, moveData);
              msg.setPlayerId(client.getPlayerId());
              client.sendMessage(msg);
            }
            
            selectedAttacker = null;
            rangeInfoLabel.setText("");
            refreshGrid();
            updateTurnLabel();
            return;
          } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Movimiento Inválido", JOptionPane.WARNING_MESSAGE);
          }
        } else {
          // Simplemente deseleccionar
          selectedAttacker = null;
          rangeInfoLabel.setText("");
          refreshGrid();
        }
      }
      return;
    }

    System.out.println("[BATTLE] Guerrero clickeado: " + clicked.getId() + ", isOwnWarrior: " + battleCtrl.isOwnWarrior(clicked));
    if (battleCtrl.isOwnWarrior(clicked)) {
      selectedAttacker = clicked;
      refreshGrid();
      updateRangeInfo(clicked);
      logArea.append("▶ Atacante seleccionado: " + clicked.getId() + " (" + clicked.getWarriorType().toUpperCase() + ")\n");
      System.out.println("[BATTLE] Atacante seleccionado: " + clicked.getId());
    } else {
      if (selectedAttacker != null) {
        // Verificar si está en rango
        if (!CombatRules.isInRange(selectedAttacker, clicked)) {
          int distance = CombatRules.calculateDistance(selectedAttacker.getPosition(), clicked.getPosition());
          int maxRange = CombatRules.getMaxRange(selectedAttacker.getWarriorType());
          JOptionPane.showMessageDialog(this, 
            "¡Fuera de rango!\n\n" +
            "Distancia: " + distance + " casillas\n" +
            "Rango máximo de " + selectedAttacker.getWarriorType().toUpperCase() + ": " + maxRange + " casillas",
            "Ataque Inválido", JOptionPane.WARNING_MESSAGE);
          return;
        }
        
        try {
          // Capturar posiciones antes del ataque para el efecto visual
          Position attPos = selectedAttacker.getPosition();
          Position tarPos = clicked.getPosition();
          IWarrior attacker = selectedAttacker; // Guardar referencia
          
          // Realizar ataque localmente
          DamageReport dr = battleCtrl.performAttack(selectedAttacker, clicked);
          String logLine = generateLogLine(attacker, clicked, dr);
          logArea.append(logLine + "\n");
          logArea.setCaretPosition(logArea.getDocument().getLength());
          
          // Efecto visual del ataque según eficiencia
          flashAttackEffect(attPos, tarPos, dr);
          
          // Enviar ataque al otro jugador con índice de batalla
          if (client != null) {
            int battleIndex = warController.getBattleIndex(battleCtrl);
            String attackData = String.format("%d|%s|%s|%d|%d|%d|%b|%.2f|%d", 
              battleIndex,
              attacker.getId(), 
              clicked.getId(),
              dr.getBaseDamage(),
              dr.getFinalDamage(),
              clicked.getHealth(),
              dr.isKilled(),
              dr.getDistanceMultiplier(),
              dr.getDistance());
            Message msg = new Message(Message.Type.ATTACK, attackData);
            msg.setPlayerId(client.getPlayerId()); // Marcar quién envió el ataque
            client.sendMessage(msg);
          }
          
          selectedAttacker = null;
          rangeInfoLabel.setText("");
          refreshGrid();
          updateTurnLabel();
          
          // Mantener la ventana abierta pero indicar que no es tu turno
          System.out.println("[LOCAL] Ataque completado. Turno actual: " + battleCtrl.getCurrentTurnArmy().getId());
          
          if (battleCtrl.isBattleOver()) {
            JOptionPane.showMessageDialog(this,
                "🏆 ¡Victoria!\n\nGanador: Ejército " + battleCtrl.getWinner().getId() + 
                "\nClan: " + battleCtrl.getWinner().getClan().getName(),
                "Batalla Terminada", JOptionPane.INFORMATION_MESSAGE);
            winner = battleCtrl.getWinner();
            warController.battleFinished(battleCtrl);
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
  
  private boolean isMyTurn() {
    // Verificar si es mi turno en esta batalla específica
    Army currentArmy = battleCtrl.getCurrentTurnArmy();
    boolean myTurn;
    if (controlsArmy1) {
      myTurn = currentArmy == battleCtrl.getArmy1();
    } else {
      myTurn = currentArmy == battleCtrl.getArmy2();
    }
    System.out.println("[BATTLE] isMyTurn check - controlsArmy1: " + controlsArmy1 + ", currentArmy: " + currentArmy.getId() + ", army1: " + battleCtrl.getArmy1().getId() + ", army2: " + battleCtrl.getArmy2().getId() + ", result: " + myTurn);
    return myTurn;
  }

  private String generateLogLine(IWarrior att, IWarrior tar, DamageReport dr) {
    String weaponStr = att.getWeapon().getName();
    String effIcon = switch (dr.getEfficiency()) {
      case OPTIMAL -> "🎯";
      case NORMAL -> "✓";
      case PENALIZED -> "⚠️";
      case OUT_OF_RANGE -> "✗";
    };
    
    String distInfo = String.format("[Dist:%d %s]", dr.getDistance(), effIcon);
    String elemMult = dr.getElementMultiplier() != 1.0 ? String.format("Elem:x%.1f ", dr.getElementMultiplier()) : "";
    String distMult = dr.getDistanceMultiplier() != 1.0 ? String.format("Dist:x%.1f ", dr.getDistanceMultiplier()) : "";
    
    int healthBefore = tar.getHealth() + dr.getFinalDamage();
    String killedStr = dr.isKilled() ? " 💀 ¡ELIMINADO!" : "";
    
    return String.format("%s %s(%s) → %s %s| Daño:%d %s%s= %d | Escudo:-%d | HP:%d→%d%s",
        effIcon, att.getId(), weaponStr, tar.getId(), distInfo,
        dr.getBaseDamage(), elemMult, distMult, dr.getEffectiveDamage(),
        dr.getAbsorbed(), healthBefore, tar.getHealth(), killedStr);
  }

  private void refreshGrid() {
    ImageResources imgRes = ImageResources.getInstance();
    System.out.println("[GRID] Refrescando grid...");
    
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
        
        // No mostrar guerreros muertos
        if (w == null || !w.isAlive()) {
          if (w != null && !w.isAlive()) {
            System.out.println("[GRID] Guerrero muerto en " + p + ": " + w.getId() + " (HP:" + w.getHealth() + ")");
          }
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
          
          // Resaltar mis guerreros
          if (isMyWarrior(w)) {
            bg = clanColor.darker();
            btn.setBorder(BorderFactory.createLineBorder(UITheme.ACCENT_GOLD.brighter(), 2));
          } else {
            btn.setBorder(BorderFactory.createLineBorder(UITheme.BATTLE_GRID_BORDER, 1));
          }
          
          // Resaltar atacante seleccionado
          if (selectedAttacker == w) {
            bg = UITheme.ACCENT_GOLD;
            btn.setBorder(BorderFactory.createLineBorder(Color.YELLOW, 3));
          } else if (selectedAttacker != null && !isMyWarrior(w)) {
            // Enemigo - mostrar indicador de rango
            Color enemyRangeColor = getEnemyRangeIndicator(selectedAttacker, w);
            btn.setBorder(BorderFactory.createLineBorder(enemyRangeColor, 3));
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
    String ownerInfo = isMyWarrior(w) ? " (TUS TROPAS)" : " (ENEMIGO)";
    
    String rangeInfo = switch (type.toLowerCase()) {
      case "melee" -> "Rango: 1-5 (Óptimo: 1-2)";
      case "ranged" -> "Rango: 1-10 (Óptimo: 5-8)";
      case "magic" -> "Rango: 1-8 (Óptimo: 3-5)";
      default -> "Rango: ???";
    };
    
    // Si hay atacante seleccionado, mostrar info de ataque
    String attackInfo = "";
    if (selectedAttacker != null && selectedAttacker != w && !isMyWarrior(w)) {
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
      "<html><b style='color:#FFD700'>%s</b>%s<br>" +
      "<b>Tipo:</b> %s<br>" +
      "<b>Salud:</b> %d<br>" +
      "<b>Escudo:</b> %d<br>" +
      "<b>Fuerza:</b> %d<br>" +
      "<b>Arma:</b> %s<br>" +
      "<b>%s</b>%s</html>",
      w.getId(),
      ownerInfo,
      type.toUpperCase(),
      w.getHealth(),
      w.getShield(),
      w.getStrength(),
      weaponName,
      rangeInfo,
      attackInfo
    );
  }
  
  private boolean isMyWarrior(IWarrior w) {
    if (controlsArmy1) {
      return battleCtrl.getArmy1().getWarriors().contains(w);
    } else {
      return battleCtrl.getArmy2().getWarriors().contains(w);
    }
  }

  private Color getContrastColor(Color bg) {
    int r = bg.getRed();
    int g = bg.getGreen();
    int b = bg.getBlue();
    double brightness = (r * 299 + g * 587 + b * 114) / 1000.0;
    return brightness > 128 ? Color.BLACK : Color.WHITE;
  }

  private void updateTurnLabel() {
    String turnInfo = "🎮 Turno: Ejército " + battleCtrl.getCurrentTurnArmy().getId() + 
                      " (" + battleCtrl.getCurrentTurnArmy().getClan().getName() + ")";
    if (isMyTurn()) {
      turnInfo += " - ¡TU TURNO!";
      turnLabel.setForeground(new Color(0, 200, 0));
    } else {
      turnInfo += " - Esperando...";
      turnLabel.setForeground(new Color(200, 50, 50));
    }
    turnLabel.setText(turnInfo);
  }
  
  public void updateTurnDisplay() {
    SwingUtilities.invokeLater(() -> {
      updateTurnLabel();
      refreshGrid();
    });
  }
  
  private void flashAttackEffect(Position attacker, Position target, DamageReport dr) {
    if (attacker == null || target == null) return;
    if (attacker.row >= GRID_SIZE || attacker.col >= GRID_SIZE) return;
    if (target.row >= GRID_SIZE || target.col >= GRID_SIZE) return;
    
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
        if (count[0] < 5) {
          attackerBtn.setBackground(originalAttackerBg);
          targetBtn.setBackground(originalTargetBg);
        }
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
  
  public void handleRemoteAttack(String attackData) {
    SwingUtilities.invokeLater(() -> {
      try {
        String[] parts = attackData.split("\\|");
        if (parts.length < 6) {
          logArea.append("Error: datos de ataque incompletos\n");
          return;
        }
        
        String attackerId = parts[0];
        String targetId = parts[1];
        int baseDamage = Integer.parseInt(parts[2]);
        int finalDamage = Integer.parseInt(parts[3]);
        int remainingHealth = Integer.parseInt(parts[4]);
        boolean killed = Boolean.parseBoolean(parts[5]);
        double distanceMultiplier = parts.length > 6 ? Double.parseDouble(parts[6]) : 1.0;
        int distance = parts.length > 7 ? Integer.parseInt(parts[7]) : 0;
        
        System.out.println("[REMOTE] Procesando ataque: " + attackerId + " -> " + targetId);
        System.out.println("[REMOTE] finalDamage=" + finalDamage + ", remainingHealth=" + remainingHealth + ", killed=" + killed);
        
        IWarrior attacker = battleCtrl.findWarriorById(attackerId);
        IWarrior target = battleCtrl.findWarriorById(targetId);
        
        if (attacker == null || target == null) {
          logArea.append("Error: guerrero no encontrado (attacker=" + attackerId + ", target=" + targetId + ")\n");
          return;
        }
        
        // Capturar posiciones para efecto visual
        Position attPos = attacker.getPosition();
        Position tarPos = target.getPosition();
        
        System.out.println("[REMOTE] Posición atacante: " + attPos + ", Posición objetivo: " + tarPos);
        System.out.println("[REMOTE] Target health antes: " + target.getHealth());
        
        // IMPORTANTE: Establecer health directamente, NO recalcular daño
        target.setHealth(remainingHealth);
        
        System.out.println("[REMOTE] Target health después: " + target.getHealth() + ", isAlive: " + target.isAlive());
        
        // Determinar si murió basándose en el health real
        boolean actuallyKilled = !target.isAlive();
        
        // Si fue asesinado, mover atacante a posición del objetivo
        if (actuallyKilled) {
          System.out.println("[REMOTE] Procesando muerte del guerrero " + targetId);
          // Primero quitar al muerto del grid
          battleCtrl.removeFromGrid(tarPos);
          target.setPosition(null);
          // Mover atacante a la posición del muerto (moveWarrior se encarga de quitar de attPos)
          battleCtrl.moveWarrior(attacker, tarPos);
          System.out.println("[REMOTE] Atacante movido a " + tarPos);
        }
        
        // IMPORTANTE: Cambiar el turno después del ataque
        battleCtrl.nextTurn();
        
        // Verificar si la batalla terminó
        battleCtrl.checkBattleEnd();
        
        // Generar reporte para el log con info de distancia
        CombatRules.AttackEfficiency eff = distanceMultiplier >= 1.4 ? CombatRules.AttackEfficiency.OPTIMAL :
                                           distanceMultiplier >= 1.0 ? CombatRules.AttackEfficiency.NORMAL :
                                           CombatRules.AttackEfficiency.PENALIZED;
        DamageReport dr = new DamageReport(baseDamage, 1.0, distanceMultiplier, 
                                          (int)(baseDamage * distanceMultiplier), 0, finalDamage, 
                                          actuallyKilled, distance, eff);
        String logLine = generateLogLine(attacker, target, dr);
        logArea.append("[ENEMIGO] " + logLine + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
        
        // Efecto visual
        flashAttackEffect(attPos, tarPos, dr);
        
        refreshGrid();
        updateTurnLabel();
        
        System.out.println("[REMOTE] Turno después del ataque: " + battleCtrl.getCurrentTurnArmy().getId());
        System.out.println("[REMOTE] isMyTurn después: " + isMyTurn());
        
        // Verificar si la batalla terminó
        if (battleCtrl.isBattleOver()) {
          JOptionPane.showMessageDialog(this,
            "🏆 ¡Victoria!\n\nGanador: Ejército " + battleCtrl.getWinner().getId() + 
            "\nClan: " + battleCtrl.getWinner().getClan().getName(),
            "Batalla Terminada", JOptionPane.INFORMATION_MESSAGE);
          winner = battleCtrl.getWinner();
          warController.battleFinished(battleCtrl);
        }
      } catch (Exception e) {
        logArea.append("Error procesando ataque: " + e.getMessage() + "\n");
        e.printStackTrace();
      }
    });
  }
  
  public void handleRemoteMove(String moveData) {
    SwingUtilities.invokeLater(() -> {
      try {
        String[] parts = moveData.split("\\|");
        if (parts.length < 5) {
          logArea.append("Error: datos de movimiento incompletos\n");
          return;
        }
        
        String warriorId = parts[0];
        int oldRow = Integer.parseInt(parts[1]);
        int oldCol = Integer.parseInt(parts[2]);
        int newRow = Integer.parseInt(parts[3]);
        int newCol = Integer.parseInt(parts[4]);
        
        System.out.println("[REMOTE MOVE] " + warriorId + " de (" + oldRow + "," + oldCol + ") a (" + newRow + "," + newCol + ")");
        
        IWarrior warrior = battleCtrl.findWarriorById(warriorId);
        if (warrior == null) {
          logArea.append("Error: guerrero no encontrado: " + warriorId + "\n");
          return;
        }
        
        // Verificar posición actual del guerrero antes de mover
        Position currentPos = warrior.getPosition();
        System.out.println("[REMOTE MOVE] Posición actual del guerrero: " + 
                          (currentPos != null ? "(" + currentPos.row + "," + currentPos.col + ")" : "null"));
        
        // Verificar si la posición actual coincide con la esperada
        if (currentPos == null || currentPos.row != oldRow || currentPos.col != oldCol) {
          System.out.println("[REMOTE MOVE] WARNING: Posición actual no coincide con la esperada!");
          // Corregir: primero quitar de la posición donde realmente está
          if (currentPos != null) {
            battleCtrl.removeFromGrid(currentPos);
          }
        }
        
        Position newPos = new Position(newRow, newCol);
        
        // Actualizar posición en el grid (moveWarrior ya elimina de la posición vieja)
        battleCtrl.moveWarrior(warrior, newPos);
        
        // Cambiar turno
        battleCtrl.nextTurn();
        
        logArea.append("[ENEMIGO] 🚶 " + warriorId + " se movió de (" + oldRow + "," + oldCol + 
                      ") a (" + newRow + "," + newCol + ")\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
        
        refreshGrid();
        updateTurnLabel();
        
      } catch (Exception e) {
        logArea.append("Error procesando movimiento: " + e.getMessage() + "\n");
        e.printStackTrace();
      }
    });
  }
}
