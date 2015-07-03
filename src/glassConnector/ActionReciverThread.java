package glassConnector;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
//import java.time.LocalDateTime;
import org.joda.time.LocalDateTime;

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

	public ActionReciverThread(MainWindow window,String id) throws IOException {
		socket = new DatagramSocket(null);
		socket.setReuseAddress(true);
		socket.bind(new InetSocketAddress("0.0.0.0",PORT));
		this.window = window;
		socketOpened = true;
		String dateTime = LocalDateTime.now().toString();
		dateTime = dateTime.replace('T', ' ')
				.substring(0, dateTime.indexOf('.')).replace(':', '.');
		file = new File("logs/" + id + "/" + dateTime+"/log_glass.txt");
		if(!(file.getParentFile().exists())){
			file.getParentFile().mkdirs();
		}
	}

	public void run() {
		byte[] buf = new byte[1000];

		DatagramPacket dgp = new DatagramPacket(buf, buf.length);
		try {
			fw = new FileWriter(file, true);
			while (open) {
				socket.receive(dgp);
				message = new String(dgp.getData(), 0, dgp.getLength());
				switch (message) {
				case "received":
					System.out.println("done");
					window.setStartSignalAck(true);
					break;
				case "time finished":
					socket.close();

					return;
				default:
					window.setCaps_glass(message.equals("on"));
					fw.write(message + " at " + LocalTime.now() + newline);
					fw.flush();
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
		open=false;
	}
	public File getLog(){
		return file;
	}
}
