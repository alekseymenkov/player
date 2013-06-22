package gdi_object;

public enum PenStyle {

	NoPen(0),
	SolidLine(1),
	DashLine(2),
	DotLine(3),
	DashDotLine(4),
	DashDotDotLine(5),
	CustomDashLine(6);

	private int mType;  

	private PenStyle(int type) {  
		mType = type;  
	}  

	public byte getType() {  
		return (byte)(mType & 0xff);  
	}  


	public static PenStyle getType(byte id) {

		for (PenStyle type : PenStyle.values()) {
			if (type.getType() == id)
				return type;
		}

		return PenStyle.NoPen;
	}
}
