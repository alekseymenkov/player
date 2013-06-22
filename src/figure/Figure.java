package figure;


import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;


public class Figure {

	// Тип фигуры
	FigureType mFigureType;
	// Z-индекс
	byte mZIndex;
	// Флаг статичности фигуры
	boolean mIsStatic;
	// Параметры
	ArrayList<Integer> mPoints;
	
	
	Figure(DataInputStream in) throws IOException {
		
		mPoints = new ArrayList<Integer>();
		
		readObject(in);
	}

	
	public Figure(FigureType type) {
		
		mFigureType = type;
		mZIndex = 0;
		mIsStatic = false;
	}


	private void readObject (DataInputStream in) throws IOException {

		// Тип объекта
		mFigureType = FigureType.getType(in.readByte());
		// Z-индекс
		mZIndex = in.readByte();
		// Флаг статичность фигуры
		mIsStatic = in.readBoolean();
		// Параметры
		int pointsCount = in.readInt();
		for (int i = 0; i < pointsCount; i++) {
			mPoints.add(in.readInt());
		}
		
		return;
	}


	public FigureType getFigureType() {
		
		return mFigureType;
	}


	public byte getZIndex() {
		
		return mZIndex;
	}


	public boolean isStatic() {
		
		return mIsStatic;
	}


	public ArrayList<Integer> getPoints() {
		
		return mPoints;
	}
	
	
	public void setPoints(ArrayList<Integer> points) {
		
		mPoints = points;
		return;
	}
}
