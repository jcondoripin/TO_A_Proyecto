package com.battlesimulator.ui;

import com.battlesimulator.domain.Clan;
import com.battlesimulator.usecases.WarController;
import java.awt.*;
import javax.swing.*;

public class MainFrame extends JFrame {
  private Clan clan1;
  private Clan clan2;
  private final JButton configBtn;
  private final JButton startBtn;

  public MainFrame() {
    setTitle("Batalla de Clanes - Menú Principal");
    setSize(400, 200);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLayout(new FlowLayout());

    configBtn = new JButton("Configurar Clanes");
    configBtn.addActionListener(e -> new ConfigDialog(this).setVisible(true));
    add(configBtn);

    startBtn = new JButton("Iniciar Guerra");
    startBtn.setEnabled(false);
    startBtn.addActionListener(e -> {
      if (clan1 != null && clan2 != null) {
        new WarController(clan1, clan2).setVisible(true);
      }
    });
    add(startBtn);
  }

  public void setClans(Clan c1, Clan c2) {
    this.clan1 = c1;
    this.clan2 = c2;
    startBtn.setEnabled(true);
    JOptionPane.showMessageDialog(this, "Clanes configurados correctamente. Puedes iniciar la guerra.");
  }
}