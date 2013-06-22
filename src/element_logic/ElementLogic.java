package element_logic;


import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;

import figure.Figure;
import figure.FigureFactory;
import parameter.Parameter;
import parameter.ParameterFactory;
import record.Record;
import record.RecordFactory;

import android.graphics.Color;


public class ElementLogic {

	// Логика элемента
	ArrayList<Parameter> mParameters;
	ArrayList<Figure> mFigures;
	ArrayList<Record> mRecords;
	ArrayList<Record> mDefaultRecords;
	// Хеш модуля в BASE-64
	String mModuleHash;
	// Имя модуля в UTF-16
	String mModuleName;
	// Картинка-описание
	String mPreviewImagePath;
	// Стартовый угол поворота элемента
	short mStartAngle;
	// Размер элемента
	int mSizeWidth;
	int mSizeHeight;
	// Размер области предупреждения
	int mWarningSizeWidth;
	int mWarningSizeHeight;
	// Названия дополнительных параметров (не используется)
	ArrayList<String> mExtraParameterName;
	// Тип дополнительных параметров
	ArrayList<ExtraParameterType> mExtraParameterType;
	// Тип элементов UI с дополнительными параметрами (не используется)
	ArrayList<ExtraUIElementType> mExtraUIElementType;
	// Параметры по умолчанию
	ArrayList<Object> mDefaultExtraValues;
	// Тип логического элемента
	ElementLogicType mElementLogicType;


	ElementLogic(DataInputStream in) throws IOException {

		initialize();
		readObject(in);
	}


	private void initialize() {

		mParameters = new ArrayList<Parameter>();
		mFigures = new ArrayList<Figure>();
		mRecords = new ArrayList<Record>();
		mDefaultRecords = new ArrayList<Record>();

		mExtraParameterName = new ArrayList<String>();
		mExtraParameterType = new ArrayList<ExtraParameterType>();
		mExtraUIElementType = new ArrayList<ExtraUIElementType>();
		mDefaultExtraValues = new ArrayList<Object>();
	}


	private void readObject(DataInputStream in) throws IOException {

		// Массив параметров
		int parametersCount = in.readInt();
		for (int i = 0; i < parametersCount; i++) {
			mParameters.add(ParameterFactory.createObject(in));
		}

		// Массив фигур
		int figuresCount = in.readInt();
		for (int i = 0; i < figuresCount; i++) {
			mFigures.add(FigureFactory.createObject(in));
		}

		// Массив записей
		int recordsCount = in.readInt();
		for (int i = 0; i < recordsCount; i++) {
			mRecords.add(RecordFactory.createObject(in));
		}

		// Массив записей по умолчанию
		int defaultRecordsCount = in.readInt();
		for (int i = 0; i < defaultRecordsCount; i++) {
			mDefaultRecords.add(RecordFactory.createObject(in));
		}

		// Длина текста с названием модуля
		int moduleNameStringLenght = in.readInt();
		if (moduleNameStringLenght != 0xFFFFFFFF) {
			byte[] buffer = new byte[moduleNameStringLenght];
			in.read(buffer);		
			mModuleName = new String(buffer, "UTF-16");
		}

		// Длина текста с названием модуля
		int previewImagePathStringLenght = in.readInt();
		if (previewImagePathStringLenght != 0xFFFFFFFF) {
			byte[] buffer = new byte[previewImagePathStringLenght];
			in.read(buffer);		
			mPreviewImagePath = new String(buffer, "UTF-16");
		}

		// Стартовый угол
		mStartAngle = in.readShort();

		// Размер элемента
		mSizeWidth = in.readInt();
		mSizeHeight = in.readInt();

		// Размер области предупреждения
		mWarningSizeWidth = in.readInt();
		mWarningSizeHeight = in.readInt();

		// Названия дополнительных параметров
		int extraParameterNamesCount = in.readInt();
		for (int i = 0; i < extraParameterNamesCount; i++) {
			int stringLenght = in.readInt();
			if (stringLenght == 0xFFFFFFFF)
				continue;

			byte[] buffer = new byte[stringLenght];
			in.read(buffer);		
			mExtraParameterName.add(new String(buffer, "UTF-16"));
		}

		// Тип дополнительных параметров
		int extraParameterTypesCount = in.readInt();
		for (int i = 0; i < extraParameterTypesCount; i++) {
			ExtraParameterType extraParameterType = ExtraParameterType.getType(in.readByte());
			mExtraParameterType.add(extraParameterType);
		}

		// Тип UI,связанного с дополнительным параметром
		int extraUIElementTypesCount = in.readInt();
		for (int i = 0; i < extraUIElementTypesCount; i++) {
			ExtraUIElementType extraUIElementType = ExtraUIElementType.getType(in.readByte());
			mExtraUIElementType.add(extraUIElementType);
		}	

		// Тип UI,связанного с дополнительным параметром
		int defaultExtraValuesCount = in.readInt();
		for (int i = 0; i < defaultExtraValuesCount; i++) {	
			
			// Тип данных
			in.readInt();
			
			// Нулевой флаг
			boolean nullFlag = in.readBoolean();
			if (nullFlag)
				in.readInt();

			switch (mExtraParameterType.get(i)) {
			case String:
				// Обработка нулевого значения
				if (nullFlag) {
					mDefaultExtraValues.add(new String());
				} else {
					int stringLenght = in.readInt();
					if (stringLenght == 0xFFFFFFFF)
						break;
					byte[] buffer = new byte[stringLenght];
					in.read(buffer);		
					mDefaultExtraValues.add(new String(buffer, "UTF-16"));
				}
				break;

			case Integer:
				if (nullFlag) {
					mDefaultExtraValues.add(0);
				} else {	
					mDefaultExtraValues.add(in.readInt());
				}
				break;

			case Real:
				if (nullFlag) {
					mDefaultExtraValues.add(0.0);
				} else {	
					mDefaultExtraValues.add(in.readFloat());
				}
				break;

			case Bool:
				if (nullFlag) {
					mDefaultExtraValues.add(false);
				} else {	
					mDefaultExtraValues.add(in.readBoolean());
				}
				break;

			case Color:
				if (nullFlag) {
					mDefaultExtraValues.add(Color.BLACK);
				} else {	
					in.readByte();
					short alpha = in.readShort();
					short red = in.readShort();
					short green = in.readShort();
					short blue = in.readShort();
					in.readShort();
					mDefaultExtraValues.add(Color.argb(alpha, red, green, blue));
				}
				break;
			}
		}	
		
		// Тип элемента
		mElementLogicType = ElementLogicType.getType(in.readByte());

		return;
	}


	public ArrayList<Parameter> getParameters() {
		
		return mParameters;
	}


	public ArrayList<Figure> getFigures() {
		
		return mFigures;
	}


	public ArrayList<Record> getRecords() {
		
		return mRecords;
	}


	public ArrayList<Record> getDefaultRecords() {
		
		return mDefaultRecords;
	}


	public String getModuleHash() {
		
		return mModuleHash;
	}


	public String getModuleName() {
		
		return mModuleName;
	}


	public short getStartAngle() {
		
		return mStartAngle;
	}


	public int getSizeWidth() {
		
		return mSizeWidth;
	}


	public int getSizeHeight() {
		
		return mSizeHeight;
	}


	public int getWarningSizeWidth() {
		
		return mWarningSizeWidth;
	}


	public int getWarningSizeHeight() {
		
		return mWarningSizeHeight;
	}


	public ArrayList<ExtraParameterType> getExtraParameterType() {
		
		return mExtraParameterType;
	}


	public ElementLogicType getElementLogicType() {
		
		return mElementLogicType;
	}
}
