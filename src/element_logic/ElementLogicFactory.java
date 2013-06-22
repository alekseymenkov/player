package element_logic;

import java.io.DataInputStream;
import java.io.IOException;

public class ElementLogicFactory {
	
	private ElementLogicFactory() {
		
	}
	
	static public ElementLogic createObject(DataInputStream in) throws IOException {
		
		return new ElementLogic(in);
	}
}
