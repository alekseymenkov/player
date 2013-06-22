package record;


import gdi_object.GDIObject;
import gdi_object.GDIObjectFactory;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;


public class Record {

	int mParameterID;
	ArrayList<Boolean> mState;
	int mFigureID;
	ArrayList<GDIObject> mGDIObjects;
	short mAngle;

	
	Record(DataInputStream in) throws IOException {
		
		mState = new ArrayList<Boolean>();
		mGDIObjects = new ArrayList<GDIObject>();
		
		readObject(in);
	}
	
	
	private void readObject(DataInputStream in) throws IOException {
		
		mParameterID = in.readInt();
		
		int statesCount = in.readInt();
		for (int i = 0; i < statesCount; i++) {
			mState.add(in.readBoolean());
		}
		
		mFigureID = in.readInt();
		
		int objectsCount = in.readInt();
		for (int i = 0; i < objectsCount; i++) {
			mGDIObjects.add(GDIObjectFactory.createObject(in));
		}
		
		mAngle = in.readShort();		
		
		return;
	}
	
	
	public int getParameterID() {
		
		return mParameterID;
	}


	public ArrayList<Boolean> getState() {
		
		return mState;
	}


	public int getFigureID() {
		
		return mFigureID;
	}


	public ArrayList<GDIObject> getGDIObjects() {
		
		return mGDIObjects;
	}


	public short getAngle() {
		
		return mAngle;
	}
}
