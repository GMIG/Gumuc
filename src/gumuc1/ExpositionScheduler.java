package gumuc1;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

public class ExpositionScheduler {

	private DateScheduler shedOnInfokiosk; 
	private DateScheduler shedOn; 
	private DateScheduler shedOff;
	
	public LocalTime getTimeOnDateOn(LocalDate date){
		return shedOn.getTimeOnDate(date);
	}

	public LocalTime getTimeOnDateOff(LocalDate date){
		return shedOff.getTimeOnDate(date);
	}

	ExpositionScheduler(Exposition expo) {
		shedOn = new DateScheduler((Void)->expo.powerAllUp());
		shedOff = new DateScheduler((Void)->expo.powerAllDown());
		shedOnInfokiosk = new DateScheduler((Void)->expo.powerUpInfokiosks());
	}
	
	public void setDefaultSchedule(LocalDate start, LocalDate end){
		List<LocalDateTime> onSchedule = new ArrayList<LocalDateTime>();
		List<LocalDateTime> offSchedule = new ArrayList<LocalDateTime>();
		List<LocalDateTime> infoKioskOnSchedule = new ArrayList<LocalDateTime>();

		LocalDate next = start.minusDays(1);
		while ((next = next.plusDays(1)).isBefore(end.plusDays(1))) {
			if(next.getDayOfWeek() == DayOfWeek.THURSDAY){
				onSchedule.add(next.atTime(LocalTime.of(11, 25)));
				offSchedule.add(next.atTime(LocalTime.of(21, 00)));
			}
			else if ((next.getDayOfWeek() != DayOfWeek.MONDAY) &&
						!( (next.getMonth().length(next.isLeapYear()) - next.getDayOfMonth()) < 7 && next.getDayOfWeek() == DayOfWeek.FRIDAY)){
					onSchedule.add(next.atTime(LocalTime.of(10, 25)));
					offSchedule.add(next.atTime(LocalTime.of(19, 00)));
			}
			infoKioskOnSchedule.add(next.atTime(LocalTime.of(10, 42)));
		}
		shedOnInfokiosk.setDateTimes(infoKioskOnSchedule);
		shedOn.setDateTimes(onSchedule);
		shedOff.setDateTimes(offSchedule);
	}
	
	private void changeDateTime(DateScheduler sched,LocalDateTime time,LocalDateTime time2){
		sched.removeAllDateTimeWithinDay(time.toLocalDate());
		sched.addDateTime(time);
		if(time2!=null)
			sched.addDateTime(time2);
	}
	
	public void changeOnDateTime(LocalDateTime time,LocalDateTime time2){
		changeDateTime(shedOn,time,time2);
	}
	
	public void changeOffDateTime(LocalDateTime time,LocalDateTime time2){
		changeDateTime(shedOff,time,time2);
	}
	
	public void doNotTurnOnOnDate(LocalDate date){
		shedOn.removeAllDateTimeWithinDay(date);
	}
	
	public void doNotTurnOffOnDate(LocalDate date){
		shedOff.removeAllDateTimeWithinDay(date);
	}
	
	public void setSchedulerDisabled(boolean disabled){
		shedOn.setDisabled(disabled);
		shedOff.setDisabled(disabled);
	}
	
	public static void main(String[] args) {
		Exposition e = new Exposition();
		ExpositionScheduler s = new ExpositionScheduler(e);
		e.initDefaultBrixes();
		e.initDefaultDatatons();
		e.initDefaultProjectors();
		e.initDefaultRelays();
		s.setDefaultSchedule(LocalDate.now(), LocalDate.now().plusMonths(12));
		s.changeOnDateTime(LocalDateTime.of(2015, Month.DECEMBER, 11, 21,00,40), null);
	}


}
