package gumuc1;

import javafx.beans.property.ObjectProperty;
import javafx.scene.paint.Paint;

public interface FilledIndicator {
	public ObjectProperty<Paint> powerStateFill();
	public ObjectProperty<Paint> connectionStateFill();
}
