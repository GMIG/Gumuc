package gumuc1;

import org.apache.log4j.Logger;


import java.net.UnknownHostException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class Exposition {

	static final Logger logger = Logger.getLogger(Exposition.class);
	
	private ArrayList<Brix> infokiosks = new ArrayList<>();
	private ArrayList<Brix> brixes = new ArrayList<>();
	private ArrayList<Dataton> datatons = new ArrayList<>();
	private DatatonClusterMaster datatonMaster = null;
	private ArrayList<Projector> projectors= new ArrayList<>();
	private ArrayList<Relay> relays= new ArrayList<>();
	
	public List<Projector> getProjectors(){
		return Collections.unmodifiableList(projectors);
	}
	
	public List<Brix> getBrix(){
		return Collections.unmodifiableList(brixes);
	}
	
	private void refreshList(List<? extends IPDevice> list){
		//list.forEach((item)->item.establishConnected());
		list.forEach((item)->item.establishPowered());
	}
	
	public void refresh(){
		refreshList(projectors);
		refreshList(brixes);
		refreshList(datatons);
		refreshList(infokiosks);
		//relays.forEach((item)->item.establishConnected());
		relays.forEach((item)->item.establishTurned());

	}
	
	public Exposition() {
	}
	
	public void initDefaultBrixes(){
		try{
			Brix brixLMB = new Brix(("10.8.1.1"),"fc:aa:14:f3:35:73");//lmb 
			Brix brixDoors = new Brix(("10.8.1.2"),"fc:aa:14:f3:35:ca");//doors
			Brix brixProj = new Brix(("10.8.1.3"),"fc:aa:14:f3:35:e8");//projector
			Brix brixSpiski = new Brix(("10.8.1.7"),"fc:aa:14:f3:35:f0");//lists
			brixes.add(brixLMB);
			brixes.add(brixDoors);
			brixes.add(brixProj);
			brixes.add(brixSpiski);
		}
		catch( UnknownHostException e){
			e.printStackTrace();
		}
	}
	
	public void addBrix(Brix brix){
		brixes.add(brix);
	}

	
	public void initDefaultDatatons(){
		try{
			datatons.add(new Dataton("10.8.1.101","00:20:98:02:d0:aa"));
			datatons.add(new Dataton("10.8.1.102","00:20:98:02:d1:7e"));
			datatons.add(new Dataton("10.8.1.103","00:20:98:02:d1:09"));
			datatons.add(new Dataton("10.8.1.104","00:20:98:02:cf:26"));
			datatonMaster = new DatatonClusterMaster("10.8.1.105","00:20:98:02:d1:25");
			datatons.add(datatonMaster);
		}
		catch( UnknownHostException e){
			e.printStackTrace();
		}
	}
	public void addDataton(Dataton dat){
		datatons.add(dat);
	}
	public void setDatatonClusterMaster(DatatonClusterMaster dat){
		datatonMaster = dat;
	}

	
	public void initDefaultProjectors(){
		try{
			for(int i=1; i<=10;i++){
				Projector proj = new Projector("10.8.3."+i);
				projectors.add(proj);
			}
		}
		catch( UnknownHostException e){
			e.printStackTrace();
		}
	}
	
	public void addProjector(Projector projector){
				projectors.add(projector);
	}

	public void initDefaultRelays(){
		try{
			for(int i=1;i<19;i++){
				Relay relay = new Relay("10.8.4."+i);
				relays.add(relay);
				RelayServer.getInstance().addClient(relay);
				//RelayResenderServer.getInstance();
			}
		}
		catch( UnknownHostException e){
			e.printStackTrace();
		}
	}
	
	public void addRelay(Relay rel){
		relays.add(rel);
		RelayServer.getInstance().addClient(rel);
		RelayResenderServer.getInstance();

	}
	
	public void initDefaults(){
		initDefaultBrixes();
		initDefaultDatatons();
		initDefaultProjectors();
		initDefaultRelays();
	}
	
	public void addInfokiosk(Brix e){
		infokiosks.add(e);
	}
	
	public static final int DELAY_TO_NEXT_BRIX_START = 3000;
	public static final int DELAY_TO_NEXT_DATATON_START = 2000;
	public static final int DELAY_TO_NEXT_RELAY_START = 500;


	public void scheduledPowerUp(ArrayList<? extends PowerSwitchable> devices, int delay){
		final ScheduledExecutorService scheduler =
			     Executors.newScheduledThreadPool(devices.size());
		for( int i =0;i < devices.size();i++){
			final PowerSwitchable dev = devices.get(i);
			Consumer <Void> cons = (i == devices.size() - 1 )? (Void)->scheduler.shutdown():(Void)->{;};
			scheduler.schedule(new Runnable(){
					@Override
					public void run() {		
						dev.powerUp();
						cons.accept(null);
					}
				}, i*delay, TimeUnit.MILLISECONDS);
		}
	}
	
	
	// 12 MyGULAG files. If week number is odd we take 1-6 numbers. 7-12 otherwize
	// monday is a day off - that is why counting is starting with tuesday
	public void loadAndRunDatatons(){
		LocalDate date = LocalDate.now();
		TemporalField woy = WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear(); 
		int weekNumber = date.get(woy);
		int noOfFile = date.getDayOfWeek().getValue() - 1;
		if (date.getDayOfWeek().equals(DayOfWeek.MONDAY)) 
			noOfFile = (int)(Math.random()*6+1);
		noOfFile += 6*(weekNumber % 5);

		datatonMaster.runTimeline("expofin",
				"Returns-right",
				"Ours-days-left",
				"MyGULAG"+noOfFile,
				"BeginGULAG-214UP",
				"Ot-aresta-214-right",
				"Soviet-214-left",
				"PN-dveri");
	}
	
	public void controlRelays(int ledID,boolean cmdID){
		relays.forEach((relay)->{relay.sendSwitchCommand(ledID, cmdID);});
	}

	
	public void powerDownRelay(){
		//scheduledPowerUp(relays,DELAY_TO_NEXT_RELAY_START);
		relays.forEach((relay)->{relay.powerDown();});
	}
	
	public void powerUpRelay(){
		scheduledPowerUp(relays,DELAY_TO_NEXT_RELAY_START);
	}
	
	public void powerUpBrixes(){
		scheduledPowerUp(brixes,DELAY_TO_NEXT_BRIX_START);
	}
	
	public void powerDownBrixes(){
		brixes.forEach((brix) -> brix.powerDown());
	}
	
	public void powerUpInfokiosks(){
		scheduledPowerUp(infokiosks,DELAY_TO_NEXT_BRIX_START);
	}
	
	public void powerDownInfokiosks(){
		infokiosks.forEach((infokiosk) -> infokiosk.powerDown());
	}
	
	public void powerUpDatatons(){
		scheduledPowerUp(datatons,DELAY_TO_NEXT_DATATON_START);
	}
	
	public void powerUpAndRunDatatons(){
		@SuppressWarnings("unused")
		EventCounterSafe <IPDevice.PowerState> counterDatatons = new EventCounterSafe<IPDevice.PowerState>(
				datatons,(counterOn1)-> {
					logger.info("Running Shows");
					loadAndRunDatatons();
				},IPDevice.PowerState.POWERED_UP);
		powerUpDatatons();
	}
	
	public void powerDownDatatons(){
		datatons.forEach(dataton -> dataton.powerDown());
	}
	
	public void powerUpProjectors(){
		projectors.forEach((projector) -> projector.powerUp());
	}
	
	public void powerDownProjectors(){
		projectors.forEach((projector) -> projector.powerDown());
	}
	
	public void restartBrixes(){
		brixes.forEach((brix) -> brix.restart());
	}
	
	
	public void powerAllUp(){		

		@SuppressWarnings("unused")
		EventCounterSafe <IPDevice.PowerState> counterProjectors = new EventCounterSafe<IPDevice.PowerState>(
				projectors,(counterOn)-> {
					logger.info("Switching on datatons and brixes");
					@SuppressWarnings("unused")
					EventCounterSafe <IPDevice.PowerState> counterDatatons = new EventCounterSafe<IPDevice.PowerState>(
							datatons,(counterOn1)-> {
								logger.info("Running Shows");
								loadAndRunDatatons();
							},IPDevice.PowerState.POWERED_UP);
					powerUpDatatons();
					powerUpBrixes();
				},IPDevice.PowerState.POWERED_UP);
		powerUpProjectors();
		powerUpRelay();
		powerUpInfokiosks();
	}
	
	
	public void powerAllDown(){		
		powerDownInfokiosks();

		@SuppressWarnings("unused")
		EventCounterSafe <IPDevice.PowerState> counter1 = new EventCounterSafe<IPDevice.PowerState>(
				projectors,(counterOn)-> {
					logger.info("Switching off datatons");
					powerDownDatatons();
				},IPDevice.PowerState.POWERED_DOWN);
		powerDownProjectors();
		powerDownRelay();
		powerDownBrixes();

	}
	
	public static void main(String[] args) {	
		try{

			
			//LocalDate date = LocalDate.now();
		
			String [] files ={
				"null",
				"01-01Belikov.mpg",
				"01-02Viskrebenczova.mpg",
				"01-03Smilga.mpg",
				"01-04Esters.mpg",
				"01-05Uspenskaya.mpg",
				"01-06Zavodova.mpg",
				"01-07Mazus.mpg",
				"01-08Bodyagina.mpg",
				"01-09Fidelgolcz.mpg",
				"01-10Hachatryan.mpg",
				"01-11Ivanova.mpg",
				"01-12Loskueva.mpg",
				"01-13Tolstoy.mpg",
				"01-14Shelkovsky.mpg",
				"01-15Pechenova.mpg",
				"01-16Dzenzeol.mpg",
				"01-17Bernakevich.mpg",
				"01-18Raeva.mpg",
				"01-19Yakovleva.mpg",
				"01-20Samorodnizkaya.mpg",
				"01-21Itakov.mpg",
				"01-22Nesterenko.mpg",
				"01-23Tartikova.mpg",
				"01-24Krizanovskaya.mpg",
				"01-25.mpg",
				"01-26.mpg",
				"01-27.mpg",
				"01-28.mpg",
				"01-29.mpg",
				"01-30.mpg",

			};
			
			HashMap <Integer,ArrayList<LocalDate>> map = new HashMap<>();

			for(int i=0;i < files.length;i++){
				map.put(i, new ArrayList<LocalDate>());
			}
			
			for(LocalDate date = LocalDate.of(2016, 02, 01);date.isBefore(LocalDate.of(2017, 01, 01));date= date.plusDays(1)){
				TemporalField woy = WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear(); 
				int weekNumber = date.get(woy);
				int noOfFile = date.getDayOfWeek().getValue() - 1;
				//if (date.getDayOfWeek().equals(DayOfWeek.MONDAY)) noOfFile = (int)(Math.random()*6+1);
				//if ((weekNumber % 2) == 0) 
				noOfFile += 6*(weekNumber % 5);
				//map.get(noOfFile).add(date);
				if (!date.getDayOfWeek().equals(DayOfWeek.MONDAY))
					map.get(noOfFile).add(date);
			}
			
			map.keySet().forEach((num)->{
				System.out.print(files[num] + " ");
				map.get(num).forEach((date)->System.out.print(date + ", "));
				System.out.println();
			}
			);

			//Exposition expo = new Exposition();
			//expo.initDefaultBrixes();
			//expo.initDefaultDatatons();
			//expo.initDefaultProjectors();
			/*expo.initDefaultRelays();
			while(true){
				relays.forEach((rel1)->
				rel1.sendUpTurnOff());
				relays.forEach((rel1)->
				rel1.sendDownTurnOff());

				Thread.sleep(1000);
				relays.forEach((rel1)->
				rel1.sendUpTurnOn());
				relays.forEach((rel1)->
				rel1.sendDownTurnOn());

				Thread.sleep(1000);


			}*/
		//expo.powerUpBrixes();
//expo.powerUpRelay()		;	//expo.powerUpBrixes();
//expo.powerDownBrixes();
			//expo.powerAllUp();
			//ObjectMapper mapper = new ObjectMapper(); 
			//expo.powerAllDown();
			/*expo.relays.forEach(relay-> {
				relay.addObserver(new Observer(){
					@Override public void update(Observable arg0, Object arg1) {
						logger.info(((Relay)arg0).getRelay1State());
						logger.info(((Relay)arg0).getRelay2State());
					}
				});
				relay.establishTurned();
			});*/
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
}
