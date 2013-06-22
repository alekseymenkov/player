package element_interface;


public enum ModuleState {
	Success(0),
	Warning(1),
	Critical(2);


	private int mType;  

	
	private ModuleState(int type) {  
		mType = type;  
	}  

	
	public byte getType() {  
		return (byte)(mType & 0xff);  
	}


	public static ModuleState getType(byte id) {

		for (ModuleState type : ModuleState.values()) {
			if (type.getType() == id)
				return type;
		}

		return ModuleState.Critical;
	}
}
