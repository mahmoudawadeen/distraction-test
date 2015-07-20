package glassConnector;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.file.Files;




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
	boolean restartReceivedBoolean = false;

	boolean socketOpened;

	public ActionReciverThread(MainWindow window, String id , String dateTime) throws IOException {
		socket = new DatagramSocket(null);
		socket.setReuseAddress(true);
		socket.bind(new InetSocketAddress("0.0.0.0", PORT));
		this.window = window;
		socketOpened = true;
		dateTime = dateTime.replace('T', ' ')
				.substring(0, dateTime.indexOf('.')).replace(':', '.');
		file = new File("logs/" + id + "/" + dateTime + "/log_glass.txt");
		if (!(file.getParentFile().exists())) {
			file.getParentFile().mkdirs();
		}
		fw = new FileWriter(file, true);
	}

	public void run() {
		byte[] buf = new byte[1000];

		DatagramPacket dgp = new DatagramPacket(buf, buf.length);
		try {
			
			while (true) {
				socket.receive(dgp);
				message = new String(dgp.getData(), 0, dgp.getLength());
				System.out.println(message);
				switch (message) {
					case "restart received" :
						restartReceivedBoolean = true;
						window.restartApplication();
						break;
					case "received" :
						window.setStartSignalAck(true);
						window.startFeedback();
						break;
					case "time finished" :
						socket.close();

						return;
					default :
						if (open) {
							window.setCaps_glass(message.equals("on"));
							fw.write(message + " at " + System.currentTimeMillis()
									+ newline);
							fw.flush();
						}
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
		open = false;
	}
	public File getLog() {
		return file;
	}
	public void deleteLog() throws IOException{
		fw.close();
		Files.deleteIfExists(file.toPath());
	}
	public boolean getRestartRecieved() {
		return restartReceivedBoolean;
	}
}
