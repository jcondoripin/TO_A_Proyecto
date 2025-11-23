package com.battlesimulator.ui;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class ImageResources {
  private static ImageResources instance;
  private final Map<String, BufferedImage> imageCache = new HashMap<>();
  
  // Rutas de imágenes por tipo de guerrero
  private static final String IMAGES_PATH = "resources/images/warriors/";
  private static final String MELEE_ICON = "melee.png";
  private static final String RANGED_ICON = "ranged.png";
  private static final String MAGIC_ICON = "magic.png";
  private static final String DEFAULT_ICON = "default.png";
  
  private ImageResources() {
    loadImages();
  }
  
  public static ImageResources getInstance() {
    if (instance == null) {
      instance = new ImageResources();
    }
    return instance;
  }
  
  private void loadImages() {
    // Intentar cargar imágenes desde archivos
    tryLoadImage("melee", IMAGES_PATH + MELEE_ICON);
    tryLoadImage("ranged", IMAGES_PATH + RANGED_ICON);
    tryLoadImage("magic", IMAGES_PATH + MAGIC_ICON);
    tryLoadImage("default", IMAGES_PATH + DEFAULT_ICON);
    
    // Si no existen, crear imágenes por defecto
    if (!imageCache.containsKey("melee")) {
      imageCache.put("melee", createDefaultWarriorImage("M", new Color(180, 50, 50)));
    }
    if (!imageCache.containsKey("ranged")) {
      imageCache.put("ranged", createDefaultWarriorImage("R", new Color(50, 150, 50)));
    }
    if (!imageCache.containsKey("magic")) {
      imageCache.put("magic", createDefaultWarriorImage("W", new Color(100, 100, 200)));
    }
    if (!imageCache.containsKey("default")) {
      imageCache.put("default", createDefaultWarriorImage("?", new Color(128, 128, 128)));
    }
  }
  
  private void tryLoadImage(String key, String path) {
    try {
      File file = new File(path);
      if (file.exists()) {
        BufferedImage img = ImageIO.read(file);
        imageCache.put(key, img);
        System.out.println("Imagen cargada: " + path);
      }
    } catch (IOException e) {
      System.out.println("No se pudo cargar imagen: " + path + " - Usando imagen por defecto");
    }
  }
  
  private BufferedImage createDefaultWarriorImage(String letter, Color baseColor) {
    int size = 48;
    BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2d = img.createGraphics();
    
    // Anti-aliasing
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    
    // Escudo/círculo base
    g2d.setColor(baseColor);
    g2d.fillOval(2, 2, size - 4, size - 4);
    
    // Borde más oscuro
    g2d.setColor(baseColor.darker());
    g2d.setStroke(new BasicStroke(2));
    g2d.drawOval(2, 2, size - 4, size - 4);
    
    // Letra central
    g2d.setColor(Color.WHITE);
    g2d.setFont(new Font("Arial", Font.BOLD, 24));
    FontMetrics fm = g2d.getFontMetrics();
    int x = (size - fm.stringWidth(letter)) / 2;
    int y = ((size - fm.getHeight()) / 2) + fm.getAscent();
    g2d.drawString(letter, x, y);
    
    g2d.dispose();
    return img;
  }
  
  public ImageIcon getWarriorIcon(String warriorType, Color clanColor) {
    String key = warriorType.toLowerCase();
    if (!imageCache.containsKey(key)) {
      key = "default";
    }
    
    BufferedImage baseImage = imageCache.get(key);
    BufferedImage coloredImage = applyColorTint(baseImage, clanColor);
    return new ImageIcon(coloredImage);
  }
  
  private BufferedImage applyColorTint(BufferedImage source, Color tintColor) {
    BufferedImage tinted = new BufferedImage(
      source.getWidth(), 
      source.getHeight(), 
      BufferedImage.TYPE_INT_ARGB
    );
    
    Graphics2D g2d = tinted.createGraphics();
    g2d.drawImage(source, 0, 0, null);
    
    // Aplicar tinte de color
    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.4f));
    g2d.setColor(tintColor);
    g2d.fillRect(0, 0, source.getWidth(), source.getHeight());
    
    g2d.dispose();
    return tinted;
  }
  
  public ImageIcon getScaledIcon(String warriorType, Color clanColor, int width, int height) {
    ImageIcon icon = getWarriorIcon(warriorType, clanColor);
    Image scaled = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
    return new ImageIcon(scaled);
  }
  
  // Método para recargar imágenes si se actualizan
  public void reloadImages() {
    imageCache.clear();
    loadImages();
  }
}
