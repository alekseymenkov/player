package figure;


public enum FigureType {
    Undefined(0),
    Line(1),
    Ellipse(2),
    Pie(3),
    Chord(4),
    Rect(5),
    Polygon(6),
    Text(7),
    Arc(8),
    RoundedRect(9);


	private int mType;  

	private FigureType(int type) {  
		mType = type;  
	}  

	public byte getType() {  
		return (byte)(mType & 0xff);  
	}

	
	public static FigureType getType(byte id) {

		for (FigureType type : FigureType.values()) {
			if (type.getType() == id)
				return type;
		}
		
		return FigureType.Undefined;
	}
}