package network;


public interface SocketStateChangeListener {
	
	void OnChangeSocketState(int id, SocketState state);
}

