package element_interface;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.os.Environment;



public class ElementInterfaceLoader extends DefaultHandler{

	private static final String ELEMENT = "element";
	private static final String WIDTH = "width";
	private static final String HEIGHT = "height";
	private static final String SIZE_MODIFICATOR = "sizeModificator";
	private static final String X_POSITION = "xPosition";
	private static final String ELEMENT_NAME = "elementName";
	private static final String MODULE_NAME = "moduleName";
	private static final String MODULE_HASH = "moduleHash";
	private static final String ANGLE = "angle";
	private static final String Y_POSITION = "yPosition";
	private static final String PARAMETER = "parameter";
	private static final String START_BYTE = "startByte";
	private static final String START_BIT = "startBit";
	private static final String BITS_COUNT = "bitsCount";
	private static final String IS_USED = "isUsed";
	private static final String EXTRA_PARAMETER = "extraParameter";
	private static final String VALUE = "value";
	private static final String DESCRIPTION = "description";

	private String mDirectory;
	private int mSceneNumber = 0;

	private ArrayList<ElementInterface> mElements;
	private ArrayList<ElementInterfaceParameter> mParameters;
	private ArrayList<ElementInterfaceExtraParameter> mExtraParameters;
	private ElementInterface mElementInterface;
	private StringBuilder mStringBuilder;



	public ArrayList<ElementInterface> getElements(){
		return mElements;
	}

	

	public void setFile(String directory, int sceneNumber) {
		mDirectory = directory  + "/settings";
		mSceneNumber = sceneNumber;
    }
	
	
	
	public void parseDocument() throws ParserConfigurationException, SAXException, IOException {

		// Доступность SD
		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
			return;
		
		// Получение пути к SD
		File file = new File(mDirectory + "/" + "elements" + mSceneNumber + ".xml");
        if (!file.exists())
            throw new IOException("Settings file isn't exist!");
		

		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser parser;
		parser = factory.newSAXParser();
		parser.parse(file, this);

    }


	public void characters(char[] ch, int start, int length)
			throws SAXException {
		super.characters(ch, start, length);
		mStringBuilder.append(ch, start, length);

    }


	public void endElement(String uri, String localName, String name)
			throws SAXException {
		super.endElement(uri, localName, name);

		if (localName.equalsIgnoreCase(ELEMENT)){
			mElementInterface.setParameters(mParameters);
			mElementInterface.setExtraParameters(mExtraParameters);
			mElements.add(mElementInterface);
		} 

		mStringBuilder.setLength(0);

    }


	public void startDocument() throws SAXException {
		super.startDocument();
		mElements = new ArrayList<ElementInterface>();
		mStringBuilder = new StringBuilder();

    }


	public void startElement(String uri, String localName, String name,
			Attributes attributes) throws SAXException {
		super.startElement(uri, localName, name, attributes);

		if (localName.equalsIgnoreCase(ELEMENT)) {
			mElementInterface = new ElementInterface();
						
			// Размер элемента 
			mElementInterface.setSize(Integer.parseInt(attributes.getValue(WIDTH)),
					Integer.parseInt(attributes.getValue(HEIGHT)));
			// Модификатор размера
			mElementInterface.setSizeModificator(Double.parseDouble(attributes.getValue(SIZE_MODIFICATOR)));
			// Местоположение элемента на экране
			mElementInterface.setPosition(Integer.parseInt(attributes.getValue(X_POSITION)),
					Integer.parseInt(attributes.getValue(Y_POSITION)));
			// Название элемента
			mElementInterface.setElementName(attributes.getValue(ELEMENT_NAME));
			// Название модуля
			mElementInterface.setModuleName(attributes.getValue(MODULE_NAME));
			// Хеш модуля (в BASE64)
			mElementInterface.setModuleHash(attributes.getValue(MODULE_HASH));
			// Стартовый угол
			mElementInterface.setAngle(Integer.parseInt(attributes.getValue(ANGLE)));

			// Очистка списка параметров
			mParameters = new ArrayList<ElementInterfaceParameter>();
			// Очистка списка дополнительных параметров
			mExtraParameters = new ArrayList<ElementInterfaceExtraParameter>();

		} else if (localName.equalsIgnoreCase(PARAMETER)) {
			// Стартовый байт
			int startByte = Integer.parseInt(attributes.getValue(START_BYTE));
			// Стартовый бит
			int startBit = Integer.parseInt(attributes.getValue(START_BIT));
			// Количество бит
			int bitsCount = Integer.parseInt(attributes.getValue(BITS_COUNT));
			// Описание параметра
			String description = attributes.getValue(DESCRIPTION);
			// Параметр используется?
			boolean isUsed = Integer.parseInt(attributes.getValue(IS_USED)) == 0 ? false : true;

			mParameters.add(new ElementInterfaceParameter(startByte, startBit, bitsCount, description, isUsed));

		} else if (localName.equalsIgnoreCase(EXTRA_PARAMETER)) {
			// Значение дополнительного параметра
			String value = attributes.getValue(VALUE);

			mExtraParameters.add(new ElementInterfaceExtraParameter(value));
		}

    }
}