package gumuc1;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javafx.application.Application;
import javafx.beans.property.Property;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import jfxtras.scene.control.LocalDatePicker;
/*
 * Main application class. It is responsible for loading exposition devices settings from json,
 * initialising devices, adding them to exposition class.
 * It also initialises schedule and loads its data.
 */
public class ExpositionControl extends Application {

	private static final Logger logger = Logger.getLogger(ExpositionControl.class);

	//ArrayList <Property<String>> ff = new ArrayList <Property<String>>();
	
	/*
	 * Exposition that is controlled by this application 
	 */
	private Exposition expo =  new Exposition();
	/*
	 * Controller for the exposition control UI
	 */
	//private ExpositionControlController controller = null;
	/*
	 * Sheduler for this exposition
	 */
	private ExpositionScheduler scheduler = null;
	/*
	 * Exposition scheduler controller
	 */
	//private ExpositionSchedulerController ctr = null;
	/*
	 * Settings file
	 */
	private File settings = null;
	/*
	 * Attender server 
	 */
	private AttendServer aServer = null;

	/*
	 * Default settings filename
	 */
	public final static String settingsFilename =  "settings.json";
	/*
	 * Views collection - inited at int(), loaded as start()
	 */
	private List<Node> views = new ArrayList<>();
	
	/*
	 * Views collection - inited at int(), loaded as start()
	 */
	private List<IPDeviceStateChangeController> controllers = new ArrayList<>();
	
