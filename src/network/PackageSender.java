package network;


import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;


/**
 * Класс для отправки сообщений в сеть.
 */
public class PackageSender {

	// Размер сегмента - 1 байт
	final int PACKAGE_SIZE_1BYTE = 1;
	// Размер сегмента - 2 байта
	final int PACKAGE_SIZE_2BYTES = 2;
	// Частота передачи данных
	final int SEND_TIMEOUT = 1000;
	// Режим передачи - прямой
	final int DIRECT_SEND_MODE = 0;
	// Режим передачи - обратный
	final int REVERSE_SEND_MODE = 1;

	// Рабочий сокет
	private Socket mSocket;
	// Поток
	OutputStream mOutputStream;

	
	PackageSender(Socket socket) throws IOException {
		
		mSocket = socket;
		mOutputStream = mSocket.getOutputStream();

	}

	
	// Отправка пакета с именем отображаемого в память файла
	boolean sendMMFPackage(String mmfName) {

		byte[] packageToSend = createMMFPackage(mmfName);

		return sendPackage(packageToSend);
	}


	// Отправка пакета с регистрационной информацией (прямой режим передачи)
	boolean sendRegInfoPackageWithDirectMode(int elementID, int[] startBytes, int[] bytesCount) {

		byte[] packageToSend = createRegInfoPackage(DIRECT_SEND_MODE, elementID, startBytes, bytesCount);

		return sendPackage(packageToSend);
	}


	// Отправка пакета с регистрационной информацией (обратный режим передачи)
	boolean sendRegInfoPackageWithReverseMode(int elementID, int[] startBytes, int[] bytesCount) {

		byte[] packageToSend = createRegInfoPackage(REVERSE_SEND_MODE, elementID, startBytes, bytesCount);

		return sendPackage(packageToSend);
	}


	// Отправка пакета с управляющими командами
	boolean sendCommandPackage(CommandPackageType commandPackageType) {

		byte[] packageToSend = createCommandPackage(commandPackageType);

		return sendPackage(packageToSend);
	}


	// Отправка пакетов с запросом данных
	boolean sendDataRequestPackage(int requestID, int startByte, int bytesCount) {

		byte[] packageToSend = createDataRequestPackage(requestID, startByte, bytesCount);

		return sendPackage(packageToSend);
	}


	// Отправка пакетов с данными
	boolean sendDataPackage(int elementID, int value) {

		final int sizeOfInt = 4;
		byte[] packageToSend = createDataPackage(elementID, value, sizeOfInt);

		return sendPackage(packageToSend);
	}
	
	
	// Отправка пакетов с данными
	boolean sendDataPackage(int elementID, short value) {

		final int sizeOfShort = 2;
		byte[] packageToSend = createDataPackage(elementID, value, sizeOfShort);

		return sendPackage(packageToSend);
	}
	
	
	// Отправка пакетов с данными
	boolean sendDataPackage(int elementID, byte value) {

		final int sizeOfByte = 1;
		byte[] packageToSend = createDataPackage(elementID, value, sizeOfByte);

		return sendPackage(packageToSend);
	}
	
	
	// Отправка пакета
	private boolean sendPackage(byte[] packageToSend) {
		
		boolean isNoError = true;
		
		try {
			if (mOutputStream != null) {
				mOutputStream.write(packageToSend);
				mOutputStream.flush();
			}
		} catch (IOException e) {
			isNoError = false;
			e.printStackTrace();
		}

		return isNoError;
	}


