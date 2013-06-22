package network;


public enum CommandPackageType {  
    IsReady(0),  
    IsAlive(1),  
    ConnectionLimit(2);
   
    private int mType;  
   
    private CommandPackageType(int type) {  
        mType = type;  
    }  
   
    public byte getType() {  
        return (byte)(mType & 0xff);  
    }  
    
	public static CommandPackageType getType(byte id) {

		for (CommandPackageType type : CommandPackageType.values()) {
			if (type.getType() == id)
				return type;
		}

		return null;
	}
}  