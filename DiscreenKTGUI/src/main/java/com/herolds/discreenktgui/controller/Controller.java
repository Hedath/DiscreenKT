package com.herolds.discreenktgui.controller;

import com.herolds.discreenkt.api.DiscreenKTAPI;
import com.herolds.discreenkt.api.listener.DiscreenKTListener;
import com.herolds.discreenkt.api.listener.events.*;
import com.herolds.discreenktgui.config.ConfigProvider;
import com.herolds.discreenktgui.enums.SynchronizationInterval;
import com.herolds.discreenktgui.validators.PathValidator;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.DirectoryChooser;
import org.controlsfx.validation.ValidationSupport;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

public class Controller implements DiscreenKTListener {

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
    @FXML
    public ProgressBar progressBar;
    @FXML
    public Label progressLabel;
    @FXML
    public TitledPane activityPane;
    @FXML
    public TextArea progressTextArea;
    @FXML
    public TextFlow progressTextFlow;

    private final String configPath;



    private ConfigProvider configProvider;
    private final DiscreenKTAPI discreenKTAPI;

    private int maxMovieCount;


    public Controller() {
        this.configPath = getClass().getClassLoader().getResource("config.properties").getPath();

        try {
            ConfigProvider.initConfigProvider(this.configPath);
            configProvider = ConfigProvider.getInstance();
        } catch (IOException e) {
            // TODO: Show error message or something
            e.printStackTrace();
        }

        this.discreenKTAPI = new DiscreenKTAPI(this, configProvider.getConfigProperties());
    }

    @FXML
    public void initialize() {

        lastSyncLabel.setText("No synchronization.");

        syncIntervalCombo.setItems(FXCollections.observableArrayList(SynchronizationInterval.values()));

        cachePathTextField.setText(configProvider.getMovieCacheFolder());
        posterPathTextField.setText(configProvider.getPosterDownloadFolder());

        progressBar.setProgress(0);

        setupValidations();
        setupBindings();
    }

    @FXML
    public void selectCachePath(ActionEvent actionEvent) {
        this.selectPath(cachePathTextField, cachePathButton);
        configProvider.setMovieCacheFolder(cachePathTextField.getText());
    }

    @FXML
    public void selectPosterPath(ActionEvent actionEvent) {
        this.selectPath(posterPathTextField, posterPathButton);
        configProvider.setPosterDownloadFolder(posterPathTextField.getText());
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

    @FXML
    public void startDiscreenKT(ActionEvent actionEvent) {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                discreenKTAPI.startDownload(configProvider.getConfigProperties());
                return null;
            }
        };

        Thread th = new Thread(task);
        th.setDaemon(true);
        th.start();
    }

    private void selectPath(TextField textField, Button button) {
        DirectoryChooser directoryChooser = new DirectoryChooser();

        directoryChooser.setTitle("Choose cache destination folder!");
        File directory = directoryChooser.showDialog(button.getScene().getWindow());

        textField.setText(directory.getAbsolutePath());
    }

    private void setupValidations() {
        ValidationSupport support = new ValidationSupport();

        PathValidator pathValidator = new PathValidator();

        support.registerValidator(cachePathTextField, true, pathValidator);
        support.registerValidator(posterPathTextField, true, pathValidator);

        support.setErrorDecorationEnabled(true);
        support.redecorate();
    }

    private void setupBindings() {
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

    @Override
    public void onStart(StartEvent startEvent) {
        System.out.println("OnStart:" + startEvent.getNumberOfMovies());

        maxMovieCount = startEvent.getNumberOfMovies();

        Platform.runLater(() -> {
            progressBar.setProgress(0);
            progressLabel.setText("");

            progressTextFlow.getChildren().clear();
            appendToTextFlow("Started!\n", "-fx-fill: ORANGE;");
        });
    }

    @Override
    public void onPosterDownload(PosterDownloadEvent posterDownloadEvent) {
        System.out.println("OnPosterDownloadEvent:" + posterDownloadEvent.getMovieTitle());

        final double progress = progressBar.getProgress();

        double nextProgress = progress + 1.0 / maxMovieCount;

        if (nextProgress > 1.0) {
            nextProgress = 1.0;
        }

        System.out.println("Nextprog:" + nextProgress);
        progressBar.setProgress(nextProgress);

        double finalNextProgress = nextProgress;
        Platform.runLater(() -> {
            double percentage = finalNextProgress * 100;
            String progressText = new DecimalFormat("#.##").format(percentage);
            progressLabel.setText(String.valueOf(progressText + "%"));
            appendToTextFlow("Movie: " + posterDownloadEvent.getMovieTitle() + "..." + (posterDownloadEvent.isSuccess() ? "SUCCESS" : "FAILED") + "\n", posterDownloadEvent.isSuccess() ? "-fx-fill: GREEN;" : "-fx-fill: RED;");
        });

    }

    @Override
    public void onBatchFinished(BatchFinishedEvent batchFinishedEvent) {
        System.out.println("BatchFinishedEvent");
    }

    @Override
    public void onError(ErrorEvent errorEvent) {
        System.out.println("ErrorEvent");
    }

    @Override
    public void onFinish(FinishEvent finishEvent) {
        System.out.println("FinishEvent");
        Platform.runLater(() -> {
            progressBar.setProgress(1);
            progressLabel.setText("Finished!");

            appendToTextFlow("Finished!", "-fx-fill: ORANGE;");
        });
    }

    private void appendToTextFlow(String text, String style) {
        Text txt = new Text();
        txt.setText(text);
        txt.setStyle(style);
        progressTextFlow.getChildren().add(txt);
    }
}
