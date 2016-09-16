package gumuc1;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

public class PSShutdownExecutor implements Runnable {

	private static final Logger logger = Logger.getLogger(PSShutdownExecutor.class);
	public final static int PS_SHUTDOWN_RESPONCE_TIMOUT = 60000;
	
	private static String psshutdownEXEPath = "C:\\pstools\\";
	public static String getPsshutdownEXEPath() {
		return psshutdownEXEPath;
	}

	private static boolean psshutdownEXEPathLocked = false;

	public static void setPsshutdownEXEPath(String psshutdownEXEPath) throws FileNotFoundException {
		if(!psshutdownEXEPathLocked)
			PSShutdownExecutor.psshutdownEXEPath = psshutdownEXEPath;
		File f = new File(psshutdownEXEPath + psshutdownEXE);
		if(!f.exists()){
			logger.warn("PSshutdown.exe not found at " + psshutdownEXEPath + psshutdownEXE);
			throw new FileNotFoundException();
		}

	}

	private static final String psshutdownEXE = "psshutdown.exe";

	public static String getPsshutdownexe() {
		return psshutdownEXE;
	}

	public static final String restartCommand = "-r";
	public static final String shutdownCommand = "-s";
	public static final String poweroffCommand = "-k"; // DO NOT DO THIS NO WOL ACTIVE

	public static final String delayFlag= "-t";
	public static final String timoutFlag= "-n";
	public static final String forceShutdownFlag= "-f";
	public static final String userFlag= "-u";
	public static final String passwordFlag= "-p";
	
	private final String commandToExecute;

	private StringBuffer output = new StringBuffer();
	private Process commandExecutor;

	public interface PSShutdownExecutorCallback{
		public void confirmationReceived();
		public void confiramtionNotReceived();
	}
	
	PSShutdownExecutorCallback activeCallback;
	
	public PSShutdownExecutor(String IP, String command, String userName,String password, PSShutdownExecutorCallback callback) {
		psshutdownEXEPathLocked = true;

		commandToExecute = psshutdownEXEPath + psshutdownEXE + " \\\\"+ IP + " " + command + " " + 
									userFlag + " " + userName + " " + passwordFlag + " " + password + " ";
		File f = new File(psshutdownEXEPath + psshutdownEXE);
		if(!f.exists()){
			callback.confiramtionNotReceived();
			logger.warn("PSshutdown.exe not found at " + psshutdownEXEPath + psshutdownEXE);
		}
		else
			activeCallback = callback;
	}

	private void execShell(String command){
		try {
			// c:\psshutdown.exe \\10.8.0.32 -s -u brix -p ""
	    	logger.info("psshutdown sending:" + command);
			commandExecutor = Runtime.getRuntime().exec(command);
			boolean success = commandExecutor.waitFor(PS_SHUTDOWN_RESPONCE_TIMOUT,TimeUnit.MILLISECONDS);
			
			if(success){
				BufferedReader reader = 
				         new BufferedReader(new InputStreamReader(commandExecutor.getInputStream()));
			    String line = "";		
			    boolean confirmationReceived = false;
			    while ((line = reader.readLine())!= null) {
			    	output.append(line + "\n");
			    	logger.info("psshutdown console output:" + line);
			    	if (line.contains("is scheduled to shut down in")){
			    		activeCallback.confirmationReceived();
			    		confirmationReceived = true;
			    	}
			    	if (!confirmationReceived)
			    		activeCallback.confiramtionNotReceived();
			    }
			}
			else{
				logger.warn("No PSshutdown answer received");
	    		activeCallback.confiramtionNotReceived();
			}
				
				
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		execShell(commandToExecute);
	}

}
