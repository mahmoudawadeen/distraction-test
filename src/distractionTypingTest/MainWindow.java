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
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
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
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.apache.commons.lang3.StringUtils;

public class MainWindow extends JPanel {

	private static final long serialVersionUID = -4434843170363458281L;
	protected JTextArea textField;
	protected JTextArea textArea;
	protected JButton finish;
	private boolean timerStarted;

	private double timeStarted;
	private double timeEnded;
	private double time = 0;

	private String text = "";
	private String originalText = "";

	private File file;
	private boolean caps;
	private ActionReciverThread art;
	private boolean startSignalAck;

	private final static String newline = "\n";

	private static int correct;
	private static long delay;
	private static int totalLogLines;
	private static int totalGlassLogLines;

	public MainWindow() {
		super(new GridBagLayout());
		file = new File("log.txt");
		caps = Toolkit.getDefaultToolkit().getLockingKeyState(
				KeyEvent.VK_CAPS_LOCK);
		try {
			art = new ActionReciverThread(this);
			Runnable capslock = new Runnable() {
				@Override
				public void run() {
					FileWriter fw;
					try {
						if (caps != Toolkit.getDefaultToolkit()
								.getLockingKeyState(KeyEvent.VK_CAPS_LOCK)) {
							fw = new FileWriter(file, true);
							fw.write(((Toolkit.getDefaultToolkit()
									.getLockingKeyState(KeyEvent.VK_CAPS_LOCK)) ? "on"
									: "off")
									+ " at " + LocalTime.now() + newline);
							caps = !caps;
							fw.close();
						}
					} catch (IOException e) {

						e.printStackTrace();
					}
				}
			};
			ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);
			exec.scheduleAtFixedRate(capslock, 0, 500, TimeUnit.MILLISECONDS);
		} catch (IOException e1) {

			e1.printStackTrace();
		}
		art.start();
		textField = new JTextArea(20, 40);
		textField.setLineWrap(true);
		textField.setWrapStyleWord(true);
		textField.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
				if (!startSignalAck) {
					try {
						byte buf[] = "start".getBytes();
						DatagramSocket startSignalSocket = new DatagramSocket();
						InetAddress hostAddress = InetAddress
								.getByName("137.250.171.64");
						DatagramPacket startSignalPacket = new DatagramPacket(
								buf, buf.length, hostAddress, 34144);
						startSignalSocket.send(startSignalPacket);
						System.out.println("start sent");
						startSignalSocket.close();

					} catch (SocketException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (UnknownHostException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
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
						text += textField.getText();
						// textArea.append(time / 1000000000.0 + newline);
						// text += " ";
						// textArea.append(text);
						textArea.setText(text);
						textField.selectAll();

						// Make sure the new text is visible, even if there
						// was a selection in the text area.
						textArea.setCaretPosition(textArea.getDocument()
								.getLength());
					}
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
			}
		});
		textArea = new JTextArea(20, 20);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		final JButton startButton = new JButton();
		startButton.setText("Start Test");
		final GridBagConstraints c = new GridBagConstraints();
		c.gridwidth = GridBagConstraints.REMAINDER;

		c.fill = GridBagConstraints.HORIZONTAL;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0;
		c.weighty = 1.0;
		JScrollPane scrollPane = new JScrollPane(textArea);
		JScrollPane scrollPaneText = new JScrollPane(textField);
		textArea.setEditable(true);
		textArea.setText("Enter the text for the test.");
		textArea.selectAll();
		add(scrollPane, c);
		add(startButton, c);

		startButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				// Add Components to this panel.
				originalText = textArea.getText();
				startButton.setVisible(false);
				textArea.setText(null);
				textArea.setEditable(false);
				finish = new JButton("finish");
				finish.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						art.closeSocket();
						JFrame topFrame = (JFrame) SwingUtilities
								.getWindowAncestor(MainWindow.this);
						if (((JButton) e.getSource()).getText()
								.equals("finish")) {
							textField.setEditable(false);

							textArea.setFont(textArea.getFont().deriveFont(35));
							time /= 1000000000.0;
							compareLogs();
							textArea.setText(((time == 0 || text.length() == 0) ? "zero"
									: ((text.length() * 1.0) / time) + "")
									+ " characters per second"
									+ newline
									+ "the two strings are "
									+ StringUtils.getJaroWinklerDistance(text,
											originalText)
									+ "% similar"
									+ newline
									+ "the total number of lines in glass log is: "
									+ totalGlassLogLines
									+ newline
									+ "the total delay is: "
									+ delay
									+ newline
									+ "the total number of correct caps lock hits is: "
									+ correct
									+ newline
									+ "the total number of lines in log is: "
									+ totalLogLines);
							finish.setText("restart");
							scrollPane.getVerticalScrollBar().setValue(0);
							refreshFrame(false);

						} else {
							String[] args = new String[0];
							topFrame.dispose();
							main(args);
						}

					}
				});
				MainWindow.this.removeAll();
				MainWindow.this.add(scrollPaneText, c);
				MainWindow.this.add(scrollPane, c);
				MainWindow.this.add(finish, c);
				MainWindow.this.refreshFrame(true);
				textField.requestFocus();
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
		frame.addWindowListener(new WindowListener() {

			@Override
			public void windowOpened(WindowEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowIconified(WindowEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowDeiconified(WindowEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowDeactivated(WindowEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowClosing(WindowEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowClosed(WindowEvent e) {
				// TODO Auto-generated method stub

			}

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

	public static void main(String[] args) {
		// Schedule a job for the event dispatch thread:
		// creating and showing this application's GUI.
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

	public void setStartSignalAck(boolean startSignalAck) {
		this.startSignalAck = startSignalAck;
	}

	public void done() {
		finish.doClick();
	}

	public static void compareLogs() {
		try {

			HashMap<Boolean, ArrayList<String>> timings = new HashMap<>();
			BufferedReader log = new BufferedReader(new FileReader("log.txt"));
			BufferedReader glassLog = new BufferedReader(new FileReader(
					"log_glass.txt"));
			timings.put(true, new ArrayList<String>());
			timings.put(false, new ArrayList<String>());
			while (glassLog.ready()) {
				String line = glassLog.readLine();
				timings.get(line.charAt(0) == 'o').add(line);
				totalGlassLogLines++;
			}
			glassLog.close();
			while (log.ready()) {
				String line = log.readLine();
				ArrayList<String> timing = timings.get(line.charAt(0) == 'o');
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
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@SuppressWarnings("deprecation")
	public static long getDifference(String first, String second) {
		String[] firstSplitted = first.split(" ")[2].split(":");
		String[] secondSplitted = second.split(" ")[2].split(":");
		Time t1 = new Time(Integer.parseInt(firstSplitted[0]),
				Integer.parseInt(firstSplitted[1]),
				(int) Double.parseDouble(firstSplitted[2]));
		Time t2 = new Time(Integer.parseInt(secondSplitted[0]),
				Integer.parseInt(secondSplitted[1]),
				(int) Double.parseDouble(secondSplitted[2]));
		return t1.getTime() - t2.getTime();

	}
}