	// Генерация пакета "Имя MMF"
	private byte[] createMMFPackage(String mmfName) {

		// Конвертация имени MMF (в UTF-8) в байты
		byte[] mmfNameBytes = mmfName.getBytes();

		// Тип пакета (1 байт)
		byte packageType = PackageType.MMFName.getType();

		// Размер пакета (2 байта)
		short sizeOfPackage = (short)(mmfNameBytes.length + PACKAGE_SIZE_1BYTE + PACKAGE_SIZE_2BYTES);
		byte[] packageSize = shortToByte(sizeOfPackage);

		// Генерация пакета
		byte[] packageToSend = new byte [sizeOfPackage];
		System.arraycopy(packageSize, 0, packageToSend, 0, PACKAGE_SIZE_2BYTES);
		packageToSend[PACKAGE_SIZE_2BYTES] = packageType;
		System.arraycopy(mmfNameBytes, 0, packageToSend, PACKAGE_SIZE_2BYTES + PACKAGE_SIZE_1BYTE, mmfNameBytes.length);

		return packageToSend;
	}


	// Генерация пакета с регистрационными данными
	private byte[] createRegInfoPackage(int mode, int elementID, int[] startBytes, int[] bytesCount) {
		
		// Защита от дурака
		if (startBytes.length != bytesCount.length)
			return null;

		// Количество частей
		int partsCount = startBytes.length;

		// Тип пакета
		byte packageType = PackageType.RegInfo.getType();

		// Режим передачи
		byte connectType = (byte)mode;

		// Размер пакета
		short sizeOfPackage = (short)(PACKAGE_SIZE_2BYTES * 3 + PACKAGE_SIZE_2BYTES * partsCount * 2);
		byte[] packageSize = shortToByte(sizeOfPackage);

		// Генерация пакета
		byte[] packageToSend = new byte [sizeOfPackage];
		// Копирование размера
		System.arraycopy(packageSize, 0, packageToSend, 0, PACKAGE_SIZE_2BYTES);
		// Установка типа пакета
		packageToSend[PACKAGE_SIZE_2BYTES] = packageType;
		// Установка режима передачи
		packageToSend[PACKAGE_SIZE_2BYTES + PACKAGE_SIZE_1BYTE] = connectType;
		// Установка номера регистрируемого элемента
		System.arraycopy(shortToByte((short)elementID), 0, packageToSend, PACKAGE_SIZE_2BYTES * 2, PACKAGE_SIZE_2BYTES);
		// Установка значений стартового байта и количества байт
		for (int i = 0; i < partsCount; i++) {
			System.arraycopy(shortToByte((short)startBytes[i]), 0, packageToSend, PACKAGE_SIZE_2BYTES * 2 + PACKAGE_SIZE_2BYTES * (i * 2 + 1), PACKAGE_SIZE_2BYTES);
			System.arraycopy(shortToByte((short)bytesCount[i]), 0, packageToSend, PACKAGE_SIZE_2BYTES * 2 + PACKAGE_SIZE_2BYTES * (i * 2 + 2), PACKAGE_SIZE_2BYTES);
		}

		return packageToSend;
	}


	// Создание командного пакета
	private byte[] createCommandPackage(CommandPackageType commandPackageType) {

		// Размер пакета
		short sizeOfPackage = (short)(PACKAGE_SIZE_2BYTES + PACKAGE_SIZE_1BYTE + PACKAGE_SIZE_1BYTE);

		// Тип пакета
		byte packageType = PackageType.Command.getType();

		// Тип команды
		byte commandType = commandPackageType.getType();

		// Генерация пакета
		byte[] packageToSend = new byte [sizeOfPackage];
		// Копирование размера
		System.arraycopy(shortToByte(sizeOfPackage), 0, packageToSend, 0, PACKAGE_SIZE_2BYTES);
		// Установка типа пакета
		packageToSend[PACKAGE_SIZE_2BYTES] = packageType;
		// Установка типа команды
		packageToSend[PACKAGE_SIZE_2BYTES + PACKAGE_SIZE_1BYTE] = commandType;

		return packageToSend;
	}


