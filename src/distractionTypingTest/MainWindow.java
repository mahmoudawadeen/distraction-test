package distractionTypingTest;

import glassConnector.ActionReciverThread;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDateTime;

public class MainWindow extends JPanel {

	private static final String ADDRESS = "137.250.171.64";
	private static final int PORT = 34144;

	private static final long serialVersionUID = -4434843170363458281L;
	protected JTextArea inputTextArea;
	protected JTextArea outputTextArea;
	protected JButton finish;
	protected JScrollPane scrollPane;
	protected JScrollPane scrollPaneText;
	protected GridBagConstraints c;
	protected JButton startButton;
	protected JTextField idTextField;
	protected JComboBox<String> glassState;

	protected static String states[] = {"double", "colored", "fading"};

	private boolean timerStarted;

	private double timeStarted;
	private double timeEnded;
	private double time = 0;

	private String text = "";
	private String originalText = "";

	private static File file;
	private static File file_glass;
	private static File file_text;
	private boolean capsLockBoolean;
	private ActionReciverThread art;
	private boolean startSignalAck;
	private String selectedState;

	private boolean caps_glass;
	public long lastKeyTypedAt;
	public boolean sleep;

	private final static String newline = "\n";

	private static int correct;
	private static long delay;
	private static int totalLogLines;
	private static int totalGlassLogLines;

	private static JavaSoundRecorder recorder = null;

	private boolean done;

	private static String[] mainArgs;
	private String id;
	private String doneState;

	DatagramSocket socket;
	InetAddress hostAddress;
	byte[] buf;
	DatagramPacket dgp;

	public MainWindow() {
		super(new GridBagLayout());
		try {
			socket = new DatagramSocket();
			hostAddress = InetAddress.getByName(ADDRESS);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		capsLockBoolean = Toolkit.getDefaultToolkit().getLockingKeyState(
				KeyEvent.VK_CAPS_LOCK);
		inputTextArea = new JTextArea(20, 40);
		inputTextArea.setLineWrap(true);
		inputTextArea.setWrapStyleWord(true);
		inputTextArea.addKeyListener(new textFieldKeyListener());
		outputTextArea = new JTextArea(20, 20);
		outputTextArea.setLineWrap(true);
		outputTextArea.setWrapStyleWord(true);
		startButton = new JButton();
		startButton.setText("Start Test");
		c = new GridBagConstraints();
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0;
		c.weighty = 1.0;
		scrollPane = new JScrollPane(outputTextArea);
		scrollPaneText = new JScrollPane(inputTextArea);
		outputTextArea.setEditable(true);
		outputTextArea.setText("Enter the text for the test.");
		outputTextArea.selectAll();
		if (mainArgs.length == 0)
			idTextField = new JTextField("Enter user id");
		else {
			idTextField = new JTextField(mainArgs[0]);
			String[] newStates = new String[2];
			int i = 0;
			for (String state : states) {
				if (!state.equals(mainArgs[1]))
					newStates[i++] = state;

			}
			states = newStates;
		}
		idTextField.selectAll();
		glassState = new JComboBox<String>(states);
		add(idTextField, c);
		add(glassState, c);
		add(scrollPane, c);
		add(startButton, c);
		startButton.addActionListener(new startButtonActionListener());
		Runtime.getRuntime().addShutdownHook(new Thread("app-shutdown-hook") {
			@Override
			public void run() {
				sendMessage("bye");
			}
		});

	}
	/**
	 * Create the GUI and show it. For thread safety, this method should be
	 * invoked from the event dispatch thread.
	 */
	private static void createAndShowGUI() {
		// Create and set up the window.
		JFrame frame = new JFrame("Typing Test");

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation(dim.width / 2 - frame.getSize().width / 2, dim.height
				/ 2 - frame.getSize().height / 2);
		// Add contents to the window.

		MainWindow window = new MainWindow();
		frame.add(window);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowActivated(WindowEvent e) {
				Robot robot;
				try {
					robot = new Robot();
					robot.keyPress(KeyEvent.VK_CAPS_LOCK);
					robot.keyRelease(KeyEvent.VK_CAPS_LOCK);
					robot.keyPress(KeyEvent.VK_CAPS_LOCK);
					robot.keyRelease(KeyEvent.VK_CAPS_LOCK);
				} catch (AWTException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

			}
		});
		window.refreshFrame(true);

		// Display the window.
		frame.setVisible(true);
	}

	public void refreshFrame(boolean pack) {
		JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
		if (pack)
			topFrame.pack();
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		topFrame.setLocation(dim.width / 2 - topFrame.getSize().width / 2,
				dim.height / 2 - topFrame.getSize().height / 2);
	}

