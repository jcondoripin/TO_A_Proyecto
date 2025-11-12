package com.battlesimulator.ui;

import com.battlesimulator.domain.*;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

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
    setSize(500, 300);
    setLayout(new GridLayout(5, 1));

    // Clan 1
    JPanel clan1Panel = new JPanel(new FlowLayout());
    clan1Panel.add(new JLabel("Clan 1:"));
    nameField1 = new JTextField("Clan A", 10);
    clan1Panel.add(nameField1);
    colorBtn1 = new JButton("Color1");
    colorBtn1.setBackground(color1);
    colorBtn1.addActionListener(e -> chooseColor(1));
    clan1Panel.add(colorBtn1);
    armiesSpinner1 = new JSpinner(new SpinnerNumberModel(2, 1, 3, 1));
    clan1Panel.add(new JLabel("Ejércitos:"));
    clan1Panel.add(armiesSpinner1);
    warriorsSpinner1 = new JSpinner(new SpinnerNumberModel(10, 5, 30, 1));
    clan1Panel.add(new JLabel("Guerreros/Ej:"));
    clan1Panel.add(warriorsSpinner1);
    add(clan1Panel);

    // Clan 2
    JPanel clan2Panel = new JPanel(new FlowLayout());
    clan2Panel.add(new JLabel("Clan 2:"));
    nameField2 = new JTextField("Clan B", 10);
    clan2Panel.add(nameField2);
    colorBtn2 = new JButton("Color2");
    colorBtn2.setBackground(color2);
    colorBtn2.addActionListener(e -> chooseColor(2));
    clan2Panel.add(colorBtn2);
    armiesSpinner2 = new JSpinner(new SpinnerNumberModel(2, 1, 3, 1));
    clan2Panel.add(new JLabel("Ejércitos:"));
    clan2Panel.add(armiesSpinner2);
    warriorsSpinner2 = new JSpinner(new SpinnerNumberModel(10, 5, 30, 1));
    clan2Panel.add(new JLabel("Guerreros/Ej:"));
    clan2Panel.add(warriorsSpinner2);
    add(clan2Panel);

    randomCheck = new JCheckBox("Generar guerreros aleatorios", true);
    add(randomCheck);

    JButton configBtn = new JButton("Configurar");
    configBtn.addActionListener(e -> configureClans());
    add(configBtn);

    JButton cancelBtn = new JButton("Cancelar");
    cancelBtn.addActionListener(e -> dispose());
    add(cancelBtn);

    pack();
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
    Clan clanA = createClan(nameField1.getText(), color1, (int) armiesSpinner1.getValue(),
        (int) warriorsSpinner1.getValue());
    Clan clanB = createClan(nameField2.getText(), color2, (int) armiesSpinner2.getValue(),
        (int) warriorsSpinner2.getValue());
    mainFrame.setClans(clanA, clanB);
    dispose();
  }

  private Clan createClan(String name, Color color, int numArmies, int numWarriorsPerArmy) {
    Clan clan = new Clan(name);
    clan.setColor(color);
    Random rand = new Random();
    for (int i = 0; i < numArmies; i++) {
      Army army = new Army();
      army.setId(String.valueOf((char) ('A' + i)));
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