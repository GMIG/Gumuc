package gumuc1;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

class EventCounter <T> implements Observer{
	public EventCounter(Consumer<Integer> onCounter,T state, int exeThreshold) {
		super();
		this.onCounter = onCounter;
		this.state = state;
		this.exeThreshold = exeThreshold;
		counter.set(0);
	}
	public int exeThreshold;
	public static AtomicInteger counter = new AtomicInteger(0);
	public T state;
	Consumer<Integer> onCounter;
	@Override
	public void update(Observable arg0, Object arg1) {
		if(arg0 instanceof IPDevice){
			IPDevice dev = (IPDevice) arg0;
			if((IPDevice.PowerState.class.equals(state.getClass()) &&  ((IPDevice) arg0).getCurrentPowerState() == state) ||
					(IPDevice.ConnectionState.class.equals(state.getClass()) && ((IPDevice) arg0).getCurrentConnectionState() == state)	){
				Exposition.logger.info("counter value " + counter.get());
				if(counter.get() == exeThreshold){
					counter.incrementAndGet();
					Exposition.logger.info("execing because of" + dev.getCurrentConnectionState() + " or " + dev.getCurrentPowerState());
					dev.deleteObserver(this);
					onCounter.accept(counter.get());
				}
				else if (counter.get() > exeThreshold){
					dev.deleteObserver(this);
				}
				else{
					counter.incrementAndGet();
				}
			}
		}
	}
}