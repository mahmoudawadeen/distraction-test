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
import java.nio.file.Files;
import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
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
import javax.swing.text.AbstractDocument;
import javax.swing.text.DocumentFilter;

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
	protected JButton restart;

	protected static String states[];
	protected static String ArrayOfStates[][] = {
			{"double", "colored", "fading", "controlled"},
			{"double", "colored", "controlled", "fading"},
			{"double", "fading", "colored", "controlled"},
			{"double", "fading", "controlled", "colored"},
			{"double", "controlled", "colored", "fading"},
			{"double", "controlled", "fading", "colored"},
			{"colored", "double", "controlled", "fading"},
			{"colored", "double", "fading", "controlled"},
			{"colored", "fading", "controlled", "double"},
			{"colored", "fading", "double", "controlled"},
			{"colored", "controlled", "fading", "double"},
			{"colored", "controlled", "double", "fading"},
			{"fading", "double", "colored", "controlled"},
			{"fading", "double", "controlled", "colored"},
			{"fading", "colored", "double", "controlled"},
			{"fading", "colored", "controlled", "double"},
			{"fading", "controlled", "double", "colored"},
			{"fading", "controlled", "colored", "double"},
			{"controlled", "double", "fading", "colored"},
			{"controlled", "double", "colored", "fading"},
			{"controlled", "colored", "fading", "double"},
			{"controlled", "colored", "double", "fading"},
			{"controlled", "fading", "colored", "double"},
			{"controlled", "fading", "double", "colored"}};

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

	private boolean caps_glass = true;
	public long lastKeyTypedAt;
	public boolean sleep;

	private final static String newline = "\n";

	private static int correct;
	private static long delay;
	private static int totalLogLines;
	private static int totalGlassLogLines;
	private static int droppedEvents;
	private static int timedOutEvents;

	private static JavaSoundRecorder recorder = null;

	private boolean done;
	private boolean restartBoolean;

	private static String[] mainArgs;
	private String id;

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
		final AbstractDocument doc = (AbstractDocument) inputTextArea
				.getDocument();
		doc.setDocumentFilter(new DocumentFilter() {
			@Override
			public void remove(final FilterBypass fb, final int offset,
					final int length) {

			}
		});
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
		if (mainArgs.length == 0) {
			idTextField = new JTextField("Enter user id");
			Random random = new Random();
			int low = 0;
			int high = 23;
			int r = random.nextInt(high - low) + low;
			states = ArrayOfStates[r];
		} else {
			idTextField = new JTextField(mainArgs[0]);
			if (mainArgs.length > 1)
				states = mainArgs[1].split(" ");
			else {
				Random random = new Random();
				int low = 0;
				int high = 23;
				int r = random.nextInt(high - low) + low;
				states = ArrayOfStates[r];
			}
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
			HashMap<Boolean, ArrayList<String>> timingsGlass = new HashMap<>();
			HashMap<Boolean, ArrayList<String>> timingsLog = new HashMap<>();
			timingsGlass.put(true, new ArrayList<String>());
			timingsGlass.put(false, new ArrayList<String>());
			if (file_glass.exists()) {
				BufferedReader glassLog = new BufferedReader(new FileReader(
						file_glass));
				while (glassLog.ready()) {
					String line = glassLog.readLine();
					timingsGlass.get(line.contains("on")).add(line);
					totalGlassLogLines++;
				}
				glassLog.close();
			}
			timingsLog.put(true, new ArrayList<String>());
			timingsLog.put(false, new ArrayList<String>());
			if (file.exists()) {
				BufferedReader log = new BufferedReader(new FileReader(file));
				while (log.ready()) {
					String line = log.readLine();
					timingsLog.get(line.contains("on")).add(line);
					totalLogLines++;
				}
				log.close();
			}

			ArrayList<String> logTemp = timingsLog.get(true);
			ArrayList<String> glassTemp = timingsGlass.get(true);
			int k = 0;
			while (k < 2) {
				int sizeGlass = glassTemp.size();
				int sizeLog = logTemp.size();
				for (int i = 0; i < sizeGlass; i++) {
					for (int j = 0; j < sizeLog; j++) {
						long diff = getDifference(logTemp.get(j),
								glassTemp.get(i));
						if (diff > 0 && diff < 5499) {
							correct++;
							delay += diff;
							glassTemp.remove(i);
							logTemp.remove(j);
							sizeGlass--;
							sizeLog--;
							i--;
							j--;
							break;
						} else if (diff > 5499 && diff < 15000) {
							timedOutEvents++;
							glassTemp.remove(i);
							logTemp.remove(j);
							sizeGlass--;
							sizeLog--;
							i--;
							j--;
							break;
						} else if (diff > 15000) {
							droppedEvents++;
							glassTemp.remove(i);
							sizeGlass--;
							i--;
							break;
						}

					}
				}
				droppedEvents += glassTemp.size();
				logTemp = timingsLog.get(false);
				glassTemp = timingsGlass.get(false);
				k++;
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
			if (e.getKeyCode() == KeyEvent.VK_CAPS_LOCK)
				lastKeyTypedAt = System.nanoTime();

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
			if (!done && !restartBoolean) {
				String mesg = ((Toolkit.getDefaultToolkit().getLockingKeyState(
						KeyEvent.VK_CAPS_LOCK) != caps_glass)) ? "bad" : "good";
				sendMessage(mesg);
				double time = (System.nanoTime() - lastKeyTypedAt) / 1000000.0;
				if (time > 2000 && !sleep) {
					sendMessage("sleep");
					sendMessage("sleep");
					sleep = true;
				} else {
					if (time <= 2000 && sleep) {
						sleep = false;
						sendMessage("wakeup");
						sendMessage("wakeup");
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
						idTextField.getText(), dateTime);
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
			restart = new JButton("restart");
			restart.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					art.closeSocket();
					sendMessage("restart");
				}
			});
			finish = new JButton("finish");
			finish.addActionListener(new finnishButtonActionListner());
			MainWindow.this.removeAll();
			MainWindow.this.add(scrollPaneText, c);
			MainWindow.this.add(scrollPane, c);
			GridBagConstraints c2 = new GridBagConstraints();
			c2.gridwidth = 2;
			c2.fill = GridBagConstraints.BOTH;
			c2.weightx = 0.5;
			c2.weighty = 0.5;
			MainWindow.this.add(finish, c2);
			MainWindow.this.add(restart, c2);
			JFrame temp = (JFrame) SwingUtilities
					.getWindowAncestor(MainWindow.this);
			temp.setTitle(temp.getTitle() + " -  " + selectedState);
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
						+ recorder.recordingTime.getTime()
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
						+ totalLogLines + newline
						+ "the total number of dropped events is: "
						+ droppedEvents + newline
						+ "the total number of timed out events is: "
						+ timedOutEvents;
				outputTextArea.setText(output);
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
				MainWindow.this.remove(finish);
				MainWindow.this.refreshFrame(true);
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
			if (!done) {
				if (mainArgs.length > 1)
					command.add(mainArgs[1]);
				else {
					String oldStates = "";
					for (String state : states) {
						oldStates += state + " ";
					}
					command.add(oldStates);
				}
				Files.deleteIfExists(file.toPath());
				Files.deleteIfExists(file_text.toPath());
				art.deleteLog();
				recorder.finish();
				Files.deleteIfExists(recorder.wavFile.toPath());
				Files.deleteIfExists(file.getParentFile().toPath());
			} else {
				String newStates = "";
				for (String state : states) {
					if (!state.equals(selectedState))
						newStates += state + " ";
				}
				command.add(newStates);
			}
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

	}
}
