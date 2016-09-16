package gumuc1;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioDatagramAcceptor;

public class RelayServer extends IoHandlerAdapter {
	
	   private static RelayServer instance = new RelayServer();

	   public static RelayServer getInstance() {
	      return instance;
	   }


	public static final int RELAY_PORT = 7777;
	private HashMap <String,Relay> relayIPTable = new HashMap<String,Relay>();

	public synchronized void addClient(Relay client){
		relayIPTable.put(client.getIPString(), client);
	}
	
	public Collection <Relay> getCients(){
		return Collections.unmodifiableCollection(relayIPTable.values());
	}
	
	public  Map<String,Relay> getIPTable(){
		return Collections.unmodifiableMap(relayIPTable);
	}

	
	protected RelayServer()  {
	        NioDatagramAcceptor server = new NioDatagramAcceptor();
			ProtocolCodecFilter pcf = new ProtocolCodecFilter(
					 new RelayEncoder(), new RelayDecoder());
			server.setCloseOnDeactivation(false);
			server.getFilterChain().addLast("log1", new LoggingFilter("log1"));
			server.getFilterChain().addLast("endecode", pcf);
			server.getFilterChain().addLast("log2", new LoggingFilter("log2"));

			server.setHandler(this);
	        try {
	        	server.bind(new InetSocketAddress(RELAY_PORT));
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
	
	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		String reply = ((InetSocketAddress)session.getRemoteAddress()).getHostString();
    	if(relayIPTable.containsKey(reply)) {
    			if (message instanceof RelayDecoder.SwitchReply ){
    				Relay relay = relayIPTable.get(reply);
    				relay.processReply((RelayDecoder.SwitchReply) message);
    			}


    	}
	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {
		super.sessionClosed(session);
	}

	@Override
	public void sessionOpened(IoSession session) throws Exception {
		super.sessionOpened(session);
	}


}
