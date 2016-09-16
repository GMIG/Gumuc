package gumuc1;

class RelaySwitchCommand extends RelayCommand{
	public RelaySwitchCommand(Relay whoSent, int ledID, boolean cmdID) {
		super(whoSent);
		this.ledID = ledID;
		this.cmdID = cmdID;
	}

	public static final String command =  "!SetR";
	public static final String separator =  "_";

	private int ledID;
	private boolean cmdID;
	private int lifetime = 0;

	public int getLifetime() {
		return lifetime;
	}
	public void incrementLifetime() {
		this.lifetime++;
	}
	public int getLedID() {
		return ledID;
	}
	public void setLedID(int ledID) {
		this.ledID = ledID;
	}
	public boolean getCmdID() {
		return cmdID;
	}
	public void setCmdID(boolean cmdID) {
		this.cmdID = cmdID;
	}
	
	public String getCommandString(){
		return (command + ((cmdID)? 1 : 0)+ separator + ledID);
	}
}