package com.battlesimulator.ui;

import com.battlesimulator.database.DatabaseManager;
import com.battlesimulator.database.User;
import java.awt.*;
import java.sql.SQLException;
import javax.swing.*;

public class LoginDialog extends JDialog {
  private final JTextField usernameField = new JTextField(20);
  private final JPasswordField passwordField = new JPasswordField(20);
  private User loggedInUser = null;
  
  public LoginDialog(JFrame parent) {
    super(parent, "Iniciar Sesión", true);
    initUI();
  }
  
  private void initUI() {
    setSize(450, 320);
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
    JLabel titleLabel = new JLabel("INICIAR SESIÓN", SwingConstants.CENTER);
    UITheme.styleTitleLabel(titleLabel);
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.gridwidth = 2;
    mainPanel.add(titleLabel, gbc);
    
    // Espacio
    gbc.gridy = 1;
    mainPanel.add(Box.createRigidArea(new Dimension(0, 10)), gbc);
    
    // Usuario
    gbc.gridwidth = 1;
    gbc.gridy = 2;
    gbc.gridx = 0;
    JLabel userLbl = new JLabel("Usuario:");
    UITheme.styleLabel(userLbl);
    mainPanel.add(userLbl, gbc);
    
    gbc.gridx = 1;
    UITheme.styleTextField(usernameField);
    mainPanel.add(usernameField, gbc);
    
    // Contraseña
    gbc.gridy = 3;
    gbc.gridx = 0;
    JLabel passLbl = new JLabel("Contraseña:");
    UITheme.styleLabel(passLbl);
    mainPanel.add(passLbl, gbc);
    
    gbc.gridx = 1;
    UITheme.styleTextField(passwordField);
    mainPanel.add(passwordField, gbc);
    
    add(mainPanel, BorderLayout.CENTER);
    
    // Panel de botones
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
    UITheme.stylePanel(buttonPanel);
    
    JButton loginBtn = new JButton("Entrar");
    loginBtn.addActionListener(e -> login());
    UITheme.stylePrimaryButton(loginBtn);
    buttonPanel.add(loginBtn);
    
    JButton registerBtn = new JButton("Registrarse");
    registerBtn.addActionListener(e -> openRegister());
    UITheme.styleButton(registerBtn);
    buttonPanel.add(registerBtn);
    
    JButton cancelBtn = new JButton("Cancelar");
    cancelBtn.addActionListener(e -> dispose());
    UITheme.styleButton(cancelBtn);
    buttonPanel.add(cancelBtn);
    
    add(buttonPanel, BorderLayout.SOUTH);
    
    // Enter para login
    passwordField.addActionListener(e -> login());
    usernameField.addActionListener(e -> passwordField.requestFocus());
  }
  
  private void login() {
    String username = usernameField.getText().trim();
    String password = new String(passwordField.getPassword());
    
    if (username.isEmpty() || password.isEmpty()) {
      JOptionPane.showMessageDialog(this, 
        "Por favor ingrese usuario y contraseña",
        "Campos vacíos", JOptionPane.WARNING_MESSAGE);
      return;
    }
    
    try {
      DatabaseManager db = DatabaseManager.getInstance();
      loggedInUser = db.loginUser(username, password);
      JOptionPane.showMessageDialog(this,
        "¡Bienvenido " + loggedInUser.getUsername() + "!",
        "Login Exitoso", JOptionPane.INFORMATION_MESSAGE);
      dispose();
    } catch (SQLException e) {
      JOptionPane.showMessageDialog(this,
        e.getMessage(),
        "Error de Login", JOptionPane.ERROR_MESSAGE);
      passwordField.setText("");
      passwordField.requestFocus();
    }
  }
  
  private void openRegister() {
    RegisterDialog registerDialog = new RegisterDialog((JFrame) getParent());
    registerDialog.setVisible(true);
    
    if (registerDialog.getRegisteredUser() != null) {
      loggedInUser = registerDialog.getRegisteredUser();
      dispose();
    }
  }
  
  public User getLoggedInUser() {
    return loggedInUser;
  }
}
