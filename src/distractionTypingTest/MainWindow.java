package distractionTypingTest;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

public class MainWindow extends JPanel {

	protected JTextField textField;
	protected JTextArea textArea;
	private boolean timerStarted;

	private long timeStarted;
	private long timeEnded;
	private long time;

	private String text="";

	private final static String newline = "\n";

	public MainWindow() {
		super(new GridBagLayout());

		textField = new JTextField(0);
		textField.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
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
						textArea.append(time / 1000000.0 + newline);
						textArea.append(text + newline);
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
				// TODO Auto-generated method stub

			}

			@Override
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub

			}
		});
		textArea = new JTextArea(20, 20);
		textArea.setEditable(false);
		final JButton startButton = new JButton();
		startButton.setText("Start Test");
		final GridBagConstraints c = new GridBagConstraints();
		c.gridwidth = GridBagConstraints.REMAINDER;

		c.fill = GridBagConstraints.HORIZONTAL;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0;
		c.weighty = 1.0;
		add(startButton, c);
		startButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JScrollPane scrollPane = new JScrollPane(textArea);

				// Add Components to this panel.
				startButton.setVisible(false);
				MainWindow.this.add(textField, c);
				MainWindow.this.add(scrollPane, c);
				JFrame topFrame = (JFrame) SwingUtilities
						.getWindowAncestor(MainWindow.this);
				topFrame.pack();
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

		// Add contents to the window.
		frame.add(new MainWindow());

		// Display the window.
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		// Schedule a job for the event dispatch thread:
		// creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}
}
