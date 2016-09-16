
package gumuc1;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Observable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 
 * Abstract class for IP-controlled Device.
 * Only has IP address info 
 * 
 *
 */
public abstract class IPDevice extends Observable{
	/**
	* Logger for this class
	*/
	private static final Logger logger = Logger.getLogger(IPDevice.class);
	
	final private InetAddress myAddress;
	enum ConnectionState{
		UNDEFINED("Подключение неизвестно"),
		CONNECTED("Подключено"),
		UNCONNECTED("Не подключено");
	    public final String name ;
	    ConnectionState(String in_name){
	    	name = in_name;
	    }
		public static ConnectionState fromBoolean(boolean val){
			return (val)?CONNECTED:UNCONNECTED;
		}
	};
	
	private ConnectionState currentConnectionState = ConnectionState.UNDEFINED;
	
	enum PowerState{
		UNDEFINED("Включение неизвестно"),
		POWERED_UP("Включено"),
		POWERED_DOWN("Выключено (standby)");
	    public final String name;
		PowerState(String in_name){
	    	name = in_name;
	    }
		public static PowerState fromBoolean(boolean val){
			return (val)?POWERED_UP:POWERED_DOWN;
		}
	};
	
	private PowerState currentPowerState = PowerState.UNDEFINED;
	
	public ConnectionState getCurrentConnectionState() {
		return currentConnectionState;
	}

	public PowerState getCurrentPowerState() {
		return currentPowerState;
	}
	
	
	protected void setCurrentConnectionState(ConnectionState newState){
		if(currentConnectionState != newState){
			currentConnectionState = newState;
			this.setChanged();
			this.notifyObservers();
		}
	}; 
	
	protected void setCurrentPowerState(PowerState newState){
		if(currentPowerState != newState){
			currentPowerState = newState;
			this.setChanged();
			this.notifyObservers();
		}
	}; 
	
	public InetAddress getAddr() {
		return myAddress;
	}
	
	public String getIPString() {
		return myAddress.getHostAddress();
	}

	IPDevice(InetAddress in_IP){
		myAddress = in_IP;
	}
	
	@JsonCreator
	IPDevice(@JsonProperty("ip") String ipString) throws UnknownHostException{
		this(InetAddress.getByName(ipString));
	}
	
	protected abstract int getConnectedTimout();
	final public void establishConnected(){
		new Thread(new Runnable(){
			public void run(){
				try{
					logger.info("Sending ping");
					boolean result = myAddress.isReachable(getConnectedTimout());
					setCurrentConnectionState(ConnectionState.fromBoolean(result));
					logger.info("ping result:" + result);
				}
				catch(IOException e){
					setCurrentConnectionState(ConnectionState.UNDEFINED);
				}
			}
		}, "PING_"+getIPString()).start();
	}
	
	public abstract void establishPowered();
	
}
