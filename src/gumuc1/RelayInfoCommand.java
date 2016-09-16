package gumuc1;

class RelayInfoCommand extends RelayCommand{
	public RelayInfoCommand(Relay whoSent) {
		super(whoSent);
	}

	public static final String command =  "!GetAll";
}