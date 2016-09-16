package gumuc1;

import java.nio.charset.Charset;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.apache.mina.filter.codec.textline.LineDelimiter;
import org.apache.mina.filter.codec.textline.TextLineEncoder;

public class DatatonEncoder extends TextLineEncoder {

	public static abstract class DatatonCommand{
		public abstract String getCommandString();
		abstract public int getID();
		public String getCommandWithID(){
			return "["+ getID() +"]"+getCommandString();
		}

	};
	
	public static class DatatonPingCommand extends DatatonCommand{
		public static final int ID = DatatonDecoder.PingReply.code;

		public static final String command =  "ping";
		@Override
		public String getCommandString(){
			return command;
		}
		@Override
		public int getID() {
			return ID;
		}

	}
	
	public static class DatatonAuthCommand extends DatatonCommand{
		public static final int ID = DatatonDecoder.AuthReply.code;
		public static final String command =  "authenticate";
		public final int level;
		public DatatonAuthCommand(int level) {
			super();
			this.level = level;
		}
		@Override
		public String getCommandString(){
			return command + " " + String.valueOf(level);
		}

		@Override
		public int getID() {
			return ID;
		}
	}
	
	public static class DatatonLoadCommand extends DatatonCommand{
		public static final int ID = DatatonDecoder.LoadReply.code;
		public static final String command =  "load";
		private final String showToLoad;
		public String getShowToLoad() {
			return showToLoad;
		}

		public DatatonLoadCommand(String show) {
			super();
			this.showToLoad = show;
		}
		@Override
		public String getCommandString(){
			return command + " \"" + showToLoad + "\"";
		}

		@Override
		public int getID() {
			return ID;
		}
	}
	
	public static class DatatonShutdownCommand extends DatatonCommand{
		public static final int ID = DatatonDecoder.ShutdownReply.code;
		public static final String command =  "powerDown";
		public DatatonShutdownCommand() {
			super();
		}
		@Override
		public String getCommandString(){
			return command;
		}
		@Override
		public int getID() {
			return ID;
		}
	}
	
	public static class DatatonStatusCommand extends DatatonCommand{
		public static final int ID = DatatonDecoder.StatusReply.code;
		public static final String command =  "getStatus";
		public DatatonStatusCommand() {
			super();
		}
		@Override
		public String getCommandString(){
			return command;
		}
		@Override
		public int getID() {
			return ID;
		}
	}
	public static class DatatonRunCommand extends DatatonCommand{
		public static final int ID = DatatonDecoder.RunReply.code;
		public static final String command =  "run";
		private final String timelineToRun;
		public String getTimelineToRun() {
			return timelineToRun;
		}
		
		public DatatonRunCommand() {
			super();
			timelineToRun = "";
		}

		public DatatonRunCommand(String timeline) {
			super();
			timelineToRun = timeline;
		}
		@Override
		public String getCommandString(){
			return command + " "+timelineToRun;
		}
		@Override
		public int getID() {
			return ID;
		}
	}
	
	public DatatonEncoder() {
        super(Charset.forName("UTF-8"), LineDelimiter.MAC);
	}

	@Override
    public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
       
    	if(message instanceof DatatonCommand){
    		DatatonCommand command = (DatatonCommand) message;
    		super.encode(session, command.getCommandWithID(), out);
    	}
    }

}
