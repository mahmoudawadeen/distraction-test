package glassConnector;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import org.joda.time.LocalTime;

import distractionTypingTest.MainWindow;

public class ActionReciverThread extends Thread {
	DatagramSocket socket;
	final int PORT = 4747;
	private final static String newline = "\n";
	boolean open = true;
	File file;
	FileWriter fw;
	MainWindow window;
	String message;

	boolean socketOpened;

	public ActionReciverThread(MainWindow window) throws IOException {
		socket = new DatagramSocket(null);
		socket.setReuseAddress(true);
		socket.bind(new InetSocketAddress("0.0.0.0",PORT));
		this.window = window;
		socketOpened = true;
	}

	public void run() {

		byte[] buf = new byte[1000];

		DatagramPacket dgp = new DatagramPacket(buf, buf.length);
		try {
			file = new File("log_glass.txt");
			fw = new FileWriter(file, true);
			while (true) {
				socket.receive(dgp);
				message = new String(dgp.getData(), 0, dgp.getLength());
				switch (message) {
				case "received":
					window.setStartSignalAck(true);
					break;
				case "time finished":
					socket.close();

					return;
				default:
					fw.write(message + " at " + LocalTime.now() + newline);
					fw.flush();
//					System.out.println(message+ " at " + LocalTime.now());
					break;
				}
			}
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("here");
			e.printStackTrace();
		}
	}

	public void closeSocket() {
		message = "time finished";
	}
	public File getLog(){
		return file;
	}
}
