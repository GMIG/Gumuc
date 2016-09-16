package gumuc1;

import java.nio.charset.Charset;
import java.util.LinkedList;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.filterchain.IoFilter.NextFilter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.textline.LineDelimiter;
import org.apache.mina.filter.codec.textline.TextLineDecoder;


public class RelayDecoder extends TextLineDecoder{
	
	class CountReply{
    	public static final String prefixReply =  "!COUNT";
    	private int time;
    	CountReply(int in_time){
    		time = in_time;
    	}
		public int getTime() {
			return time;
		}
		public void setTime(int time) {
			this.time = time;
		}
	}

	class SwitchReply{
    	public SwitchReply(int ledID, boolean cmdID) {
			this.ledID = ledID;
			this.statusID = cmdID;
		}

		public static final String prefixReply =  "!LED";
    	public static final String onReply =  "ON";
    	public static final String offReply =  "OFF";

    	private int ledID;
		private boolean statusID;

    	public int getLedID() {
			return ledID;
		}
		public void setLedID(int ledID) {
			this.ledID = ledID;
		}
		public boolean getCmdID() {
			return statusID;
		}
		public void setCmdID(boolean cmdID) {
			this.statusID = cmdID;
		}
		
		public boolean answers(RelaySwitchCommand command){
			return (command.getCmdID() == getCmdID() && command.getLedID() == getLedID() );
		}
	}

	public RelayDecoder() {
        super(Charset.forName("UTF-8"), LineDelimiter.MAC);
	}
	
    private Object parseCommand(String line)  {
    	
    	if(line.contains(CountReply.prefixReply))
    		return new CountReply(Integer.parseInt(line.substring(6)));

    	if(line.contains(RelayDecoder.SwitchReply.prefixReply)){
    		if(line.contains(RelayDecoder.SwitchReply.onReply))
    			return new RelayDecoder.SwitchReply(Integer.parseInt(line.substring(6)),true);
    		if(line.contains(RelayDecoder.SwitchReply.offReply))
    			return new RelayDecoder.SwitchReply(Integer.parseInt(line.substring(7)),false);
    	}
    	
    	return null;
    	
    }
	
    @Override
    public void decode(IoSession session, IoBuffer in, final ProtocolDecoderOutput out) throws Exception {
    	
        final LinkedList<String> lines = new LinkedList<String>();
        super.decode(session, in, new ProtocolDecoderOutput() {
            public void write(Object message) {
                lines.add((String) message);
            }
            public void flush(NextFilter nextFilter, IoSession session) {}
        });
        
        for (String s: lines) {
            out.write(parseCommand(s));
        }
    }

}
