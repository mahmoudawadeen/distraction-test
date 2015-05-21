package glassConnector;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ActionReciver   {
	 public static void main(String[] args) throws Exception {
		    DatagramSocket socket = new DatagramSocket();
		    byte[] buf = new byte[1000];
		    DatagramPacket dp = new DatagramPacket(buf, buf.length);

		    InetAddress hostAddress = InetAddress.getByName("localhost");
		    
		    socket.close();
		  }
}