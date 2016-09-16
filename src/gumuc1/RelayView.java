package gumuc1;


import javafx.scene.shape.Rectangle;

import javafx.beans.property.ObjectProperty;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;


public class RelayView extends Pane {

	private final Rectangle upRect;
	private final Rectangle downRect;

	private final Path connector;

	public final static Double squareSize = 30.0;
	
	public final static Double startX = squareSize/2;
	public final static Double startY = 0.0;
	
	public final static Double connectorRadius = squareSize/2;

	private final ObjectProperty<Paint> rectUpFill;
	private final ObjectProperty<Paint> rectDownFill;

	private final ObjectProperty<Paint> connectorFill;
	
	
	public ObjectProperty<Paint> getRectUpFill() {
		return rectUpFill;
	}


	public ObjectProperty<Paint> getRectDownFill() {
		return rectDownFill;
	}


	public ObjectProperty<Paint> connectionStateFill(){
		return connectorFill;
	}
	
	
	public RelayView(int X,int Y) {
		super();
		upRect = new Rectangle(0.0, 0.0,squareSize,squareSize/2);
		downRect = new Rectangle(0.0, squareSize/2,squareSize,squareSize/2);
		
		upRect.setLayoutX(startX);
		upRect.setLayoutY(startY);
		upRect.setFill(Color.LIGHTGREY);
		
		downRect.setLayoutX(startX);
		downRect.setLayoutY(startY);
		downRect.setFill(Color.LIGHTGREY);
        //polygon.getBoundsInParent();
        //this.setMinSize(polygon.getBoundsInParent().getWidth(), polygon.getBoundsInParent().getHeight());
        this.setLayoutX(X);
        this.setLayoutY(Y);
        MoveTo moveTo = new MoveTo();
        	moveTo.setX(0.0);
        	moveTo.setY(0.0);
        
        ArcTo arcTo = new ArcTo();
        	arcTo.setX(0.0);
        	arcTo.setY(connectorRadius);
        	arcTo.setRadiusX(connectorRadius/2);
        	arcTo.setRadiusY(connectorRadius/2);
        	
        connector = new Path();
        connector.getElements().addAll(moveTo,arcTo);
        connector.setFill(Color.LIGHTGREY);
        connector.setFillRule(FillRule.NON_ZERO);
        connector.setLayoutX(startX);
        connector.setLayoutY(startY);
        connector.setStrokeWidth(0);
        
        connectorFill = connector.fillProperty();
        rectUpFill  = upRect.fillProperty();
        rectDownFill = downRect.fillProperty();
        
        this.getChildren().addAll(upRect,downRect,connector);
	}

}

