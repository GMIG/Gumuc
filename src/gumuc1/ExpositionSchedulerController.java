package gumuc1;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.GridPane;
import jfxtras.scene.control.CalendarPicker;
import jfxtras.scene.control.ListSpinner;
import jfxtras.scene.control.LocalDatePicker;

public class ExpositionSchedulerController {

	private static final Logger logger = Logger.getLogger(ExpositionSchedulerController.class);
	private HashSet<TimeAdder> adders = new HashSet<>();
	ExpositionScheduler scheduler;
	private CalendarPicker calendar;
	private File schedule;
	
    DateTimeFormatter formatterDate =
            DateTimeFormatter.ofPattern("dd.MM.yy");

    DateTimeFormatter formatterTime =
            DateTimeFormatter.ofPattern("HH-mm");

	/*
	 * Constructs the controller and injects it's methods to LocalDatePicker (jfxtras) and GridPane (where control text boxes are)
	 */
	public ExpositionSchedulerController(GridPane timePane, LocalDatePicker picker, ExpositionScheduler scheduler, File schedule) {
		this.scheduler = scheduler;
		this.calendar = (CalendarPicker)(picker.getChildrenUnmodifiable().get(0));
		this.schedule = schedule;

		// The grid pane of LocalDatePicker
		GridPane gridp = (GridPane) calendar.getChildrenUnmodifiable().get(0);
		for(Node child:gridp.getChildren()){
			// Adding the time adder to each of picker buttons
			// TimeAdder takes 
			if(child instanceof ToggleButton){
			    ToggleButton tbutton =(ToggleButton) child;
			    TimeAdder adder = new TimeAdder(this, tbutton);
			    tbutton.textProperty().bindBidirectional(adder);
			    adders.add(adder);
			}
		}
		((ToggleButton)timePane.lookup("#btnTurnScheduleOn")).
			setOnAction((action)->scheduler.setSchedulerDisabled(false));
		((ToggleButton)timePane.lookup("#btnTurnScheduleOff")).
			setOnAction((action)->scheduler.setSchedulerDisabled(true));
		((Button)timePane.lookup("#btnSaveChanges")).
			setOnAction((action)->{
				ObjectMapper mapper = new ObjectMapper();
				JsonNode rootNode;
				try {
					rootNode = mapper.readTree(schedule);

					JsonFactory factory = new JsonFactory();
					ObjectMapper om = new ObjectMapper(factory);
					factory.setCodec(om);
					ObjectNode node = om.createObjectNode();
					node.setAll((ObjectNode)rootNode);
					
					ObjectNode newRecord = om.createObjectNode();
					String onHours = ((TextField)timePane.lookup("#txtTurnOnHours")).getText();
					String onMinutes = ((TextField)timePane.lookup("#txtTurnOnMinutes")).getText();
					String offHours = ((TextField)timePane.lookup("#txtTurnOffHours")).getText();
					String offMinutes = ((TextField)timePane.lookup("#txtTurnOffMinutes")).getText();
					
					newRecord.put("day", picker.getLocalDate().format(formatterDate));
					if(!onHours.equals("") && !onMinutes.equals(""))
						newRecord.put("on", onHours + "-" + onMinutes);
					else
						newRecord.put("on", "" );

					if(!offHours.equals("") && !offMinutes.equals(""))
						newRecord.put("off", offHours + "-" + offMinutes);
					else
						newRecord.put("off", "" );

							//((TextField)timePane.lookup("#txtTurnOnHours")).getText() + "-"));

					node.withArray("schedule").add(newRecord);

					JsonGenerator gen = factory.createGenerator(schedule,
							JsonEncoding.UTF8);
					gen.useDefaultPrettyPrinter();
					gen.writeTree(node);
					gen.close();
					updateExpositionScheduler();
					updateExpositionSchedulerView();
				}
				catch (IOException e) {
					logger.warn("JSON file error");
					e.printStackTrace();
				}

			});

		picker.localDateProperty().addListener(new ChangeListener<LocalDate>(){
			@Override
			public void changed(ObservableValue<? extends LocalDate> observable, LocalDate oldValue,
					LocalDate newValue) {
				final LocalDate compared = (newValue == null)? oldValue:newValue;
				Stream<TimeAdder> stream = adders.stream().filter((adder)->{
					return compared.equals(adder.getDate());});
				Optional<TimeAdder> adder =  stream.findFirst();
				((TextField)timePane.lookup("#txtTurnOnMinutes")).setText(String.valueOf(adder.get().getTimeFrom().getMinute()));
				((TextField)timePane.lookup("#txtTurnOnHours")).setText(String.valueOf(adder.get().getTimeFrom().getHour()));
				
				((TextField)timePane.lookup("#txtTurnOffMinutes")).setText(String.valueOf(adder.get().getTimeTo().getMinute()));
				((TextField)timePane.lookup("#txtTurnOffHours")).setText(String.valueOf(adder.get().getTimeTo().getHour()));
			}
			
		});
		updateExpositionScheduler();
		updateExpositionSchedulerView();
	}
	
	public void updateExpositionSchedulerView(){
		GridPane gridp = (GridPane) calendar.getChildrenUnmodifiable().get(0);
		@SuppressWarnings("unchecked")
		ListSpinner <String>spin = (ListSpinner<String>)gridp.lookup("#monthListSpinner");
		int index = spin.getIndex();
		spin.setIndex(1);
		spin.setIndex(index);
		/*gridp.getChildren().forEach((node)->{
			if(node instanceof ToggleButton){
				((ToggleButton) node).fire();
				return;
			}
			
		});*/
	}

	public void updateExpositionScheduler(){
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode;
		try {
			rootNode = mapper.readTree(schedule);
			JsonNode settingsNode = rootNode.get("schedule");
			Iterator<JsonNode> scheduleData = settingsNode.elements();
			while(scheduleData.hasNext()){
				JsonNode scheduleJSON = scheduleData.next();
			    LocalDate date = LocalDate.parse(scheduleJSON.get("day").asText(), formatterDate);
			    
			    try{
				    LocalTime onTime = LocalTime.parse(scheduleJSON.get("on").asText(),formatterTime);
				    LocalTime offTime1 = LocalTime.parse(scheduleJSON.get("off").asText(),formatterTime);
				    LocalDateTime onRecord = LocalDateTime.of(date, onTime);
				    LocalDateTime offRecord1 = LocalDateTime.of(date, offTime1);
				    LocalDateTime offRecord2 = null;
	
				    if(scheduleJSON.has("off2")){
				    	LocalTime offTime2 = LocalTime.parse(scheduleJSON.get("off2").asText(),formatterTime);
					     offRecord2 = LocalDateTime.of(date, offTime2);
				    }
				    scheduler.changeOnDateTime(onRecord, null);
				    scheduler.changeOffDateTime(offRecord1, offRecord2);
			    }
			    catch(DateTimeParseException e){
			    	if(scheduleJSON.get("on").asText().equals(""))
			    		scheduler.doNotTurnOnOnDate(date);
			    	if(scheduleJSON.get("off").asText().equals(""))
			    		scheduler.doNotTurnOffOnDate(date);
			    }
			}
		}
		catch(JsonProcessingException e){
			logger.warn("JSON parsing error");
			e.printStackTrace();
		}
		catch (IOException e) {
			logger.warn("JSON file error");
			e.printStackTrace();
		}
	}

}
