package item;


import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

import android.content.Context;
import element_interface.ElementInterface;
import element_interface.ElementInterfaceLoader;
import element_logic.ElementLogic;
import element_logic.ElementLogicLoader;


public class ItemsCreator {

	
	ElementInterfaceLoader mElementInterfaceLoader;
	ElementLogicLoader mElementLogicLoader;
	Context mContext;


	public ItemsCreator(Context context) {
		mContext = context;
		mElementInterfaceLoader = new ElementInterfaceLoader();
		mElementLogicLoader = new ElementLogicLoader();
	}


	public ArrayList<Item> loadItems(String directory, int groupID) throws IOException {

		ArrayList<Item> items = new ArrayList<Item>();
		ArrayList<ElementInterface> elements;

		// Установка директории для загрузчика логики
		mElementLogicLoader.setFile(directory);
		
		try {
			mElementInterfaceLoader.setFile(directory, groupID);
			mElementInterfaceLoader.parseDocument();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
		
		elements = mElementInterfaceLoader.getElements();
		
		int i = 0;
		for (ElementInterface elementInterface : elements) {
			String moduleName = elementInterface.getModuleName();
			ElementLogic elementLogic = mElementLogicLoader.loadElementLogic(moduleName);

            if (elementLogic == null)
                continue;

			Item item = new Item(mContext, i, groupID, elementLogic, elementInterface);
			item.setPosition(elementInterface.getX(), elementInterface.getY());
			items.add(item);
			i++;
		}
		
		return items;
	}
}
