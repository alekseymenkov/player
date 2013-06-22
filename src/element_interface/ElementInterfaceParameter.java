package element_interface;

import parameter.ParameterType;

public class ElementInterfaceParameter {

	int mStartByte;
	int mStartBit;
	int mBitsCount;
	String mDescription;
	boolean mIsCorrect;
	boolean mIsUsed;
	ParameterType mType;
    
	
	ElementInterfaceParameter(int startByte, int startBit, int bitsCount, String description, boolean isUsed) {
		mStartByte = startByte;
		mStartBit = startBit;
		mBitsCount = bitsCount;
		mDescription = description;
		mIsUsed = isUsed;
	}
	
	
	public int getStartByte() {
		return mStartByte;
	}
	
	
	public int getStartBit() {
		return mStartBit;
	}
	
	
	public int getBitsCount() {
		return mBitsCount;
	}
	
	
	public String getDescription() {
		return mDescription;
	}
	
	
	public boolean isUsed() {
		return mIsUsed;
	}
	
	
	public boolean isCorrect() {
		return mIsCorrect;
	}
	
	
	public void setIsCorrect(boolean isCorrect) {
		mIsCorrect = isCorrect;
	}


	public ParameterType getType() {
		return mType;
	}


	public void setType(ParameterType type) {
		mType = type;
	}
};