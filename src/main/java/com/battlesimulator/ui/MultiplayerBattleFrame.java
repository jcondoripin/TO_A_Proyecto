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
    logArea.append("Batalla iniciada. Espera tu turno para atacar.\n");
    logArea.append("Controlas: " + (controlsArmy1 ? "Ejército 1" : "Ejército 2") + "\n");
  }

  private void initUI() {
    setSize(1400, 900);
    setLayout(new BorderLayout());
    getContentPane().setBackground(UITheme.PRIMARY_DARK);

    // Panel superior con turno
    JPanel topPanel = new JPanel(new BorderLayout());
    UITheme.stylePanel(topPanel);
    topPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
    
    JLabel titleLabel = new JLabel("BATALLA MULTIJUGADOR", SwingConstants.CENTER);
    UITheme.styleTitleLabel(titleLabel);
    topPanel.add(titleLabel, BorderLayout.NORTH);
    
    turnLabel.setHorizontalAlignment(SwingConstants.CENTER);
    UITheme.styleLabel(turnLabel);
    turnLabel.setFont(UITheme.SUBTITLE_FONT);
    turnLabel.setForeground(UITheme.ACCENT_GOLD);
    topPanel.add(turnLabel, BorderLayout.CENTER);
    
    add(topPanel, BorderLayout.NORTH);

    // Grid left con mejor diseño
    JPanel gridContainer = new JPanel(new BorderLayout());
    UITheme.stylePanel(gridContainer);
    gridContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    
    JPanel gridPanel = new JPanel(new GridLayout(GRID_SIZE, GRID_SIZE, 2, 2));
    gridPanel.setBackground(UITheme.BATTLE_GRID_BORDER);
    gridPanel.setBorder(BorderFactory.createLineBorder(UITheme.ACCENT_GOLD, 3));
    
    for (int r = 0; r < GRID_SIZE; r++) {
      for (int c = 0; c < GRID_SIZE; c++) {
        JButton btn = new JButton();
        btn.setPreferredSize(new Dimension(70, 70));
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

    // Panel de logs con mejor diseño
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
  
  private void setupNetworkListener() {
    // El cliente ya tiene un listener, aquí solo procesamos los mensajes de ataque
  }

  private void handleButtonClick(int row, int col) {
    // Solo permitir acciones si es el turno del jugador
    if (!isMyTurn()) {
      JOptionPane.showMessageDialog(this, "No es tu turno!", "Espera", JOptionPane.WARNING_MESSAGE);
      return;
    }
    
    Position p = new Position(row, col);
    IWarrior clicked = battleCtrl.getWarriorAt(p);
    if (clicked == null || !clicked.isAlive())
      return;

    if (battleCtrl.isOwnWarrior(clicked)) {
      selectedAttacker = clicked;
      refreshGrid();
      logArea.append("Atacante seleccionado: " + clicked.getId() + "\n");
    } else {
      if (selectedAttacker != null) {
        try {
          // Capturar posiciones antes del ataque para el efecto visual
          Position attPos = selectedAttacker.getPosition();
          Position tarPos = clicked.getPosition();
          
          // Realizar ataque localmente
          DamageReport dr = battleCtrl.performAttack(selectedAttacker, clicked);
          String logLine = generateLogLine(selectedAttacker, clicked, dr);
          logArea.append(logLine + "\n");
          logArea.setCaretPosition(logArea.getDocument().getLength());
          
          // Efecto visual del ataque
          flashAttackEffect(attPos, tarPos, dr.isKilled());
          
          // Enviar ataque al otro jugador
          if (client != null) {
            String attackData = String.format("%s|%s|%d|%d|%d|%b", 
              selectedAttacker.getId(), 
              clicked.getId(),
              dr.getBaseDamage(),
              dr.getFinalDamage(),
              clicked.getHealth(),
              dr.isKilled());
            Message msg = new Message(Message.Type.ATTACK, attackData);
            client.sendMessage(msg);
          }
          
          selectedAttacker = null;
          refreshGrid();
          updateTurnLabel();
          
          if (battleCtrl.isBattleOver()) {
            JOptionPane.showMessageDialog(this,
                "Ganador batalla: Ej. " + battleCtrl.getWinner().getId() + " ("
                    + battleCtrl.getWinner().getClan().getName() + ")",
                "Victoria batalla", JOptionPane.INFORMATION_MESSAGE);
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
  
  private boolean isMyTurn() {
    Army currentArmy = battleCtrl.getCurrentTurnArmy();
    if (controlsArmy1) {
      return currentArmy == battleCtrl.getArmy1();
    } else {
      return currentArmy == battleCtrl.getArmy2();
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
    ImageResources imgRes = ImageResources.getInstance();
    
    for (int r = 0; r < GRID_SIZE; r++) {
      for (int c = 0; c < GRID_SIZE; c++) {
        Position p = new Position(r, c);
        IWarrior w = battleCtrl.getWarriorAt(p);
        JButton btn = gridButtons[r][c];
        
        if (w == null) {
          btn.setText("");
          btn.setIcon(null);
          btn.setBackground(UITheme.BATTLE_GRID_EMPTY);
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
          }
          
          btn.setBackground(bg);
          
          // Color del texto
          Color fg = getContrastColor(bg);
          btn.setForeground(fg);
          
          // Tooltip detallado
          String weaponName = w.getWeapon().getName();
          String ownerInfo = isMyWarrior(w) ? " (TUS TROPAS)" : " (ENEMIGO)";
          String tooltip = String.format(
            "<html><b>%s</b>%s<br>" +
            "Tipo: %s<br>" +
            "Salud: %d<br>" +
            "Escudo: %d<br>" +
            "Fuerza: %d<br>" +
            "Arma: %s</html>",
            w.getId(),
            ownerInfo,
            warriorType.toUpperCase(),
            w.getHealth(),
            w.getShield(),
            w.getStrength(),
            weaponName
          );
          btn.setToolTipText(tooltip);
        }
      }
    }
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
    String turnInfo = battleCtrl.getTurnDescription();
    if (isMyTurn()) {
      turnInfo += " - ¡TU TURNO!";
      turnLabel.setForeground(Color.GREEN.darker());
    } else {
      turnInfo += " - Esperando...";
      turnLabel.setForeground(Color.RED);
    }
    turnLabel.setText(turnInfo);
  }
  
  private void flashAttackEffect(Position attacker, Position target, boolean killed) {
    if (attacker == null || target == null) return;
    
    JButton attackerBtn = gridButtons[attacker.row][attacker.col];
    JButton targetBtn = gridButtons[target.row][target.col];
    
    Color originalAttackerBg = attackerBtn.getBackground();
    Color originalTargetBg = targetBtn.getBackground();
    
    Timer timer = new Timer(100, null);
    final int[] count = {0};
    
    timer.addActionListener(e -> {
      if (count[0] % 2 == 0) {
        attackerBtn.setBackground(Color.YELLOW);
        targetBtn.setBackground(killed ? Color.RED : Color.ORANGE);
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
  
  public void handleRemoteAttack(String attackData) {
    SwingUtilities.invokeLater(() -> {
      logArea.append("Ataque enemigo recibido\n");
      refreshGrid();
      updateTurnLabel();
    });
  }
}
