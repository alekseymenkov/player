package element_logic;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;

public class ElementLogicLoader {

	private String mDirectory;
	HashMap<String, ElementLogic> mLoadedModules = new HashMap<String, ElementLogic>();
	
	
	public void setFile(String directory) {
		mDirectory = directory  + "/modules";
		return;
	}
	
	
	
	public ElementLogic loadElementLogic(String fileName) throws IOException {
		
		// Исключение повторной загрузки модулей
		if (mLoadedModules.containsKey(fileName))
			return mLoadedModules.get(fileName);

		// Загрузка модуля
		File file = new File(mDirectory + "/" + fileName);
		if (!file.exists())
			return null;

		// Поток чтения файла
		FileInputStream fis = new FileInputStream(file);
		// Поток с чтения данных из файла
		DataInputStream dis = new DataInputStream(fis);
		// Парсинг модуля
		ElementLogic logic = ElementLogicFactory.createObject(dis);
		
		mLoadedModules.put(fileName, logic);
		
		return logic;
	}
}
