package gumuc1;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Brix extends WOLEnabledDevice implements PowerSwitchable{
	/**
	* Logger for this class
	*/
	private static final Logger logger = Logger.getLogger(Brix.class);

	protected final static int BRIX_PING_TIMOUT = 4000;
	protected final static int  WOL_TO_ON_DELAY = 5000;
	protected String userName = "brix";
	protected String password = "\"\"";

	
    protected int getOnToActiveDelay(){return WOL_TO_ON_DELAY;};

	
	public Brix(InetAddress in_IP, String in_mac) {
		super(in_IP, in_mac);
	}

	public Brix(String ipString, String in_mac) throws UnknownHostException {
		super(ipString, in_mac);
	}

	@Override
	protected int getConnectedTimout() {
		return BRIX_PING_TIMOUT;
	}

	@Override
	public void establishPowered() {
		establishConnected();//TODO udp status capture
	}
	
	public void powerUp() {
		this.sendMagicPacketWithConfirmation( new WOLEnabledDevice.magicPacketSenderCallback(){

			@Override
			public void packetSent(int noOfPacket) {}
			@Override
			public void confirmationReceived() {
				Brix.this.setCurrentConnectionState(ConnectionState.CONNECTED);
				Brix.this.setCurrentPowerState(PowerState.POWERED_UP);
			}
			@Override
			public void confiramtionNotReceived() {
				Brix.this.setCurrentConnectionState(ConnectionState.UNCONNECTED);
				Brix.this.setCurrentPowerState(PowerState.UNDEFINED);
			}
			@Override
			public void socketError(IOException e) {
				e.printStackTrace();
			}
		});
	}
	
	private Thread shutdowner;
	
	private PSShutdownExecutor.PSShutdownExecutorCallback callback = new PSShutdownExecutor.PSShutdownExecutorCallback(){
		@Override
		public void confirmationReceived() {
			Brix.this.setCurrentConnectionState(ConnectionState.CONNECTED);
			Brix.this.setCurrentPowerState(PowerState.POWERED_DOWN);
		}
		@Override
		public void confiramtionNotReceived() {
			Brix.this.setCurrentConnectionState(ConnectionState.CONNECTED);
			Brix.this.setCurrentPowerState(PowerState.UNDEFINED);
		}
		
	};
	
	public void powerDown() {
		if (shutdowner == null || (shutdowner.getState() != Thread.State.WAITING && 
						shutdowner.getState() != Thread.State.RUNNABLE)){
			shutdowner = new Thread(new PSShutdownExecutor(getIPString(),
						PSShutdownExecutor.shutdownCommand + " " +
						PSShutdownExecutor.delayFlag + " " + 5, userName,password,  callback), "ShutdownThread");
			shutdowner.start();
		}
		else
			logger.info("Concurrent shutdown command");
	}
	
	public void restart() {
		if (shutdowner == null || (shutdowner.getState() != Thread.State.WAITING && 
						shutdowner.getState() != Thread.State.RUNNABLE)){
			shutdowner = new Thread(new PSShutdownExecutor(getIPString(),
						PSShutdownExecutor.restartCommand  + " " + 
						PSShutdownExecutor.delayFlag + " " + 5, userName, password, callback), "RestartThread");
			shutdowner.start();
		}
		else
			logger.info("Concurrent shutdown command");

	}

	
	
	
	public static void main(String[] args) {	
		try{
			Brix brixLMB = new Brix(("10.8.1.3"),"fc:aa:14:f3:35:73");//lmb 10.8.1.1
			brixLMB.powerDown();
			//brixLMB.establishConnected();
			//brixLMB.restart();
			//Brix brixDoors = new Brix(("10.8.1.2"),"fc:aa:14:f3:35:ca");//doors
			//Brix brixProj = new Brix(("10.8.1.3"),"fc:aa:14:f3:35:e8");//doors
			//Brix brixSpiski = new Brix(("10.8.1.7"),"fc:aa:14:f3:35:f0");//doors
			
			
			//ArrayList <Brix> brixes  = new ArrayList <>();
			//brixes.add(brixLMB);brixes.add(brixDoors);brixes.add(brixSpiski);brixes.add(brixProj);
			//Brix b2 = new Brix("10.8.1.1","fc:aa:14:f3:35:73");
			
			/*for(Brix b :brixes){
				Observer debug = new Observer(){
					
					@Override
					public void update(Observable arg0, Object arg1) {
						// TODO Auto-generated method stub
						System.out.print(((Brix)arg0).getIPString() + ":");
						System.out.print(((Brix)arg0).getCurrentPowerState() + " ");
						System.out.println(((Brix)arg0).getCurrentConnectionState() +" ");
					}
				};
				b.powerDown();
				b.addObserver(debug);
				//b.establishConnected();
				//b.establishPowered();

			}
			//brixProj.powerOn();
			Thread.sleep(40000);
			for(Brix b :brixes){
				System.out.print(b.getIPString() + ":");
				System.out.print(b.getCurrentPowerState() + " ");
				System.out.println(b.getCurrentConnectionState() +" ");
			}*/
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

}
