package gumuc1;

import org.apache.log4j.Logger;

import static org.apache.mina.statemachine.event.IoHandlerEvents.*;

import org.apache.mina.core.future.CloseFuture;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.statemachine.StateControl;
import org.apache.mina.statemachine.annotation.IoHandlerTransition;
import org.apache.mina.statemachine.annotation.State;
import org.apache.mina.statemachine.event.Event;
import gumuc1.Dataton.DatatonCommandContext;

public class DatatonFSM {
	/**
	* Logger for this class
	*/
	private static final Logger logger = Logger.getLogger(DatatonFSM.class);

    @State public static final String ROOT = "Root";
    @State(ROOT) public static final String PING_RECEIVED = "PR";

    @State(ROOT) public static final String UNDEFINED = "Undefined";
    @State(ROOT) public static final String CONNECTION_ESTABLISHED = "Connection established";
    @State(CONNECTION_ESTABLISHED) public static final String AUTH_SENT = "Connection established";

    	@State(CONNECTION_ESTABLISHED) public static final String  AUTHENTIFICATION_PASSED = "Auth received";
    		@State(AUTHENTIFICATION_PASSED) public static final String  LOAD_COMMAND_PASSED = "Load passed";
    		@State(AUTHENTIFICATION_PASSED) public static final String  SHUT_COMMAND_PASSED = "Shut passed";

	public DatatonFSM() {
		
	}
    
	private void writeNextCommand(DatatonCommandContext context, IoSession session){
    	if(!context.commandQueue.isEmpty()){
    		DatatonEncoder.DatatonCommand cmd = context.commandQueue.poll();
    		session.write(cmd);
    		logger.info(cmd.getClass());
    	}
    	else
    	{
        		//@SuppressWarnings("unused")
    			//CloseFuture future = session.close(false);
    	}
	}
   /* @IoHandlerTransition(on = MESSAGE_SENT , in = INFO_RECEIVED)
    public void sendCommand(DatatonCommandContext context, IoSession session, DatatonPingCommand command) {
    	//System.out.println(session.getRemoteAddress().toString() + " established");
    }*/

    @IoHandlerTransition(on = MESSAGE_RECEIVED , in = ROOT)
    public void sendCommand(DatatonCommandContext context, IoSession session, DatatonDecoder.PingReply reply) {
    	writeNextCommand(context,session);
    	context.hanldePingReceived();
    }
    
    @IoHandlerTransition(on = SESSION_OPENED, in = ROOT, next = CONNECTION_ESTABLISHED)
    public void sendCommand(DatatonCommandContext context, IoSession session) {
    	//System.out.println(session.getRemoteAddress().toString() + " pjlink 0 received");\
    	//DatatonCommand cmd = context.authCommand.orElseGet(() -> context.finalCommand);
    	writeNextCommand(context,session);
    	//session.write(context.);
    }
    
    @IoHandlerTransition(on = MESSAGE_RECEIVED, in = AUTHENTIFICATION_PASSED)
    public void sendCommand(DatatonCommandContext context, IoSession session, DatatonDecoder.StatusReply reply) {
    	writeNextCommand(context,session);
    	context.hanldeStatusReceived(reply);
    }
    
    @IoHandlerTransition(on = MESSAGE_RECEIVED, in = AUTHENTIFICATION_PASSED)
    public void sendCommand(DatatonCommandContext context, IoSession session, DatatonDecoder.LoadReply reply) {
    	//System.out.println(session.getRemoteAddress().toString() + " pjlink 0 received");
       // session.write(context.command);
    	if(reply.done){
    		writeNextCommand(context,session);
    		StateControl.breakAndCallNext(LOAD_COMMAND_PASSED);
    	}

    }
    
    @IoHandlerTransition(on = MESSAGE_RECEIVED, in = CONNECTION_ESTABLISHED, next = AUTHENTIFICATION_PASSED)
    public void sendCommand(DatatonCommandContext context, IoSession session, DatatonDecoder.AuthReply reply) {
    	//System.out.println(session.getRemoteAddress().toString() + " pjlink 0 received");
       // session.write(context.command);
    	writeNextCommand(context,session);

    }
    
    @IoHandlerTransition(on = MESSAGE_RECEIVED, in = LOAD_COMMAND_PASSED)
    public void sendCommand(DatatonCommandContext context, IoSession session, DatatonDecoder.RunReply reply) {
    	//System.out.println(session.getRemoteAddress().toString() + " pjlink 0 received");
       // session.write(context.command);
    	writeNextCommand(context,session);

    }
    
 /*   @IoHandlerTransition(on = MESSAGE_SENT, in = AUTHENTIFICATION_PASSED, next=SHUT_COMMAND_PASSED)
    public void sendCommand(DatatonCommandContext context, IoSession session, DatatonDecoder.ShutdownReply reply) {
    	//System.out.println(session.getRemoteAddress().toString() + " pjlink 0 received");
       // session.write(context.command);
    	//writeNextCommand(context,session);
		//CloseFuture future = session.close(true);

    }*/
    
    
    @IoHandlerTransition(on = ANY,in = ROOT , weight = 100)
    public void closeSessionOnInputClosed(Event event, IoSession session) {
    	if (event.getId().equals("inputClosed")){
    		@SuppressWarnings("unused")
			CloseFuture future = session.closeOnFlush();
    	}
    	//else
    		//System.out.println("unhandledEvent " + event);
    }
    
    @IoHandlerTransition(on = EXCEPTION_CAUGHT,in = ROOT, next=SHUT_COMMAND_PASSED)
    public void closeSessionOnDatatonDisconnected(Event event, IoSession session, Throwable cause) {
    	if (cause instanceof java.io.IOException){
    		@SuppressWarnings("unused")
			CloseFuture future = session.closeNow();
    	}
    	//else
    		//System.out.println("unhandledEvent " + event);
    }
    

}