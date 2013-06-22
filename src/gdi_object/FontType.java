package gdi_object;

public enum FontType {
    TextType(1),
    HeaderType(2);
    
    private int mType;  
    
    private FontType(int type) {  
        mType = type;  
    }  
   
    public byte getType() {  
        return (byte)(mType & 0xff);  
    } 
    
	public static FontType getType(byte id) {

		for (FontType type : FontType.values()) {
			if (type.getType() == id)
				return type;
		}

		return FontType.TextType;
	}
}
