package distractionTypingTest;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

public class MainWindow extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4434843170363458281L;
	protected JTextField textField;
	protected JTextArea textArea;
	private boolean timerStarted;

	private double timeStarted;
	private double timeEnded;
	private double time = 0;

	private String text = "";

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
						textArea.append(time / 1000000000.0 + newline);
						text+=" ";
						textArea.append(text+ newline);
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
		JScrollPane scrollPane = new JScrollPane(textArea);
		textArea.setEditable(true);
		textArea.setText("Enter the text for the test.");
		textArea.selectAll();
		add(scrollPane,c);
		add(startButton, c);	
		
		startButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				

				// Add Components to this panel.
				startButton.setVisible(false);
				textArea.setText(null);
				textArea.setEditable(false);
				JButton finnish = new JButton("finish");
				finnish.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						JFrame topFrame = (JFrame) SwingUtilities
								.getWindowAncestor(MainWindow.this);
						if (((JButton) e.getSource()).getText()
								.equals("finish")) {
							textField.setEditable(false);

							textArea.setFont(textArea.getFont().deriveFont(35));
							time /= 1000000000.0;
							textArea.setText(((time == 0 || text.length() == 0) ? "zero"
									: ((text.length() * 1.0) / time) + "")
									+ " characters per second" + newline);
							finnish.setText("restart");

							topFrame.setSize(scrollPane
									.getHorizontalScrollBar().getMaximum(),
									topFrame.getHeight());
							scrollPane.getVerticalScrollBar().setValue(0);
							refreshFrame(false);

						} else {
							textField.setEditable(true);
							time = 0;
							text = "";
							textArea.setText(null);
							textField.setText(null);
							textField.requestFocus();
							finnish.setText("finish");
							MainWindow.this.refreshFrame(true);
						}

					}
				});

				
				MainWindow.this.add(textField, c);
				MainWindow.this.add(scrollPane, c);
				MainWindow.this.add(finnish, c);
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
	}
}
