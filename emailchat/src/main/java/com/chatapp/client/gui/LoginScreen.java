package com.chatapp.client.gui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;

public class LoginScreen {

    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label loginErrorLabel;
    @FXML
    private TextField serverAddressField;

    @FXML
    private TextField regEmailField;
    @FXML
    private TextField regUsernameField;
    @FXML
    private PasswordField regPasswordField;
    @FXML
    private PasswordField regConfirmPasswordField;
    @FXML
    private Label registerErrorLabel;

    /**
     * Gère l'action du bouton "Login"
     */
    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();
        String serverAddress = serverAddressField.getText().trim();

        if (email.isEmpty() || password.isEmpty() || serverAddress.isEmpty()) {
            loginErrorLabel.setTextFill(Color.RED);
            loginErrorLabel.setText("Veuillez remplir tous les champs !");
            return;
        }

        // Simuler une connexion réussie (intégration serveur plus tard)
        if (email.equals("test@example.com") && password.equals("password")) {
            loginErrorLabel.setTextFill(Color.GREEN);
            loginErrorLabel.setText("Connexion réussie !");
            System.out.println("Connexion au serveur : " + serverAddress);
        } else {
            loginErrorLabel.setTextFill(Color.RED);
            loginErrorLabel.setText("Email ou mot de passe incorrect !");
        }
    }

    /**
     * Gère l'action du bouton "Register"
     */
    @FXML
    private void handleRegister() {
        String regEmail = regEmailField.getText().trim();
        String regUsername = regUsernameField.getText().trim();
        String regPassword = regPasswordField.getText();
        String regConfirmPassword = regConfirmPasswordField.getText();

        if (regEmail.isEmpty() || regUsername.isEmpty() || regPassword.isEmpty() || regConfirmPassword.isEmpty()) {
            registerErrorLabel.setTextFill(Color.RED);
            registerErrorLabel.setText("Veuillez remplir tous les champs !");
            return;
        }

        if (!regPassword.equals(regConfirmPassword)) {
            registerErrorLabel.setTextFill(Color.RED);
            registerErrorLabel.setText("Les mots de passe ne correspondent pas !");
            return;
        }

        // Simuler un enregistrement réussi (intégration BDD plus tard)
        registerErrorLabel.setTextFill(Color.GREEN);
        registerErrorLabel.setText("Inscription réussie !");
        System.out.println("Nouvel utilisateur enregistré : " + regUsername);
    }
}
