package logic;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

public class SceneSettingsLoader extends DefaultHandler {

	private static final String SERVER_ADDRESS = "serverAddress";
	private static final String PORT = "port";
	private static final String IS_AUTO_CONNECT = "isAutoConnect";
	private static final String BACKGROUND_IMAGE = "backgroundImage";
	private static final String SCALE_FACTOR = "scaleFactor";
	private static final String SCENE_NAME = "sceneName";	
	private static final String MMF_NAME = "mmfName";
	
	private String mCurrentElement;
	private String mDirectory = "";
	private int mSceneNumber = 0;
	
	private String mServerAddress;
	private int mPort;
	private boolean mIsAutoConnect;
	private String mBackgroundImage;
	private int mScaleFactor;
	private String mSceneName;
	private String mMMFName;
	
	
	
	public void setFile(String directory, int sceneNumber) {
		mDirectory = directory + "/settings";
		mSceneNumber = sceneNumber;
		return;
	}

	

	public void parseDocument() throws ParserConfigurationException, SAXException, IOException {

		// Получение пути к файлу
		File file = new File(mDirectory + "/" + "scene" + mSceneNumber + ".xml");
        if (!file.exists())
            throw new IOException("Settings file isn't exist!");
		
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser parser;
		parser = factory.newSAXParser();
		parser.parse(file, this);
		
		return;
	}

	

	public void characters(char[] ch, int start, int length)
			throws SAXException {
		super.characters(ch, start, length);

		 if (mCurrentElement.equals(SERVER_ADDRESS))
		     mServerAddress = new String(ch, start, length);
		 else if (mCurrentElement.equals(PORT))
			 mPort = Integer.parseInt(new String(ch, start, length));
		 else if (mCurrentElement.equals(IS_AUTO_CONNECT))
			 mIsAutoConnect = Boolean.parseBoolean(new String(ch, start, length));
		 else if (mCurrentElement.equals(BACKGROUND_IMAGE))
		     mBackgroundImage = new String(ch, start, length);
		 else if (mCurrentElement.equals(SCALE_FACTOR))
			 mScaleFactor = Integer.parseInt(new String(ch, start, length));
		 else if (mCurrentElement.equals(SCENE_NAME))
		     mSceneName = new String(ch, start, length);
		 else if (mCurrentElement.equals(MMF_NAME))
			 mMMFName = new String(ch, start, length);
		 
		return;
	}


	
	public void endElement(String uri, String localName, String name)
			throws SAXException {
		super.endElement(uri, localName, name);
		mCurrentElement = "";
		return;
	}

	

	public void startDocument() throws SAXException {
		super.startDocument();
		return;
	}


	
	public void startElement(String uri, String localName, String name,
			Attributes attributes) throws SAXException {
		super.startElement(uri, localName, name, attributes);
		mCurrentElement = name;
		return;
	}



	public String getServerAddress() {
		return mServerAddress;
	}



	public int getPort() {
		return mPort;
	}



	public boolean isAutoConnect() {
		return mIsAutoConnect;
	}



	public String getBackgroundImage() {
		return mBackgroundImage;
	}



	public int getScaleFactor() {
		return mScaleFactor;
	}



	public String getSceneName() {
		return mSceneName;
	}



	public String getMMFName() {
		return mMMFName;
	}
}
