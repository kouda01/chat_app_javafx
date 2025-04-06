package com.chatapp.database;

import java.sql.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import com.chatapp.common.Message;
import com.chatapp.common.User;

public class DatabaseManager {
    private Connection connection;
    
    public DatabaseManager() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC driver not found");
            e.printStackTrace();
        }
    }
    
    public boolean connect() {
        try {
            connection = DriverManager.getConnection(
                DBConfig.DB_URL, 
                DBConfig.DB_USER, 
                DBConfig.DB_PASSWORD
            );
            return true;
        } catch (SQLException e) {
            System.err.println("Failed to connect to database");
            e.printStackTrace();
            return false;
        }
    }
    
    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Failed to close database connection");
            e.printStackTrace();
        }
    }
    
    public User authenticateUser(String email, String password) {
        if (connection == null) return null;
        try {
            PreparedStatement stmt = connection.prepareStatement(
                "SELECT * FROM users WHERE email = ? AND password = ?"
            );
            stmt.setString(1, email);
            stmt.setString(2, hashPassword(password));
            
            ResultSet result = stmt.executeQuery();
            
            if (result.next()) {
                User user = new User(result.getString("email"), result.getString("username"));
                updateUserStatus(email, "ONLINE");
                return user;
            }
            
            return null;
        } catch (SQLException e) {
            System.err.println("Database error during authentication");
            e.printStackTrace();
            return null;
        }
    }
    
    public boolean registerUser(String email, String password, String username) {
        if (connection == null) return false;
        try {
            connection.setAutoCommit(false);
            PreparedStatement checkStmt = connection.prepareStatement("SELECT * FROM users WHERE email = ?");
            checkStmt.setString(1, email);
            ResultSet result = checkStmt.executeQuery();
            if (result.next()) return false;

            PreparedStatement insertStmt = connection.prepareStatement(
                "INSERT INTO users (email, password, username, status) VALUES (?, ?, ?, 'OFFLINE')"
            );
            insertStmt.setString(1, email);
            insertStmt.setString(2, hashPassword(password));
            insertStmt.setString(3, username);
            
            int rowsAffected = insertStmt.executeUpdate();
            connection.commit();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Database error during user registration");
            e.printStackTrace();
            try { connection.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            return false;
        } finally {
            try { connection.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
    }
    
    public boolean updateUserStatus(String email, String status) {
        if (connection == null) return false;
        try {
            PreparedStatement stmt = connection.prepareStatement(
                "UPDATE users SET status = ? WHERE email = ?"
            );
            stmt.setString(1, status);
            stmt.setString(2, email);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Database error updating user status");
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean storeMessage(String senderEmail, String receiverEmail, String content) {
        if (connection == null) return false;
        try {
            PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO messages (sender_email, receiver_email, content, is_read) VALUES (?, ?, ?, FALSE)"
            );
            stmt.setString(1, senderEmail);
            stmt.setString(2, receiverEmail);
            stmt.setString(3, content);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Database error storing message");
            e.printStackTrace();
            return false;
        }
    }
    
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    public List<User> getUserContacts(String userEmail) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getUserContacts'");
    }

    public boolean addContact(String userEmail, String contactEmail) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'addContact'");
    }

    public List<Message> getChatHistory(String userEmail, String contactEmail) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getChatHistory'");
    }

    public List<Message> getUnreadMessages(String userEmail) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getUnreadMessages'");
    }
}
