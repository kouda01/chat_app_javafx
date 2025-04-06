package com.chatapp.common;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int id;
    private String senderEmail;
    private String receiverEmail;
    private String content;
    private long timestamp;
    private String mediaPath;
    private MessageType type;
    
    public enum MessageType {
        TEXT, MEDIA, NOTIFICATION, LOGIN, LOGOUT, REGISTER, 
        CONTACT_LIST, ADD_CONTACT, USER_STATUS
    }
    
    public Message(String senderEmail, String receiverEmail, String content) {
        this.senderEmail = senderEmail;
        this.receiverEmail = receiverEmail;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
        this.type = MessageType.TEXT;
    }
    
    public Message(MessageType type) {
        this.type = type;
        this.timestamp = System.currentTimeMillis();
    }
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getSenderEmail() {
        return senderEmail;
    }
    
    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }
    
    public String getReceiverEmail() {
        return receiverEmail;
    }
    
    public void setReceiverEmail(String receiverEmail) {
        this.receiverEmail = receiverEmail;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getMediaPath() {
        return mediaPath;
    }
    
    public void setMediaPath(String mediaPath) {
        this.mediaPath = mediaPath;
        if (mediaPath != null && !mediaPath.isEmpty()) {
            this.type = MessageType.MEDIA;
        }
    }
    
    public MessageType getType() {
        return type;
    }
    
    public void setType(MessageType type) {
        this.type = type;
    }
    
    public String getFormattedTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(new Date(timestamp));
    }
    
    @Override
    public String toString() {
        return "[" + getFormattedTime() + "] " + senderEmail + ": " + content;
    }
}