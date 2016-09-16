package gumuc1;

import org.apache.log4j.Logger;

import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.filterchain.IoFilter.NextFilter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.textline.LineDelimiter;
import org.apache.mina.filter.codec.textline.TextLineDecoder;



public class DatatonDecoder extends TextLineDecoder {
	/**
	* Logger for this class
	*/
	private static final Logger logger = Logger.getLogger(DatatonDecoder.class);

	static public final int uniqueCode = 22550;

	static abstract class DatatonReply{ 
		public abstract int getCode();
		public abstract void parse(String line);
	};

    static class PingReply extends DatatonReply{
    	public int getCode(){
    		return code;
    	}
    	static public final int code = uniqueCode + 1;
		@Override
		public void parse(String line) {}
    }
    
    static class AuthReply extends DatatonReply{
    	public int getCode(){
    		return code;
    	}
    	static public final int code = uniqueCode + 2;
		@Override
		public void parse(String line) {}
    }
	
    static class LoadReply extends DatatonReply{
    	
    	boolean done = false;
    	
    	public int getCode(){
    		return code;
    	}
    	static public final int code = uniqueCode + 3;
		@Override
		public void parse(String line) {
				done = line.contains("Ready");			
		}
    	LoadReply(String line){
    		parse(line);
    	}

    }
    
    static class ShutdownReply extends DatatonReply{
    	public int getCode(){
    		return code;
    	}
    	static public final int code = uniqueCode + 4;
		@Override
		public void parse(String line) {}
    }
    
    static class RunReply extends DatatonReply{
    	public int getCode(){
    		return code;
    	}
    	static public final int code = uniqueCode + 5;
		@Override
		public void parse(String line) {}
    }
    
    static class StatusReply extends DatatonReply{
    	
    	String name;
    	boolean busy;
    	int health;
    	boolean displayOpen;
    	boolean showActive;
    	StatusReply(String line){
    		parse(line);
    	}
    	public int getCode(){
    		return code;
    	}
    	static public final int code = uniqueCode + 6;
		@Override
		public void parse(String line) {
			String[] tokens = line.split(" ");
			if (tokens[0].contains("Ready"))
				logger.warn("Ready Not received");
			if (tokens[1].length()-1 <= 0){
				System.out.println("fff");
			}

			name = tokens[1].substring(1, tokens[1].length()-1);
			busy = Boolean.getBoolean(tokens[2]);
			health = Integer.parseInt(tokens[3]);
			displayOpen = Boolean.getBoolean(tokens[4]);
			showActive = Boolean.getBoolean(tokens[5]);

		}
    }
    
    
    
	public DatatonDecoder() {
        super(Charset.forName("UTF-8"), LineDelimiter.CRLF);
	}
    
	private Object parseCommand(String line)  {
		logger.info(line);
		 Pattern p = Pattern.compile("\\[\\w+\\]");
		 Matcher m = p.matcher(line);
		 int extractedID;
		 if(m.find()){
			 extractedID = Integer.parseInt( m.group().subSequence(1, m.group().length()-1).toString());
		 }
		 else 
			 return null;
		 
		if(extractedID == PingReply.code)
			return new PingReply();
		if(extractedID == AuthReply.code)
			return new AuthReply();
		if(extractedID == StatusReply.code)
			return new StatusReply(line);
		if(extractedID == ShutdownReply.code)
			return new ShutdownReply();
		if(extractedID == LoadReply.code)
			return new LoadReply(line);
		if(extractedID == RunReply.code)
			return new RunReply();

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
