package gumuc1;

import java.util.EnumMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

public class IPDeviceStateChangeController implements Observer{
	
	 public static final Map<IPDevice.PowerState, Color> powerStateColorMap = new EnumMap<IPDevice.PowerState, Color>(IPDevice.PowerState.class);
	 static {
		 powerStateColorMap.put(IPDevice.PowerState.POWERED_DOWN, Color.BLACK);
		 powerStateColorMap.put(IPDevice.PowerState.POWERED_UP, Color.GREEN);
		 powerStateColorMap.put(IPDevice.PowerState.UNDEFINED, Color.GREY);
	 }
	
	 public static final Map<IPDevice.ConnectionState, Color> connectionStateColorMap = new EnumMap<IPDevice.ConnectionState, Color>(IPDevice.ConnectionState.class);
	 static {
		 connectionStateColorMap.put(IPDevice.ConnectionState.CONNECTED, Color.GREEN);
		 connectionStateColorMap.put(IPDevice.ConnectionState.UNCONNECTED, Color.RED);
		 connectionStateColorMap.put(IPDevice.ConnectionState.UNDEFINED, Color.GREY);
	 }
	 
	 protected final ObjectProperty<Paint> powerStateFill = new ObjectPropertyBase<Paint>(){
		@Override
		public Object getBean() {
			return this;
		}

		@Override
		public String getName() {
			return "powerStateFill";
		}	 
	 };
	 protected final ObjectProperty<Paint> connectionStateFill= new ObjectPropertyBase<Paint>(){
			@Override
			public Object getBean() {
				return this;
			}

			@Override
			public String getName() {
				return "connectionStateFill";
			}	 
		 };

	public IPDeviceStateChangeController(ObjectProperty<Paint> powerStateFill, ObjectProperty<Paint> connectionStateFill) {
		
		if(powerStateFill != null){
			this.powerStateFill.set(Color.LIGHTGRAY);
			powerStateFill.bind(this.powerStateFill);
		}
		
		this.connectionStateFill.set(Color.LIGHTGRAY);
		connectionStateFill.bind(this.connectionStateFill);
	}

	@Override
	public void update(Observable device, Object arg1) {
		if(!(device instanceof IPDevice))
			return;
		IPDevice ipdevice = (IPDevice) device;
		powerStateFill.set(powerStateColorMap.get(ipdevice.getCurrentPowerState()));
		connectionStateFill.set(connectionStateColorMap.get(ipdevice.getCurrentConnectionState()));
	}

}
