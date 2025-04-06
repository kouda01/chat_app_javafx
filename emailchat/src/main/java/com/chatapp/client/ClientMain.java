package com.chatapp.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ClientMain extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        try {
            System.out.println("Starting application in start method");
            
            // Print the location we're trying to load from
            System.out.println("Attempting to load FXML from: " + 
                getClass().getResource("/fxml/LoginScreen.fxml"));
            
            // Load the login screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginScreen.fxml"));
            System.out.println("FXMLLoader created");
            
            Parent root = loader.load();
            System.out.println("FXML loaded successfully");
            
            primaryStage.setTitle("Email Chat Application");
            primaryStage.setScene(new Scene(root, 400, 500));
            primaryStage.setMinWidth(400);
            primaryStage.setMinHeight(500);
            primaryStage.show();
            System.out.println("Window displayed");
        } catch (Exception e) {
            System.out.println("Exception caught: " + e.getClass().getName());
            System.out.println("Message: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public void stop() {
        // Clean up resources when application closes
        System.out.println("Application stopping");
        System.exit(0);
    }

    public static void main(String[] args) {
        System.out.println("In main method before launch");
        launch(args);
        System.out.println("In main method after launch (shouldn't reach here normally)");
    }
}