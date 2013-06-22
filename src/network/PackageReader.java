package network;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class PackageReader implements Runnable {

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

	// Слушатель событий сети
	private NetworkListener mNetworkListener;
	// Слушатель состояния сокета
	private SocketStateChangeListener mSocketStateChangeListener;
	// Сокет
	private Socket mSocket;
	// Идентификатор сокета
	private int mID;
	// Поток
	InputStream mInputStream;
	// Флаг остановки
	private volatile boolean mIsStop = false;


	public PackageReader(int id, Socket socket) throws IOException {
		mID = id;
		mSocket = socket;
		mInputStream = mSocket.getInputStream();
	}


	// Основной цикл
	public void run() {

		boolean isNoError = true;

		try {
			
			// Буфер для парсинга пакетов
			byte[] processedBuffer = new byte [4096];
			// Количество данных в буфере для разбора пакетов
			int processedBufferLenght = 0;
			// Буфер для обработки входящих данных
			byte[] buffer = new byte [512];
			// Количество данных в буфере входящих данных
			int bufferLenght = 0;

			while (!mIsStop) {	

				// Количество считанных данных (в байтах)
				bufferLenght = mInputStream.read(buffer);
				if (bufferLenght > 0) {
					System.arraycopy(buffer, 0, processedBuffer, processedBufferLenght, bufferLenght);
					processedBufferLenght += bufferLenght;
				} else if (bufferLenght < 0) {
					isNoError = false;
				}

				while (isNoError) {

					// Проверка на условие: приниято мало данных
					if (processedBufferLenght < PACKAGE_SIZE_2BYTES)
						break;

					// Проверка на условие: принят ли весь пакет целиком?
					short packageSize = byteToShort(processedBuffer[1], processedBuffer[0]);
					if (processedBufferLenght < packageSize)
						break;

					// Получение типа пакета
					byte packageType = processedBuffer[PACKAGE_SIZE_2BYTES];

					// Получение содержимого пакета в packageContent
					byte[] packageContent = new byte [packageSize - PACKAGE_SIZE_2BYTES - PACKAGE_SIZE_1BYTE];
					System.arraycopy(processedBuffer, PACKAGE_SIZE_2BYTES + PACKAGE_SIZE_1BYTE,
							packageContent, 0,
							packageSize - PACKAGE_SIZE_2BYTES - PACKAGE_SIZE_1BYTE);

					// Модификация размера данных для обработки
					processedBufferLenght -= packageSize;
					// Удаление обработанных данных
					System.arraycopy(processedBuffer, packageSize, processedBuffer, 0, processedBufferLenght);

					// Парсинг полученного пакета
					if (PackageType.Command.getType() == packageType)
						isNoError = parseCommandPackage(packageContent);
					else if (PackageType.Data.getType() == packageType)
						isNoError = parseDataPackage(packageContent);
				}

				if (!isNoError) {
					throw new IOException("Wrong package received!");
				}
			}
		} catch(IOException e) {
			e.printStackTrace();
			changeSocketState(SocketState.Disconnected);
		}
	}
	
	
	public void stop() {

		mIsStop = true;
		return;
	}

	
	public void setNetworkListener(NetworkListener listener) {

		mNetworkListener = listener;
		return;
	}


	public void setSocketStateChangeListener(SocketStateChangeListener listener) {

		mSocketStateChangeListener = listener;
		return;
	}
	

	// Разбор пакета с управляющими данными
	private boolean parseCommandPackage(byte[] packageContent) {

		CommandPackageType type = CommandPackageType.getType(packageContent[0]);
		receiveCommandPackage(type);
		return true;
	}


	// Разбор пакета с периодическими данными
	private boolean parseDataPackage(byte[] packageContent) {

		byte[] packageData = new byte [packageContent.length - PACKAGE_SIZE_2BYTES];
		System.arraycopy(packageContent, PACKAGE_SIZE_2BYTES,
				packageData, 0,
				packageContent.length - PACKAGE_SIZE_2BYTES);
		int id = byteToShort(packageContent[1], packageContent[0]);

		receiveDataPackage(id, packageData);

		return true;
	}


	// Преобразование типов (byte -> short)
	private short byteToShort(byte high, byte low) {
		
		short value = (short)(((short)high << 8) | (short)low);
		return value;
	}


	private void receiveDataPackage(int elementID, byte[] data) {

		mNetworkListener.dataPackageReceive(mID, elementID, data);
		return;
	}
	
	
	private void receiveCommandPackage(CommandPackageType commandPackageType) {

		mNetworkListener.commandPackageReceive(mID, commandPackageType);
		return;
	}


	private void changeSocketState(SocketState socketState) {

		if (mSocketStateChangeListener != null)
			mSocketStateChangeListener.OnChangeSocketState(mID, socketState);
		return;
	}
}