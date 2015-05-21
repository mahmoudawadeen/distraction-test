package glassConnector;

import java.io.IOException;
import java.net.DatagramSocket;

public class ActionReciverThread extends Thread {
	DatagramSocket socket;
	public ActionReciverThread() throws IOException {
	    socket = new DatagramSocket(4445);
	}  
}
