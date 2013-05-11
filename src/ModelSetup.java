import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * @author Will Richard and Andrew Calkins
 * This is just a little window that pops up and allows users to set the model's variables in a nice GUI
 *
 */
public class ModelSetup extends JFrame implements PropertyChangeListener, ItemListener, ActionListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	//store one DiseaseModel, so hopefully it will close the old one and start a new one each time we want it
	private static DiseaseModel model;
	
	//initial values for the fields
	private double alpha = .0002;
	private int diseaseLength = 0;
	private int population = 1000;
	private double startInfectivesPercent = .05;
	private int timesteps = 300;
	private int recoveryTime = 0;
	private boolean useSIR = false;
	private boolean avoidInfectives = false;
	private boolean useLocations = false;
	private boolean allInfectivesInSameLoc = true;
	private int numLocCols = 2;
	private int numLocRows = 2;
	private double changeLocProb = .0002;
	
	//Labels for the fields and components
	private JLabel alphaLabel;
	private JLabel diseaseLengthLabel;
	private JLabel populationLabel;
	private JLabel startInfectivesPercentLabel;
	private JLabel timestepsLabel;
	private JLabel recoveryTimeLabel;
	private JLabel useSirLabel;
	private JLabel avoidInfectivesLabel;
	private JLabel useLocationsLabel;
	private JLabel allInfectivesInSameLocLabel;
	private JLabel numLocColsLabel;
	private JLabel numLocRowsLabel;
	private JLabel changeLocProbLabel;
	
	//Strings for the labels
	private static final String alphaString = "Alpha: ";
	private static final String diseaseLengthString = "Disease Duration: ";
	private static final String populationString = "Population: ";
	private static final String startInfectivesPercentString = "Starting Percent of Infectives: ";
	private static final String timestepsString = "Timesteps: ";
	private static final String recoveryTimeString = "Recovery Time: ";
	private static final String useSirString = "Use SIR Model? ";
	private static final String avoidInfectivesString = "Avoid Infectives? ";
	private static final String startModelString = "Start Model";
	private static final String useLocationsString = "Use Locations";
	private static final String allInfectivesInSameLocString = "Start all Infectives in Same Location";
	private static final String numLocColsString = "Number of Columns";
	private static final String numLocRowsString = "Number of Rows";
	private static final String changeLocProbString = "Probability to Change Location";
	
	private static String stopModelString = "Stop Model";
	private static String pauseModelString = "Pause Model   ";
	private static String unPauseModelString = "Un-Pause Model";
	
	//Fields for variable entry
	private JFormattedTextField alphaField;
	private JFormattedTextField diseaseLengthField;
	private JFormattedTextField populationField;
	private JFormattedTextField startInfectivesPercentField;
	private JFormattedTextField timestepsField;
	private JFormattedTextField recoveryTimeField;
	private JFormattedTextField numLocColsField;
	private JFormattedTextField numLocRowsField;
	private JFormattedTextField changeLocProbField;
	
	//Formats to parse numbers in fields
	private NumberFormat alphaFormat;
	private NumberFormat diseaseLengthFormat;
	private NumberFormat populationFormat;
	private NumberFormat startInfectivesPercentFormat;
	private NumberFormat timestepsFormat;
	private NumberFormat recoveryTimeFormat;
	private NumberFormat numLocColsFormat;
	private NumberFormat numLocRowsFormat;
	private NumberFormat changeLocProbFormat;
	
	//check boxes for true/false options
	private JCheckBox useSirBox;
	private JCheckBox avoidInfectivesBox;
	private JCheckBox useLocationsBox;
	private JCheckBox allInfectivesInSameLocBox;
	
	//button to start the model
	private JButton startModelButton;
	private JButton stopModelButton;
	private JButton pauseModelButton;
	
	public ModelSetup() {
		super("Model Setup");
	
		//set up the window
		setResizable(false);
		
		//create the labels
		alphaLabel = new JLabel(alphaString);
		diseaseLengthLabel = new JLabel(diseaseLengthString);
		populationLabel = new JLabel(populationString);
		startInfectivesPercentLabel = new JLabel(startInfectivesPercentString);
		timestepsLabel = new JLabel(timestepsString);
		recoveryTimeLabel = new JLabel(recoveryTimeString);
		useSirLabel = new JLabel(useSirString);
		avoidInfectivesLabel = new JLabel(avoidInfectivesString);
		useLocationsLabel = new JLabel(useLocationsString);
		allInfectivesInSameLocLabel = new JLabel(allInfectivesInSameLocString);
		numLocColsLabel = new JLabel(numLocColsString);
		numLocRowsLabel = new JLabel(numLocRowsString);
		changeLocProbLabel = new JLabel(changeLocProbString);
		
		//Create the components
		//start with the fields
		//set up the NumberFormats
		setUpFormats();
		//now, to the fields themselves
		int numFieldColumns = 5;
		
		alphaField = new JFormattedTextField(alphaFormat);
		alphaField.setValue(new Double(alpha));
		alphaField.setColumns(numFieldColumns);
		alphaField.addPropertyChangeListener("value", this);
		
		diseaseLengthField = new JFormattedTextField(diseaseLengthFormat);
		diseaseLengthField.setValue(new Integer(diseaseLength));
		diseaseLengthField.setColumns(numFieldColumns);
		diseaseLengthField.addPropertyChangeListener("value", this);
		
		populationField = new JFormattedTextField(populationFormat);
		populationField.setValue(new Integer(population));
		populationField.setColumns(numFieldColumns);
		populationField.addPropertyChangeListener("value", this);
		
		startInfectivesPercentField = new JFormattedTextField(startInfectivesPercentFormat);
		startInfectivesPercentField.setValue(new Double(startInfectivesPercent));
		startInfectivesPercentField.setColumns(numFieldColumns);
		startInfectivesPercentField.addPropertyChangeListener("value", this);
		
		timestepsField = new JFormattedTextField(timestepsFormat);
		timestepsField.setValue(new Integer(timesteps));
		timestepsField.setColumns(numFieldColumns);
		timestepsField.addPropertyChangeListener("value", this);
		
		recoveryTimeField = new JFormattedTextField(recoveryTimeFormat);
		recoveryTimeField.setValue(new Integer(recoveryTime));
		recoveryTimeField.setColumns(numFieldColumns);
		recoveryTimeField.addPropertyChangeListener("value", this);
		
		numLocColsField = new JFormattedTextField(numLocColsFormat);
		numLocColsField.setValue(new Integer(numLocCols));
		numLocColsField.setColumns(numFieldColumns);
		numLocColsField.addPropertyChangeListener("value", this);
		
		numLocRowsField = new JFormattedTextField(numLocRowsFormat);
		numLocRowsField.setValue(new Integer(numLocRows));
		numLocRowsField.setColumns(numFieldColumns);
		numLocRowsField.addPropertyChangeListener("value", this);
		
		changeLocProbField = new JFormattedTextField(changeLocProbFormat);
		changeLocProbField.setValue(new Double(changeLocProb));
		changeLocProbField.setColumns(numFieldColumns);
		changeLocProbField.addPropertyChangeListener("value", this);
		
		//set up check boxes
		useSirBox = new JCheckBox();
		useSirBox.setSelected(useSIR);
		useSirBox.addItemListener(this);
		recoveryTimeField.setEnabled(useSIR);

		avoidInfectivesBox = new JCheckBox();
		avoidInfectivesBox.setSelected(avoidInfectives);
		avoidInfectivesBox.addItemListener(this);
		
		allInfectivesInSameLocBox = new JCheckBox();
		allInfectivesInSameLocBox.setSelected(allInfectivesInSameLoc);
		allInfectivesInSameLocBox.addItemListener(this);
		
		useLocationsBox = new JCheckBox();
		useLocationsBox.setSelected(useLocations);
		useLocationsBox.addItemListener(this);
		allInfectivesInSameLocBox.setEnabled(useLocations);
		numLocColsField.setEnabled(useLocations);
		numLocRowsField.setEnabled(useLocations);
		changeLocProbField.setEnabled(useLocations);
		
		//tell accessability about label-component pairs
		alphaLabel.setLabelFor(alphaField);
		diseaseLengthLabel.setLabelFor(diseaseLengthField);
		populationLabel.setLabelFor(populationField);
		startInfectivesPercentLabel.setLabelFor(startInfectivesPercentField);
		timestepsLabel.setLabelFor(timestepsField);
		recoveryTimeLabel.setLabelFor(recoveryTimeField);
		useSirLabel.setLabelFor(useSirBox);
		avoidInfectivesLabel.setLabelFor(avoidInfectivesBox);
		useLocationsLabel.setLabelFor(useLocationsBox);
		allInfectivesInSameLocLabel.setLabelFor(allInfectivesInSameLocBox);
		numLocColsLabel.setLabelFor(numLocColsField);
		numLocRowsLabel.setLabelFor(numLocRowsField);
		changeLocProbLabel.setLabelFor(changeLocProbField);
		
		//Lay out the labels in a panel
		JPanel labelPane = new JPanel(new GridLayout(0,1));
		labelPane.add(alphaLabel);
		labelPane.add(diseaseLengthLabel);
		labelPane.add(populationLabel);
		labelPane.add(startInfectivesPercentLabel);
		labelPane.add(timestepsLabel);
		labelPane.add(useSirLabel);
		labelPane.add(recoveryTimeLabel);
		labelPane.add(avoidInfectivesLabel);
		labelPane.add(useLocationsLabel);
		labelPane.add(allInfectivesInSameLocLabel);
		labelPane.add(numLocColsLabel);
		labelPane.add(numLocRowsLabel);
		labelPane.add(changeLocProbLabel);
		
		//Lay out the components in a panel
		JPanel componentsPane = new JPanel(new GridLayout(0,1));
		componentsPane.add(alphaField);
		componentsPane.add(diseaseLengthField);
		componentsPane.add(populationField);
		componentsPane.add(startInfectivesPercentField);
		componentsPane.add(timestepsField);
		componentsPane.add(useSirBox);
		componentsPane.add(recoveryTimeField);
		componentsPane.add(avoidInfectivesBox);
		componentsPane.add(useLocationsBox);
		componentsPane.add(allInfectivesInSameLocBox);
		componentsPane.add(numLocColsField);
		componentsPane.add(numLocRowsField);
		componentsPane.add(changeLocProbField);
		
		//put labels on left, components on right
		JPanel variablesPane = new JPanel(new BorderLayout());
		variablesPane.add(labelPane, BorderLayout.CENTER);
		variablesPane.add(componentsPane, BorderLayout.LINE_END);
		
		//make the buttons
		startModelButton = new JButton(startModelString);
		startModelButton.addActionListener(this);
		
		stopModelButton = new JButton(stopModelString);
		stopModelButton.addActionListener(this);
		
		pauseModelButton = new JButton(pauseModelString);
		pauseModelButton.addActionListener(this);
		
		//make a pannel for the buttons
		JPanel buttonsPane = new JPanel(new GridLayout(1,0));
		buttonsPane.add(startModelButton);
//		buttonsPane.add(stopModelButton);
		buttonsPane.add(pauseModelButton);
		
		//add the variables pane on top, the button below a divider
		add(variablesPane, BorderLayout.NORTH);
		add(buttonsPane, BorderLayout.SOUTH);
	}
	
	//handle property changes
	public void propertyChange(PropertyChangeEvent e) {
		Object source = e.getSource();
		//make sure numbers are all positive - if one is not, change the value and the field
		if(source == alphaField) {
			alpha = ((Number)alphaField.getValue()).doubleValue();
			if(alpha < 0) {
				alpha = -1 * alpha;
				alphaField.setValue(new Double(alpha));
			}
		}
		
		if(source == diseaseLengthField) {
			diseaseLength = ((Number)diseaseLengthField.getValue()).intValue();
			if(diseaseLength < 0) {
				diseaseLength = -1*diseaseLength;
				diseaseLengthField.setValue(new Integer(diseaseLength));
			}
		}
		
		if(source == populationField) {
			population = ((Number)populationField.getValue()).intValue();
			if(population < 0) {
				population = -1*population;
				populationField.setValue(new Integer(population));
			}
		}
		
		if(source == startInfectivesPercentField) {
			startInfectivesPercent = ((Number)startInfectivesPercentField.getValue()).doubleValue();
			if(startInfectivesPercent < 0) {
				startInfectivesPercent = -1*startInfectivesPercent;
				startInfectivesPercentField.setValue(new Double(startInfectivesPercent));
			}
		}
		
		if(source == timestepsField) {
			timesteps = ((Number)timestepsField.getValue()).intValue();
			if(timesteps < 0) {
				timesteps = -1*timesteps;
				timestepsField.setValue(new Integer(timesteps));
			}
		}
		
		if(source == recoveryTimeField) {
			recoveryTime = ((Number)recoveryTimeField.getValue()).intValue();
			if(recoveryTime < 0) {
				recoveryTime = -1*recoveryTime;
				recoveryTimeField.setValue(new Integer(recoveryTime));
				
			}
		}
		
		if(source == numLocColsField) {
			numLocCols = ((Number)numLocColsField.getValue()).intValue();
			if(numLocCols < 0) {
				numLocCols = -1*numLocCols;
				numLocColsField.setValue(new Integer(numLocCols));
			}
		}
		
		if(source == numLocRowsField) {
			numLocRows = ((Number)numLocRowsField.getValue()).intValue();
			if(numLocRows < 0) {
				numLocRows = -1 * numLocRows;
				numLocRowsField.setValue(new Integer(numLocRows));
			}
		}
		
		if(source == changeLocProbField) {
			changeLocProb = ((Number)changeLocProbField.getValue()).doubleValue();
			if(changeLocProb < 0) {
				changeLocProb = -1 * changeLocProb;
				changeLocProbField.setValue(new Double(changeLocProb));
			}
		}
	}
	
	//handle item state changes
	public void itemStateChanged(ItemEvent e) {
		Object source = e.getSource();
		
		if(source == useSirBox) {
			useSIR = (e.getStateChange() == ItemEvent.SELECTED);
			recoveryTimeField.setEnabled(useSIR);
		}
		
		if(source == avoidInfectivesBox) {
			avoidInfectives = (e.getStateChange() == ItemEvent.SELECTED);
		}
		
		if(source == useLocationsBox) {
			useLocations = (e.getStateChange() == ItemEvent.SELECTED);
			allInfectivesInSameLocBox.setEnabled(useLocations);
			numLocColsField.setEnabled(useLocations);
			numLocRowsField.setEnabled(useLocations);
			changeLocProbField.setEnabled(useLocations);
		}
		
		if(source == allInfectivesInSameLocBox) {
			allInfectivesInSameLoc = (e.getStateChange() == ItemEvent.SELECTED);
		}
	}
	
	//listen for actions
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == startModelButton) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					//stop and dispose of old model
					if(model != null) {
						model.stopModel();
						model.dispose();
					}
					
					//make a new one
					//handle diseaseLengths of 0 aka no beta
					double beta;
					if(diseaseLength <= 0) 	beta = 0;
					else beta = 1.0/diseaseLength;
					
					model = new DiseaseModel(alpha, beta, useSIR, recoveryTime, population, (int)(population*startInfectivesPercent), timesteps, avoidInfectives, useLocations, allInfectivesInSameLoc, numLocCols, numLocRows, changeLocProb);
					model.setLocation((int)getLocation().getX()+getWidth(), (int)getLocation().getY());
					model.setVisible(true);
					new Thread(new Runnable() {
						public void run() {
							model.runModel();
						}
					}).start();
					if(model.isPaused()) {
						pauseModelButton.setText(unPauseModelString);
					} else {
						pauseModelButton.setText(pauseModelString);
					}

				}
			});
		}
		if(e.getSource() == stopModelButton) {
			model.stopModel();
		}
		if(e.getSource() == pauseModelButton) {
			model.cyclePauseModel();
			if(model.isPaused()) {
				pauseModelButton.setText(unPauseModelString);
			} else {
				pauseModelButton.setText(pauseModelString);
			}
		}
	}
	
	
	//sets up the NumberFormats
	private void setUpFormats(){
//		alphaFormat = NumberFormat.getNumberInstance();
//		alphaFormat.setMaximumIntegerDigits(0);
		alphaFormat = new DecimalFormat(".#######");
		alphaFormat.setMaximumIntegerDigits(0);
		
		diseaseLengthFormat = NumberFormat.getIntegerInstance();
		
		populationFormat = NumberFormat.getIntegerInstance();
		
		startInfectivesPercentFormat = NumberFormat.getPercentInstance();
		startInfectivesPercentFormat.setMaximumIntegerDigits(2);
		
		timestepsFormat = NumberFormat.getIntegerInstance();
		
		recoveryTimeFormat = NumberFormat.getIntegerInstance();
		
		numLocColsFormat = NumberFormat.getIntegerInstance();
		
		numLocRowsFormat = NumberFormat.getIntegerInstance();
		
		changeLocProbFormat = new DecimalFormat("##.#####%");
	}
	
	public static void createAndShowGUI() {
		//create a new ModelSetup
		ModelSetup modelSetup = new ModelSetup();
		modelSetup.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		modelSetup.pack();
		modelSetup.setVisible(true);
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//schedule job for later, so we're doing things "right"
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}

}
