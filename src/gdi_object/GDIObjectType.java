package gdi_object;

public enum GDIObjectType {
	Pen(0),
	Brush(1),
	Text(2),
	Undefined(3);


	private int mType;  

	private GDIObjectType(int type) {  
		mType = type;  
	}  

	public byte getType() {  
		return (byte)(mType & 0xff);  
	}

	
	public static GDIObjectType getType(byte id) {

		for (GDIObjectType type : GDIObjectType.values()) {
			if (type.getType() == id)
				return type;
		}
		
		return GDIObjectType.Undefined;
	}
}
