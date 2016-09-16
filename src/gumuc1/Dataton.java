package gumuc1;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.LinkedBlockingDeque;

import org.apache.mina.core.RuntimeIoException;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.statemachine.State;
import org.apache.mina.statemachine.StateMachine;
import org.apache.mina.statemachine.StateMachineFactory;
import org.apache.mina.statemachine.StateMachineProxyBuilder;
import org.apache.mina.statemachine.annotation.IoHandlerTransition;
import org.apache.mina.statemachine.context.AbstractStateContext;
import org.apache.mina.statemachine.context.IoSessionStateContextLookup;
import org.apache.mina.statemachine.context.StateContext;
import org.apache.mina.statemachine.context.StateContextFactory;
import org.apache.mina.transport.socket.nio.NioSocketConnector;


public class Dataton extends WOLEnabledDevice implements PowerSwitchable{
	/**
	* Logger for this class
	*/
	private static final Logger logger = Logger.getLogger(Dataton.class);

	protected final static int DATATON_PING_TIMOUT = 5000;
	protected final static int DATATON_DEFAULT_PORT = 3039;
	protected final static int  CONNECT_TIMEOUT_DATATON = 5000;
	protected final static int  WOL_TO_ON_DELAY = 15000;

    private  NioSocketConnector connector;    
    private static DatatonFSM datatonFSM= new DatatonFSM(); 
    protected static StateMachine sm = StateMachineFactory.getInstance(
    				IoHandlerTransition.class).create(DatatonFSM.UNDEFINED,
    						datatonFSM);
    private IoSession activeSession; 
    protected DatatonCommandContext activeContext;
	
    protected int getOnToActiveDelay(){return WOL_TO_ON_DELAY;};
	
    class DatatonCommandContext extends AbstractStateContext {
         public LinkedBlockingDeque<DatatonEncoder.DatatonCommand> commandQueue = new LinkedBlockingDeque<DatatonEncoder.DatatonCommand>();

    	@Override
		public void setCurrentState(State state){
			if(state.getId() == DatatonFSM.UNDEFINED)
				setCurrentPowerState(IPDevice.PowerState.UNDEFINED);	
			if(state.getId() == DatatonFSM.CONNECTION_ESTABLISHED)
				setCurrentConnectionState(IPDevice.ConnectionState.CONNECTED);	
			if(state.getId() == DatatonFSM.SHUT_COMMAND_PASSED){
				setCurrentConnectionState(IPDevice.ConnectionState.CONNECTED);	
				setCurrentPowerState(IPDevice.PowerState.POWERED_DOWN);	
			}
			super.setCurrentState(state);

	    }
    	
    	public void hanldePingReceived(){
			setCurrentPowerState(IPDevice.PowerState.POWERED_UP);	
    	}
    	
    	public void hanldeStatusReceived(DatatonDecoder.StatusReply status){}
    	
    	public void hanldeShutdownSent(){
			setCurrentConnectionState(IPDevice.ConnectionState.CONNECTED);	
			setCurrentPowerState(IPDevice.PowerState.POWERED_DOWN);	
    	}
		
		public DatatonCommandContext() {
			super();

		}
    }
    
    protected static IoHandler createIoHandler(DatatonCommandContext context) {		
    	IoHandler handler = new StateMachineProxyBuilder().setStateContextLookup(
                new IoSessionStateContextLookup(
                		new StateContextFactory() {
                			public StateContext create() {	  
                				return context;
                			}
                		}
                )
    	   ).create(IoHandler.class, sm);
    	return handler;
    }
    
	public Dataton(InetAddress in_IP, String mac) {
		super(in_IP, mac);
		initConnector();
	}

	public Dataton(String ipString, String mac) throws UnknownHostException {
		super(ipString, mac);
		initConnector();
	}
	
	private void initConnector(){
		connector = new NioSocketConnector();
		ProtocolCodecFilter pcf = new ProtocolCodecFilter(
				new DatatonEncoder(), new DatatonDecoder());
		connector.setConnectTimeoutMillis(CONNECT_TIMEOUT_DATATON);
		//connector.set
	    connector.getFilterChain().addLast("log1", new LoggingFilter("log1"));
	    connector.getFilterChain().addLast("decode", pcf);
	    connector.getFilterChain().addLast("log2", new LoggingFilter("log2"));
	    activeContext = new DatatonCommandContext();
	}
	
