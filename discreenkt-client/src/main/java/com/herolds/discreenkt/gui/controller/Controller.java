package com.herolds.discreenkt.gui.controller;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.Optional;

import org.controlsfx.control.Notifications;
import org.controlsfx.validation.ValidationSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.herolds.discreenkt.api.DiscreenKTAPI;
import com.herolds.discreenkt.api.config.ConfigProvider;
import com.herolds.discreenkt.api.listener.DiscreenKTListener;
import com.herolds.discreenkt.api.listener.events.ErrorEvent;
import com.herolds.discreenkt.api.listener.events.FinishEvent;
import com.herolds.discreenkt.api.listener.events.PageParseEvent;
import com.herolds.discreenkt.api.listener.events.PosterDownloadEvent;
import com.herolds.discreenkt.api.listener.events.StartEvent;
import com.herolds.discreenkt.api.listener.events.StartPosterDownloadsEvent;
import com.herolds.discreenkt.api.service.exception.DiscreenKTException;
import com.herolds.discreenkt.gui.config.FxHelper;
import com.herolds.discreenkt.gui.enums.SynchronizationInterval;
import com.herolds.discreenkt.gui.validators.PathValidator;
import com.herolds.discreenkt.gui.validators.UserURLValidator;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class Controller implements DiscreenKTListener {

	private final Logger logger = LoggerFactory.getLogger(Controller.class);

	@FXML 
	public TextField userTextField;
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
	public ComboBox<SynchronizationInterval> syncIntervalCombo;
	@FXML
	public ProgressBar progressBar;
	@FXML
	public Label progressLabel;
	@FXML
	public TextArea progressTextArea;
	@FXML
	public TextFlow progressTextFlow;
	@FXML 
	public TitledPane synchronizationTitledPane;
	@FXML 
	public TitledPane activityTitledPane;
	@FXML 
	public TitledPane pathsTitledPane;
	@FXML 
	public Button stopButton;

	private final URI configPath;

	private final ConfigProvider configProvider;

	private final DiscreenKTAPI discreenKTAPI;

	private int maxMovieCount;

	private Stage stage;

	private FxHelper fxHelper;

	public Controller() throws URISyntaxException {
		this.configPath = Controller.class.getClassLoader().getResource("config.properties").toURI();
		this.fxHelper = new FxHelper();

		this.configProvider = ConfigProvider.initConfigProvider();

		this.discreenKTAPI = new DiscreenKTAPI(this, null);
	}

	@FXML
	public void initialize() {
		setLastSyncronization();

		syncIntervalCombo.setItems(FXCollections.observableArrayList(SynchronizationInterval.values()));

		userTextField.setText(configProvider.getUserUrl());
		cachePathTextField.setText(configProvider.getMovieCacheFolder());
		posterPathTextField.setText(configProvider.getPosterDownloadFolder());

		progressBar.setProgress(0);

		setupValidations();
		setupBindings();

		synchronizationTitledPane.heightProperty().addListener((obs, oldHeight, newHeight) -> stage.sizeToScene());
		activityTitledPane.heightProperty().addListener((obs, oldHeight, newHeight) -> stage.sizeToScene());
		pathsTitledPane.heightProperty().addListener((obs, oldHeight, newHeight) -> stage.sizeToScene());
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
		configProvider.setMovieCacheFolder(cachePathTextField.getText());
		configProvider.setPosterDownloadFolder(posterPathTextField.getText());
		configProvider.setUserUrl(userTextField.getText());

		try {
			configProvider.writeConfig(configPath);
		} catch (IOException e) {
			logger.error("Cannot save config: ", e);
			fxHelper.showExceptionDialog(e);
		}
	}

	@FXML
	public void undoConfig(ActionEvent actionEvent) {
		try {
			configProvider.loadConfig(configPath);
		} catch (IOException e) {
			logger.error("Cannot load config: ", e);
			fxHelper.showExceptionDialog(e);
		}

		cachePathTextField.setText(configProvider.getMovieCacheFolder());
		posterPathTextField.setText(configProvider.getPosterDownloadFolder());
		userTextField.setText(configProvider.getUserUrl());
	}

	@FXML
	public void startDiscreenKT(ActionEvent actionEvent) {
		Task<Void> task = new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				discreenKTAPI.startDownload();
				return null;
			}
		};

		Thread th = new Thread(task);
		th.setDaemon(true);
		th.start();
		
		// startButton.setDisable(true);
		stopButton.setDisable(false);
	}

	public void setup(Stage stage) {
		this.stage = stage;
	}

	private void selectPath(TextField textField, Button button) {
		DirectoryChooser directoryChooser = new DirectoryChooser();

		directoryChooser.setTitle("Choose cache destination folder!");
		File directory = directoryChooser.showDialog(button.getScene().getWindow());

		if (directory != null) {
			textField.setText(directory.getAbsolutePath());        	
		}
	}

	private void setupValidations() {
		ValidationSupport support = new ValidationSupport();

		PathValidator pathValidator = new PathValidator();

		support.registerValidator(cachePathTextField, true, pathValidator);
		support.registerValidator(posterPathTextField, true, pathValidator);

		support.registerValidator(userTextField, true, new UserURLValidator());

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

		UserURLValidator userURLValidator = new UserURLValidator();
		final BooleanBinding userTextFieldInvalid = Bindings.createBooleanBinding(() -> userURLValidator.validate(userTextField.getText()), userTextField.textProperty());
		final BooleanBinding userTextFieldUnmodified = Bindings.createBooleanBinding(()-> userTextField.getText().equals(configProvider.getUserUrl()), userTextField.textProperty());

		// The paths are unmodified, if both of them are unmodified
		final BooleanBinding unmodifiedPathsBooleanBinding = cachePathTextFieldUnmodified.and(posterPathTextFieldUnmodified).and(userTextFieldUnmodified);
		// The paths are invalid, if one of them is invalid
		final BooleanBinding invalidPathsBooleanBinding = cachePathTextFieldInvalid.or(posterPathTextFieldInvalid);

		// Save button is disabled when the paths are unmodified, or one of the paths is invalid.
		saveButton.disableProperty().bind(unmodifiedPathsBooleanBinding.or(invalidPathsBooleanBinding).or(userTextFieldInvalid));
		// Undo button is disabled when the paths are unmodified.
		undoButton.disableProperty().bind(unmodifiedPathsBooleanBinding);
		// Start button is disabled when one of the paths is invalid.
		startButton.disableProperty().bind(invalidPathsBooleanBinding.or(userTextFieldInvalid));

		// Reset button is disabled when one of the paths is invalid.
		// resetButton.disableProperty().bind(invalidPathsBooleanBinding);
	}

	@Override
	public void onStart(StartEvent startEvent) {
		Platform.runLater(() -> {
			progressBar.setProgress(0);
			progressLabel.setText("");

			progressTextFlow.getChildren().clear();
			appendToTextFlow("Started parsing KT pages!\n", fxHelper.fillColor("ORANGE"));
		});
	}

	@Override
	public void onPosterDownload(PosterDownloadEvent posterDownloadEvent) {
		logger.info("Downloading poster: {}", posterDownloadEvent.getMovieTitle());

		final double progress = progressBar.getProgress();

		double nextProgress = progress + 1.0 / maxMovieCount;

		final double finalNextProgress = nextProgress > 1.0 ? 1 : nextProgress;
		progressBar.setProgress(finalNextProgress);

		Platform.runLater(() -> {
			double percentage = nextProgress * 100;
			String progressText = new DecimalFormat("#.##").format(percentage);
			progressLabel.setText(String.valueOf(progressText + "%"));
			appendToTextFlow(posterDownloadEvent.getMovieTitle() + "..." + (posterDownloadEvent.isSuccess() ? "SUCCESS" : posterDownloadEvent.getMessage()) + "\n", posterDownloadEvent.isSuccess() ? fxHelper.fillColor("GREEN") : fxHelper.fillColor("RED"));
		});

	}

	@Override
	public void onError(ErrorEvent errorEvent) {
		logger.warn("Error during poster synchronization: {}", errorEvent.getMessage());
		fxHelper.showErrorDialog(errorEvent.getMessage());
	}

	@Override
	public void onFinish(FinishEvent finishEvent) {
		logger.info("Finished poster synchronization");

		Platform.runLater(() -> {
			progressBar.setProgress(1);
			progressLabel.setText("Finished!");

			appendToTextFlow("Finished!", fxHelper.fillColor("ORANGE"));

			setLastSyncronization();

			Notifications.create()
				.title("DiscreenKT")
				.text("Finished poster synchronization")
				.showInformation();

		});
		
		// startButton.setDisable(false);
		stopButton.setDisable(true);
	}

	private void appendToTextFlow(String text, String style) {
		Text txt = new Text();
		txt.setText(text);
		txt.setStyle(style);

		progressTextFlow.getChildren().add(txt);
	}

	private void setLastSyncronization() {
		DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
				.withLocale(Locale.getDefault())
				.withZone(ZoneId.systemDefault());

		Optional<Instant> lastSynchronization = discreenKTAPI.getLastSynchronization();

		if (lastSynchronization.isPresent()) {
			lastSyncLabel.setText(formatter.format(lastSynchronization.get()));
		} else {
			lastSyncLabel.setText("No synchronization.");
		}
	}

	@Override
	public void onPageParse(PageParseEvent event) {
		Platform.runLater(() -> {
			appendToTextFlow("Parsed page: " + event.getPageNumber() + "\n", fxHelper.fillColor("ORANGE"));
		});
	}

	@Override
	public void onStartPosterDownloads(StartPosterDownloadsEvent event) {
		logger.info("Starting downloads: {}", event.getNumberOfMovies());

		maxMovieCount = event.getNumberOfMovies();
		Platform.runLater(() -> {
			appendToTextFlow("Started poster downloads!\n", fxHelper.fillColor("ORANGE"));
		});
	}

	@FXML 
	public void stopDiscreenKT(ActionEvent event) throws DiscreenKTException {
		
	}
}
