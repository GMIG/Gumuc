package gumuc1;

import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class InfokioskView extends SquareDeviceView{


	public InfokioskView(int X,int Y) {
		super(X,Y);
        bText.setText("I");
        bText.setFont(Font.font("Verdana", FontWeight.BOLD, 15));
	}

}
