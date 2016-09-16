package gumuc1;

import java.nio.charset.Charset;
import java.util.LinkedList;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.filterchain.IoFilter.NextFilter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.textline.LineDelimiter;
import org.apache.mina.filter.codec.textline.TextLineDecoder;

public class RelayResenderDecoder extends TextLineDecoder {

	public RelayResenderDecoder() {
        super(Charset.forName("UTF-8"), LineDelimiter.MAC);
	}
	
	class RelayCommandForResending{
		static final String relaysSubnet = "10.8.4.";
		public final int relayID;
		public final boolean cmdID;
		public final String relayIPID;
		RelayCommandForResending(String relayIPID,  int relayID,boolean cmdID){
			if(relayIPID.startsWith("0")) relayIPID=relayIPID.substring(1,2);
			this.relayIPID = relaysSubnet + relayIPID;
			this.relayID = relayID;
			this.cmdID = cmdID;
		}
	}

    private Object parseCommand(String line)  {
		String IP = line.substring(0, 2);
		int relayID = Integer.parseInt(line.substring(2,3));
		boolean cmdID = (line.substring(3,4).contains("1"))?true:false;
    	return new RelayCommandForResending(IP,relayID,cmdID);
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
