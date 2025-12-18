package com.battlesimulator.ui;

import com.battlesimulator.domain.*;

import javax.swing.*;
import java.awt.*;
import java.util.Random;
import java.util.UUID;

public class ConfigDialog extends JDialog {
  private final MainFrame mainFrame;
  private JSpinner armiesSpinner1, warriorsSpinner1, armiesSpinner2, warriorsSpinner2;
  private JTextField nameField1, nameField2;
  private JButton colorBtn1, colorBtn2;
  private Color color1 = Color.BLUE, color2 = Color.RED;
  private JCheckBox randomCheck;

  public ConfigDialog(MainFrame mainFrame) {
    super(mainFrame, "Configurar Clanes", true);
    this.mainFrame = mainFrame;
    initUI();
  }

  private void initUI() {
    setSize(600, 450);
    setLayout(new BorderLayout());
    getContentPane().setBackground(UITheme.PRIMARY_DARK);
    
    // Panel principal
    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
    UITheme.stylePanel(mainPanel);
    mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    
    // Título
    JLabel titleLabel = new JLabel("CONFIGURACION DE CLANES", SwingConstants.CENTER);
    UITheme.styleTitleLabel(titleLabel);
    titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    mainPanel.add(titleLabel);
    mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

    // Clan 1
    JPanel clan1Panel = UITheme.createTitledPanel("Clan 1");
    clan1Panel.setLayout(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.fill = GridBagConstraints.HORIZONTAL;
    
    gbc.gridx = 0; gbc.gridy = 0;
    JLabel nameLabel1 = new JLabel("Nombre:");
    UITheme.styleLabel(nameLabel1);
    clan1Panel.add(nameLabel1, gbc);
    
    gbc.gridx = 1; gbc.gridwidth = 2;
    nameField1 = new JTextField("Clan A", 15);
    UITheme.styleTextField(nameField1);
    clan1Panel.add(nameField1, gbc);
    
    gbc.gridx = 3; gbc.gridwidth = 1;
    colorBtn1 = new JButton("Color");
    colorBtn1.setBackground(color1);
    colorBtn1.setForeground(Color.WHITE);
    colorBtn1.addActionListener(e -> chooseColor(1));
    UITheme.styleButton(colorBtn1);
    clan1Panel.add(colorBtn1, gbc);
    
    gbc.gridx = 0; gbc.gridy = 1;
    JLabel armyLabel1 = new JLabel("Ejércitos:");
    UITheme.styleLabel(armyLabel1);
    clan1Panel.add(armyLabel1, gbc);
    
    gbc.gridx = 1; gbc.gridwidth = 1;
    armiesSpinner1 = new JSpinner(new SpinnerNumberModel(2, 1, 3, 1));
    clan1Panel.add(armiesSpinner1, gbc);
    
    gbc.gridx = 2;
    JLabel warriorLabel1 = new JLabel("Guerreros/Ej:");
    UITheme.styleLabel(warriorLabel1);
    clan1Panel.add(warriorLabel1, gbc);
    
    gbc.gridx = 3;
    warriorsSpinner1 = new JSpinner(new SpinnerNumberModel(10, 5, 30, 1));
    clan1Panel.add(warriorsSpinner1, gbc);
    
    clan1Panel.setMaximumSize(new Dimension(550, 120));
    mainPanel.add(clan1Panel);
    mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));

    // Clan 2
    JPanel clan2Panel = UITheme.createTitledPanel("Clan 2");
    clan2Panel.setLayout(new GridBagLayout());
    gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.fill = GridBagConstraints.HORIZONTAL;
    
    gbc.gridx = 0; gbc.gridy = 0;
    JLabel nameLabel2 = new JLabel("Nombre:");
    UITheme.styleLabel(nameLabel2);
    clan2Panel.add(nameLabel2, gbc);
    
    gbc.gridx = 1; gbc.gridwidth = 2;
    nameField2 = new JTextField("Clan B", 15);
    UITheme.styleTextField(nameField2);
    clan2Panel.add(nameField2, gbc);
    
    gbc.gridx = 3; gbc.gridwidth = 1;
    colorBtn2 = new JButton("Color");
    colorBtn2.setBackground(color2);
    colorBtn2.setForeground(Color.WHITE);
    colorBtn2.addActionListener(e -> chooseColor(2));
    UITheme.styleButton(colorBtn2);
    clan2Panel.add(colorBtn2, gbc);
    
