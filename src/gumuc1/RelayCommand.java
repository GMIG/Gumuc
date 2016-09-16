package gumuc1;

class RelayCommand{
	private final Relay whoSent;

	public RelayCommand(Relay whoSent) {
		super();
		this.whoSent = whoSent;
	}

	public Relay getWhoSent() {
		return whoSent;
	}

}