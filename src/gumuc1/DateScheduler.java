package gumuc1;

import org.apache.log4j.Logger;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.TreeSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class DateScheduler {
	/**
	* Logger for this class
	*/
	private static final Logger logger = Logger.getLogger(DateScheduler.class);

	//static public  class ScheduledDateTime extends LocalDateTime{}
	public static final int SCHEDULER_PRECISION_SECONDS = 3;
	
	private TreeSet<LocalDateTime> shedule = new TreeSet<>();
	private TreeSet<LocalDateTime> shedulePassed = new TreeSet<>();
	@SuppressWarnings("unused")
	private NavigableSet<LocalDateTime> shedulePending;

	
	private final ScheduledExecutorService scheduler =
			     Executors.newScheduledThreadPool(1);

	private final Consumer<Void> onCounter;
	private Optional <ScheduledFuture<?>> currentFuture = Optional.empty();
	private Duration tillNextExecution = Duration.ZERO;
	
	private boolean disabled = false;
	
	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	DateScheduler(Consumer<Void> onCounter) {
		this.onCounter = onCounter;
	}
	
	public NavigableSet<LocalDateTime> getSchedule(){
		return Collections.unmodifiableNavigableSet(shedule);
	}
	
	public void removeAllDateTimeWithinDay(LocalDate day){
		shedule.removeIf((record)->record.toLocalDate().equals(day));
		updateScheduler();
	}
	
	public void removeDateTime(LocalDateTime dt){
		shedule.remove(dt);
		updateScheduler();
	}
	
	public void addDateTime(LocalDateTime dt){
		shedule.add(dt);
		updateScheduler();
	}
	
	public void addDateTimes(List<LocalDateTime> dates){
		shedule.addAll(dates);
		updateScheduler();
	}
	
	public void setDateTimes(List<LocalDateTime> dates){
		shedule.clear();
		addDateTimes(dates);
	}
	
	public void addDateTimes(LocalDateTime...dt){
		addDateTimes(Arrays.asList(dt));
	}
	
	public void addDateTimes(javafx.collections.ObservableList<java.time.LocalDate> dates){
		addDateTimes(dates.sorted());
	}
	
	public void setDateTimes(javafx.collections.ObservableList<java.time.LocalDate> dates){
		shedule.clear();
		addDateTimes(dates);
	}	
	
	public LocalTime getTimeOnDate(LocalDate date){
		LocalDateTime first = shedule.ceiling(LocalDateTime.of(date, LocalTime.of(0, 0)));
		if(first!= null && first.toLocalDate().equals(date) )
			return first.toLocalTime();
		else
			return null;
	}
	
	private void updateScheduler(){
		currentFuture.ifPresent((future)-> future.cancel(false));
		updateScheduleFuture();
	}
		
	private void updateScheduleFuture(){
		//if(scheduler.)
		//currentFuture.ifPresent((future)-> future.cancel(false));
		LocalDateTime timeNow = LocalDateTime.now();
		LocalDateTime nextExecution = shedule.higher(timeNow);
		if (nextExecution == null) {
			logger.warn("Last schedule value reached");
			return;
		}
		if(Math.abs(Duration.between(timeNow,nextExecution).getSeconds()) < SCHEDULER_PRECISION_SECONDS){
			shedulePassed.add(nextExecution);
			nextExecution = shedule.higher(nextExecution);
			if (nextExecution == null) {
				logger.warn("Last schedule value reached");
				return;
			}
		}
		shedulePending = shedule.tailSet(nextExecution, true);
		tillNextExecution = Duration.between(timeNow,nextExecution);
		
		logger.info("timenow " + timeNow.toString());
		logger.info("timenext " + nextExecution.toString());
		logger.info("seconds till " + tillNextExecution.getSeconds());
		
	    currentFuture.ifPresent(future->future.cancel(true));
		ScheduledFuture<?> future = scheduler.schedule(
				new Runnable(){
					@Override 
					public void run() {
						if(!DateScheduler.this.isDisabled()){
							logger.info("Running on counter");
							onCounter.accept(null);
						}
						updateScheduleFuture();
					}
				}, tillNextExecution.getSeconds(), TimeUnit.SECONDS);
		currentFuture = Optional.of(future);
	}
	
	
	public static void main(String[] args) {
		try{
			
			Exposition expo = new Exposition();
			expo.initDefaultBrixes();
			expo.initDefaultDatatons();
			expo.initDefaultProjectors();
			expo.initDefaultRelays();

			DateScheduler shedOn = new DateScheduler((Void)->expo.powerAllUp());
			DateScheduler shedOff = new DateScheduler((Void)->expo.powerAllDown());

			
			DateScheduler s = new DateScheduler((Void)->{});
			s.addDateTime(LocalDateTime.of(2015, Month.DECEMBER, 10 ,19,02,30));
			s.setDisabled(true);

			shedOn.addDateTime( LocalDateTime.of(2015, Month.DECEMBER,11 ,21,12,50));
			shedOn.addDateTime( LocalDateTime.of(2015, Month.DECEMBER, 5, 10,20,40));
			shedOn.addDateTime( LocalDateTime.of(2015, Month.DECEMBER, 9, 10,20,40));

			shedOff.addDateTime(LocalDateTime.of(2015, Month.DECEMBER, 9, 19,00,40));
			shedOff.addDateTime(LocalDateTime.of(2015, Month.DECEMBER, 4, 19,19,20));
			shedOff.addDateTime(LocalDateTime.of(2015, Month.DECEMBER, 5, 19,00,40));
			shedOff.addDateTime(LocalDateTime.of(2015, Month.DECEMBER, 6, 19,00,40));
			

			//shedOn.addDateTime( LocalDateTime.of(2015, Month.DECEMBER, 2, 13,38,30));
			//shedOn.addDateTime( LocalDateTime.of(2015, Month.DECEMBER, 2, 13,38,50));
			//shedOn.addDateTime( LocalDateTime.of(2015, Month.DECEMBER, 2, 13,36,20));
			//shedOn.addDateTime( LocalDateTime.of(2015, Month.DECEMBER, 2, 13,28,55));

		}
		catch(Exception e){
			e.printStackTrace();
		}
			
		
	}

}
