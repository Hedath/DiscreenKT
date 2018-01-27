package com.herolds.discreenktgui.controller;

import com.herolds.discreenktgui.config.ConfigProvider;
import com.herolds.discreenktgui.enums.SynchronizationInterval;
import com.herolds.discreenktgui.validators.PathValidator;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import org.controlsfx.control.Notifications;
import org.controlsfx.validation.ValidationSupport;
import java.io.File;
import java.io.IOException;

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
    public Button undoButton;
    @FXML
    public Label lastSyncLabel;
    @FXML
    public ComboBox syncIntervalCombo;


    private final String configPath;

    private ConfigProvider configProvider;

    public Controller() {
        this.configPath = getClass().getClassLoader().getResource("config.properties").getPath();
    }

    @FXML
    public void initialize() {

        try {
            ConfigProvider.initConfigProvider(this.configPath);
            configProvider = ConfigProvider.getInstance();
        } catch (IOException e) {
            // TODO: Show error message or something
            e.printStackTrace();
        }

        lastSyncLabel.setText("No synchronization.");

        syncIntervalCombo.setItems(FXCollections.observableArrayList(SynchronizationInterval.values()));

        cachePathTextField.setText(configProvider.getMovieCacheFolder());
        posterPathTextField.setText(configProvider.getPosterDownloadFolder());

        setupValidations();
        setupBindings();
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
        try {
            configProvider.writeConfig(configPath);
        } catch (IOException e) {
            // TODO: Show error message or something
            e.printStackTrace();
        }

    }

    @FXML
    public void undoConfig(ActionEvent actionEvent) {
        try {
            configProvider.loadConfig(configPath);
        } catch (IOException e) {
            // TODO: Show error message or something
            e.printStackTrace();
        }

        cachePathTextField.setText(configProvider.getMovieCacheFolder());
        posterPathTextField.setText(configProvider.getPosterDownloadFolder());
    }

    protected void selectPath(TextField textField, Button button) {
        DirectoryChooser directoryChooser = new DirectoryChooser();

        directoryChooser.setTitle("Choose cache destination folder!");
        File directory = directoryChooser.showDialog(button.getScene().getWindow());

        textField.setText(directory.getAbsolutePath());
    }

    protected void setupValidations() {
        ValidationSupport support = new ValidationSupport();

        PathValidator pathValidator = new PathValidator();

        support.registerValidator(cachePathTextField, true, pathValidator);
        support.registerValidator(posterPathTextField, true, pathValidator);

        support.setErrorDecorationEnabled(true);
        support.redecorate();
    }

    protected void setupBindings() {
        PathValidator pathValidator = new PathValidator();


        // Binding to the cache path text property to check whether it's an invalid (non existent) path
        BooleanBinding cachePathTextFieldInvalid = Bindings.createBooleanBinding(() -> pathValidator.validate(cachePathTextField.getText()), cachePathTextField.textProperty());
        // Binding to the poster path text property to check whether it's an invalid (non existent) path
        BooleanBinding posterPathTextFieldInvalid = Bindings.createBooleanBinding(() -> pathValidator.validate(posterPathTextField.getText()), posterPathTextField.textProperty());
        // Binding to the cache path text property to check whether it's been unmodified
        BooleanBinding cachePathTextFieldUnmodified = Bindings.createBooleanBinding(()-> cachePathTextField.getText().equals(configProvider.getMovieCacheFolder()), cachePathTextField.textProperty());
        // Binding to the poster path text property to check whether it's been unmodified
        BooleanBinding posterPathTextFieldUnmodified = Bindings.createBooleanBinding(()-> posterPathTextField.getText().equals(configProvider.getPosterDownloadFolder()), posterPathTextField.textProperty());

        // The paths are unmodified, if both of them are unmodified
        final BooleanBinding unmodifiedPathsBooleanBinding = cachePathTextFieldUnmodified.and(posterPathTextFieldUnmodified);
        // The paths are invalid, if one of them is invalid
        final BooleanBinding invalidPathsBooleanBinding = cachePathTextFieldInvalid.or(posterPathTextFieldInvalid);

        // Save button is disabled when the paths are unmodified, or one of the paths is invalid.
        saveButton.disableProperty().bind(unmodifiedPathsBooleanBinding.or(invalidPathsBooleanBinding));
        // Undo button is disabled when the paths are unmodified.
        undoButton.disableProperty().bind(unmodifiedPathsBooleanBinding);
        // Start button is disabled when one of the paths is invalid.
        startButton.disableProperty().bind(invalidPathsBooleanBinding);
        // Reset button is disabled when one of the paths is invalid.
        resetButton.disableProperty().bind(invalidPathsBooleanBinding);
    }
}
