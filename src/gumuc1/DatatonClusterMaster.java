package gumuc1;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import org.apache.mina.statemachine.State;

public class DatatonClusterMaster extends Dataton {
	
	enum SoftwareState{
		UNDEFINED("Undefined"),
		NOT_LOADED("Not loaded"),
		LOADED("Loaded"),
		RUNNING("Running"),
		ERROR("Error");

	    public final String name ;
	    SoftwareState(String in_name){
	    	name = in_name;
	    }
	};
	
	private SoftwareState currentSoftwareState = SoftwareState.UNDEFINED;

	public SoftwareState getCurrentSoftwareState() {
		return currentSoftwareState;
	}	
	
	protected void setCurrentSoftwareState(SoftwareState newState){
		currentSoftwareState = newState;
		this.setChanged();
		this.notifyObservers();
	}; 


	//protected AbstractStateContext activeContext = new DatatonMasterCommandContext();
    class DatatonMasterCommandContext extends DatatonCommandContext {

    	@Override
		public void setCurrentState(State state){
			super.setCurrentState(state);
			if(state.getId() == DatatonFSM.UNDEFINED)
				setCurrentPowerState(IPDevice.PowerState.UNDEFINED);	
			if(state.getId() == DatatonFSM.CONNECTION_ESTABLISHED)
				setCurrentConnectionState(IPDevice.ConnectionState.CONNECTED);	
			if(state.getId() == DatatonFSM.SHUT_COMMAND_PASSED)
				setCurrentPowerState(IPDevice.PowerState.POWERED_DOWN);	
			if(state.getId() == DatatonFSM.LOAD_COMMAND_PASSED)
				setCurrentSoftwareState(SoftwareState.LOADED);	

	    }
    	@Override
    	public void hanldePingReceived(){
			setCurrentPowerState(IPDevice.PowerState.POWERED_UP);	
    	}
    	@Override
    	public void hanldeStatusReceived(DatatonDecoder.StatusReply status){
    		if(status.health == 0)
    			setCurrentPowerState(IPDevice.PowerState.POWERED_UP);	
    		else{
    			setCurrentPowerState(IPDevice.PowerState.UNDEFINED);	
    			setCurrentSoftwareState(SoftwareState.UNDEFINED);	
    		}
    		if(status.name!="")
    			setCurrentSoftwareState(SoftwareState.LOADED);	
    		else
    			setCurrentSoftwareState(SoftwareState.NOT_LOADED);	

    	}

		public DatatonMasterCommandContext() {
			super();
		}
    }

	public DatatonClusterMaster(InetAddress in_IP, String mac) {
		super(in_IP, mac);
		activeContext = new DatatonMasterCommandContext();
	}

	public DatatonClusterMaster(String ipString, String mac) throws UnknownHostException {
		super(ipString, mac);
		activeContext = new DatatonMasterCommandContext();
	}
		
	@Override
	public void establishPowered() {
		send( new DatatonEncoder.DatatonAuthCommand(1),new DatatonEncoder.DatatonStatusCommand());
	}
	
	public void load(String show) {
		send( new DatatonEncoder.DatatonAuthCommand(1),new DatatonEncoder.DatatonLoadCommand(show));
	}
	
	public void runTimeline(String show,String...timelines) {
		int no_of_init_commands = 2;
		DatatonEncoder.DatatonCommand[] commands = new DatatonEncoder.DatatonCommand[timelines.length+no_of_init_commands];
		commands[0] = new DatatonEncoder.DatatonAuthCommand(1);
		commands[1] = new DatatonEncoder.DatatonLoadCommand(show);

		for(int i=0;i<timelines.length;i++)
			commands[i+no_of_init_commands] = new DatatonEncoder.DatatonRunCommand(timelines[i]);
		send(commands);
	}
	
	public static void main(String[] args) {
		
		try{
			
			ArrayList <Dataton> datatons = new ArrayList <> ();
			//datatons.add(new Dataton("10.8.1.105","00:20:98:02:d1:25"));
			//Dataton dat = new Dataton("10.8.1.10" + i,"00:20:98:02:d1:25");
			//ArrayList <Dataton> datatonMaster = new ArrayList <> ();

			
			Observer debug = new Observer(){
				@Override
				public void update(Observable arg0, Object arg1) {
					// TODO Auto-generated method stub
					System.out.print(((IPDevice)arg0).getIPString() + ":");
					System.out.print(((IPDevice)arg0).getCurrentPowerState() + " ");
					System.out.println(((IPDevice)arg0).getCurrentConnectionState());
				}
			};
			for(Dataton dat :datatons){

				dat.addObserver(debug);
				//dat.establishConnected();
				//dat.powerOff();
				//StateControl.breakAndCallNext(sm.getState(DatatonFSM.LOAD_COMMAND_PASSED));
			}
			DatatonClusterMaster master = new DatatonClusterMaster("10.8.1.105","00:20:98:02:d1:25");
			master.addObserver(debug);
			//master.establishPowered();
			//master.load("expofin");
			master.runTimeline("expofin","Returns-right",
					"Ours-days-left",
					"MyGULAG"+(int)(Math.random()*12+1),
					"BeginGULAG-214UP",
					"Ot-aresta-214-right",
					"Soviet-214-left",
					"PN-dveri");
			Thread.sleep(10000);
			for(Dataton d :datatons){
				System.out.print(d.getIPString() + "::");
				System.out.print(d.getCurrentPowerState() + " ");
				System.out.println(d.getCurrentConnectionState() +" ");
			}
		}
		
		catch(Exception e){
			e.printStackTrace();
		}
	}

}
