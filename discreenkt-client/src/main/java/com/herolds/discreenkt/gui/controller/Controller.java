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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

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
import com.herolds.discreenkt.gui.validators.TimeValidator;
import com.herolds.discreenkt.gui.validators.UserURLValidator;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ChangeListener;
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
	@FXML 
	public TextField timeTextField;

	private final URI configPath;

	private final ConfigProvider configProvider;

	private final DiscreenKTAPI discreenKTAPI;

	private int maxMovieCount;

	private Stage stage;

	private FxHelper fxHelper;

	private List<BooleanBinding> unmodifiedBindings;

	public Controller() throws URISyntaxException {
		this.configPath = Controller.class.getClassLoader().getResource("config.properties").toURI();
		this.unmodifiedBindings = new ArrayList<>();
		
		this.fxHelper = new FxHelper();
		this.configProvider = ConfigProvider.initConfigProvider();		
		this.discreenKTAPI = new DiscreenKTAPI(this);
	}

	@FXML
	public void initialize() {
		setLastSyncronization();

		syncIntervalCombo.setItems(FXCollections.observableArrayList(SynchronizationInterval.values()));
		
		userTextField.setText(configProvider.getUserUrl())	;
		posterPathTextField.setText(configProvider.getPosterDownloadFolder());
		
		setSyncInternalCombo();
		
		timeTextField.setText(configProvider.getSyncTime());

		progressBar.setProgress(0);

		setupValidations();
		setupBindings();
	}

	@FXML
	public void selectPosterPath(ActionEvent actionEvent) {
		this.selectPath(posterPathTextField, posterPathButton);
		configProvider.setPosterDownloadFolder(posterPathTextField.getText());
	}

	@FXML
	public void saveConfig(ActionEvent actionEvent) {
		if (syncIntervalCombo.getValue() != null) {
			configProvider.setSyncInterval(syncIntervalCombo.getValue().name());			
		}
		configProvider.setSyncTime(timeTextField.getText());
		configProvider.setPosterDownloadFolder(posterPathTextField.getText());
		configProvider.setUserUrl(userTextField.getText());		

		try {
			configProvider.writeConfig(configPath);
			
			// Re-validate unmodified bindings 
			// Otherwise "save" and "undo" button will remain enabled, 
			// although the "form" is not dirty anymore...
			unmodifiedBindings.forEach(BooleanBinding::invalidate);
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

		posterPathTextField.setText(configProvider.getPosterDownloadFolder());
		userTextField.setText(configProvider.getUserUrl());
		timeTextField.setText(configProvider.getSyncTime());
		setSyncInternalCombo();
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
		
		this.stage.focusedProperty().addListener((ov, onHidden, onShown) -> setLastSyncronization());
		
		ChangeListener<? super Number> resizeScene = (obs, oldHeight, newHeight) -> this.stage.sizeToScene();
		
		synchronizationTitledPane.heightProperty().addListener(resizeScene);
		activityTitledPane.heightProperty().addListener(resizeScene);
		pathsTitledPane.heightProperty().addListener(resizeScene);
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

		support.registerValidator(posterPathTextField, true, new PathValidator());
		support.registerValidator(userTextField, true, new UserURLValidator());
		support.registerValidator(timeTextField, true, new TimeValidator());

		support.setErrorDecorationEnabled(true);
		support.redecorate();
	}

	private void setupBindings() {		
		final BooleanBinding posterPathInvalid = fxHelper.createValidationBinding(new PathValidator(), posterPathTextField.textProperty());
		final BooleanBinding posterPathUnmodified = fxHelper.createEqualsBinding(configProvider.getPosterDownloadFolder(), posterPathTextField.textProperty());
		
		final BooleanBinding userInvalid = fxHelper.createValidationBinding(new UserURLValidator(), userTextField.textProperty());
		final BooleanBinding userUnmodified = fxHelper.createEqualsBinding(configProvider.getUserUrl(), userTextField.textProperty());
		
		final BooleanBinding timeInvalid = fxHelper.createValidationBinding(new TimeValidator(), timeTextField.textProperty());
		final BooleanBinding timeUnmodified = fxHelper.createEqualsBinding(configProvider.getSyncTime(), timeTextField.textProperty());
		
		final BooleanBinding syncComboUnmodified = Bindings.createBooleanBinding(()-> syncIntervalCombo.getValue().name() != null && syncIntervalCombo.getValue().name().equals(configProvider.getSyncInterval()), syncIntervalCombo.valueProperty());		

		final BooleanBinding unmodifiedProperties = posterPathUnmodified
				.and(userUnmodified)
				.and(timeUnmodified)
				.and(syncComboUnmodified);
		
		saveButton.disableProperty().bind(unmodifiedProperties.or(posterPathInvalid)
				.or(userInvalid)
				.or(timeInvalid));
		undoButton.disableProperty().bind(unmodifiedProperties);
		startButton.disableProperty().bind(posterPathInvalid.or(userInvalid));

		unmodifiedBindings.addAll(Arrays.asList(posterPathUnmodified, userUnmodified, timeUnmodified, syncComboUnmodified, unmodifiedProperties));
	}

	@Override
	public void onStart(StartEvent startEvent) {
		Platform.runLater(() -> {
			progressBar.setProgress(0);
			progressLabel.setText("");

			progressTextFlow.getChildren().clear();
			appendToTextFlow("Started parsing KT pages!", fxHelper.fillColor("ORANGE"));
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
			progressLabel.setText(progressText + "%");
			appendToTextFlow(String.format("%s...%s", 
					posterDownloadEvent.getMovieTitle(), (posterDownloadEvent.isSuccess() ? "SUCCESS" : posterDownloadEvent.getMessage())), 
					posterDownloadEvent.isSuccess() ? fxHelper.fillColor("GREEN") : fxHelper.fillColor("RED"));
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
		});
		
		stopButton.setDisable(true);
	}

	private void appendToTextFlow(String text, String style) {
		Text txt = new Text();
		txt.setText(text + "\n");
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
			lastSyncLabel.setText("-");
		}
	}

	@Override
	public void onPageParse(PageParseEvent event) {
		Platform.runLater(() -> {
			appendToTextFlow("Parsed page: " + event.getPageNumber(), fxHelper.fillColor("ORANGE"));
		});
	}

	@Override
	public void onStartPosterDownloads(StartPosterDownloadsEvent event) {
		logger.info("Starting downloads: {}", event.getNumberOfMovies());

		maxMovieCount = event.getNumberOfMovies();
		Platform.runLater(() -> {
			appendToTextFlow("Started poster downloads!", fxHelper.fillColor("ORANGE"));
		});
	}

	@FXML 
	public void stopDiscreenKT(ActionEvent event) throws DiscreenKTException {

	}
	
	private void setSyncInternalCombo() {
		Optional<SynchronizationInterval> enumValue = SynchronizationInterval.getEnumValue(configProvider.getSyncInterval());		
		if (enumValue.isPresent()) {
			syncIntervalCombo.setValue(enumValue.get());				
		}
	}
}