    gbc.gridx = 0; gbc.gridy = 1;
    JLabel armyLabel2 = new JLabel("Ejércitos:");
    UITheme.styleLabel(armyLabel2);
    clan2Panel.add(armyLabel2, gbc);
    
    gbc.gridx = 1; gbc.gridwidth = 1;
    armiesSpinner2 = new JSpinner(new SpinnerNumberModel(2, 1, 3, 1));
    clan2Panel.add(armiesSpinner2, gbc);
    
    gbc.gridx = 2;
    JLabel warriorLabel2 = new JLabel("Guerreros/Ej:");
    UITheme.styleLabel(warriorLabel2);
    clan2Panel.add(warriorLabel2, gbc);
    
    gbc.gridx = 3;
    warriorsSpinner2 = new JSpinner(new SpinnerNumberModel(10, 5, 30, 1));
    clan2Panel.add(warriorsSpinner2, gbc);
    
    clan2Panel.setMaximumSize(new Dimension(550, 120));
    mainPanel.add(clan2Panel);
    mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

    // Opciones adicionales
    randomCheck = new JCheckBox("Generar guerreros aleatorios", true);
    randomCheck.setFont(UITheme.BUTTON_FONT);
    randomCheck.setForeground(UITheme.TEXT_LIGHT);
    randomCheck.setBackground(UITheme.PRIMARY_DARK);
    randomCheck.setAlignmentX(Component.CENTER_ALIGNMENT);
    mainPanel.add(randomCheck);
    mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
    
    // Botones
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
    UITheme.stylePanel(buttonPanel);
    
    JButton configBtn = new JButton("Configurar y Comenzar");
    configBtn.addActionListener(e -> configureClans());
    UITheme.stylePrimaryButton(configBtn);
    buttonPanel.add(configBtn);

    JButton cancelBtn = new JButton("Cancelar");
    cancelBtn.addActionListener(e -> dispose());
    UITheme.styleButton(cancelBtn);
    buttonPanel.add(cancelBtn);
    
    mainPanel.add(buttonPanel);
    
    add(mainPanel, BorderLayout.CENTER);
    setLocationRelativeTo(getParent());
  }

  private void chooseColor(int clanNum) {
    Color newColor = JColorChooser.showDialog(this, "Elegir color para Clan " + clanNum,
        clanNum == 1 ? color1 : color2);
    if (newColor != null) {
      if (clanNum == 1) {
        color1 = newColor;
        colorBtn1.setBackground(color1);
      } else {
        color2 = newColor;
        colorBtn2.setBackground(color2);
      }
    }
  }

  private void configureClans() {
    // Validaciones
    String name1 = nameField1.getText().trim();
    String name2 = nameField2.getText().trim();
    
    if (name1.isEmpty() || name2.isEmpty()) {
      JOptionPane.showMessageDialog(this,
        "Los nombres de los clanes no pueden estar vacíos.",
        "Error de Validación", JOptionPane.ERROR_MESSAGE);
      return;
    }
    
    if (name1.equals(name2)) {
      JOptionPane.showMessageDialog(this,
        "Los nombres de los clanes deben ser diferentes.",
        "Error de Validación", JOptionPane.ERROR_MESSAGE);
      return;
    }
    
    if (color1.equals(color2)) {
      int option = JOptionPane.showConfirmDialog(this,
        "Los clanes tienen el mismo color. ¿Desea continuar de todos modos?",
        "Colores Idénticos", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
      if (option != JOptionPane.YES_OPTION) {
        return;
      }
    }
    
    try {
      Clan clanA = createClan(name1, color1, (int) armiesSpinner1.getValue(),
          (int) warriorsSpinner1.getValue());
      Clan clanB = createClan(name2, color2, (int) armiesSpinner2.getValue(),
          (int) warriorsSpinner2.getValue());
      mainFrame.setClans(clanA, clanB);
      dispose();
    } catch (Exception e) {
      JOptionPane.showMessageDialog(this,
        "Error al crear los clanes: " + e.getMessage(),
        "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  private Clan createClan(String name, Color color, int numArmies, int numWarriorsPerArmy) {
    Clan clan = new Clan(name);
    clan.setColor(color);
    Random rand = new Random();
    for (int i = 0; i < numArmies; i++) {
      Army army = new Army();
      army.setId(UUID.randomUUID().toString());
      army.setClan(clan);
      for (int j = 1; j <= numWarriorsPerArmy; j++) {
        // Generar aleatorio
        double typeRand = rand.nextDouble();
        Element elem = Element.values()[1 + rand.nextInt(4)]; // skip NONE
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
}