package com.herolds.discreenkt.gui;

import java.awt.TrayIcon;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.inject.Inject;

import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.herolds.discreenkt.api.service.DiscreenKTCache;
import com.herolds.discreenkt.gui.controller.Controller;
import com.herolds.discreenkt.gui.scheduler.DownloadPostersScheduler;

import de.codecentric.centerdevice.MenuToolkit;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Menu;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * For the tray icon solution, see: https://gist.github.com/jewelsea/e231e89e8d36ef4e5d8a
 */
public class Main extends Application {
	
	private final Logger logger = LoggerFactory.getLogger(Main.class);

	public static DiscreenKTComponent injector;
	
	@Inject
	protected DownloadPostersScheduler scheduler;
	
	@Inject
	protected DiscreenKTCache discreenKTCache;
	
    // one icon location is shared between the application tray icon and task bar icon.
    // you could also use multiple icons to allow for clean display of tray icons on hi-dpi devices.
    private static final URL iconImageLoc = Main.class.getClassLoader().getResource("gui/icon.png");

    // application stage is stored so that it can be shown and hidden based on system tray icon operations.
    private Stage stage;

    private TrayIcon trayIcon;

    // sets up the javafx application.
    // a tray icon is setup for the icon, but the main stage remains invisible until the user
    // interacts with the tray icon.
    @Override 
    public void start(final Stage stage) throws IOException, SchedulerException {
    	Main.injector.inject(this);
    	
    	// stores a reference to the stage.
        this.stage = stage;
        
        createMenu();
        
        Thread.currentThread().setUncaughtExceptionHandler((thread, throwable) -> {
            logger.error("Unexpected exception: ", throwable);
            
        	Alert alert = new Alert(AlertType.ERROR);
    		alert.setTitle("Error happened!");
    		alert.setHeaderText("Look, an Error Dialog");
    		alert.setContentText("Ooops, there was an error!");

    		alert.showAndWait();
        });

        // instructs the javafx system not to exit implicitly when the last application window is shut.
        Platform.setImplicitExit(false);

        // sets up the tray icon (using awt code run on the swing thread).
        javax.swing.SwingUtilities.invokeLater(this::addAppToTray);

        // out stage will be translucent, so give it a transparent style.

        //stage.initStyle(StageStyle.TRANSPARENT);

        // create the layout for the javafx stage.
        BorderPane layout = new BorderPane(createContent());

        layout.setStyle("-fx-background-color: rgba(255, 255, 255, 1);");

        layout.setPrefSize(370, 100);


        // this dummy app just hides itself when the app screen is clicked.
        // a real app might have some interactive UI and a separate icon which hides the app window.
        // layout.setOnMouseClicked(event -> stage.hide());

        // a scene with a transparent fill is necessary to implement the translucent app window.
        Scene scene = new Scene(layout);
        //scene.setFill(Color.TRANSPARENT);

        stage.setResizable(false);
        stage.setScene(scene);
    }

	private void createMenu() {
		// Get the toolkit
        MenuToolkit tk = MenuToolkit.toolkit();
        
        if (tk != null) {
        	// Create the default Application menu
        	Menu defaultApplicationMenu = tk.createDefaultApplicationMenu("DiscreenKT");
        	
        	// Update the existing Application menu
        	tk.setApplicationMenu(defaultApplicationMenu);
        	
        	defaultApplicationMenu.getItems().forEach(menuItem -> {
        		if (menuItem.getText() != null && menuItem.getText().toUpperCase().contains("QUIT")) {
        			menuItem.setOnAction(action -> {
        				Platform.exit();
        				discreenKTCache.close();
        				System.exit(0);
        			});
        		}
        	});
        }
	}
    
    @Override
    public void stop() throws Exception {
    	super.stop();
		scheduler.close();    		
    }

    /**
     * For this dummy app, the (JavaFX scenegraph) content, just says "hello, world".
     * A real app, might load an FXML or something like that.
     *
     * @return the main window application content.
     */
    private Node createContent() {

        Parent root = null;

        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            InputStream inputStream = getClass().getClassLoader().getResource("gui/DiscreenKTGUI.fxml").openStream();
            root = fxmlLoader.load(inputStream);
            Controller controller = fxmlLoader.getController();
            
            controller.setup(stage);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Scene scene = new Scene(root, 500, 500);
        
        stage.setTitle("DiscreenKT");
        stage.setScene(scene);
        stage.setResizable(true);
        
        stage.show();

        return root;
    }

    /**
     * Sets up a system tray icon for the application.
     */
    private void addAppToTray() {
        try {
            // ensure awt toolkit is initialized.
            java.awt.Toolkit.getDefaultToolkit();

            // app requires system tray support, just exit if there is no support.
            if (!java.awt.SystemTray.isSupported()) {
            	logger.error("No system tray support, application exiting.");
                Platform.exit();
            }

            // set up a system tray icon.
            java.awt.SystemTray tray = java.awt.SystemTray.getSystemTray();
            java.awt.Image image = ImageIO.read(iconImageLoc);
            trayIcon = new java.awt.TrayIcon(image);

            // if the user double-clicks on the tray icon, show the main app stage.
            trayIcon.addActionListener(event -> Platform.runLater(this::showStage));
            // if the user selects the default menu item (which includes the app name),
            // show the main app stage.
            java.awt.MenuItem openItem = new java.awt.MenuItem("Settings");
            openItem.addActionListener(event -> Platform.runLater(this::showStage));

            // the convention for tray icons seems to be to set the default icon for opening
            // the application stage in a bold font.
            java.awt.Font defaultFont = java.awt.Font.decode(null);
            java.awt.Font boldFont = defaultFont.deriveFont(java.awt.Font.BOLD);
            openItem.setFont(boldFont);

            // to really exit the application, the user must go to the system tray icon
            // and select the exit option, this will shutdown JavaFX and remove the
            // tray icon (removing the tray icon will also shut down AWT).
            java.awt.MenuItem exitItem = new java.awt.MenuItem("Exit");
            exitItem.addActionListener(event -> {
                Platform.exit();
                tray.remove(trayIcon);
                discreenKTCache.close();
                System.exit(0);
            });

            // setup the popup menu for the application.
            final java.awt.PopupMenu popup = new java.awt.PopupMenu();
            popup.add(openItem);
            popup.addSeparator();
            popup.add(exitItem);
            trayIcon.setPopupMenu(popup);

            // add the application tray icon to the system tray.
            tray.add(trayIcon);

        } catch (java.awt.AWTException | IOException e) {
        	logger.error("Unable to init system tray", e);
        }
    }

    /**
     * Shows the application stage and ensures that it is brought ot the front of all stages.
     */
    private void showStage() {
        if (stage != null) {        	
        	stage.setOnShowing((event) -> {
        		stage.setX(stage.getX()); 
        		stage.setY(stage.getY());	
        	});
        	
            stage.show();
            stage.toFront();
        }
    }

    public static void main(String[] args) throws IOException, java.awt.AWTException {
    	injector = DaggerDiscreenKTComponent.builder().build();
    	// Just launches the JavaFX application.
        // Due to way the application is coded, the application will remain running
        // until the user selects the Exit menu option from the tray icon.
        launch(args);
    }
}