	// Генерация пакета для запроса данных с сервера
	private byte[] createDataRequestPackage(int requestID, int startByte, int bytesCount) {

		// Размер пакета
		short sizeOfPackage = (short)(PACKAGE_SIZE_2BYTES + PACKAGE_SIZE_1BYTE + PACKAGE_SIZE_2BYTES * 3);

		// Тип пакета
		byte packageType = PackageType.SingleDataRequest.getType();

		// Генерация пакета
		byte[] packageToSend = new byte [sizeOfPackage];
		// Копирование размера
		System.arraycopy(shortToByte(sizeOfPackage), 0, packageToSend, 0, PACKAGE_SIZE_2BYTES);
		// Установка типа пакета
		packageToSend[PACKAGE_SIZE_2BYTES] = packageType;
		// Установка номера запроса
		System.arraycopy(shortToByte((short)requestID), 0, packageToSend,
				PACKAGE_SIZE_2BYTES + PACKAGE_SIZE_1BYTE, PACKAGE_SIZE_2BYTES);
		// Установка стартового байта
		System.arraycopy(shortToByte((short)startByte), 0, packageToSend,
				PACKAGE_SIZE_2BYTES + PACKAGE_SIZE_1BYTE + PACKAGE_SIZE_2BYTES, PACKAGE_SIZE_2BYTES);
		// Установка количества байт
		System.arraycopy(shortToByte((short)bytesCount), 0, packageToSend,
				PACKAGE_SIZE_2BYTES + PACKAGE_SIZE_1BYTE + PACKAGE_SIZE_2BYTES * 2, PACKAGE_SIZE_2BYTES);

		return packageToSend;
	}


	// Генерация пакета с данными
	private byte[] createDataPackage(int elementID, int value, int sizeOfValue) {
	
		// Размер пакета
		short sizeOfPackage = (short)(PACKAGE_SIZE_2BYTES * 2 + PACKAGE_SIZE_1BYTE + sizeOfValue);

		// Тип пакета
		byte packageType = PackageType.Data.getType();

		// Генерация пакета
		byte[] packageToSend = new byte [sizeOfPackage];
		// Копирование размера
		System.arraycopy(shortToByte(sizeOfPackage), 0, packageToSend, 0, PACKAGE_SIZE_2BYTES);
		// Установка типа пакета
		packageToSend[PACKAGE_SIZE_2BYTES] = packageType;
		// Установка номера пакета с данными
		System.arraycopy(shortToByte((short)elementID), 0, packageToSend,
				PACKAGE_SIZE_2BYTES + PACKAGE_SIZE_1BYTE, PACKAGE_SIZE_2BYTES);
		// Установка значения
		System.arraycopy(convertValueToByteArray(value, sizeOfValue), 0,
				packageToSend, PACKAGE_SIZE_2BYTES * 2 + PACKAGE_SIZE_1BYTE,
				sizeOfValue);

		return packageToSend;
	}


	// Преобразование значения в массив байтов (в соответствии с типом значения)
	private byte[] convertValueToByteArray(int value, int sizeOfValue) {

		final int sizeOfByte = 1;
		final int sizeOfShort = 2;
		final int sizeOfInt = 4;
		
		byte[] byteValue = new byte [sizeOfValue];
		switch (sizeOfValue) {
			case sizeOfByte:
				byteValue[0] = (byte)value;
				break;
			case sizeOfShort:
				byteValue = shortToByte((short)value);
				break;
			case sizeOfInt:
				byteValue = intToByte(value);
				break;
		}
		
		return byteValue;
	}


	// Конвертация из Short в Byte
	private byte[] shortToByte(short value)
	{
		final int bufferSize = 2;
		byte[] buffer = new byte [bufferSize];

		buffer[0] = (byte)(value & 0xff);
		buffer[1] = (byte)((value >> 8) & 0xff);

		return buffer;
	}
	
	
	// Конвертация из Int в Byte
	private byte[] intToByte(int value)
	{
		final int bufferSize = 4;
		byte[] buffer = new byte [bufferSize];

		buffer[0] = (byte)(value & 0xff);
		buffer[1] = (byte)((value >> 8) & 0xff);
		buffer[2] = (byte)((value >> 16) & 0xff);
		buffer[3] = (byte)((value >> 24) & 0xff);

		return buffer;
	}
}
