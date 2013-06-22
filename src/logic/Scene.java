package logic;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;



public class Scene {
	
	private String mServerAddress;
	private int mPort;
	private boolean mIsAutoConnect;
	private String mBackgroundImage;
	private int mScaleFactor;
	private String mSceneName;
	private String mMMFName;
	private int mWidth;
	private int mHeight;
	private String mHTMLPath;
	
	public Scene(String directory, int sceneNumber) throws IOException {
		
		SceneSettingsLoader sceneSettingsLoader = new SceneSettingsLoader();
		sceneSettingsLoader.setFile(directory, sceneNumber);
		
		try {
			sceneSettingsLoader.parseDocument();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
		
		mServerAddress = sceneSettingsLoader.getServerAddress();
		mPort = sceneSettingsLoader.getPort();
		mIsAutoConnect = sceneSettingsLoader.isAutoConnect();
		mBackgroundImage = sceneSettingsLoader.getBackgroundImage();
		mScaleFactor = sceneSettingsLoader.getScaleFactor();
		mSceneName = sceneSettingsLoader.getSceneName();
		mMMFName = sceneSettingsLoader.getMMFName();
	}

	
	
	public void setBackgroundHTML(String htmlPath) {
		mHTMLPath = htmlPath;
		return;
	}
	
	
	
	public String getBackgroundHTML() {
		return mHTMLPath;
	}
	
	
	
	public void setSize(int width, int height) {
		mWidth = width;
		mHeight = height;
		return;
	}
	
	
	
	public int getWidth() {
		return mWidth;
	}
	
	
	
	public int getHeight() {
		return mHeight;
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

	
	
	public void setMMFName(String mmfName) {
		mMMFName = mmfName;
		return;
	}
	
	
	
	public String getMMFName() {
		return mMMFName;
	}
}
