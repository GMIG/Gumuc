package gumuc1;

import org.apache.log4j.Logger;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.mina.core.RuntimeIoException;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioDatagramConnector;


public class Relay extends IPDevice implements IoHandler,PowerSwitchable{
	/**
	* Logger for this class
	*/
	private static final Logger logger = Logger.getLogger(Relay.class);

	protected final static int RELAY_PING_TIMOUT = 10000;
	protected final static int CONNECT_TIMEOUT_RELAY = 4000;
	protected final static int RESENDER_DELAY = 5000;
	protected final static int RESENDER_MAX_COMMAND_LIFETIME = 5;
	
    private NioDatagramConnector connector;
    private IoSession activeSession;
    
    ScheduledExecutorService resender = Executors.newSingleThreadScheduledExecutor();
    ScheduledExecutorService infoChecker = Executors.newSingleThreadScheduledExecutor();

    private ArrayList<RelaySwitchCommand> commands = new ArrayList<>();
    private boolean infoSentNoAnswerReceivedYet = false;

    private RelayState relay1State = RelayState.UNDEFINED;
    public synchronized RelayState getRelay1State() {
		return relay1State;
	}

    protected synchronized void setRelay1State(RelayState relay1State) {
    	if(this.relay1State!=relay1State){
			this.relay1State = relay1State;
			this.notifyObservers();
			this.setChanged();
    	}
	}

	private RelayState relay2State = RelayState.UNDEFINED;

	public synchronized RelayState getRelay2State() {
		return relay2State;
	}

	protected synchronized void setRelay2State(RelayState relay2State) {
    	if(this.relay2State!=relay2State){
			this.relay2State = relay2State;
			this.notifyObservers();
			this.setChanged();
    	}
	}
    
	@Override
	protected int getConnectedTimout() {
		return RELAY_PING_TIMOUT;
	}
	
    public void processReply(RelayDecoder.SwitchReply reply){
    	setCurrentConnectionState(IPDevice.ConnectionState.CONNECTED);
    	infoSentNoAnswerReceivedYet = false;
		Iterator<RelaySwitchCommand> iterator = commands.iterator();
		while (iterator.hasNext()) {
			RelaySwitchCommand command = iterator.next();
			if (reply.answers(command)){
					iterator.remove();
					break;
			}
		}	
    	if (reply.getLedID() == 1){ 
    		setRelay1State(RelayState.fromBoolean(reply.getCmdID()));
        	//setCurrentConnectionState(IPDevice.ConnectionState.CONNECTED);
    	}
    	if (reply.getLedID() == 2) {
    		setRelay2State(RelayState.fromBoolean(reply.getCmdID()));
        	//setCurrentConnectionState(IPDevice.ConnectionState.CONNECTED);
    	}
    }

	public Relay(InetAddress in_IP) {
		super(in_IP);
		initConnector();
	}

	public Relay(String ipString) throws UnknownHostException {
		super(ipString);
		initConnector();
	}
	
	private void initConnector(){
		connector = new NioDatagramConnector();
		ProtocolCodecFilter pcf = new ProtocolCodecFilter(
				 new RelayEncoder(), new RelayDecoder());
		connector.setConnectTimeoutMillis(CONNECT_TIMEOUT_RELAY);
	    connector.getFilterChain().addLast("log1", new LoggingFilter("log1"));
	    connector.getFilterChain().addLast("decode", pcf);
	    connector.getFilterChain().addLast("log2", new LoggingFilter("log2"));
	}
	
    private void send(RelayCommand command) {
    	if(activeSession!=null && activeSession.isConnected()){
    		activeSession.write( command);
    	}
    	else{
		    connector.setHandler(this);
	        try {
	            ConnectFuture future = connector.connect(new InetSocketAddress(getAddr(), RelayServer.RELAY_PORT));
	            future.awaitUninterruptibly();
	            activeSession = future.getSession();
	    		activeSession.write( command);
	        } catch (RuntimeIoException e) {
	        	setCurrentConnectionState(IPDevice.ConnectionState.UNDEFINED);
	        }
    	}
    }
	
