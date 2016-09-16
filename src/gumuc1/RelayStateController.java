package gumuc1;

import java.util.EnumMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

public class RelayStateController extends IPDeviceStateChangeController implements Observer{
	
	 public static final Map<RelayState, Color> relayStateColorMap = new EnumMap<RelayState, Color>(RelayState.class);
	 static {
		 relayStateColorMap.put(RelayState.LIGHT_OFF, Color.BLACK);
		 relayStateColorMap.put(RelayState.LIGHT_ON, Color.AQUA);
		 relayStateColorMap.put(RelayState.UNDEFINED, Color.GREY);
	 }
	 
	 private final ObjectProperty<Paint> upStateFill = new ObjectPropertyBase<Paint>(){
		@Override
		public Object getBean() {
			return this;
		}

		@Override
		public String getName() {
			return "upStateFill";
		}	 
	 };

	 private final ObjectProperty<Paint> downStateFill = new ObjectPropertyBase<Paint>(){
		@Override
		public Object getBean() {
			return this;
		}

		@Override
		public String getName() {
			return "downStateFill";
		}	 
	 };

	public RelayStateController(ObjectProperty<Paint> upStateFill, ObjectProperty<Paint> downStateFill, 
																		ObjectProperty<Paint> connectionStateFill) {
		super(null, connectionStateFill);
		this.upStateFill.set(Color.LIGHTGRAY);
		this.downStateFill.set(Color.LIGHTGRAY);

		upStateFill.bind(this.upStateFill);
		downStateFill.bind(this.downStateFill);

		connectionStateFill.bind(this.connectionStateFill);
	}
	
	@Override
	public void update(Observable device, Object arg1) {
		if(!(device instanceof Relay))
			return;
		Relay relay = (Relay) device;
		upStateFill.set(relayStateColorMap.get(relay.getRelay2State()));
		downStateFill.set(relayStateColorMap.get(relay.getRelay1State()));

		connectionStateFill.set(connectionStateColorMap.get(relay.getCurrentConnectionState()));
	}

}
