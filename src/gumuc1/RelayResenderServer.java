package gumuc1;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioDatagramAcceptor;

import gumuc1.RelayResenderDecoder.RelayCommandForResending;

public class RelayResenderServer extends IoHandlerAdapter {

   private static RelayResenderServer instance = null;

   public static RelayResenderServer getInstance() {
      if(instance == null) {
         instance = new RelayResenderServer();
      }
      return instance;
   }

	public static final int INPUT_PORT = 10203;

	public RelayResenderServer() {
        NioDatagramAcceptor server = new NioDatagramAcceptor();
		ProtocolCodecFilter pcf = new ProtocolCodecFilter(
				 new RelayEncoder(), new RelayResenderDecoder());
		server.setCloseOnDeactivation(false);
		server.getFilterChain().addLast("log1", new LoggingFilter("log1"));
		server.getFilterChain().addLast("endecode", pcf);
		server.getFilterChain().addLast("log2", new LoggingFilter("log2"));

		server.setHandler(this);
        try {
        	server.bind(new InetSocketAddress(INPUT_PORT));
		} catch (IOException e) {
			e.printStackTrace();
		}	
    }

	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		super.messageReceived(session, message);
		if (message instanceof RelayCommandForResending ){
			RelayCommandForResending command = (RelayCommandForResending)message;
			Relay relay = RelayServer.getInstance().getIPTable().get(command.relayIPID);
			relay.sendSwitchCommand(command.relayID,command.cmdID);
		}

	}

	@Override
	public void messageSent(IoSession session, Object message) throws Exception {
		// TODO Auto-generated method stub
		super.messageSent(session, message);
	}
	
	public static void main(String[] args) {
		
		try{
			RelayServer.getInstance().addClient(new Relay("10.8.4.1"));
			RelayResenderServer.getInstance().getClass();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

}
