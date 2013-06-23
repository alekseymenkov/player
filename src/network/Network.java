package network;

import java.net.Socket;
import java.io.*;





// Класс, описывающий сетевое взаимодействие
public class Network implements Runnable  {

	// Сокет
	Socket mSocket;
	// Класс, отвечающий за отправку пакетов
	PackageSender mPackageSender;
	// Класс, отвечающий за получение пакетов
	PackageReader mPackageReader;
	Thread mPackageReaderThread;

	// Адрес сервера
	String mHost;
	// Порт сервера
	int mPort;
	// Идентификатор вкладки
	int mID;

	// Слушатель изменений состояния сокета
	SocketStateChangeListener mSocketStateChangeListener = null;

	// Флаг для остановки подключения
	boolean mIsStop;

	// Обработка событий получения данных
	NetworkListener mNetworkListener;


	/**
	 * Конструктор объекта клиента
	 * @param tabID - идентификатор сокета (целое число от 0 до N)
	 */
	public Network(int tabID) {

		// Инициализация полей
		mID = tabID;
		mIsStop = false;
	}

	
	
	/**
	 * Установка адреса сервера
	 * @param host - IP адрес или localhost или доменное имя
	 * @param port - порт, на котором висит сервер
	 */
	public void setServerAddress(String host, int port) {

		// Инициализация полей
		mHost = host;
		mPort = port;

		mIsStop = false;
	}
	

	
	// Главный цикл класса
	public void run() {

		// Состояние сети - "Попытка подключения"
		changeNetworkState(SocketState.Connecting);

		try {

			mSocket = new Socket(mHost, mPort);
			mPackageSender = new PackageSender(mSocket);
			mPackageReader = new PackageReader(mID, mSocket);
		} catch (IOException e) {
			// Состояние сети - "Подключение разорвано"
			changeNetworkState(SocketState.Disconnected);
			e.printStackTrace();
			return;
		}

		// Обработка аварийного выхода
		if (mIsStop)
			return;

		mPackageReader.setNetworkListener(mNetworkListener);
		mPackageReader.setSocketStateChangeListener(mSocketStateChangeListener);
		mPackageReaderThread = new Thread(mPackageReader, "reader_" + mID);
		mPackageReaderThread.start();

		// Состояние сети - "Подключение установлено"
		changeNetworkState(SocketState.Connected);
	}

	
	// Отправка пакета с именем отображаемого в память файла
	public boolean sendMMFPackage(String mmfName) {

		return mPackageSender.sendMMFPackage(mmfName);
	}


	// Отправка пакета с регистрационной информацией (прямой режим передачи)
	public boolean sendRegInfoPackageWithDirectMode(int elementID, int[] startBytes, int[] bytesCount) {

		return mPackageSender.sendRegInfoPackageWithDirectMode(elementID, startBytes, bytesCount);
	}


	// Отправка пакета с управляющими командами
	public boolean sendCommandPackage(CommandPackageType commandPackageType) {
		
		return mPackageSender.sendCommandPackage(commandPackageType);
	}
	
	
	
	public boolean isConnected() {
        return mSocket != null && mSocket.isConnected();
	}



	// Отключение от сети
	public synchronized void close() {

		try {
            mIsStop = true;
            if (mPackageReader != null)
                mPackageReader.stop();
			if (mSocket != null && !mSocket.isClosed())
				mSocket.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
    }


	// Установка слушателя для данных о режимах работы БИНС
	public void setNetworkListener(NetworkListener networkListener) {
		mNetworkListener = networkListener;
    }


	// Установка слушателя для отслеживания работы сети
	public void setSocketStateChangeListener(SocketStateChangeListener socketStateChangeListener) {
		mSocketStateChangeListener = socketStateChangeListener;
    }


	private void changeNetworkState(SocketState state) {
		if (mSocketStateChangeListener != null)
			mSocketStateChangeListener.OnChangeSocketState(mID, state);
    }
}