    public void sendSwitchCommand(int ledID,boolean cmdID){
		RelaySwitchCommand command = new RelaySwitchCommand(this,ledID,cmdID);
		commands.add(command);
		resender.execute(new Runnable(){	        
			@Override
			public void run() {
				if(command.getLifetime() > RESENDER_MAX_COMMAND_LIFETIME){
		        	setCurrentConnectionState(IPDevice.ConnectionState.UNCONNECTED);
		        	commands.remove(command);
				}
				else if (commands.contains(command)){
					send(command);
					logger.info("sending " +  command.getCommandString());
					command.incrementLifetime();
					resender.schedule(this, RESENDER_DELAY, TimeUnit.MILLISECONDS);
				}
			}
		});
    }
    
    
    public void sendInfoCommand(){
		RelayInfoCommand command = new RelayInfoCommand(this);
		send(command);
		infoSentNoAnswerReceivedYet = true;
		infoChecker.schedule(new Runnable(){	        
			@Override
			public void run() {
				if(infoSentNoAnswerReceivedYet){
		        	setCurrentConnectionState(IPDevice.ConnectionState.UNCONNECTED);
				}
			}
		}, RESENDER_DELAY, TimeUnit.MILLISECONDS);
    }
    
	public void sendUpTurnOn(){
		this.sendSwitchCommand(2,true);
	}
	
	public void sendUpTurnOff(){
		this.sendSwitchCommand(2,false);
	}
	
	public void sendDownTurnOn(){
		this.sendSwitchCommand(1,true);
	}
	public void sendDownTurnOff(){
		this.sendSwitchCommand(1,false);
	}
	
	public void establishTurned(){
		//RelayInfoCommand command = new RelayInfoCommand(this);
		//send(command);
		 sendInfoCommand();
	}

	@Override
	public void establishPowered() {
		establishTurned();//setCurrentPowerState(PowerState.UNDEFINED);
	}

	//static RelayServer server;
	public static void main(String[] args) {
		
		try{
			
			RelayServer server = RelayServer.getInstance();
			//Relay rel1 = new Relay("10.8.4.9");
			//server.addClient(rel1);
			//rel1.sendDownTurnOn();
			
			/*for(int i=1;i<19;i++){
				Relay rel1 = new Relay("10.8.4."+i);
	
				//proj.powerOff();
				Observer debug = new Observer(){
	
					@Override
					public void update(Observable arg0, Object arg1) {
						// TODO Auto-generated method stub
						System.out.print(((IPDevice)arg0).getIPString() + ":");
						System.out.print(((IPDevice)arg0).getCurrentPowerState() + " ");
						System.out.print(((IPDevice)arg0).getCurrentConnectionState() +" ");
						System.out.print(((Relay)arg0).getRelay1State() + " ");
						System.out.print(((Relay)arg0).getRelay2State() + " " + "\n");
					}
				};
				rel1.addObserver(debug);
	
				server.addClient(rel1);
				
				rel1.establishTurned();
				//rel1.sendUpTurnOn();
				//rel1.sendDownTurnOn();

				//rel1.establishTurned();//.establishConnected();
			}
			Thread.sleep(10000);
			for( Relay rel :server.getCients()){
				//System.out.println(rel.establishConnected());
				System.out.println(rel.getIPString()+ ":"+ rel.getRelay1State());
				System.out.println(rel.getIPString()+ ":"+ rel.getRelay2State());

			}*/
			Relay rel1 = new Relay("10.8.0.16");
			server.addClient(rel1);

			//Thread.sleep(30*60*1000);
			
			while(true){

				rel1.sendUpTurnOff();
				rel1.sendDownTurnOff();

			Thread.sleep(1000);
				rel1.sendUpTurnOn();
				rel1.sendDownTurnOn();
				//rel1.establishPowered();

				for(int i=0;i<60*30;i++){
					//rel1.establishPowered();
					Thread.sleep(1000);

				}
				//Thread.sleep(30*60*1000);


			}
			
/*
			Thread.sleep(3000);
			rel1.establishTurned();
			rel2.establishTurned();

			Thread.sleep(80000);
			rel1.establishTurned();
			rel2.establishTurned();*/

		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public void exceptionCaught(IoSession arg0, Throwable arg1) throws Exception {}
	@Override
	public void inputClosed(IoSession arg0) throws Exception {}
	@Override
	public void messageReceived(IoSession arg0, Object arg1) throws Exception {
	}
	@Override
	public void messageSent(IoSession arg0, Object arg1) throws Exception {}
	@Override
	public void sessionClosed(IoSession arg0) throws Exception {}
	@Override
	public void sessionCreated(IoSession arg0) throws Exception {}
	@Override
	public void sessionIdle(IoSession arg0, IdleStatus arg1) throws Exception {}
	@Override
	public void sessionOpened(IoSession arg0) throws Exception {
	}

	@Override
	public void powerUp() {
		sendUpTurnOn();
		sendDownTurnOn();
	}

	@Override
	public void powerDown() {
		sendUpTurnOff();	
		sendDownTurnOff();
	}
}
