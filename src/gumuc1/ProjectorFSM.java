package gumuc1;

import static org.apache.mina.statemachine.event.IoHandlerEvents.*;

import org.apache.mina.core.future.CloseFuture;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.statemachine.StateControl;
import org.apache.mina.statemachine.annotation.IoHandlerTransition;
import org.apache.mina.statemachine.annotation.IoHandlerTransitions;
import org.apache.mina.statemachine.annotation.State;
import org.apache.mina.statemachine.event.Event;

import gumuc1.Projector.ProjectorCommandContext;

public class ProjectorFSM {
    @State public static final String ROOT = "Root";
    @State(ROOT) public static final String UNDEFINED = "Undefined";
    @State(ROOT) public static final String CONNECTION_ESTABLISHED = "Connection established";
    @State(ROOT) public static final String PJLINK_WELCOME_RECEIVED = "PJLink welcome received";
    @State(ROOT) public static final String POWERED_UP = "Powered up";
    @State(ROOT) public static final String POWERED_DOWN = "Powered down";
    
	public ProjectorFSM() {
		
	}

    @IoHandlerTransition(on = SESSION_OPENED, in = ROOT, next=CONNECTION_ESTABLISHED)
    public void waitForWelcome(ProjectorCommandContext context, IoSession session) {
    	//System.out.println(session.getRemoteAddress().toString() + " established");
    }
    
    @IoHandlerTransition(on = MESSAGE_RECEIVED, in = CONNECTION_ESTABLISHED, next=PJLINK_WELCOME_RECEIVED)
    public void sendCommand(ProjectorCommandContext context, IoSession session, PJLinkDecoder.PJLinkWelcomeReply reply) {
    	//System.out.println(session.getRemoteAddress().toString() + " pjlink 0 received");

        session.write(context.command);
    }
    
    @IoHandlerTransitions({
        @IoHandlerTransition(on = MESSAGE_RECEIVED, in = POWERED_DOWN, next=POWERED_UP),
        @IoHandlerTransition(on = MESSAGE_RECEIVED, in = PJLINK_WELCOME_RECEIVED, next=POWERED_UP),
        @IoHandlerTransition(on = MESSAGE_RECEIVED, in = POWERED_UP, next=POWERED_DOWN),
        @IoHandlerTransition(on = MESSAGE_RECEIVED, in = PJLINK_WELCOME_RECEIVED, next=POWERED_DOWN)
    })
    public void switchToPowerStateOnSuccesfulPowerSwitch(Event event,ProjectorCommandContext context,
    											IoSession session, PJLinkDecoder.PJLinkPowerChangeOKReply reply) {
    	if(context.command.equals(PJLinkDecoder.powerUpCommand))
    		StateControl.breakAndGotoNext(POWERED_UP);
    	if(context.command.equals(PJLinkDecoder.powerDownCommand))
    		StateControl.breakAndGotoNext(POWERED_DOWN);
    	
    	
        session.write(PJLinkDecoder.powerInfoCommand);

    }
    
    
    @IoHandlerTransition(on = ANY,in = ROOT , weight = 100)
    public void closeSessionOnInputClosed(Event event, IoSession session) {
    	
    	if (event.getId().equals("inputClosed")){
    		@SuppressWarnings("unused")
			CloseFuture future = session.close(false);
    	}
    	//else
    		//System.out.println("unhandledEvent " + event);
    }
    
    @IoHandlerTransition(on = MESSAGE_RECEIVED, in = ROOT, next=POWERED_UP)
    public void changeToPowerUpOnInfoCommand(ProjectorCommandContext context, IoSession session, PJLinkDecoder.PJLinkInfoPowerUpReply reply) {
    }
    
    @IoHandlerTransition(on = MESSAGE_RECEIVED, in = ROOT, next=UNDEFINED)
    public void changeToUndefinedOnErrorReply(ProjectorCommandContext context, IoSession session, PJLinkDecoder.PJLinkPowerErrorReply reply) {
        session.write(PJLinkDecoder.powerInfoCommand);
    }

    @IoHandlerTransition(on = MESSAGE_RECEIVED, in = ROOT, next=POWERED_DOWN)
    public void changeToPowerDownOnInfoCommand(ProjectorCommandContext context, IoSession session, PJLinkDecoder.PJLinkInfoPowerDownReply reply) {
    }   
}
