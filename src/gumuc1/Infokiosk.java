package gumuc1;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Infokiosk extends Brix {

	protected final static int INFOKIOSK_PING_TIMOUT = 5000;
	protected final static int  WOL_TO_ON_DELAY = 5000;

	@Override
	protected int getConnectedTimout() {
		return INFOKIOSK_PING_TIMOUT;
	}

	
	public Infokiosk(InetAddress in_IP, String in_mac) {
		super(in_IP, in_mac);
		this.MagicPacketPort = 9;
		this.userName="Administrator";
		this.password = "1";

	}

	public Infokiosk(String ipString, String in_mac) throws UnknownHostException {
		super(ipString, in_mac);
		this.MagicPacketPort = 9;
		this.userName="Administrator";
		this.password = "1";

	}

	
	public static void main(String[] args) {	
		try{
			Infokiosk in = new Infokiosk("10.8.5.1","d8:cb:8a:97:44:25");
			in.powerUp();
			//in.establishConnected();
			//in.powerDown();
			
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

}

