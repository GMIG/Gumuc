package gumuc1;

import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.function.Consumer;

public class  EventCounterSafe <T>{
	/**
	* Logger for this class
	*/
	private static final Logger logger = Logger.getLogger(EventCounterSafe.class);	
	
	public T state;
	Consumer<Integer> onCounter;

	
	public class ChangeListener implements Observer{
		@Override
		public void update(Observable o, Object arg) {
			if(o instanceof IPDevice){
				IPDevice dev = (IPDevice) o;
				if((IPDevice.PowerState.class.equals(state.getClass()) &&  dev.getCurrentPowerState() == state) ||
						(IPDevice.ConnectionState.class.equals(state.getClass()) && dev.getCurrentConnectionState() == state)	){
					logger.info("Adding device. Total length " +  observableDevicesThatReceivedConfirm.size());
					observableDevicesThatReceivedConfirm.add(dev);
					if(observableDevicesThatReceivedConfirm.size() >= observableDevices.size()){
						logger.info("Executing");
						onCounter.accept(observableDevices.size());
						observableDevices.forEach((device)->device.deleteObserver(this));
						//observableDevices.clear();
						//observableDevicesThatReceivedConfirm.clear();
						
					}		
				}
			}
		}
	}
	
	
	Set<IPDevice> observableDevices = Collections.synchronizedSet(new HashSet<IPDevice>());
	Set<IPDevice> observableDevicesThatReceivedConfirm = Collections.synchronizedSet(new HashSet<IPDevice>());

	public EventCounterSafe(Collection<? extends IPDevice> devices,Consumer<Integer> onCounter,T state) {
		observableDevices.addAll(devices);
		this.state = state;
		this.onCounter = onCounter;
		ChangeListener listener = new ChangeListener();
		observableDevices.forEach((device)->device.addObserver(listener));
	}
}
