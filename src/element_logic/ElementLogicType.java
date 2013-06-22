package element_logic;

public enum ElementLogicType {
    Undefined(0),
    SingleIndicator(1),
    DoubleIndicator(2),
    Common(3),
    FuelDensity(4),
    SingleIndicatorDark(5),
    DoubleIndicatorDark(6),
    Flap(7),
    FlapDark(8),
    Bus(9),
    BusDark(10);


	private int mType;  

	private ElementLogicType(int type) {  
		mType = type;  
	}  

	public byte getType() {  
		return (byte)(mType & 0xff);  
	}

	
	public static ElementLogicType getType(byte id) {

		for (ElementLogicType type : ElementLogicType.values()) {
			if (type.getType() == id)
				return type;
		}
		
		return ElementLogicType.Undefined;
	}
}