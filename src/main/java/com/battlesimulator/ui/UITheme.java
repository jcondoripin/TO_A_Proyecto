package com.battlesimulator.ui;

import java.awt.*;
import javax.swing.*;

public class UITheme {
  // Colores del tema
  public static final Color PRIMARY_DARK = new Color(20, 20, 30);
  public static final Color SECONDARY_DARK = new Color(30, 30, 45);
  public static final Color ACCENT_GOLD = new Color(218, 165, 32);
  public static final Color ACCENT_RED = new Color(220, 50, 50);
  public static final Color ACCENT_BLUE = new Color(50, 120, 220);
  public static final Color TEXT_LIGHT = new Color(240, 240, 240);
  public static final Color TEXT_GOLD = new Color(255, 215, 0);
  public static final Color BATTLE_GRID_EMPTY = new Color(40, 40, 50);
  public static final Color BATTLE_GRID_BORDER = new Color(80, 80, 100);
  
  // Fuentes
  public static final Font TITLE_FONT = new Font("Serif", Font.BOLD, 28);
  public static final Font SUBTITLE_FONT = new Font("Serif", Font.BOLD, 18);
  public static final Font NORMAL_FONT = new Font("SansSerif", Font.PLAIN, 14);
  public static final Font BUTTON_FONT = new Font("SansSerif", Font.BOLD, 14);
  public static final Font SMALL_FONT = new Font("SansSerif", Font.PLAIN, 12);
  
  public static void styleButton(JButton button) {
    button.setFont(BUTTON_FONT);
    button.setBackground(SECONDARY_DARK);
    button.setForeground(ACCENT_GOLD);
    button.setFocusPainted(false);
    button.setBorder(BorderFactory.createCompoundBorder(
      BorderFactory.createLineBorder(ACCENT_GOLD, 2),
      BorderFactory.createEmptyBorder(8, 15, 8, 15)
    ));
    button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    
    button.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseEntered(java.awt.event.MouseEvent evt) {
        button.setBackground(ACCENT_GOLD);
        button.setForeground(PRIMARY_DARK);
      }
      
      public void mouseExited(java.awt.event.MouseEvent evt) {
        button.setBackground(SECONDARY_DARK);
        button.setForeground(ACCENT_GOLD);
      }
    });
  }
  
  public static void stylePrimaryButton(JButton button) {
    button.setFont(BUTTON_FONT);
    button.setBackground(ACCENT_GOLD);
    button.setForeground(PRIMARY_DARK);
    button.setFocusPainted(false);
    button.setBorder(BorderFactory.createCompoundBorder(
      BorderFactory.createLineBorder(ACCENT_GOLD.brighter(), 2),
      BorderFactory.createEmptyBorder(10, 20, 10, 20)
    ));
    button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    
    button.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseEntered(java.awt.event.MouseEvent evt) {
        button.setBackground(ACCENT_GOLD.brighter());
      }
      
      public void mouseExited(java.awt.event.MouseEvent evt) {
        button.setBackground(ACCENT_GOLD);
      }
    });
  }
  
  public static void stylePanel(JPanel panel) {
    panel.setBackground(PRIMARY_DARK);
  }
  
  public static void styleLabel(JLabel label) {
    label.setForeground(TEXT_LIGHT);
    label.setFont(NORMAL_FONT);
  }
  
  public static void styleTitleLabel(JLabel label) {
    label.setForeground(TEXT_GOLD);
    label.setFont(TITLE_FONT);
  }
  
  public static void styleTextArea(JTextArea textArea) {
    textArea.setBackground(SECONDARY_DARK);
    textArea.setForeground(TEXT_LIGHT);
    textArea.setFont(SMALL_FONT);
    textArea.setCaretColor(ACCENT_GOLD);
  }
  
  public static void styleTextField(JTextField textField) {
    textField.setBackground(SECONDARY_DARK);
    textField.setForeground(TEXT_LIGHT);
    textField.setFont(NORMAL_FONT);
    textField.setCaretColor(ACCENT_GOLD);
    textField.setBorder(BorderFactory.createCompoundBorder(
      BorderFactory.createLineBorder(ACCENT_GOLD, 1),
      BorderFactory.createEmptyBorder(5, 8, 5, 8)
    ));
  }
  
  public static void styleDialog(JDialog dialog) {
    dialog.getContentPane().setBackground(PRIMARY_DARK);
  }
  
  public static void styleFrame(JFrame frame) {
    frame.getContentPane().setBackground(PRIMARY_DARK);
  }
  
  public static JPanel createTitledPanel(String title) {
    JPanel panel = new JPanel();
    panel.setBackground(PRIMARY_DARK);
    panel.setBorder(BorderFactory.createTitledBorder(
      BorderFactory.createLineBorder(ACCENT_GOLD, 2),
      title,
      javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
      javax.swing.border.TitledBorder.DEFAULT_POSITION,
      SUBTITLE_FONT,
      TEXT_GOLD
    ));
    return panel;
  }
}
