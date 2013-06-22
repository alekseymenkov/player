package parameter;


import java.io.DataInputStream;
import java.io.IOException;


public class ParameterFactory {

	private ParameterFactory() {
		
	}
	
	public static Parameter createObject(DataInputStream in) throws IOException {
		
		return new Parameter(in);
	}
}
