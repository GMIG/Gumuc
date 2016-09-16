package gumuc1;

import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class BrixView extends SquareDeviceView{

	public BrixView(int X,int Y) {
		super(X,Y);
        bText.setText("B");
        bText.setFont(Font.font("Verdana", FontWeight.BOLD, 15));
	}

}

