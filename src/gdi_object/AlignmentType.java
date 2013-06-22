package gdi_object;

public enum AlignmentType {

	AlignLeft(1),
	AlignRight(2),
	AlignHCenter(4),
	AlignJustify(8),
	AlignAbsolute(16),
	AlignTop(32),
	AlignBottom(64),
	AlignVCenter(128),
	AlignCenter(128 + 4);
	
    private int mType;  
    
    private AlignmentType(int type) {  
        mType = type;  
    }  
   
    public byte getType() {  
        return (byte)(mType & 0xff);  
    }
    
	public static AlignmentType getType(byte id) {

		for (AlignmentType type : AlignmentType.values()) {
			if (type.getType() == id)
				return type;
		}

		return AlignmentType.AlignCenter;
	}
}
