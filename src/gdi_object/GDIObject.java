package gdi_object;


import java.io.DataInputStream;
import java.io.IOException;
import android.graphics.Color;


// Формат: http://qt-project.org/doc/qt-4.8/datastreamformat.html

public class GDIObject {

	// Тип объекта
	GDIObjectType mGDIType;
	// Параметры цвета
	int mColor1;
	int mColor2;
	boolean mIsBlinked;
	// Параметры карандаша
	double mPenWidth;
	PenStyle mPenStyle;
	// Параметры текста
	String mText;
	double mSizeModificator;
	FontType mFontType;
	AlignmentType mTextAlign;

	
	GDIObject(DataInputStream in) throws IOException {
		
		readObject(in);
	}
	

	private void readObject(DataInputStream in) throws IOException {

		// Тип объекта
		mGDIType = GDIObjectType.getType(in.readByte());

		// Цвет №1
		in.readByte();
		int alpha = in.readChar() / 256;
		int red = in.readChar() / 256;
		int green = in.readChar() / 256;
		int blue = in.readChar() / 256;
		in.readShort();
		mColor1 = Color.argb(alpha, red, green, blue);
		
		// Цвет №2
		in.readByte();
		alpha = in.readChar() / 256;
		red = in.readChar() / 256;
		green = in.readChar() / 256;
		blue = in.readChar() / 256;
		in.readShort();
		mColor2 = Color.argb(alpha, red, green, blue);

		// Флаг мигания
		mIsBlinked = in.readBoolean();

		switch (mGDIType) {
		case Pen:
			// Толщина карандаша
			mPenWidth = in.readDouble();
			// Стиль карандаша
			mPenStyle = PenStyle.getType(in.readByte());
			break;

		case Text:
			// Текст
			int stringLenght = in.readInt();
			if (stringLenght != 0xFFFFFFFF) {
				byte[] buffer = new byte[stringLenght];
				in.read(buffer);		
				mText = new String(buffer, "UTF-16");
			}
			// Модификатор размера
			mSizeModificator = in.readDouble();
			// Тип шрифта
			mFontType = FontType.getType(in.readByte());
			// Выравнивание текста
			mTextAlign = AlignmentType.getType(in.readByte());
			break;

		case Brush:
			break;

		case Undefined:
			break;
		}

		return;
	}


	public GDIObjectType getGDIType() {
		
		return mGDIType;
	}


	public int getColor(int index) {
		
		switch (index) {
		case 0:
			return mColor1;
		case 1:
			return mColor2;
		default:
			return Color.MAGENTA;
		}
	}


	public boolean isBlinked() {
		
		return mIsBlinked;
	}


	public double getPenWidth() {
		
		return mPenWidth;
	}


	public PenStyle getPenStyle() {
		
		return mPenStyle;
	}


	public String getText() {
		
		return mText;
	}


	public double getSizeModificator() {
		
		return mSizeModificator;
	}


	public FontType getFontType() {
		
		return mFontType;
	}


	public AlignmentType getTextAlign() {
		
		return mTextAlign;
	}
}
