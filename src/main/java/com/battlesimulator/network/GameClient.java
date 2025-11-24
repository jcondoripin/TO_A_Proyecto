package com.battlesimulator.network;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;
import java.net.*;

public class GameClient {
  private Socket socket;
  private BufferedReader in;
  private PrintWriter out;
  private boolean connected = false;
  private String playerId;
  private final ObjectMapper objectMapper = new ObjectMapper();
  private MessageListener listener;
  
  public interface MessageListener {
    void onMessageReceived(Message message);
    void onConnected(String playerId);
    void onDisconnected();
    void onError(String errorMessage);
  }
  
  public GameClient(MessageListener listener) {
    this.listener = listener;
  }
  
  public boolean connect(String host, int port) {
    try {
      socket = new Socket();
      socket.connect(new InetSocketAddress(host, port), 10000); // 10 second timeout
      socket.setSoTimeout(0); // Infinite read timeout - no timeout during game
      in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      out = new PrintWriter(socket.getOutputStream(), true);
      connected = true;
      
      // Iniciar thread de escucha
      new Thread(() -> {
        try {
          String line;
          while (connected && (line = in.readLine()) != null) {
            Message msg = jsonToMessage(line);
            if (msg != null) {
              // Si es mensaje de conexión, guardar el ID
              if (msg.getType() == Message.Type.CONNECT && playerId == null) {
                playerId = msg.getData();
                if (listener != null) {
                  listener.onConnected(playerId);
                }
              } else if (listener != null) {
                listener.onMessageReceived(msg);
              }
            }
          }
        } catch (SocketTimeoutException e) {
          if (connected && listener != null) {
            listener.onError("Tiempo de espera agotado. El servidor no responde.");
          }
        } catch (IOException e) {
          if (connected) {
            System.out.println("Conexión perdida con el servidor: " + e.getMessage());
            if (listener != null) {
              listener.onError("Conexión perdida con el servidor.");
            }
          }
        } finally {
          disconnect();
        }
      }, "GameClient-Listener").start();
      
      return true;
    } catch (SocketTimeoutException e) {
      String error = "No se pudo conectar al servidor. Tiempo de espera agotado.";
      System.out.println(error);
      if (listener != null) {
        listener.onError(error);
      }
      return false;
    } catch (UnknownHostException e) {
      String error = "Host desconocido: " + host;
      System.out.println(error);
      if (listener != null) {
        listener.onError(error);
      }
      return false;
    } catch (IOException e) {
      String error = "Error al conectar: " + e.getMessage();
      System.out.println(error);
      if (listener != null) {
        listener.onError(error);
      }
      return false;
    }
  }
  
  public void disconnect() {
    if (connected) {
      connected = false;
      try {
        sendMessage(new Message(Message.Type.DISCONNECT, ""));
        if (socket != null && !socket.isClosed()) {
          socket.close();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
      if (listener != null) {
        listener.onDisconnected();
      }
    }
  }
  
  public void sendMessage(Message message) {
    if (connected && out != null) {
      try {
        String json = messageToJson(message);
        out.println(json);
        if (out.checkError()) {
          throw new IOException("Error al enviar mensaje");
        }
      } catch (Exception e) {
        System.err.println("Error enviando mensaje: " + e.getMessage());
        if (listener != null) {
          listener.onError("Error al enviar mensaje al servidor.");
        }
      }
    }
  }
  
  private String messageToJson(Message msg) {
    try {
      return objectMapper.writeValueAsString(msg);
    } catch (Exception e) {
      e.printStackTrace();
      return "{}";
    }
  }
  
  private Message jsonToMessage(String json) {
    try {
      return objectMapper.readValue(json, Message.class);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }
  
  public boolean isConnected() {
    return connected;
  }
  
  public String getPlayerId() {
    return playerId;
  }
}
