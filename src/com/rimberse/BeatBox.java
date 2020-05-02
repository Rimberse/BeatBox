package com.rimberse;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class BeatBox {
	
	JPanel mainPanel;
	ArrayList<JCheckBox> checkboxList;
	Sequencer sequencer;
	Sequence sequence;
	Track track;
	JFrame theFrame;
	JCheckBox c;
	JTextField textFieldX;
	JTextField textFieldY;
	int x;
	int y;
	boolean[] checkboxState = null;
	
	String[] instrumentNames = {"Bass Drum", "Closed Hi - Hat", "Open Hi - Hat", "Acoustic Snare", "Crash Cymbal", "Hand Clap", 
	"High Tom", "Hi Bongo", "Maracas", "Whistle", "Low Conga", "Cowbell", "Vibraslap", "Low - mid Tom", "High Agogo", "Open Hi Conga"};
	int[] instruments = {35, 42, 46, 38, 49, 39, 50, 60, 70, 72, 64, 56, 58, 47, 67, 63};
	
	int[] coordinatesX = {17, 34, 50, 66, 82, 98, 114, 130, 146, 162, 178, 194, 210, 226, 242, 258, 274};
	int[] coordinatesY = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};
	
	public static void main (String[] args) {
		new BeatBox().buildGUI();
	}
	
	public void buildGUI() {
		
		theFrame = new JFrame("Cyber BeatBox");
		theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		BorderLayout layout = new BorderLayout();
		JPanel background = new JPanel(layout);
		background.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		checkboxList = new ArrayList<>();
		Box buttonBox = new Box(BoxLayout.Y_AXIS);
		
		JButton start = new JButton("Start");
		start.addActionListener(new MyStartListener());
		buttonBox.add(start);
		
		JButton stop = new JButton("Stop");
		stop.addActionListener(new MyStopListener());
		buttonBox.add(stop);
		
		JButton upTempo = new JButton("Tempo Up");
		upTempo.addActionListener(new MyUpTempoListener());
		buttonBox.add(upTempo);
		
		JButton downTempo = new JButton("Tempo Down");
		downTempo.addActionListener(new MyDownTempoListener());
		buttonBox.add(downTempo);
		
		JButton serializeIt = new JButton("Save the pattern");
		serializeIt.addActionListener(new MySendListener());
		buttonBox.add(serializeIt);
		
		JButton restore = new JButton("Restore the pattern");
		restore.addActionListener(new MyReadInListener());
		buttonBox.add(restore);
		
		JButton checkAllBoxes = new JButton("Check all the boxes");
		checkAllBoxes.addActionListener(new MyCheckAllBoxesListener());
		buttonBox.add(checkAllBoxes);
		
		JButton uncheckBoxes = new JButton("Uncheck all the boxes");
		uncheckBoxes.addActionListener(new MyUncheckBoxesListener());
		buttonBox.add(uncheckBoxes);
		
		JButton checkBoxAtXY = new JButton("Check the box at X, Y coordinates");
		checkBoxAtXY.addActionListener(new MyCheckBoxAtXYListener());
		buttonBox.add(checkBoxAtXY);
		
		JLabel labelX = new JLabel("Enter the X coordinate of the box:");
		buttonBox.add(labelX);
		
		textFieldX = new JTextField();
		textFieldX.requestFocus();
		textFieldX.addActionListener(new MyTextFieldXListener());
		buttonBox.add(textFieldX);
		
		JLabel labelY = new JLabel("Enter the Y coordinate of the box:");
		buttonBox.add(labelY);
		
		textFieldY = new JTextField();
		textFieldY.requestFocus();
		textFieldY.addActionListener(new MyTextFieldYListener());
		buttonBox.add(textFieldY);
		
		Box nameBox = new Box(BoxLayout.Y_AXIS);
		for (int i = 0; i < 16; i++) {
			
			nameBox.add(new Label(instrumentNames[i]));
		}
		
		background.add(BorderLayout.EAST, buttonBox);
		background.add(BorderLayout.WEST, nameBox);
		
		theFrame.getContentPane().add(background);
		
		GridLayout grid = new GridLayout(17, 17);
		grid.setVgap(1);
		grid.setHgap(2);
		mainPanel = new JPanel(grid);
		background.add(BorderLayout.CENTER, mainPanel);
		
		for (int i = 0; i < 274; i++) {
			
			if (i == 0) {
				JLabel labelij = new JLabel("X/Y");
				mainPanel.add(labelij);
			}
			
			if(i >= 0 && i < 16) {
				JLabel labelj = new JLabel(" " + coordinatesY[i]);
				mainPanel.add(labelj);
			}
			
			for (int j = 0; j < coordinatesX.length; j++) {
				
				if (i == coordinatesX[j]) {
					JLabel labeli = new JLabel(" " + coordinatesY[j]);
					mainPanel.add(labeli);
				}
			}
			
			if (i > 17) {
				
				c = new JCheckBox();
				c.setSelected(false);
				checkboxList.add(c);
				mainPanel.add(c);
			}
			
		}
		
		setUpMidi();
		
		theFrame.setBounds(50, 50, 300, 300);
		theFrame.pack();
		theFrame.setVisible(true);
		
	}
	
	public void setUpMidi() {
		
		try {
			
			sequencer = MidiSystem.getSequencer();
			sequencer.open();
			sequence = new Sequence(Sequence.PPQ, 4);
			track = sequence.createTrack();
			sequencer.setTempoInBPM(120);
			//sequencer.setTempoFactor(1.0F);
			
		} catch(Exception e) { e.printStackTrace(); }
	}
	
	public void buildTrackAndStart() {
		
		int[] trackList = null;
		
		sequence.deleteTrack(track);
		track = sequence.createTrack();
		
		for (int i = 0; i < 16; i++) {
			
			trackList = new int[16];
			
			int key = instruments[i];	
			
			for (int j = 0; j < 16; j++) {
				
				JCheckBox jc = checkboxList.get(j + 16 * i);
				if (jc.isSelected()) {
					trackList[j] = key;
				} else {
					trackList[j] = 0;
				}
			}
			
			makeTracks(trackList);
			track.add(makeEvent(176, 1, 127, 0, 16));
		}
		
		track.add(makeEvent(192, 9, 1, 0, 15));
		
		try {
			sequencer.setSequence(sequence);
			sequencer.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
			sequencer.start();
			sequencer.setTempoInBPM(120);
		} catch (Exception e) { e.printStackTrace(); }
	}
	
	public class MyStartListener implements ActionListener {
		public void actionPerformed(ActionEvent a) {
			buildTrackAndStart();
		}
	}
	
	public class MyStopListener implements ActionListener {
		public void actionPerformed(ActionEvent a) {
			sequencer.stop();
		}
	}
	
	public class MyUpTempoListener implements ActionListener {
		public void actionPerformed(ActionEvent a) {
			float tempoFactor = sequencer.getTempoFactor();
			sequencer.setTempoFactor((float)(tempoFactor * 1.03));
		}
	}
	
	public class MyDownTempoListener implements ActionListener {
		public void actionPerformed(ActionEvent a) {
			float tempoFactor = sequencer.getTempoFactor();
			sequencer.setTempoFactor((float)(tempoFactor * .97));
		}
	}
	
	public class MySendListener implements ActionListener {
		public void actionPerformed(ActionEvent a) {
			checkboxState = new boolean[256];
			
			for (int i = 0; i < 256; i++) {
				JCheckBox check = (JCheckBox) checkboxList.get(i);
				if (check.isSelected()) {
					checkboxState[i] = true;
				} 
			}
			
			JFileChooser fileSave = new JFileChooser();
			fileSave.showSaveDialog(theFrame);
			saveFile(fileSave.getSelectedFile());
		}
	}
	
	public class MyReadInListener implements ActionListener {
		public void actionPerformed(ActionEvent a) {
			JFileChooser fileOpen = new JFileChooser();
			fileOpen.showOpenDialog(theFrame);
			loadFile(fileOpen.getSelectedFile());
			
		}
	}
	
	public class MyCheckAllBoxesListener implements ActionListener {
		public void actionPerformed(ActionEvent a) {
			for (int i = 0; i < 256; i++) {
				JCheckBox b2 = checkboxList.get(i);
				b2.setSelected(true);
			}
		}
	}
	
	public class MyUncheckBoxesListener implements ActionListener {
		public void actionPerformed(ActionEvent a) {
			for (int i = 0; i < 256; i++) {
				JCheckBox b = checkboxList.get(i);
				b.setSelected(false);
			}
		}
	}
	
	public class MyCheckBoxAtXYListener implements ActionListener {
		public void actionPerformed(ActionEvent a) {
			try {
				x = Integer.parseInt(textFieldX.getText());
				y = Integer.parseInt(textFieldY.getText());
				
			} catch(NumberFormatException e) {
				textFieldX.setText("Please enter the number for X coordinate, that is less than 16");
				textFieldY.setText("Please enter the number for Y coordinate, that is less than 16");
			}
			
			if (x < 16 && y < 16 && x >= 0 && y >= 0) {
				JCheckBox xy = checkboxList.get(y + 16 * x);
				xy.setSelected(true);
			} else {
				textFieldX.setText("X should be less than 16 and equal or greater than 0");
				textFieldY.setText("Y should be less than 16 and equal or greater than 0");
			}
		}
	}
	
	public class MyTextFieldXListener implements ActionListener {
		public void actionPerformed(ActionEvent a) {
			textFieldX.setText(textFieldX.getText());
		}
	}
	
	public class MyTextFieldYListener implements ActionListener {
		public void actionPerformed(ActionEvent a) {
			textFieldY.setText(textFieldY.getText());
		}
	}
	
	public void makeTracks(int[] list) {
		for (int i = 0; i < 16; i++) {
			int key = list[i];
			
			if(key != 0)
			{
				track.add(makeEvent(144, 9, key, 100, i));
				track.add(makeEvent(128, 9, key, 100, i + 1));
			}
		}
	}
	
	public MidiEvent makeEvent (int comd, int chan, int one, int two, int tick) {
		MidiEvent event = null;
		
		try {
			ShortMessage a = new ShortMessage();
			a.setMessage(comd, chan, one, two);
			event = new MidiEvent(a, tick);
			
		} catch(Exception e) { e.printStackTrace(); }
		return event;
	}
	
	private void saveFile(File file) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			
			for(boolean state : checkboxState) {
				writer.write(String.valueOf(state) + "\r\n");
			}
			
			writer.close();
		} catch(IOException ex) {
			System.out.println("couldn't write the pattern");
			ex.printStackTrace();
		}
	}
	
	private void loadFile(File file) {	
		int e = 0;
		boolean CheckBoxState[] = new boolean[256];
				
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String state = null;
			while ((state = reader.readLine()) != null) {
				CheckBoxState[e++] = Boolean.valueOf(state);
			}
			reader.close();
			
		} catch(Exception ex) {
			System.out.println("couldn't read the pattern");
			ex.printStackTrace();
		}
		
		for (int i = 0; i < 256; i++) {
			JCheckBox check = (JCheckBox) checkboxList.get(i);
			
			if(CheckBoxState[i]) {
				check.setSelected(true);
			} else {
				check.setSelected(false);
			}
		}	
		
		sequencer.stop();
		buildTrackAndStart();
	}
}