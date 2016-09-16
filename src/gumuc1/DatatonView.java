package gumuc1;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class DatatonView extends SquareDeviceView{

	public DatatonView(int X,int Y) {
		super(X,Y);
		
		bText.setText("D");
        bText.setFont(Font.font("Tahoma", FontWeight.THIN, 15));
        bText.setFill(Color.WHITE);
	}

}

