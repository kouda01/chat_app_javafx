package com.chatapp.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.chatapp.common.Message;
import com.chatapp.common.User;
import com.chatapp.database.DBConfig;
import com.chatapp.database.DatabaseManager;

public class Server {
    private ServerSocket serverSocket;
    private DatagramSocket udpSocket;
    private DatabaseManager dbManager;
    private ExecutorService threadPool;
    
    // Map to keep track of online users and their client handlers
    private Map<String, ClientHandler> onlineUsers;
    
    public Server() {
        dbManager = new DatabaseManager();
        onlineUsers = new HashMap<>();
        threadPool = Executors.newCachedThreadPool();
    }
    
    public void start() {
        try {
            // Connect to database
            if (!dbManager.connect()) {
                System.err.println("Failed to connect to database. Exiting...");
                return;
            }
            
            // Start TCP server
            serverSocket = new ServerSocket(DBConfig.SERVER_PORT);
            System.out.println("Server started on port " + DBConfig.SERVER_PORT);
            
            // Start UDP server for notifications
            udpSocket = new DatagramSocket(DBConfig.UDP_PORT);
            System.out.println("UDP server started on port " + DBConfig.UDP_PORT);
            
            // Start UDP listener thread
            threadPool.execute(this::listenForUdpMessages);
            
            // Accept client connections
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());
                
                // Create a new thread for each client
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                threadPool.execute(clientHandler);
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            shutdown();
        }
    }
    
    private void listenForUdpMessages() {
        try {
            byte[] buffer = new byte[1024];
            
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                udpSocket.receive(packet);
                
                // Process UDP message (for notifications)
                String message = new String(packet.getData(), 0, packet.getLength());
                System.out.println("UDP message received: " + message);
                
                // Handle notification logic here
            }
        } catch (IOException e) {
            System.err.println("UDP server error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void sendUdpNotification(String userEmail, String message) {
        try {
            for (Map.Entry<String, ClientHandler> entry : onlineUsers.entrySet()) {
                if (entry.getKey().equals(userEmail)) {
                    InetAddress address = entry.getValue().getClientSocket().getInetAddress();
                    byte[] data = message.getBytes();
                    DatagramPacket packet = new DatagramPacket(
                        data, data.length, address, DBConfig.UDP_PORT
                    );
                    udpSocket.send(packet);
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Error sending UDP notification: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void shutdown() {
        try {
            // Disconnect all clients
            for (ClientHandler handler : onlineUsers.values()) {
                handler.closeConnection();
            }
            
            // Close server sockets
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            
            if (udpSocket != null && !udpSocket.isClosed()) {
                udpSocket.close();
            }
            
            // Disconnect from database
            dbManager.disconnect();
            
            // Shutdown thread pool
            threadPool.shutdown();
            
            System.out.println("Server shutdown complete");
        } catch (IOException e) {
            System.err.println("Error during server shutdown: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void addOnlineUser(String email, ClientHandler handler) {
        onlineUsers.put(email, handler);
        
        // Notify other users that this user is now online
        broadcastUserStatus(email, "ONLINE");
    }
    
    public void removeOnlineUser(String email) {
        onlineUsers.remove(email);
        
        // Update user status in database
        dbManager.updateUserStatus(email, "OFFLINE");
        
        // Notify other users that this user is now offline
        broadcastUserStatus(email, "OFFLINE");
    }
    
    public void broadcastUserStatus(String email, String status) {
        // Create a status update message
        Message statusMsg = new Message(Message.MessageType.USER_STATUS);
        statusMsg.setSenderEmail(email);
        statusMsg.setContent(status);
        
        // Send to all online users who have this user in their contacts
        for (ClientHandler handler : onlineUsers.values()) {
            if (!handler.getUserEmail().equals(email)) {
                handler.sendMessage(statusMsg);
            }
        }
    }
    
    public void forwardMessage(Message message) {
        String receiverEmail = message.getReceiverEmail();
        
        // Store message in database
        dbManager.storeMessage(
            message.getSenderEmail(),
            receiverEmail,
            message.getContent()
        );
        
        // If receiver is online, forward the message
        ClientHandler receiverHandler = onlineUsers.get(receiverEmail);
        if (receiverHandler != null) {
            receiverHandler.sendMessage(message);
            
            // Also send UDP notification
            sendUdpNotification(receiverEmail, "New message from " + message.getSenderEmail());
        }
    }
    
    public User authenticateUser(String email, String password) {
        return dbManager.authenticateUser(email, password);
    }
    
    public boolean registerUser(String email, String password, String username) {
        return dbManager.registerUser(email, password, username);
    }
    
    public List<User> getUserContacts(String userEmail) {
        return dbManager.getUserContacts(userEmail);
    }
    
    public boolean addContact(String userEmail, String contactEmail) {
        return dbManager.addContact(userEmail, contactEmail);
    }
    
    public List<Message> getUnreadMessages(String userEmail) {
        return dbManager.getUnreadMessages(userEmail);
    }
    
    public List<Message> getChatHistory(String userEmail, String contactEmail) {
        return dbManager.getChatHistory(userEmail, contactEmail);
    }
    
    public boolean isUserOnline(String email) {
        return onlineUsers.containsKey(email);
    }
}