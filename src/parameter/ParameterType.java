package parameter;


public enum ParameterType {
    Common(0),
    Number(1);

	private int mType;  

	private ParameterType(int type) {  
		mType = type;  
	}  

	public byte getType() {  
		return (byte)(mType & 0xff);  
	}

	
	public static ParameterType getType(byte id) {

		for (ParameterType type : ParameterType.values()) {
			if (type.getType() == id)
				return type;
		}
		
		return ParameterType.Common;
	}
}