package com.herolds.discreenktgui.App;

import com.herolds.discreenktgui.validators.PathValidator;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import org.controlsfx.validation.ValidationSupport;
import java.io.File;

public class Controller {

    @FXML
    public Button cachePathButton;
    @FXML
    public TextField cachePathTextField;
    @FXML
    public Button posterPathButton;
    @FXML
    public TextField posterPathTextField;
    @FXML
    public Button saveButton;
    @FXML
    public Button startButton;
    @FXML
    public Button resetButton;


    @FXML
    public void initialize() {
        setupValidations();
    }

    @FXML
    public void selectCachePath(ActionEvent actionEvent) {
        this.selectPath(cachePathTextField, cachePathButton);
    }

    @FXML
    public void selectPosterPath(ActionEvent actionEvent) {
        this.selectPath(posterPathTextField, posterPathButton);
    }

    @FXML
    public void saveConfig(ActionEvent actionEvent) {

    }

    @FXML
    public void undoConfig(ActionEvent actionEvent) {

    }

    protected void selectPath(TextField textField, Button button) {
        DirectoryChooser directoryChooser = new DirectoryChooser();

        directoryChooser.setTitle("Choose cache destination folder!");
        File directory = directoryChooser.showDialog(button.getScene().getWindow());

        textField.textProperty().setValue(directory.getAbsolutePath());
    }

    protected void setupValidations() {
        ValidationSupport support = new ValidationSupport();

        PathValidator pathValidator = new PathValidator();

        BooleanBinding cachePathTextFieldValid = Bindings.createBooleanBinding(() -> pathValidator.validate(cachePathTextField.getText()), cachePathTextField.textProperty());
        BooleanBinding posterPathTextFieldValid = Bindings.createBooleanBinding(() -> pathValidator.validate(posterPathTextField.getText()), posterPathTextField.textProperty());

        final BooleanBinding validPathsBooleanBinding = cachePathTextFieldValid.or(posterPathTextFieldValid);

        saveButton.disableProperty().bind(validPathsBooleanBinding);
        startButton.disableProperty().bind(validPathsBooleanBinding);
        resetButton.disableProperty().bind(validPathsBooleanBinding);

        support.registerValidator(cachePathTextField, true, pathValidator);
        support.registerValidator(posterPathTextField, true, pathValidator);
    }
}
