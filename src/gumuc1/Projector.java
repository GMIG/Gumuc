package gumuc1;

import org.apache.log4j.Logger;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import org.apache.mina.core.RuntimeIoException;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.LineDelimiter;
import org.apache.mina.filter.codec.textline.TextLineEncoder;
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

/**
 * 
 * PJ-Link controlled projector
 * Contains high-level methods for handling pj-link operation 
 * 
 *
 */
public class Projector extends IPDevice implements PowerSwitchable{
	/**
	* Logger for this class
	*/
	private static final Logger logger = Logger.getLogger(Projector.class);
	
	protected final static int PROJECTOR_PING_TIMOUT = 5000;
    private final static int CONNECT_TIMEOUT_PJLINK = 1000;
    private final static int PJLINK_DEFAULT_PORT = 4352;
	
    private  NioSocketConnector connector;    
    private static ProjectorFSM projFSM= new ProjectorFSM(); 
    private static StateMachine sm = StateMachineFactory.getInstance(
    				IoHandlerTransition.class).create(ProjectorFSM.UNDEFINED,
            		projFSM);
    private IoSession activeSession; 
	private ProjectorCommandContext activeContext = new ProjectorCommandContext();
    
	@Override
	protected int getConnectedTimout() {
		return PROJECTOR_PING_TIMOUT;
	}

    class ProjectorCommandContext extends AbstractStateContext{
    	String command;
    	@Override
    	public void setCurrentState(State state){
    		if(state.getId() == ProjectorFSM.UNDEFINED)
    			setCurrentPowerState(IPDevice.PowerState.UNDEFINED);
    		if(state.getId() == ProjectorFSM.POWERED_UP){
    			setCurrentPowerState(IPDevice.PowerState.POWERED_UP);
    			//setCurrentConnectionState(IPDevice.ConnectionState.CONNECTED);
    		}
    		if(state.getId() == ProjectorFSM.POWERED_DOWN)
    			setCurrentPowerState(IPDevice.PowerState.POWERED_DOWN);
    		if(state.getId() == ProjectorFSM.CONNECTION_ESTABLISHED)
    			setCurrentConnectionState(IPDevice.ConnectionState.CONNECTED);
    		super.setCurrentState(state);
    	}	
    };
   
    private static IoHandler createIoHandler(ProjectorCommandContext context) {		
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
    
    private void send(String command) {
    	activeContext.command = command;
    	if(activeSession!=null && activeSession.isConnected()){
    		activeSession.write(activeContext.command);
    	}
    	else{
		    connector.setHandler(createIoHandler(activeContext));
	        try {
	            ConnectFuture future = connector.connect(new InetSocketAddress(getAddr(), PJLINK_DEFAULT_PORT));
	            future.awaitUninterruptibly();
	            future.addListener(new IoFutureListener<ConnectFuture>(){

					@Override
					public void operationComplete(ConnectFuture arg0) {
			            activeSession = arg0.getSession();
			    		//activeSession.write(activeContext.command);
					}
	            	
	            });
	        } catch (RuntimeIoException e) {
	        	//setCurrentConnectionState(IPDevice.ConnectionState.UNCONNECTED);
	        	logger.warn("TCP not connected in "+ getIPString());
	        	setCurrentPowerState(IPDevice.PowerState.UNDEFINED);
	        }
    	}
    }
	
    
	public Projector(InetAddress in_IP) {
		super(in_IP);
		initConnector();
	}

	public Projector(String ipString) throws UnknownHostException {
		super(ipString);
		initConnector();
	}
	
	private void initConnector(){
		connector = new NioSocketConnector();
		ProtocolCodecFilter pcf = new ProtocolCodecFilter(
				new TextLineEncoder(Charset.forName("UTF-8"), LineDelimiter.MAC), new PJLinkDecoder());
		connector.setConnectTimeoutMillis(CONNECT_TIMEOUT_PJLINK);
	    connector.getFilterChain().addLast("log1", new LoggingFilter("log1"));
	    connector.getFilterChain().addLast("decode", pcf);
	    connector.getFilterChain().addLast("log2", new LoggingFilter("log2"));

	}
	
	public void powerUp() {
	    send(PJLinkDecoder.powerUpCommand);
	}
	
	public void powerDown() {
	    send(PJLinkDecoder.powerDownCommand);
	}
	
	@Override
	public void establishPowered() {
	    send(PJLinkDecoder.powerInfoCommand);
	}
	
	public static void main(String[] args) {
		
		try{
			
			/*for(int i=1; i<11;i++){
			Projector proj = new Projector("10.8.3."+i);
			//proj.powerOff();
			//proj2.establishPowered();
			Observer debug = new Observer(){

				@Override
				public void update(Observable arg0, Object arg1) {
					// TODO Auto-generated method stub
					System.out.print(((IPDevice)arg0).getIPString() + ":");
					System.out.print(((IPDevice)arg0).getCurrentPowerState() + " ");
					System.out.println(((IPDevice)arg0).getCurrentConnectionState());
				}
			};
			proj.addObserver(debug);
			Thread.sleep(3000);
			proj.powerOff();
			}*/
			
			//Thread.sleep(10000);
			ArrayList <Projector> projs = new ArrayList <>();
			for(int i=1; i<11;i++){
				Projector proj = new Projector("10.8.3."+i);
				projs.add(proj);
				Observer debug = new Observer(){

					@Override
					public void update(Observable arg0, Object arg1) {
						// TODO Auto-generated method stub
						System.out.print(((IPDevice)arg0).getIPString() + ":");
						System.out.print(((IPDevice)arg0).getCurrentPowerState() + " ");
						System.out.println(((IPDevice)arg0).getCurrentConnectionState());
					}
				};
				proj.addObserver(debug);
			//
		
				//proj.establishPowered();
			//
				proj.powerUp();

			//System.out.println(proj.getCurrentPowerState());
			}
			Thread.sleep(10000);
			for(Projector pr:projs){
				System.out.print(pr.getIPString() + ":");
				System.out.print(pr.getCurrentConnectionState() +" ");
				System.out.println(pr.getCurrentPowerState());
			}
			//proj.powerOff();
			//proj.establishPowered();

		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

}