	@Override
	public void init() {
		try {
			super.init();
			// Load settings
			// Get the location of settings jsonn and inits File
		    URL url = ExpositionControl.class.getProtectionDomain().getCodeSource().getLocation(); 
		  	String jarPath = null;
		  	jarPath = URLDecoder.decode(url.getFile(), "UTF-8"); 
		    String parentPath = new File(jarPath).getParentFile().getPath()+File.separator;
			if(!jarPath.contains("jar")) parentPath = parentPath + "bin\\gumuc1\\";
			settings = new File(parentPath + settingsFilename);
			if(!settings.exists()){
				// Loads defaults if file is not found
				logger.warn("no settings.json found at " +parentPath+settingsFilename);
				logger.warn("loading defaults - no UI available");
				expo.initDefaults();
			}
			else{
				// Get settings JSON
				ObjectMapper mapper = new ObjectMapper();
				JsonNode rootNode = mapper.readTree(settings);
				JsonNode settingsNode = rootNode.path("settings");
				PSShutdownExecutor.setPsshutdownEXEPath(settingsNode.path("psshutdownPath").asText());
				JsonNode expoJSON = rootNode.path("exposition");
				// Initing projecors
				JsonNode projectors = expoJSON.path("projectors");
				Iterator<JsonNode> projectorData = projectors.elements();
				while(projectorData.hasNext()){
					JsonNode projectorJSON = projectorData.next();
					Projector proj = new Projector(projectorJSON.get("ip").asText());
					expo.addProjector(proj);
					ProjectorView projView = new ProjectorView(projectorJSON.get("viewX").asInt(),
																projectorJSON.get("viewY").asInt(),
																projectorJSON.get("angle").asInt());
					IPDeviceStateChangeController controller = new IPDeviceStateChangeController(projView.powerStateFill(), 
																								projView.connectionStateFill());
					proj.addObserver(controller);
					views.add(projView);
				}
				// Initing brixes
				JsonNode brixes = expoJSON.path("brix");
				Iterator<JsonNode> brixData = brixes.elements();
				while(brixData.hasNext()){
					JsonNode brixJSON = brixData.next();
					Brix brix = new Brix(brixJSON.get("ip").asText(),brixJSON.get("mac").asText());
					expo.addBrix(brix);
					BrixView brixView = new BrixView(brixJSON.get("viewX").asInt(),
																brixJSON.get("viewY").asInt());
					IPDeviceStateChangeController controller = new IPDeviceStateChangeController(brixView.powerStateFill(), 
																								brixView.connectionStateFill());
					brix.addObserver(controller);
					views.add(brixView);
				}
				// Initing infokiosk
				JsonNode kiosk = expoJSON.path("infokiosk");
				Iterator<JsonNode> kioskData = kiosk.elements();
				while(kioskData.hasNext()){
					JsonNode kioskJSON = kioskData.next();
					Infokiosk info = new Infokiosk(kioskJSON.get("ip").asText(),kioskJSON.get("mac").asText());
					expo.addInfokiosk(info);
					InfokioskView kioskView = new InfokioskView(kioskJSON.get("viewX").asInt(),
															kioskJSON.get("viewY").asInt());
					IPDeviceStateChangeController controller = new IPDeviceStateChangeController(kioskView.powerStateFill(), 
																								 kioskView.connectionStateFill());
					info.addObserver(controller);
					controllers.add(controller);
					views.add(kioskView);
				}
				// Initing datatons
				JsonNode datatons = expoJSON.path("datatons");
				Iterator<JsonNode> datatonData = datatons.elements();
				while(datatonData.hasNext()){
					JsonNode datatonJSON = datatonData.next();
					Dataton dat;
					if(datatonJSON.has("master")){
						DatatonClusterMaster datcm = new DatatonClusterMaster(datatonJSON.get("ip").asText(),datatonJSON.get("mac").asText());
						expo.setDatatonClusterMaster(datcm);
						dat = datcm;
					}
					else
						dat = new Dataton(datatonJSON.get("ip").asText(),datatonJSON.get("mac").asText());
					expo.addDataton(dat);
					DatatonView datView = new DatatonView(datatonJSON.get("viewX").asInt(),
														datatonJSON.get("viewY").asInt());
					IPDeviceStateChangeController controller = new IPDeviceStateChangeController(datView.powerStateFill(), 
																							datView.connectionStateFill());
					dat.addObserver(controller);
					views.add(datView);
					
				}
				// Initing relays
				JsonNode relays = expoJSON.path("relays");
				Iterator<JsonNode> relayData = relays.elements();
				while(relayData.hasNext()){
					JsonNode relayJSON = relayData.next();
					Relay rel = new Relay(relayJSON.get("ip").asText());
					expo.addRelay(rel);
					RelayView relView = new RelayView(relayJSON.get("viewX").asInt(),
														relayJSON.get("viewY").asInt());
					RelayStateController ctr = new RelayStateController(relView.getRectUpFill(),relView.getRectDownFill(), 
																									relView.connectionStateFill());
					rel.addObserver(ctr);
					views.add(relView);
				}
			}
			// Initing scheduler
			scheduler = new ExpositionScheduler(expo);
			scheduler.setDefaultSchedule(LocalDate.now(), LocalDate.now().plusMonths(12));
			// Initing attender server
			try{
				aServer = new AttendServer(expo); 
			}
			catch(IOException e){
				logger.warn("Attender server could not be started");
				e.printStackTrace();
			}
		} catch (UnknownHostException e) {
			logger.warn(settingsFilename + " contains wrong ip or mac addresses");
			e.printStackTrace();
		}
		  catch (JsonProcessingException e){
			logger.warn(settingsFilename + " processing error");
			logger.warn("loading defaults - no UI available");
			expo.initDefaults();
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			logger.warn("no "+ PSShutdownExecutor.getPsshutdownexe() + " found");
			logger.warn("brix power-off will not work");
			e.printStackTrace();
		} catch (IOException e) {
			logger.warn(settingsFilename + " reading error");
			logger.warn("loading defaults - no UI available");
			expo.initDefaults();
			e.printStackTrace();
		} 
		catch(Exception e){
			logger.warn("Init exception");
			e.printStackTrace();
		}
	}
	
	
	@Override
	public void start(Stage primaryStage) {
		try {
			// Load expositionControl 
			FXMLLoader loader = new FXMLLoader(getClass().getResource("ExpositionControl.fxml"));
			Scene scene = new Scene(loader.load());
			ExpositionControlController controller = loader.<ExpositionControlController>getController();
			// Here injection is too much - init controller with exposition directly
		    controller.setExposition(expo);
			scene.getStylesheets().add(getClass().getResource("expo.css").toExternalForm());
			primaryStage.setScene(scene);
			// Constructing map view and adding all views constructed at init()
			Pane mapPane = (Pane) scene.lookup("#pnMap");
			ExpositionView expoview= new ExpositionView();
			mapPane.getChildren().addAll(expoview.rooms);
			mapPane.getChildren().addAll(views);
			((Button) mapPane.lookup("#btnRefresh")).toFront();

			// for debug
			mapPane.setOnMouseClicked(new EventHandler<MouseEvent>(){
				@Override
				public void handle(MouseEvent arg0) {
					System.out.println(arg0.getX()+" "+arg0.getY());
				}
			});
			primaryStage.show();
			// A little hack here
			// I really didn't want to write my own calendar picker that is why I used 
			// the one from fxtras. The problem is that picker in fxtras did not allow to add text to date buttons
			// so ExpositionSchedulerController is a hack that does the thing
			ExpositionSchedulerController ctr = new ExpositionSchedulerController(
									 (GridPane) scene.lookup("#timeSelectPane"),
									 (LocalDatePicker)scene.lookup("#picker"),
									 scheduler,
									 settings);
			ctr.updateExpositionScheduler();
			ctr.updateExpositionSchedulerView();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		Application.launch(args);
	}
}
