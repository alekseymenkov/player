package parameter;


import java.io.DataInputStream;
import java.io.IOException;


public class Parameter {

	ParameterType mParameterType;
	int mExtraParameter;
	short mBitsCount;
	
	
	Parameter(DataInputStream in) throws IOException {
		
		readObject(in);
	}
	
	
	private void readObject(DataInputStream in) throws IOException {

		mParameterType = ParameterType.getType(in.readByte());
		
		mExtraParameter = in.readInt();
		
		mBitsCount = in.readShort();
		
		return;
	}


	public ParameterType getParameterType() {
		
		return mParameterType;
	}


	public int getExtraParameter() {
		
		return mExtraParameter;
	}


	public short getBitsCount() {
		
		return mBitsCount;
	}
}
