package element_interface;

import java.util.ArrayList;



import parameter.ParameterType;

public class ElementInterface {
	
	// Данные о запрашиваемых байтах и битах
	ArrayList<ElementInterfaceParameter> mParameters;
	// Дополнительные параметры
	ArrayList<ElementInterfaceExtraParameter> mExtraParameters;
	
	// Диапазон запрашиваемых данных
	int mMinByte;
	int mMaxByte;
	
	// Состояние элемента
	ArrayList<Boolean> mState;
	
	// Координаты элемента
	int mX;
	int mY;
	
	// Размер
	int mSizeWidth;
	int mSizeHeight;
	
	// Указатель на логику работы элемента
	//ElementLogic mElementLogic;
	
	// Количество параметров
	//int mParametersCount;
	
	// Реальное количество параметров
	//int mActualParametersCount;
	
	// Модификатор размера
	double mSizeModificator;
	
	// Имя файла с модулем
	String mModuleName;
	
	// Хеш модуля
	String mModuleHash;
	
	// Имя элемента
	String mElementName;
	
	// Угол поворота элемента
	int mAngle;
	
	// Работоспособность модуля
	ModuleState mModuleState;
	

	public ArrayList<Integer> calculateRange() {
	    
		final int partOfByteSize = 7;
	    final int byteSize = 8;

	    int minByte = Integer.MAX_VALUE;
	    int maxByte = Integer.MIN_VALUE;

        for (ElementInterfaceParameter parameter : mParameters) {
            if (!parameter.isUsed())
                continue;


            if (parameter.getStartByte() < minByte)
                minByte = parameter.getStartByte();

            int bitsCount = 0;
            if (parameter.getType() != ParameterType.Number)
                bitsCount = parameter.getBitsCount();                // TODO: Тут было значение количества бит из ElementLogic. Если будет работать без него - объединить строки
            else if (parameter.getType() != ParameterType.Number)
                bitsCount = parameter.getBitsCount();

            int currentMaxByte = parameter.getStartByte() + (parameter.getStartBit() + bitsCount + partOfByteSize) / byteSize;
            if (currentMaxByte > maxByte)
                maxByte = currentMaxByte;
        }

        mMinByte = minByte;
        mMaxByte = maxByte;

	    // Формирование границ
	    final int arrayListCapacity = 2;
	    ArrayList<Integer> range = new ArrayList<Integer>(arrayListCapacity);
	    range.add(minByte);
	    range.add(maxByte - minByte);

	    return range;
	}

    public int getMinByte() {
        return mMinByte;
    }

//	public ArrayList<Integer> getStartBytes() {
//		return mStartBytes;
//	}
//
//	
//	public void setStartBytes(ArrayList<Integer> startBytes) {
//		mStartBytes = startBytes;
//	}
//
//	
//	public ArrayList<Integer> getStartBits() {
//		return mStartBits;
//	}
//
//	
//	public void setStartBits(ArrayList<Integer> startBits) {
//		mStartBits = startBits;
//	}
//
//	
//	public ArrayList<Integer> getBitsCount() {
//		return mBitsCount;
//	}
//
//	
//	public void setBitsCount(ArrayList<Integer> bitsCount) {
//		mBitsCount = bitsCount;
//	}
//
//	
//	public ArrayList<String> getDescriptions() {
//		return mDescriptions;
//	}
//
//	
//	public void setDescriptions(ArrayList<String> descriptions) {
//		mDescriptions = descriptions;
//	}
//
//	
//	public ArrayList<Boolean> getIsParameterCorrect() {
//		return mIsParameterCorrect;
//	}
//
//	
//	public void setIsParameterCorrect(ArrayList<Boolean> isParameterCorrect) {
//		mIsParameterCorrect = isParameterCorrect;
//	}
//
//	
//	public ArrayList<Boolean> getIsParametersUsed() {
//		return mIsParametersUsed;
//	}
//
//	
//	public void setIsParametersUsed(ArrayList<Boolean> isParametersUsed) {
//		mIsParametersUsed = isParametersUsed;
//	}

//	
//	public int getMinByte() {
//		return mMinByte;
//	}
//
//	
//	public void setMinByte(int minByte) {
//		mMinByte = minByte;
//	}
//
//	
//	public int getMaxByte() {
//		return mMaxByte;
//	}
//
//	
//	public void setMaxByte(int maxByte) {
//		mMaxByte = maxByte;
//	}

	
	public ArrayList<Boolean> getState() {
		return mState;
	}

	
	public void setState(ArrayList<Boolean> state) {
		mState = state;
	}

	
	public int getX() {
		return mX;
	}

	
	public void setPosition(int x, int y) {
		mX = x;
		mY = y;
	}

	
	public int getY() {
		return mY;
	}

	
	public int getSizeWidth() {
		return mSizeWidth;
	}
	
	
	public int getSizeHeight() {
		return mSizeHeight;
	}

	
	public void setSize(int width, int height) {
		mSizeWidth = width;
		mSizeHeight = height;
	}

	
//	public ElementLogic getElementLogic() {
//		return mElementLogic;
//	}
//
//	
//	public void setElementLogic(ElementLogic elementLogic) {
//		mElementLogic = elementLogic;
//	}
//
//	
//	public int getParametersCount() {
//		return mParametersCount;
//	}
//
//	
//	public void setParametersCount(int parametersCount) {
//		mParametersCount = parametersCount;
//	}
//
//	
//	public int getActualParametersCount() {
//		return mActualParametersCount;
//	}
//
//	
//	public void setActualParametersCount(int actualParametersCount) {
//		mActualParametersCount = actualParametersCount;
//	}

	
	public double getSizeModificator() {
		return mSizeModificator;
	}

	
	public void setSizeModificator(double sizeModificator) {
		mSizeModificator = sizeModificator;
	}

	
	public String getModuleName() {
		return mModuleName;
	}

	
	public void setModuleName(String moduleName) {
		mModuleName = moduleName;
	}

	
	public String getModuleHash() {
		return mModuleHash;
	}

	
	public void setModuleHash(String moduleHash) {
		mModuleHash = moduleHash;
	}

	
	public String getElementName() {
		return mElementName;
	}

	
	public void setElementName(String elementName) {
		mElementName = elementName;
	}

	
	public int getAngle() {
		return mAngle;
	}

	
	public void setAngle(int angle) {
		mAngle = angle;
	}

	
	public ModuleState getModuleState() {
		return mModuleState;
	}

	
	public void setModuleState(ModuleState moduleState) {
		mModuleState = moduleState;
	}

	
	public ArrayList<ElementInterfaceExtraParameter> getExtraParameters() {
		return mExtraParameters;
	}

	
	public void setExtraParameters(ArrayList<ElementInterfaceExtraParameter> extraParameters) {
		mExtraParameters = extraParameters;
	}

	
	public ArrayList<ElementInterfaceParameter> getParameters() {
		return mParameters;
	}
	
	
	public void setParameters(ArrayList<ElementInterfaceParameter> parameters) {
		mParameters = parameters;
	}
}