	public void powerUp() {
		this.sendMagicPacketWithConfirmation( new WOLEnabledDevice.magicPacketSenderCallback(){
			@Override
			public void packetSent(int noOfPacket) {}
			@Override
			public void confirmationReceived() {
				//Dataton.this.setCurrentConnectionState(ConnectionState.CONNECTED);
				Dataton.this.send(new DatatonEncoder.DatatonPingCommand());
			}
			@Override
			public void confiramtionNotReceived() {
				Dataton.this.setCurrentConnectionState(ConnectionState.UNCONNECTED);
				Dataton.this.setCurrentPowerState(PowerState.UNDEFINED);
			}
			@Override
			public void socketError(IOException e) {
				e.printStackTrace();
			}
		});
	}

	public void powerDown() {
		send(new DatatonEncoder.DatatonAuthCommand(1),new DatatonEncoder.DatatonShutdownCommand());
	}
	
	@Override
	public void establishPowered() {
		send(new DatatonEncoder.DatatonPingCommand());
	}
	
    protected synchronized void send(DatatonEncoder.DatatonCommand...DatatonCommandContextcommand) {    	

    	if(activeSession!=null && activeSession.isConnected()){
        	for(DatatonEncoder.DatatonCommand cmd:DatatonCommandContextcommand)
        		activeContext.commandQueue.add(cmd);

    		if(activeContext.commandQueue.toArray().length == DatatonCommandContextcommand.length)
    			activeSession.write(activeContext.commandQueue.remove());
    	}
    	else{
    		IoHandler h = createIoHandler(activeContext);
		    connector.setHandler(h);
        	activeContext.commandQueue.clear();
        	for(DatatonEncoder.DatatonCommand cmd:DatatonCommandContextcommand)
        		activeContext.commandQueue.add(cmd);
            ConnectFuture future = connector.connect(new InetSocketAddress(getAddr(), DATATON_DEFAULT_PORT));
            future.addListener(new IoFutureListener<ConnectFuture>(){
				@Override
				public void operationComplete(ConnectFuture arg0) {
			        try {
			        	activeSession = arg0.getSession();
			        }
		            catch (RuntimeIoException e) {
			        	setCurrentConnectionState(IPDevice.ConnectionState.UNCONNECTED);
			        	setCurrentPowerState(IPDevice.PowerState.UNDEFINED);
			        	logger.info("TCP not connected");
			        }
				}
            });
    	}

    }
	
	
	@Override
	protected int getConnectedTimout() {
		return DATATON_PING_TIMOUT;
	}
	
	public static void main(String[] args) {
		
		try{
			
			/*ArrayList <Dataton> datatons = new ArrayList <> ();
			datatons.add(new Dataton("10.8.1.101","00:20:98:02:d0:aa"));
			datatons.add(new Dataton("10.8.1.102","00:20:98:02:d1:7e"));
			datatons.add(new Dataton("10.8.1.103","00:20:98:02:d1:09"));
			datatons.add(new Dataton("10.8.1.104","00:20:98:02:cf:26"));
			datatons.add(new Dataton("10.8.1.105","00:20:98:02:d1:25"));*/
			//Dataton dat = new Dataton("10.8.1.10" + i,"00:20:98:02:d1:25");
			(new Dataton("10.8.1.102","00:20:98:02:d1:7e")).powerDown();;

			(new Dataton("10.8.1.102","00:20:98:02:d1:7e")).powerDown();;
			/*for(Dataton dat :datatons){
				Observer debug = new Observer(){
	
					@Override
					public void update(Observable arg0, Object arg1) {
						// TODO Auto-generated method stub
						System.out.print(((IPDevice)arg0).getIPString() + ":");
						System.out.print(((IPDevice)arg0).getCurrentPowerState() + " ");
						System.out.println(((IPDevice)arg0).getCurrentConnectionState());
					}
				};
				dat.addObserver(debug);
				//dat.establishPowered();
				//dat.powerOn();
				//dat.powerDown();
				//dat.powerUp();
				//dat.powerOn();
				//StateControl.breakAndCallNext(sm.getState(DatatonFSM.LOAD_COMMAND_PASSED));
			}*/
			//datatons.get(4).establishPowered();
			//datatons.get(4).powerOff();
//			datatons.get(4).addObserver(new Observer(){
//				@Override
//				public void update(Observable arg0, Object arg1) {
//					Dataton dat = (Dataton)arg0;
//					if(dat.getCurrentConnectionState()==IPDevice.ConnectionState.CONNECTED){
//						//dat.establishPowered();
//						//dat.deleteObserver(this);
//					}
//				}
//			});
			Thread.sleep(10000);
			/*for(Dataton d :datatons){
				System.out.print(d.getIPString() + "::");
				System.out.print(d.getCurrentPowerState() + " ");
				System.out.println(d.getCurrentConnectionState() +" ");
			}*/
		}
		
		catch(Exception e){
			e.printStackTrace();
		}
	}
}
