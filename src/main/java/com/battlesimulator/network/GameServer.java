package com.battlesimulator.network;

import com.battlesimulator.domain.Clan;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GameServer {
  private static final int PORT = 5555;
  private ServerSocket serverSocket;
  private final Map<String, ClientHandler> clients = new ConcurrentHashMap<>();
  private boolean running = false;
  private final ObjectMapper objectMapper = new ObjectMapper();
  private ServerListener listener;
  
  public interface ServerListener {
    void onPlayerConnected(String playerId);
    void onPlayerDisconnected(String playerId);
    void onClanConfigured(String playerId, Clan clan);
    void onWarStartRequested();
    void onAttackReceived(String playerId, String attackData);
    void onChatMessage(String playerId, String message);
  }
  
  public GameServer(ServerListener listener) {
    this.listener = listener;
  }
  
  public void start() throws IOException {
    int port = PORT;
    int attempts = 0;
    IOException lastException = null;
    
    // Intentar hasta 5 puertos diferentes
    while (attempts < 5) {
      try {
        serverSocket = new ServerSocket();
        serverSocket.setReuseAddress(true);
        serverSocket.bind(new java.net.InetSocketAddress(port));
        running = true;
        System.out.println("Servidor iniciado en puerto " + port);
        break; // Éxito, salir del bucle
      } catch (IOException e) {
        lastException = e;
        if (serverSocket != null && !serverSocket.isClosed()) {
          try { serverSocket.close(); } catch (Exception ex) {}
        }
        port++; // Intentar con el siguiente puerto
        attempts++;
        System.err.println("Puerto " + port + " ocupado, intentando puerto " + (port + 1));
      }
    }
    
    if (!running) {
      throw new IOException("No se pudo iniciar el servidor después de " + attempts + " intentos", lastException);
    }
    
    new Thread(() -> {
      while (running) {
        try {
          Socket clientSocket = serverSocket.accept();
          String clientId = UUID.randomUUID().toString().substring(0, 8);
          ClientHandler handler = new ClientHandler(clientSocket, clientId);
          clients.put(clientId, handler);
          new Thread(handler).start();
          System.out.println("Cliente conectado: " + clientId);
          if (listener != null) {
            listener.onPlayerConnected(clientId);
          }
        } catch (IOException e) {
          if (running) {
            e.printStackTrace();
          }
        }
      }
    }).start();
  }
  
  public void stop() {
    running = false;
    try {
      // Cerrar todos los clientes primero
      for (ClientHandler client : clients.values()) {
        client.close();
      }
      clients.clear();
      
      // Cerrar el socket del servidor
      if (serverSocket != null && !serverSocket.isClosed()) {
        serverSocket.close();
      }
      
      System.out.println("Servidor detenido y puerto liberado");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  public void broadcast(Message message) {
    String json = messageToJson(message);
    for (ClientHandler client : clients.values()) {
      client.send(json);
    }
  }
  
  public void sendToPlayer(String playerId, Message message) {
    ClientHandler client = clients.get(playerId);
    if (client != null) {
      client.send(messageToJson(message));
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
  
  public int getConnectedPlayers() {
    return clients.size();
  }
  
  public Set<String> getPlayerIds() {
    return new HashSet<>(clients.keySet());
  }
  
  public int getActualPort() {
    if (serverSocket != null && serverSocket.isBound()) {
      return serverSocket.getLocalPort();
    }
    return PORT;
  }
  
  class ClientHandler implements Runnable {
    private final Socket socket;
    private final String clientId;
    private BufferedReader in;
    private PrintWriter out;
    
    public ClientHandler(Socket socket, String clientId) {
      this.socket = socket;
      this.clientId = clientId;
      try {
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        // Enviar ID al cliente
        Message connectMsg = new Message(Message.Type.CONNECT, clientId, "SERVER");
        send(messageToJson(connectMsg));
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    
    @Override
    public void run() {
      try {
        String line;
        while ((line = in.readLine()) != null) {
          Message msg = jsonToMessage(line);
          if (msg != null) {
            handleMessage(msg);
          }
        }
      } catch (IOException e) {
        System.out.println("Cliente desconectado: " + clientId);
      } finally {
        disconnect();
      }
    }
    
    private void handleMessage(Message msg) {
      msg.setPlayerId(clientId);
      
      switch (msg.getType()) {
        case CLAN_CONFIG:
          if (listener != null) {
            // Aquí recibiríamos la configuración del clan
            listener.onClanConfigured(clientId, null); // TODO: deserializar clan
          }
          break;
        case START_WAR:
          if (listener != null) {
            listener.onWarStartRequested();
          }
          break;
        case ATTACK:
          if (listener != null) {
            listener.onAttackReceived(clientId, msg.getData());
          }
          // Broadcast a todos
          broadcast(msg);
          break;
        case BATTLE_UPDATE:
          // Retransmitir actualización de batalla a todos los clientes
          System.out.println("[SERVER] Retransmitiendo BATTLE_UPDATE: " + msg.getData());
          broadcast(msg);
          break;
        case BATTLE_END:
          // Retransmitir fin de batalla
          broadcast(msg);
          break;
        case WAR_END:
          // Retransmitir fin de guerra
          broadcast(msg);
          break;
        case TURN_END:
          // Retransmitir fin de turno
          System.out.println("[SERVER] Retransmitiendo TURN_END de " + msg.getUsername());
          broadcast(msg);
          break;
        case CHAT:
          if (listener != null) {
            listener.onChatMessage(clientId, msg.getData());
          }
          broadcast(msg);
          break;
        case DISCONNECT:
          disconnect();
          break;
        default:
          break;
      }
    }
    
    public void send(String message) {
      if (out != null) {
        out.println(message);
      }
    }
    
    public void close() {
      try {
        if (socket != null && !socket.isClosed()) {
          socket.close();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    
    private void disconnect() {
      clients.remove(clientId);
      close();
      if (listener != null) {
        listener.onPlayerDisconnected(clientId);
      }
    }
  }
}
