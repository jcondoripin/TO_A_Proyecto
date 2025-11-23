package com.battlesimulator.ui;

import com.battlesimulator.database.DatabaseManager;
import com.battlesimulator.database.User;
import java.awt.*;
import java.sql.SQLException;
import javax.swing.*;

public class RegisterDialog extends JDialog {
  private final JTextField usernameField = new JTextField(20);
  private final JPasswordField passwordField = new JPasswordField(20);
  private final JPasswordField confirmPasswordField = new JPasswordField(20);
  private User registeredUser = null;
  
  public RegisterDialog(JFrame parent) {
    super(parent, "Registro de Usuario", true);
    initUI();
  }
  
  private void initUI() {
    setSize(480, 400);
    setLocationRelativeTo(getParent());
    setLayout(new BorderLayout(10, 10));
    UITheme.styleDialog(this);
    
    // Panel principal
    JPanel mainPanel = new JPanel(new GridBagLayout());
    UITheme.stylePanel(mainPanel);
    mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.insets = new Insets(8, 8, 8, 8);
    
    // Título
    JLabel titleLabel = new JLabel("CREAR GUERRERO", SwingConstants.CENTER);
    UITheme.styleTitleLabel(titleLabel);
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.gridwidth = 2;
    mainPanel.add(titleLabel, gbc);
    
    // Instrucciones
    JLabel infoLabel = new JLabel("<html><center>Requisitos<br>Usuario mínimo 3 caracteres<br>Contraseña mínimo 4 caracteres</center></html>");
    UITheme.styleLabel(infoLabel);
    infoLabel.setFont(UITheme.SMALL_FONT);
    infoLabel.setForeground(UITheme.ACCENT_GOLD.darker());
    gbc.gridy = 1;
    mainPanel.add(infoLabel, gbc);
    
    // Espacio
    gbc.gridy = 2;
    mainPanel.add(Box.createRigidArea(new Dimension(0, 5)), gbc);
    
    // Usuario
    gbc.gridwidth = 1;
    gbc.gridy = 3;
    gbc.gridx = 0;
    JLabel userLbl = new JLabel("Usuario:");
    UITheme.styleLabel(userLbl);
    mainPanel.add(userLbl, gbc);
    
    gbc.gridx = 1;
    UITheme.styleTextField(usernameField);
    mainPanel.add(usernameField, gbc);
    
    // Contraseña
    gbc.gridy = 4;
    gbc.gridx = 0;
    JLabel passLbl = new JLabel("Contraseña:");
    UITheme.styleLabel(passLbl);
    mainPanel.add(passLbl, gbc);
    
    gbc.gridx = 1;
    UITheme.styleTextField(passwordField);
    mainPanel.add(passwordField, gbc);
    
    // Confirmar contraseña
    gbc.gridy = 5;
    gbc.gridx = 0;
    JLabel confirmLbl = new JLabel("Confirmar:");
    UITheme.styleLabel(confirmLbl);
    mainPanel.add(confirmLbl, gbc);
    
    gbc.gridx = 1;
    UITheme.styleTextField(confirmPasswordField);
    mainPanel.add(confirmPasswordField, gbc);
    
    add(mainPanel, BorderLayout.CENTER);
    
    // Panel de botones
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
    UITheme.stylePanel(buttonPanel);
    
    JButton registerBtn = new JButton("Unirse a la Batalla");
    registerBtn.addActionListener(e -> register());
    UITheme.stylePrimaryButton(registerBtn);
    buttonPanel.add(registerBtn);
    
    JButton cancelBtn = new JButton("Cancelar");
    cancelBtn.addActionListener(e -> dispose());
    UITheme.styleButton(cancelBtn);
    buttonPanel.add(cancelBtn);
    
    add(buttonPanel, BorderLayout.SOUTH);
    
    // Enter para registrar
    confirmPasswordField.addActionListener(e -> register());
  }
  
  private void register() {
    String username = usernameField.getText().trim();
    String password = new String(passwordField.getPassword());
    String confirmPassword = new String(confirmPasswordField.getPassword());
    
    if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
      JOptionPane.showMessageDialog(this,
        "Por favor complete todos los campos",
        "Campos vacíos", JOptionPane.WARNING_MESSAGE);
      return;
    }
    
    if (!password.equals(confirmPassword)) {
      JOptionPane.showMessageDialog(this,
        "Las contraseñas no coinciden",
        "Error", JOptionPane.ERROR_MESSAGE);
      passwordField.setText("");
      confirmPasswordField.setText("");
      passwordField.requestFocus();
      return;
    }
    
    try {
      DatabaseManager db = DatabaseManager.getInstance();
      registeredUser = db.registerUser(username, password);
      JOptionPane.showMessageDialog(this,
        "¡Cuenta creada exitosamente!\nBienvenido " + registeredUser.getUsername(),
        "Registro Exitoso", JOptionPane.INFORMATION_MESSAGE);
      dispose();
    } catch (SQLException | IllegalArgumentException e) {
      JOptionPane.showMessageDialog(this,
        e.getMessage(),
        "Error de Registro", JOptionPane.ERROR_MESSAGE);
      passwordField.setText("");
      confirmPasswordField.setText("");
      usernameField.requestFocus();
    }
  }
  
  public User getRegisteredUser() {
    return registeredUser;
  }
}
