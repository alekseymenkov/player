package logic;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.os.Environment;


public class ProjectSettingsLoader extends DefaultHandler {

	private static final String PROJECT_NAME = "projectName";
	private static final String UNITS_COUNT = "unitsCount";

	final String fileName = "project.xml";

	private String mCurrentElement;
	private String mDirectory = "";
	
	private int mUnitsCount;
	private String mProjectName;
	
	
	
	public int getUnitsCount() {
		
		return mUnitsCount;
	}
	
	
	
	public String getProjectName() {
		
		return mProjectName;
	}

	
	
	public void setProject(String directory) {
		mDirectory = directory;
	}

	

	public void parseDocument() throws ParserConfigurationException, SAXException, IOException {

		// Получение пути к файлу
		File file = new File(mDirectory + "/" + fileName);
		if (!file.exists())
			throw new IOException("Project file isn't exist!");
		
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser parser;
		parser = factory.newSAXParser();
		parser.parse(file, this);

    }


	public void characters(char[] ch, int start, int length)
			throws SAXException {
		super.characters(ch, start, length);

		 if (mCurrentElement.equals(UNITS_COUNT))
		     mUnitsCount = Integer.parseInt(new String(ch, start, length));
		 else if (mCurrentElement.equals(PROJECT_NAME))
			 mProjectName = new String(ch, start, length);

    }


	public void endElement(String uri, String localName, String name)
			throws SAXException {
		super.endElement(uri, localName, name);
		mCurrentElement = "";
    }


	public void startDocument() throws SAXException {
		super.startDocument();
    }


	public void startElement(String uri, String localName, String name,
			Attributes attributes) throws SAXException {
		super.startElement(uri, localName, name, attributes);
		mCurrentElement = name;
    }
}