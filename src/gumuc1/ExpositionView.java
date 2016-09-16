package gumuc1;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

public  class ExpositionView {
	private static int totalWidth = 800;
	private static int totalHeight = 500;
	
	
	public final List<Node> rooms = new ArrayList<Node>();
	public ExpositionView() {
		
		Rectangle all = new Rectangle(0,0,totalWidth,totalHeight);
		Rectangle hall3up = new Rectangle(0,0,totalWidth/4,totalHeight/2);
		Rectangle hall3down = new Rectangle(0,totalHeight/2,totalWidth/4,totalHeight/2);
		Rectangle hall2_207up = new Rectangle(totalWidth/4,0,totalWidth/4,totalHeight/3);
		Rectangle hall2_207down = new Rectangle(totalWidth/4,totalHeight/3,totalWidth/4,totalHeight/3);
		Rectangle hall2_214up = new Rectangle(2*totalWidth/4,0,totalWidth/4,totalHeight/3);
		Rectangle hall2_214down = new Rectangle(2*totalWidth/4,totalHeight/3,totalWidth/4,totalHeight/3);
		Rectangle hall2_open = new Rectangle(totalWidth/4,2*totalHeight/3,2*totalWidth/4,totalHeight/3);
		Rectangle hall1up = new Rectangle(3*totalWidth/4,0,totalWidth/4,totalHeight/2);
		Rectangle hall1down = new Rectangle(3*totalWidth/4,totalHeight/2,totalWidth/4,totalHeight/2);

		rooms.add(all);
		rooms.add(hall3up);rooms.add(hall3down);
		rooms.add(hall2_207up);rooms.add(hall2_207down);
		rooms.add(hall2_214up);rooms.add(hall2_214down);rooms.add(hall2_open);
		rooms.add(hall1up);rooms.add(hall1down);
		
		rooms.forEach((room)->{
			((Shape)room).setFill(Color.WHITE);
			((Shape)room).setStroke(Color.BLACK);
			((Shape)room).setStrokeWidth(3);
		});
		
		int yOffset=0; double delta = DatatonView.squareSize*1.2;
		for(IPDevice.PowerState state:IPDevice.PowerState.values()){
			
			Rectangle legend = new Rectangle(DatatonView.squareSize,2*totalHeight/3 - DatatonView.squareSize*2 + yOffset,
														DatatonView.squareSize, DatatonView.squareSize);
			legend.setFill(IPDeviceStateChangeController.powerStateColorMap.get(state));
			Label label = new Label(state.name);
			label.setLayoutX(DatatonView.squareSize + DatatonView.squareSize);
			label.setLayoutY(2*totalHeight/3 - DatatonView.squareSize*2 + yOffset);
			//legend.setFill(IPDeviceStateChangeController.powerStateColorMap.get(state));
			yOffset+=delta;

			rooms.add(legend); rooms.add(label);
		}
		
		for(IPDevice.ConnectionState state:IPDevice.ConnectionState.values()){
			
	        MoveTo moveTo = new MoveTo();
	        double x = DatatonView.squareSize;
	        double y = 2*totalHeight/3 - DatatonView.squareSize*2 + yOffset;
        	moveTo.setX(x);
        	moveTo.setY(y);

	        ArcTo arcTo = new ArcTo();
	    	arcTo.setX(x);
	    	arcTo.setY(y + DatatonView.connectorRadius);
	    	arcTo.setRadiusX(DatatonView.connectorRadius/2);
	    	arcTo.setRadiusY(DatatonView.connectorRadius/2);
	    	
	    	Path connector = new Path();
	        connector.getElements().addAll(moveTo,arcTo);
	        connector.setFillRule(FillRule.NON_ZERO);
	        connector.setStrokeWidth(0);
	        connector.setFill(IPDeviceStateChangeController.connectionStateColorMap.get(state));

			Label label = new Label(state.name);
			label.setLayoutX(x + DatatonView.connectorRadius/2);
			label.setLayoutY(y);
			//legend.setFill(IPDeviceStateChangeController.powerStateColorMap.get(state));
			yOffset+=delta;

			rooms.add(connector); rooms.add(label);
		}


		
	}

}
