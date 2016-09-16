package gumuc1;

import javafx.beans.property.ObjectProperty;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Polygon;

public class ProjectorView extends Pane implements FilledIndicator{

	private final Polygon polygon;
	private final Path connector;
	public final static Double squareSize = 30.0;
	public final static Double objSize = squareSize/2;
	public final static Double startX = squareSize/2;
	public final static Double startY = 0.0;
	
	//public final static Double connectorStartX = startX;
	//public final static Double connectorStartY = startY;
	public final static Double connectorRadius = squareSize/2;

	private final ObjectProperty<Paint> polygonFill;

	private final ObjectProperty<Paint> connectorFill;
	
	public ObjectProperty<Paint> powerStateFill(){
		return polygonFill;
	}
	
	public ObjectProperty<Paint> connectionStateFill(){
		return connectorFill;
	}
	
	
	public ProjectorView(int X,int Y, int rotation) {
		super();
        polygon = new Polygon();
        polygon.getPoints().addAll(new Double[]{
        		0.0, 0.0,
        		squareSize, 0.0,
        		squareSize, objSize,
        		objSize + squareSize,0.0,
        		objSize + squareSize,squareSize,
        		squareSize, objSize,
        		squareSize,squareSize,
        		0.0,squareSize});
        polygon.setLayoutX(startX);
        polygon.setLayoutY(startY);
        polygon.setFill(Color.LIGHTGREY);
        //polygon.getBoundsInParent();
        //this.setMinSize(polygon.getBoundsInParent().getWidth(), polygon.getBoundsInParent().getHeight());
        this.setRotate(rotation);
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
        polygonFill = polygon.fillProperty();
        this.getChildren().addAll(polygon,connector);
	}

}
