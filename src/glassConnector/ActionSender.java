package glassConnector;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ActionSender {
	public static void main(String[] args) throws IOException, InterruptedException {
		DatagramSocket socket = new DatagramSocket();
		byte[] buf = "capslock on".getBytes();
		InetAddress hostAddress = InetAddress.getByName("localhost");
		DatagramPacket dp = new DatagramPacket(buf, buf.length,hostAddress,5286);
		socket.send(dp);
		Thread.sleep(30000);
		buf = "capslock off".getBytes();
		dp = new DatagramPacket(buf, buf.length,hostAddress,5286);
		socket.send(dp);
	}

}