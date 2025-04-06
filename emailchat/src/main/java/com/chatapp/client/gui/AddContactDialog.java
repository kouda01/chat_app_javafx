package com.chatapp.client.gui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddContactDialog {
    @FXML private TextField emailTextField;
    @FXML private Label errorLabel;
    @FXML private Button cancelButton;
    @FXML private Button addButton;
    
    private Stage dialogStage;
    private boolean addClicked = false;
    private String contactEmail;

    /**
     * Initializes the controller class.
     */
    @FXML
    private void initialize() {
        errorLabel.setText("");
    }

    /**
     * Sets the stage of this dialog.
     * @param dialogStage
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Returns true if the user clicked Add, false otherwise.
     * @return
     */
    public boolean isAddClicked() {
        return addClicked;
    }

    /**
     * Returns the email entered by the user.
     * @return
     */
    public String getContactEmail() {
        return contactEmail;
    }

    /**
     * Called when the user clicks the Add button.
     */
    @FXML
    private void handleAddContact() {
        if (isInputValid()) {
            contactEmail = emailTextField.getText().trim();
            addClicked = true;
            dialogStage.close();
        }
    }

    /**
     * Called when the user clicks the Cancel button.
     */
    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    /**
     * Validates the user input in the text field.
     * @return true if the input is valid
     */
    private boolean isInputValid() {
        String errorMessage = "";
        String email = emailTextField.getText().trim();

        if (email == null || email.isEmpty()) {
            errorMessage = "Please enter an email address";
        } else if (!email.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            errorMessage = "Please enter a valid email address";
        }

        if (errorMessage.isEmpty()) {
            return true;
        } else {
            errorLabel.setText(errorMessage);
            return false;
        }
    }
}