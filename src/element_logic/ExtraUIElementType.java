package element_logic;


public enum ExtraUIElementType {
    Undefined(0),
    LineEdit(1),
    ColorDialog(2),
    CheckBox(3),
    SpinBox(4);


	private int mType;  

	private ExtraUIElementType(int type) {  
		mType = type;  
	}  

	public byte getType() {  
		return (byte)(mType & 0xff);  
	}

	
	public static ExtraUIElementType getType(byte id) {

		for (ExtraUIElementType type : ExtraUIElementType.values()) {
			if (type.getType() == id)
				return type;
		}
		
		return ExtraUIElementType.Undefined;
	}
}