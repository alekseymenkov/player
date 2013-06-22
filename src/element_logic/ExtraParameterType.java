package element_logic;

public enum ExtraParameterType {
    Undefined(0),
    String(1),
    Integer(2),
    Real(3),
    Bool(4),
    Color(5);


	private int mType;  

	private ExtraParameterType(int type) {  
		mType = type;  
	}  

	public byte getType() {  
		return (byte)(mType & 0xff);  
	}

	
	public static ExtraParameterType getType(byte id) {

		for (ExtraParameterType type : ExtraParameterType.values()) {
			if (type.getType() == id)
				return type;
		}
		
		return ExtraParameterType.Undefined;
	}
}