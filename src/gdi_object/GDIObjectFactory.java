package gdi_object;


import java.io.DataInputStream;
import java.io.IOException;


public class GDIObjectFactory {

	private GDIObjectFactory() {
		
	}
	
	public static GDIObject createObject(DataInputStream in) throws IOException {
		
		return new GDIObject(in);
	}
}
