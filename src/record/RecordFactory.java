package record;

import java.io.DataInputStream;
import java.io.IOException;

public class RecordFactory {
	
	private RecordFactory() {
		
	}
	
	public static Record createObject(DataInputStream in) throws IOException {
		
		return new Record(in);
	}
}
