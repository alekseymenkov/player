package figure;

import java.io.DataInputStream;
import java.io.IOException;

public class FigureFactory {

	private FigureFactory() {
		
	}
	
	static public Figure createObject(DataInputStream in) throws IOException {
		
		return new Figure(in);
	}
}
