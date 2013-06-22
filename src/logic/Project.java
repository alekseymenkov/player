package logic;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;



public class Project {
	
	private int mUnitsCount;
	private String mProjectName;
	
	public Project(String directory) throws IOException {
		ProjectSettingsLoader projectSettingsLoader = new ProjectSettingsLoader();

		
		projectSettingsLoader.setProject(directory);
		try {
			projectSettingsLoader.parseDocument();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
		
		mUnitsCount = projectSettingsLoader.getUnitsCount();
		mProjectName = projectSettingsLoader.getProjectName();
	}
	
	
	
	public int getUnitsCount() {
		
		return mUnitsCount;
	}
	
	
	
	public String getProjectName() {
		
		return mProjectName;
	}
}
