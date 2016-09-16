package gumuc1;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

import javafx.beans.InvalidationListener;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ToggleButton;

public class TimeAdder implements Property<String>{
	/**
	 * 
	 */
	private final ExpositionSchedulerController expositionSchedulerController;
	public final ToggleButton button;
	private LocalTime timeFrom = LocalTime.of(0, 0);
	public LocalTime getTimeFrom() {
		return timeFrom;
	}

	public LocalTime getTimeTo() {
		return timeTo;
	}
	private LocalTime timeTo= LocalTime.of(0, 0);
	public static final String FROM_TO_SEPARATOR = "-";

	TimeAdder(ExpositionSchedulerController expositionSchedulerController, ToggleButton button){
		this.expositionSchedulerController = expositionSchedulerController;
		this.button = button;
	    button.setLineSpacing(-2);

	}
	
	public void setTime(LocalTime timeFrom,LocalTime timeTo){
		this.timeFrom = timeFrom;
		this.timeTo = timeTo;
		//setValue(button.textProperty().get());
	}
	public void setTimeEmpty(){
		this.timeFrom = LocalTime.of(0, 0);
		this.timeTo =  LocalTime.of(0, 0);
		//setValue(button.textProperty().get());

	}

	
	private String getLabel(){
		if(timeFrom.getHour()!=0 &&timeTo.getHour() !=0)
			return "\n" + timeFrom.getHour()+  FROM_TO_SEPARATOR + timeTo.getHour();
		else 
			return "\n   ";
	}
	
	public LocalDate getDate(){
		try{
			LocalDate date = LocalDate.parse(this.button.getId().substring(0));
			return date;
		}
		catch(DateTimeParseException e){
			return null;
		}
	}
	
	@Override public Object getBean() {return null;}
	@Override public String getName() {return null;}
	@Override public void addListener(ChangeListener<? super String> listener) {}
	@Override public String getValue() {
		return button.getText() + getLabel();
	}
	@Override public void removeListener(ChangeListener<? super String> listener) {}
	@Override public void addListener(InvalidationListener listener) {}
	@Override public void removeListener(InvalidationListener listener) { }
	@Override
	public void setValue(String arg0) {
		LocalDate buttonDate = LocalDate.parse(button.getId().substring(0));
		if(buttonDate != null){
			LocalTime timeOfDateOn = this.expositionSchedulerController.scheduler.getTimeOnDateOn(buttonDate);
			LocalTime timeOfDateOff = this.expositionSchedulerController.scheduler.getTimeOnDateOff(buttonDate);
			if(timeOfDateOn!=null && timeOfDateOff!=null){
				setTime(timeOfDateOn, timeOfDateOff);
			}
			else
				setTimeEmpty();
		}
		button.textProperty().set(arg0 + getLabel());
	    button.setLineSpacing(-2);
	}
	@Override public void bind(ObservableValue<? extends String> observable) {}
	@Override public void bindBidirectional(Property<String> other) { }
	@Override public boolean isBound() { return false; }
	@Override public void unbind() { }
	@Override public void unbindBidirectional(Property<String> other) { }
}