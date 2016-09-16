package gumuc1;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import jfxtras.scene.control.LocalDatePicker;

public class ExpositionControlController implements Initializable{

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button btnExpoOn;

    @FXML
    private Button btnExpoOff;

    @FXML
    private Button btnProjOn;

    @FXML
    private Button btnProjOff;

    @FXML
    private Button btnDatatonOn;

    @FXML
    private Button btnDatatonOff;

    @FXML
    private Button btnDatatonRestart;

    @FXML
    private Button btnDatatonRun;

    @FXML
    private Button btnBrixOn;

    @FXML
    private Button btnBrixOff;

    @FXML
    private Button btnBrixRestart;
    
    @FXML
    private Button btnInfokioskOn;

    @FXML
    private Button btnInfokioskOff; 
    
    @FXML
    private Button btnInfokioskRestart; 
       
    @FXML
    private Button btnRelayUpOn;

    @FXML
    private Button btnRelayUpOff;

    @FXML
    private Button btnRelayDownOn;

    @FXML
    private Button btnRelayDownOff;

    @FXML
    private TextField txtTurnOnHours;

    @FXML
    private TextField txtTurnOnMinutes;

    @FXML
    private TextField txtTurnOffHours;

    @FXML
    private TextField txtTurnOffMinutes;

    @FXML
    private TextField txtTillTurnOnDays;

    @FXML
    private TextField txtTillTurnOffDays;

    @FXML
    private TextField txtTillTurnOnHours;

    @FXML
    private TextField txtTillTurnOnMinutes;

    @FXML
    private TextField txtTillTurnOffHours;

    @FXML
    private TextField txtTillTurnOffMinutes;

    @FXML
    private Button btnSaveChanges;

    @FXML
    private Button btnReset;
    
    @FXML
    private LocalDatePicker picker;

    @FXML
    private Pane pnMap;
    
    @FXML
    private Button btnRefresh;
    
    @FXML
    private GridPane timeSelectPane;
    
    public void setExposition(Exposition expo){
    	btnExpoOn.setOnAction(event->expo.powerAllUp());
    	btnExpoOff.setOnAction(event->expo.powerAllDown());
    	
    	btnProjOff.setOnAction(event->expo.powerDownProjectors());
    	btnProjOn.setOnAction(event->expo.powerUpProjectors());
   
    	btnDatatonOn.setOnAction(event->expo.powerUpAndRunDatatons());
    	btnDatatonOff.setOnAction(event->expo.powerDownDatatons());
    	btnDatatonRestart.setOnAction(event->expo.loadAndRunDatatons());

    	btnBrixOn.setOnAction(event->expo.powerUpBrixes());
    	btnBrixOff.setOnAction(event->expo.powerDownBrixes());
    	btnBrixRestart.setOnAction(event->expo.restartBrixes());
    	
    	btnInfokioskOn.setOnAction(event->expo.powerUpInfokiosks());
    	btnInfokioskOff.setOnAction(event->expo.powerDownInfokiosks());
    	//btnInfokioskRestart.setOnAction(event->expo.restartBrixes());
    	
    	btnRelayUpOn.setOnAction(event->expo.controlRelays(2, true));
    	btnRelayUpOff.setOnAction(event->expo.controlRelays(2, false));

    	btnRelayDownOn.setOnAction(event->expo.controlRelays(1, true));
    	btnRelayDownOff.setOnAction(event->expo.controlRelays(1, false));
    	
    	btnRefresh.setOnAction(event->expo.refresh());
    	btnRefresh.toFront();
    	
    	btnDatatonRun.setOnAction(event->expo.loadAndRunDatatons());
    	

    }
    
    
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
        assert btnExpoOn != null : "fx:id=\"btnExpoOn\" was not injected: check your FXML file 'int.fxml'.";
        assert btnExpoOff != null : "fx:id=\"btnExpoOff\" was not injected: check your FXML file 'int.fxml'.";
        assert btnProjOn != null : "fx:id=\"btnProjOn\" was not injected: check your FXML file 'int.fxml'.";
        assert btnProjOff != null : "fx:id=\"btnProjOff\" was not injected: check your FXML file 'int.fxml'.";
        assert btnDatatonOn != null : "fx:id=\"btnDatatonOn\" was not injected: check your FXML file 'int.fxml'.";
        assert btnDatatonOff != null : "fx:id=\"btnDatatonOff\" was not injected: check your FXML file 'int.fxml'.";
        assert btnDatatonRestart != null : "fx:id=\"btnDatatonRestart\" was not injected: check your FXML file 'int.fxml'.";
        assert btnDatatonRun != null : "fx:id=\"btnDatatonRun\" was not injected: check your FXML file 'int.fxml'.";
        assert btnBrixOn != null : "fx:id=\"btnBrixOn\" was not injected: check your FXML file 'int.fxml'.";
        assert btnBrixOff != null : "fx:id=\"btnBrixOff\" was not injected: check your FXML file 'int.fxml'.";
        assert btnBrixRestart != null : "fx:id=\"btnBrixRestart\" was not injected: check your FXML file 'int.fxml'.";
        assert btnRelayUpOn != null : "fx:id=\"btnRelayUpOn\" was not injected: check your FXML file 'int.fxml'.";
        assert btnRelayUpOff != null : "fx:id=\"btnRelayUpOff\" was not injected: check your FXML file 'int.fxml'.";
        assert btnRelayDownOn != null : "fx:id=\"btnRelayDownOn\" was not injected: check your FXML file 'int.fxml'.";
        assert btnRelayDownOff != null : "fx:id=\"btnRelayDownOff\" was not injected: check your FXML file 'int.fxml'.";
        assert txtTurnOnHours != null : "fx:id=\"txtTurnOnHours\" was not injected: check your FXML file 'int.fxml'.";
        assert txtTurnOnMinutes != null : "fx:id=\"txtTurnOnMinutes\" was not injected: check your FXML file 'int.fxml'.";
        assert txtTurnOffHours != null : "fx:id=\"txtTurnOffHours\" was not injected: check your FXML file 'int.fxml'.";
        assert txtTurnOffMinutes != null : "fx:id=\"txtTurnOffMinutes\" was not injected: check your FXML file 'int.fxml'.";
        assert txtTillTurnOnDays != null : "fx:id=\"txtTillTurnOnDays\" was not injected: check your FXML file 'int.fxml'.";
        assert txtTillTurnOffDays != null : "fx:id=\"txtTillTurnOffDays\" was not injected: check your FXML file 'int.fxml'.";
        assert txtTillTurnOnHours != null : "fx:id=\"txtTillTurnOnHours\" was not injected: check your FXML file 'int.fxml'.";
        assert txtTillTurnOnMinutes != null : "fx:id=\"txtTillTurnOnMinutes\" was not injected: check your FXML file 'int.fxml'.";
        assert txtTillTurnOffHours != null : "fx:id=\"txtTillTurnOffHours\" was not injected: check your FXML file 'int.fxml'.";
        assert txtTillTurnOffMinutes != null : "fx:id=\"txtTillTurnOffMinutes\" was not injected: check your FXML file 'int.fxml'.";
        assert btnSaveChanges != null : "fx:id=\"btnSaveChanges\" was not injected: check your FXML file 'int.fxml'.";
        assert btnReset != null : "fx:id=\"btnReset\" was not injected: check your FXML file 'int.fxml'.";

	}
}

