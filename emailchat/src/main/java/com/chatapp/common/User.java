package com.chatapp.common;

import java.io.Serializable;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String email;
    private String username;
    private String status;
    
    public User(String email, String username) {
        this.email = email;
        this.username = username;
        this.status = "OFFLINE";
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    @Override
    public String toString() {
        return username + " (" + email + ") - " + status;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User user = (User) obj;
        return email.equals(user.email);
    }
    
    @Override
    public int hashCode() {
        return email.hashCode();
    }
}