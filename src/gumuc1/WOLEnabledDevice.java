package gumuc1;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class WOLEnabledDevice extends IPDevice {
	/**
	* Logger for this class
	*/
	protected int MagicPacketPort = 0;
	private static InetAddress MagicPacketAddress;

	
	// Duration between ping received and full operation
	protected abstract int getOnToActiveDelay();
	
	private static final Logger logger = Logger.getLogger(WOLEnabledDevice.class);
	
	final private String myMacAddr;
	final private String[] macAddrHex;
	
	private Thread WOLSenderThread;

	public String getMacAddr() {
		return myMacAddr;
	}
	
	public static final char SEPARATOR = ':';

	public WOLEnabledDevice(InetAddress in_IP, String mac) {
		super(in_IP);
		myMacAddr = cleanMac(mac);
		macAddrHex = validateMac(myMacAddr);
		try {
			MagicPacketAddress = InetAddress.getByName("255.255.255.255");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	public WOLEnabledDevice(String ipString, String mac) throws UnknownHostException {
		super(ipString);
		myMacAddr = cleanMac(mac);
		macAddrHex = validateMac(myMacAddr);
		try {
			MagicPacketAddress = InetAddress.getByName("255.255.255.255");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	public  String cleanMac(String mac) throws IllegalArgumentException
	{
		final String[] hex = validateMac(mac);

		StringBuffer sb = new StringBuffer();
		boolean isMixedCase = false;

		// check for mixed case
		for(int i=0; i<6; i++) {
			sb.append(hex[i]);
		}
		String testMac = sb.toString();
		if((testMac.toLowerCase().equals(testMac) == false) && (testMac.toUpperCase().equals(testMac) == false)) {
			isMixedCase = true;
		}

		sb = new StringBuffer();
		for(int i=0; i<6; i++) {
			// convert mixed case to lower
			if(isMixedCase == true) {
				sb.append(hex[i].toLowerCase());
			}else{
				sb.append(hex[i]);
			}
			if(i < 5) {
				sb.append(SEPARATOR);
			}
		}
		return sb.toString();
	}

	private  String[] validateMac(String mac) throws IllegalArgumentException
	{
		// error handle semi colons
		mac = mac.replace(";", ":");

		// attempt to assist the user a little
		String newMac = "";

		if(mac.matches("([a-zA-Z0-9]){12}")) {
			// expand 12 chars into a valid mac address
			for(int i=0; i<mac.length(); i++){
				if((i > 1) && (i % 2 == 0)) {
					newMac += ":";
				}
				newMac += mac.charAt(i);
			}
		}else{
			newMac = mac;
		}

		// regexp pattern match a valid MAC address
		final Pattern pat = Pattern.compile("((([0-9a-fA-F]){2}[-:]){5}([0-9a-fA-F]){2})");
		final Matcher m = pat.matcher(newMac);

		if(m.find()) {
			String result = m.group();
			return result.split("(\\:|\\-)");
		}else{
			throw new IllegalArgumentException("Invalid MAC address");
		}
	}
	
	public interface magicPacketSenderCallback{
		public void packetSent(int noOfPacket);
		public void confirmationReceived();
		public void confiramtionNotReceived();
		public void socketError(IOException e);
	}
	
	private int magicPacketResenderLimit = 5;

	private int pingTimeout = 20000;
	
	public int getPingTimeout() {
		return pingTimeout;
	}

	public void setPingTimeout(int pingTimeout) {
		this.pingTimeout = pingTimeout;
	}

	public int getMagicPacketResenderLimit() {
		return magicPacketResenderLimit;
	}

	public void setMagicPacketResenderLimit(int magicPacketResenderLimit) {
			this.magicPacketResenderLimit = magicPacketResenderLimit;
	}

	public void sendMagicPacketWithConfirmation(magicPacketSenderCallback callback){
		if (WOLSenderThread == null ||  
				(WOLSenderThread.getState() != Thread.State.WAITING && 
				WOLSenderThread.getState() != Thread.State.RUNNABLE)){
			WOLSenderThread = new Thread( new Runnable(){
				public void run(){
					try {
						int noOfResendings = 0;
						while(noOfResendings < magicPacketResenderLimit){
							sendSingleMagicPacket();
							logger.info("Sending wakeup to " + myMacAddr);
							callback.packetSent(noOfResendings);
							if(getAddr().isReachable(pingTimeout)){
								logger.info("Reply received from " + myMacAddr);
								Thread.sleep(getOnToActiveDelay());
								callback.confirmationReceived();
								return;
							}
							else
								noOfResendings += 1;
						}
						logger.info("Reply not received from " + myMacAddr);
						callback.confiramtionNotReceived();
						
					} catch (IOException e ) {
						callback.socketError(e);
					}
					catch(InterruptedException e){
						e.printStackTrace();
					}
					
				}
			},"WOLTester");
			WOLSenderThread.start();
		}
		else
			logger.info("Concurrent send wakeup task in " + myMacAddr);
		
	}
		
	
	private void sendSingleMagicPacket() throws IOException 
	{
		logger.debug("Sending wakeup to" + myMacAddr);
		
		// convert to base16 bytes
		final byte[] macBytes = new byte[6];
		for(int i=0; i<6; i++) {
			macBytes[i] = (byte) Integer.parseInt(macAddrHex[i], 16);
		}

		final byte[] bytes = new byte[102];

		// fill first 6 bytes
		for(int i=0; i<6; i++) {
			bytes[i] = (byte) 0xff;
		}
		// fill remaining bytes with target MAC
		for(int i=6; i<bytes.length; i+=macBytes.length) {
			System.arraycopy(macBytes, 0, bytes, i, macBytes.length);
		}

		// create socket to IP
		final DatagramPacket packet = new DatagramPacket(bytes, bytes.length, MagicPacketAddress, MagicPacketPort);
		final DatagramSocket socket = new DatagramSocket();
		socket.send(packet);
		socket.close();
					
	}
}
