package gumuc1;

import java.nio.charset.Charset;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.apache.mina.filter.codec.textline.LineDelimiter;
import org.apache.mina.filter.codec.textline.TextLineEncoder;

public class RelayEncoder extends TextLineEncoder{
	
	public RelayEncoder() {
        super(Charset.forName("UTF-8"), LineDelimiter.MAC);
	}

    @SuppressWarnings("static-access")
	@Override
    public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
       
    	if(message instanceof RelaySwitchCommand){
    		RelaySwitchCommand switchMessage = (RelaySwitchCommand) message;
    		super.encode(session, switchMessage.getCommandString(), out);
    	}
    	
    	if(message instanceof RelayInfoCommand){
    		RelayInfoCommand infoMessage = (RelayInfoCommand) message;
    		super.encode(session, infoMessage.command, out);
    	}
    	
    	
    }
}