	public void setStartSignalAck(boolean startSignalAck) {
		this.startSignalAck = startSignalAck;
	}

	public static long getDifference(String first, String second) {
		String firstSplitted = first.split(" ")[2];
		String secondSplitted = second.split(" ")[2];
		Time t1 = new Time(Long.parseLong(firstSplitted));
		Time t2 = new Time(Long.parseLong(secondSplitted));
		return t1.getTime() - t2.getTime();

	}

	public static void compareLogs() {
		try {
			HashMap<Boolean, ArrayList<String>> timings = new HashMap<>();
			if (file_glass.exists()) {
				BufferedReader glassLog = new BufferedReader(new FileReader(
						file_glass));
				timings.put(true, new ArrayList<String>());
				timings.put(false, new ArrayList<String>());
				while (glassLog.ready()) {
					String line = glassLog.readLine();
					timings.get(line.contains("on")).add(line);
					totalGlassLogLines++;
				}
				glassLog.close();
			}
			if (file.exists()) {
				BufferedReader log = new BufferedReader(new FileReader(file));
				while (log.ready()) {
					String line = log.readLine();
					ArrayList<String> timing = timings.get(line.contains("on"));
					int size = timing.size();
					for (int i = 0; i < size; i++) {
						if (getDifference(line, timing.get(i)) >= 0
								&& getDifference(line, timing.get(i)) <= 5499) {
							correct++;
							delay += (getDifference(line, timing.get(i)));
							timings.remove((timing.get(i)));
							break;
						}
					}
					totalLogLines++;
				}
				log.close();
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	class textFieldKeyListener implements KeyListener {
		@Override
		public void keyTyped(KeyEvent e) {
			lastKeyTypedAt = System.nanoTime();
			if (!startSignalAck) {
				sendMessage(selectedState);
			}
			if (e.getKeyChar() != KeyEvent.VK_ENTER) {
				if (!timerStarted) {
					timerStarted = true;
					timeStarted = System.nanoTime();
				}
			} else {
				if (timerStarted) {
					timerStarted = false;
					timeEnded = System.nanoTime();
					time += timeEnded - timeStarted;
					text += inputTextArea.getText();
					// textArea.append(time / 1000000000.0 + newline);
					// text += " ";
					// textArea.append(text);
					outputTextArea.setText(text);
					inputTextArea.selectAll();

					// Make sure the new text is visible, even if there
					// was a selection in the text area.
					outputTextArea.setCaretPosition(outputTextArea
							.getDocument().getLength());
				}
			}
		}

		@Override
		public void keyPressed(KeyEvent e) {
		}

		@Override
		public void keyReleased(KeyEvent e) {
		}

	}

	class capsLockRunnable implements Runnable {
		public void run() {
			FileWriter fw;
			try {
				if (capsLockBoolean != Toolkit.getDefaultToolkit()
						.getLockingKeyState(KeyEvent.VK_CAPS_LOCK)) {
					fw = new FileWriter(file, true);
					fw.write(((Toolkit.getDefaultToolkit()
							.getLockingKeyState(KeyEvent.VK_CAPS_LOCK))
							? "on"
							: "off")
							+ " at " + System.currentTimeMillis() + newline);
					capsLockBoolean = !capsLockBoolean;
					fw.close();
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	class capsLockFeedbackRunnable implements Runnable {

		@Override
		public void run() {
			if (!done) {
				sendMessage(((capsLockBoolean != caps_glass)) ? "bad" : "good");
				double time = (System.nanoTime() - lastKeyTypedAt) / 1000000.0;
				if (time > 2000 && !sleep) {
					sendMessage("sleep");
					sendMessage("sleep");
					System.out.println("sleep");
					sleep = true;
				} else {
					if (time <= 2000 && sleep) {
						sleep = false;
						sendMessage("wakeup");
						sendMessage("wakeup");
						System.out.println("wakeup");
					}

				}
			}
		}
	}

	class startButtonActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {

			// Add Components to this panel.
			String dateTime = LocalDateTime.now().toString();
			dateTime = dateTime.replace('T', ' ')
					.substring(0, dateTime.indexOf('.')).replace(':', '.');
			file = new File("logs/" + idTextField.getText() + "/" + dateTime
					+ "/log.txt");
			file_text = new File("logs/" + idTextField.getText() + "/"
					+ dateTime + "/output.txt");
			id = idTextField.getText();
			if (!(file.getParentFile().exists())) {
				file.getParentFile().mkdirs();
			}
			if (!(file_text.getParentFile().exists())) {
				file_text.getParentFile().mkdirs();
			}
			try {
				art = new ActionReciverThread(MainWindow.this,
						idTextField.getText());
				art.start();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			selectedState = (String) glassState.getSelectedItem();
			originalText = outputTextArea.getText();
			startButton.setVisible(false);
			outputTextArea.setText(null);
			outputTextArea.setEditable(false);
			finish = new JButton("finish");
			finish.addActionListener(new finnishButtonActionListner());
			MainWindow.this.removeAll();
			MainWindow.this.add(scrollPaneText, c);
			MainWindow.this.add(scrollPane, c);
			MainWindow.this.add(finish, c);
			MainWindow.this.refreshFrame(true);
			inputTextArea.requestFocus();
			// creates a new thread that waits for a specified
			// of time before stopping
			recorder = new JavaSoundRecorder(file_text.getParentFile()
					.getPath() + "/sound.wav");
			Thread record = new Thread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					recorder.start();
				}
			});
			// start
			record.start();
			Runtime.getRuntime().addShutdownHook(
					new Thread("app-shutdown-hook") {
						@Override
						public void run() {
							recorder.finish();
						}
					});

		}
	}
	public void startFeedback() {
		ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);
		exec.scheduleAtFixedRate(new capsLockRunnable(), 0, 300,
				TimeUnit.MILLISECONDS);
		ScheduledExecutorService execFeedback = Executors
				.newScheduledThreadPool(1);
		execFeedback.scheduleAtFixedRate(new capsLockFeedbackRunnable(), 0,
				100, TimeUnit.MILLISECONDS);
	}
	class finnishButtonActionListner implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			done = true;
			doneState = selectedState;
			recorder.finish();
			art.closeSocket();
			art.interrupt();
			file_glass = art.getLog();
			if (((JButton) e.getSource()).getText().equals("finish")) {
				inputTextArea.setEditable(false);
				outputTextArea.setFont(outputTextArea.getFont().deriveFont(35));
				time /= 1000000000.0;
				compareLogs();
				String output = "*test results*"
						+ newline
						+ selectedState
						+ newline
						+ ((time == 0 || text.length() == 0) ? "zero" : ((text
								.length() * 1.0) / time) + "")
						+ " characters per second"
						+ newline
						+ "the two strings are "
						+ StringUtils
								.getJaroWinklerDistance(text, originalText)
						+ "% similar" + newline
						+ "the total number of lines in glass log is: "
						+ totalGlassLogLines + newline + "the total delay is: "
						+ delay + newline + "the average delay is: "
						+ delay / ((correct == 0) ? 1 : correct) + newline
						+ "the total number of correct caps lock hits is: "
						+ correct + newline
						+ "the total number of lines in log is: "
						+ totalLogLines;
				outputTextArea.setText(output);
				finish.setText("restart glass");
				try {
					FileWriter fw = new FileWriter(file_text);
					fw.write(text);
					fw.write(output);
					fw.flush();
					fw.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				scrollPane.getVerticalScrollBar().setValue(0);
				if (startSignalAck)
					sendMessage("finish");
				refreshFrame(false);

			} else {
				// String[] args = new String[0];
				// topFrame.dispose();
				// correct = 0;
				// delay = 0;
				// totalLogLines = 0;
				// totalGlassLogLines = 0;
				// file = null;
				// file_glass = null;
				// main(args);
				art.closeSocket();
				sendMessage("restart");
				finish.setText("restart test");
			}

		}
	}

	public void setCaps_glass(boolean caps_glass) {
		this.caps_glass = caps_glass;
	}

	public void restartApplication() {
		sendMessage("ack received");
		try {
			final String javaBin = System.getProperty("java.home")
					+ File.separator + "bin" + File.separator + "java";
			File currentJar = new File("mainWindow.jar");
			/* is it a jar file? */
			if (!currentJar.getName().endsWith(".jar")) {
				return;
			}
			/* Build command: java -jar application.jar */
			final ArrayList<String> command = new ArrayList<String>();
			command.add(javaBin);
			command.add("-jar");
			command.add(currentJar.getPath());
			command.add(id);
			command.add(doneState);

			final ProcessBuilder builder = new ProcessBuilder(command);
			builder.start();
			System.exit(0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void sendMessage(String message) {
		try {
			buf = message.getBytes();
			dgp = new DatagramPacket(buf, buf.length, hostAddress, PORT);
			socket.send(dgp);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		// Schedule a job for the event dispatch thread:
		// creating and showing this application's GUI.
		mainArgs = args;
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
		// HashMap<Boolean, ArrayList<String>> test = new HashMap<>();
		// test.put(true, new ArrayList<>());
		// test.get(true).add("hey");
		// test.get(true).add("hey2");
		// test.get(true).add("hey3");
		// for(String x : test.get(true)){
		// test.get(true).remove(x);
		// }
		// System.out.println(test.get(true).size());
	}
}
