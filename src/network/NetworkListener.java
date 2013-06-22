package network;

public interface NetworkListener {
	
	public void dataPackageReceive(int id, int elementID, byte[] data);
	public void commandPackageReceive(int id, CommandPackageType commandPackageType);
}