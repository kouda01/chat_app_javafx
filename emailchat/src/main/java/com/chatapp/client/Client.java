package com.chatapp.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.chatapp.common.Message;
import com.chatapp.common.User;
import com.chatapp.database.DBConfig;

public class Client {
    private Socket socket;
    private DatagramSocket udpSocket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private String serverAddress;
    private int serverPort;
    private String email;
    private String username;
    private boolean connected;
    private List<User> contacts;
    private List<MessageListener> messageListeners;
    private ExecutorService threadPool;
    
    public interface MessageListener {
        void onMessageReceived(Message message);
        void onConnectionClosed();
    }
    
    public Client(String serverAddress) {
        this.serverAddress = serverAddress;
        this.serverPort = DBConfig.SERVER_PORT;
        this.connected = false;
        this.contacts = new ArrayList<>();
        this.messageListeners = new ArrayList<>();
        this.threadPool = Executors.newFixedThreadPool(2);
    }
    
    public boolean connect() {
        try {
            // Connect to server
            socket = new Socket(serverAddress, serverPort);
            
            // Create streams
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());
            
            // Create UDP socket for notifications
            udpSocket = new DatagramSocket();
            
            // Start message listener thread
            threadPool.execute(this::listenForMessages);
            
            // Start UDP listener
            threadPool.execute(this::listenForUdpMessages);
            
            connected = true;
            return true;
        } catch (IOException e) {
            System.err.println("Connection failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public void disconnect() {
        if (connected) {
            try {
                // Send logout message
                Message logoutMsg = new Message(Message.MessageType.LOGOUT);
                sendMessage(logoutMsg);
                
                // Clean up resources
                if (inputStream != null) {
                    inputStream.close();
                }
                
                if (outputStream != null) {
                    outputStream.close();
                }
                
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
                
                if (udpSocket != null && !udpSocket.isClosed()) {
                    udpSocket.close();
                }
                
                // Shutdown thread pool
                threadPool.shutdown();
                
                connected = false;
                email = null;
                username = null;
            } catch (IOException e) {
                System.err.println("Error during disconnect: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    private void listenForMessages() {
        try {
            while (connected) {
                Message message = (Message) inputStream.readObject();
                
                if (message != null) {
                    // Notify all listeners
                    for (MessageListener listener : messageListeners) {
                        listener.onMessageReceived(message);
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Connection to server lost: " + e.getMessage());
            
            connected = false;
            
            // Notify listeners of disconnection
            for (MessageListener listener : messageListeners) {
                listener.onConnectionClosed();
            }
        }
    }
    
    private void listenForUdpMessages() {
        try {
            byte[] buffer = new byte[1024];
            
            while (connected) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                udpSocket.receive(packet);
                
                // Process notification
                String notification = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Notification received: " + notification);
                
                // Create a notification message
                Message notificationMsg = new Message(Message.MessageType.NOTIFICATION);
                notificationMsg.setContent(notification);
                
                // Notify listeners
                for (MessageListener listener : messageListeners) {
                    listener.onMessageReceived(notificationMsg);
                }
            }
        } catch (IOException e) {
            if (connected) {
                System.err.println("UDP connection error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    public void sendMessage(Message message) {
        try {
            if (connected) {
                outputStream.writeObject(message);
                outputStream.flush();
            }
        } catch (IOException e) {
            System.err.println("Error sending message: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public boolean login(String email, String password) {
        if (!connected) {
            connect();
        }
        
        Message loginMsg = new Message(Message.MessageType.LOGIN);
        loginMsg.setContent(email + ":" + password);
        
        try {
            sendMessage(loginMsg);
            
            // Wait for response
            Message response = (Message) inputStream.readObject();
            
            if (response.getType() == Message.MessageType.LOGIN) {
                String content = response.getContent();
                
                if (content.startsWith("SUCCESS")) {
                    this.email = email;
                    this.username = content.split(":")[1];
                    
                    // Request contact list
                    requestContactList();
                    
                    return true;
                }
            }
            
            return false;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Login error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean register(String email, String password, String username) {
        if (!connected) {
            connect();
        }
        
        Message registerMsg = new Message(Message.MessageType.REGISTER);
        registerMsg.setContent(email + ":" + password + ":" + username);
        
        try {
            sendMessage(registerMsg);
            
            // Wait for response
            Message response = (Message) inputStream.readObject();
            
            if (response.getType() == Message.MessageType.REGISTER) {
                return response.getContent().equals("SUCCESS");
            }
            
            return false;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Registration error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public void requestContactList() {
        Message contactListMsg = new Message(Message.MessageType.CONTACT_LIST);
        sendMessage(contactListMsg);
    }
    
    public boolean addContact(String contactEmail) {
        Message addContactMsg = new Message(Message.MessageType.ADD_CONTACT);
        addContactMsg.setContent(contactEmail);
        
        try {
            sendMessage(addContactMsg);
            
            // Wait for response
            Message response = (Message) inputStream.readObject();
            
            if (response.getType() == Message.MessageType.ADD_CONTACT) {
                return response.getContent().startsWith("SUCCESS");
            }
            
            return false;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Add contact error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public void sendChatMessage(String receiverEmail, String content) {
        Message chatMsg = new Message(email, receiverEmail, content);
        sendMessage(chatMsg);
    }
    
    public void addMessageListener(MessageListener listener) {
        messageListeners.add(listener);
    }
    
    public void removeMessageListener(MessageListener listener) {
        messageListeners.remove(listener);
    }
    
    public String getEmail() {
        return email;
    }
    
    public String getUsername() {
        return username;
    }
    
    public boolean isConnected() {
        return connected;
    }
    
    public List<User> getContacts() {
        return contacts;
    }
    
    public void updateContacts(List<User> contacts) {
        this.contacts = contacts;
    }

    public void logout() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'logout'");
    }

    public void setMessageListener(Object object) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setMessageListener'");
    }
}