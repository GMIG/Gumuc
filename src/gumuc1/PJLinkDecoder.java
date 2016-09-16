package gumuc1;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedList;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.filterchain.IoFilter.NextFilter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.textline.LineDelimiter;
import org.apache.mina.filter.codec.textline.TextLineDecoder;

public class PJLinkDecoder extends TextLineDecoder {
	
	public static final String powerUpCommand = "%1POWR 1" ;
	public static final String powerDownCommand = "%1POWR 0" ;
	public static final String powerInfoCommand = "%1POWR ?" ;

	static abstract class PJLinkReply{ public abstract String getName();};
	
    static class PJLinkWelcomeReply extends PJLinkReply{
    	public static final String reply =  "PJLINK 0";
    	public String getName()			{return reply;};
    }
    
    static class PJLinkPowerChangeOKReply extends PJLinkReply {
    	public static final String reply =  "%1POWR=OK";
    	public String getName()		{return reply;}
    }
    //static class PJLinkPowerChangeOKReply extends PJLinkReply{public String getName()		{return "%1POWR=OK";}}
    
    static class PJLinkInfoPowerUpReply extends PJLinkReply{
    	public static final String reply =  "%1POWR=1";
    	public String getName()			{return reply;}
    }
    static class PJLinkInfoPowerDownReply extends PJLinkReply{
    	public static final String reply =  "%1POWR=0";
    	public String getName()		{return reply;}}

     class PJLinkPowerErrorReply extends PJLinkReply{
    	public static final String reply =  "%1POWR=ERR";
    	public int errorCode;
    	public String getName()	{return reply;}
    	PJLinkPowerErrorReply(int in_errorCode){
    		errorCode = in_errorCode;
    	}
    }
    
    ArrayList<PJLinkReply> allPJLinkReplies = new ArrayList<>();
	
	public PJLinkDecoder() {
        super(Charset.forName("UTF-8"), LineDelimiter.MAC);
        allPJLinkReplies.add(new PJLinkWelcomeReply());
        allPJLinkReplies.add(new PJLinkPowerChangeOKReply());
        allPJLinkReplies.add(new PJLinkInfoPowerUpReply());
        allPJLinkReplies.add(new PJLinkInfoPowerDownReply());
	}

    private Object parseCommand(String line)  {
    	for(PJLinkReply reply:allPJLinkReplies){
    		if(reply.getName().contains(line))
    			return reply;
    	}
    	if(line.contains(PJLinkPowerErrorReply.reply))
    		return new PJLinkPowerErrorReply(Integer.valueOf(line.substring(10)));
    	
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
