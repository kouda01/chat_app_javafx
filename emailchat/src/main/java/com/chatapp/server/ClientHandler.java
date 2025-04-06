package com.chatapp.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

import com.chatapp.common.Message;
import com.chatapp.common.User;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private Server server;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private String userEmail;
    private boolean running;
    
    public ClientHandler(Socket clientSocket, Server server) {
        this.clientSocket = clientSocket;
        this.server = server;
        this.running = true;
        
        try {
            // Create streams for communication
            outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
            inputStream = new ObjectInputStream(clientSocket.getInputStream());
        } catch (IOException e) {
            System.err.println("Error creating streams: " + e.getMessage());
            e.printStackTrace();
            closeConnection();
        }
    }
    
    @Override
    public void run() {
        try {
            while (running) {
                // Read message from client
                Message message = (Message) inputStream.readObject();
                
                if (message != null) {
                    handleMessage(message);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error handling client: " + e.getMessage());
        } finally {
            // Clean up when client disconnects
            if (userEmail != null) {
                server.removeOnlineUser(userEmail);
                System.out.println("User disconnected: " + userEmail);
            }
            closeConnection();
        }
    }
    
    private void handleMessage(Message message) {
        switch (message.getType()) {
            case LOGIN:
                handleLogin(message);
                break;
                
            case LOGOUT:
                handleLogout();
                break;
                
            case REGISTER:
                handleRegistration(message);
                break;
                
            case TEXT:
            case MEDIA:
                server.forwardMessage(message);
                break;
                
            case CONTACT_LIST:
                sendContactList();
                break;
                
            case ADD_CONTACT:
                handleAddContact(message);
                break;
                
            default:
                System.out.println("Unknown message type received");
        }
    }
    
    private void handleLogin(Message message) {
        // Extract login details from message content
        String[] parts = message.getContent().split(":");
        String email = parts[0];
        String password = parts[1];
        
        // Authenticate user
        User user = server.authenticateUser(email, password);
        
        Message response = new Message(Message.MessageType.LOGIN);
        
        if (user != null) {
            // Login successful
            this.userEmail = email;
            response.setContent("SUCCESS:" + user.getUsername());
            server.addOnlineUser(email, this);
            
            // Send any unread messages to the user
            sendUnreadMessages();
        } else {
            // Login failed
            response.setContent("FAILED:Invalid email or password");
        }
        
        sendMessage(response);
    }
    
    private void handleLogout() {
        if (userEmail != null) {
            server.removeOnlineUser(userEmail);
            userEmail = null;
        }
        
        // Send confirmation
        Message response = new Message(Message.MessageType.LOGOUT);
        response.setContent("SUCCESS");
        sendMessage(response);
    }
    
    private void handleRegistration(Message message) {
        // Extract registration details from message content
        String[] parts = message.getContent().split(":");
        String email = parts[0];
        String password = parts[1];
        String username = parts[2];
        
        // Register user
        boolean success = server.registerUser(email, password, username);
        
        Message response = new Message(Message.MessageType.REGISTER);
        
        if (success) {
            response.setContent("SUCCESS");
        } else {
            response.setContent("FAILED:Email already registered or database error");
        }
        
        sendMessage(response);
    }
    
    private void sendContactList() {
        if (userEmail != null) {
            List<User> contacts = server.getUserContacts(userEmail);
            
            // Mark which contacts are online
            for (User contact : contacts) {
                if (server.isUserOnline(contact.getEmail())) {
                    contact.setStatus("ONLINE");
                } else {
                    contact.setStatus("OFFLINE");
                }
            }
            
            // Send contacts list to client
            Message contactsMessage = new Message(Message.MessageType.CONTACT_LIST);
            contactsMessage.setContent("CONTACTS:" + contacts.size());
            sendMessage(contactsMessage);
            
            // Send each contact as a separate message
            for (User contact : contacts) {
                Message contactMessage = new Message(Message.MessageType.CONTACT_LIST);
                contactMessage.setContent(contact.getEmail() + ":" + contact.getUsername() + ":" + contact.getStatus());
                sendMessage(contactMessage);
            }
        }
    }
    
    private void handleAddContact(Message message) {
        String contactEmail = message.getContent();
        
        boolean success = server.addContact(userEmail, contactEmail);
        
        Message response = new Message(Message.MessageType.ADD_CONTACT);
        
        if (success) {
            // Also add the reverse contact relationship automatically
            server.addContact(contactEmail, userEmail);
            response.setContent("SUCCESS:" + contactEmail);
        } else {
            response.setContent("FAILED:User not found or already in contacts");
        }
        
        sendMessage(response);
    }
    
    private void sendUnreadMessages() {
        List<Message> unreadMessages = server.getUnreadMessages(userEmail);
        
        for (Message message : unreadMessages) {
            sendMessage(message);
        }
    }
    
    public void sendMessage(Message message) {
        try {
            outputStream.writeObject(message);
            outputStream.flush();
        } catch (IOException e) {
            System.err.println("Error sending message to client: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void closeConnection() {
        running = false;
        
        try {
            if (inputStream != null) {
                inputStream.close();
            }
            
            if (outputStream != null) {
                outputStream.close();
            }
            
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing connection: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public Socket getClientSocket() {
        return clientSocket;
    }
    
    public String getUserEmail() {
        return userEmail;
    }
}