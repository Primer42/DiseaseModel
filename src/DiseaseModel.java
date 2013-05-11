import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * @author Will Richard and Andrew Calkins
 * The actual model.  Opens up a new window, sets up all the things that need to be displayed, 
 * and runs the simulation for the requested number of steps.
 *
 */
public class DiseaseModel extends JFrame implements ChangeListener, WindowListener {
	private static final long serialVersionUID = 1L;

	//keep track of the Graphics object
	Graphics g;

	//the variables and constants to set up the timestep delay slider
	private int timestepDelay;
	private static final int INIT_DELAY = 500;
	private static final int MIN_DELAY = 0;
	private static final int MAX_DELAY = 2000;
	private static JSlider delaySlider;

	//store all the people
	private Vector<Person> people;

	//store all the totals of different types of people, for each timestep by type
	private int[][] totals;

	//the total size of the window
	private static final int FRAME_HEIGHT = 550;
	private static final int FRAME_WIDTH = 1100;

	//the screen shows all of the People moving
	private static final Rectangle SCREEN = new Rectangle(5, 25, FRAME_WIDTH/2 -50, FRAME_HEIGHT - 35);

	//When you have several locations, store the bounding Rectangles for those locations
	private Rectangle[][] locations;
	private static final int LOCATION_BUFFER_WIDTH = 5*Person.WIDTH;

	//the graph display area
	private static final Rectangle GRAPH_BOUNDING_RECT = new Rectangle(FRAME_WIDTH - (int)SCREEN.getWidth(), 25, FRAME_WIDTH/2-60, FRAME_HEIGHT - 35);

	//Disease variables
	public static double alpha;
	public static double beta;
	public static boolean useSIR;
	public static int recoveryDelay;
	private boolean moveAwayFromInfectives;
	private boolean useLocations;
	private double changeLocationProb;
	private Random numGen;

	private int totalNumTimesteps;
	private int curTimestep = 0;

	//control model activity methods
	private boolean pauseModel;
	private boolean stopModel;

	//set the background color for the frame
	public static final Color backgroundColor = Color.WHITE;

	/**
	 * The constructor
	 * Does a lot - see in line comments
	 */
	public DiseaseModel(double alpha, double beta, boolean useSIR, int recoveryDelay, int initTotalPeople, int initNumInfectives, int numTimesteps, boolean moveAwayFromInfectives, boolean _useLocations, boolean allInfectivesInSameLoc, int numLocationCols, int numLocationRows, double _changeLocationProb) {
		super("Disease Model");

		//check passed values - if not OK FREAK OUT!!!
		if(alpha > 1.0 || alpha < 0.0
				|| beta > 1.0 || beta < 0.0 
				|| recoveryDelay < 0
				|| initTotalPeople < 0
				|| initNumInfectives < 0 || initNumInfectives > initTotalPeople) {
			System.out.println("INVALID ARGUMENTS");
			System.exit(0);
		}

		//set up the of the frame
		setSize(FRAME_WIDTH, FRAME_HEIGHT);
		setResizable(false);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(this);
		setBackground(Color.WHITE);

		//setting up the delay and slider to control it, and put it into the Frame
		timestepDelay = INIT_DELAY;
		delaySlider = new JSlider(JSlider.VERTICAL, MIN_DELAY, MAX_DELAY, INIT_DELAY);
		Font sliderFont = new Font("Serif", Font.ITALIC, 15);
		delaySlider.setFont(sliderFont);
		delaySlider.addChangeListener(this);

		delaySlider.setMajorTickSpacing(MAX_DELAY/10);
		delaySlider.setPaintTicks(true);
		delaySlider.setPaintLabels(true);

		JPanel sliderPannel = new JPanel();
		sliderPannel.add(delaySlider);

		getContentPane().add(delaySlider, BorderLayout.CENTER);

		//put the screen (with the people in it) and the graph (with the graph in it) in panels
		JPanel leftPane = new JPanel();
		leftPane.setSize(SCREEN.getSize());

		JPanel rightPanel = new JPanel();
		rightPanel.setSize(GRAPH_BOUNDING_RECT.getSize());

		//add them to the frame
		add(leftPane, BorderLayout.WEST);
		add(rightPanel, BorderLayout.EAST);

		//if we're using locations, set them up
		if(_useLocations) {
			locations = new Rectangle[numLocationCols][numLocationRows];
			//calculate the size of each rectangle
			int locationWidth = (int)(SCREEN.getWidth() - LOCATION_BUFFER_WIDTH) / numLocationCols;
			int locationHeight = (int)(SCREEN.getHeight() - LOCATION_BUFFER_WIDTH) / numLocationRows;

			for(int c = 0; c < numLocationCols; c++) {
				for(int r = 0; r < numLocationRows; r++) {
					int x = (int)(c * (locationWidth + LOCATION_BUFFER_WIDTH) + SCREEN.getX());
					int y = (int)(r * (locationHeight + LOCATION_BUFFER_WIDTH) + SCREEN.getY());
					locations[c][r] = new Rectangle(x, y, locationWidth, locationHeight);
				}
			}
		}

		//make all the people and place them
		people = new Vector<Person>(initTotalPeople);
		numGen = new Random();
		int i = 0;
		//add the specified number of infectives
		for(; i < initNumInfectives; i++) {
			int newPersonX;
			int newPersonY;
			Rectangle newPersonLoc;
			if(_useLocations) {
				//if infectives start in the same location, put them all in 0,0
				if(allInfectivesInSameLoc) {
					newPersonLoc = locations[0][0];
				} else {
					//randomly determine which row and col they are in
					int newPersonCol = numGen.nextInt(numLocationCols);
					int newPersonRow = numGen.nextInt(numLocationRows);
					newPersonLoc = locations[newPersonCol][newPersonRow];
				}
				//and place them into that location, again randomly
				newPersonX = numGen.nextInt((int)newPersonLoc.getWidth() - Person.WIDTH) + (int) newPersonLoc.getX();
				newPersonY = numGen.nextInt((int)newPersonLoc.getHeight() - Person.HEIGHT) + (int) newPersonLoc.getY();
			} else {
				//randomly place them in the SCREEN
				newPersonX = numGen.nextInt((int)SCREEN.getWidth() - Person.WIDTH) + (int)SCREEN.getX();
				newPersonY = numGen.nextInt((int)SCREEN.getHeight() - Person.HEIGHT) + (int)SCREEN.getY();
				newPersonLoc = SCREEN;
			}
			people.add(new Person(newPersonX, newPersonY, Person.INFECTIVE, newPersonLoc));
		}
		//add the specified number of suseptibles
		for(; i < initTotalPeople; i++) {
			int newPersonX;
			int newPersonY;
			Rectangle newPersonLoc;
			if(_useLocations) {
				//if all the infectives start in the same locatation, do not put people in location 0,0
				int newPersonCol = 0;
				int newPersonRow = 0;
				if(allInfectivesInSameLoc) {
					while(newPersonCol == 0 && newPersonRow == 0) {
						newPersonCol = numGen.nextInt(numLocationCols);
						newPersonRow = numGen.nextInt(numLocationRows);
					}
				} else {
					//randomly determine which row and col they are in
					newPersonCol = numGen.nextInt(numLocationCols);
					newPersonRow = numGen.nextInt(numLocationRows);
				}
				newPersonLoc = locations[newPersonCol][newPersonRow];
				//and place them into that location, again randomly
				newPersonX = numGen.nextInt((int)newPersonLoc.getWidth() - Person.WIDTH) + (int) newPersonLoc.getX();
				newPersonY = numGen.nextInt((int)newPersonLoc.getHeight() - Person.HEIGHT) + (int) newPersonLoc.getY();
			} else {
				//randomly place them in the SCREEN
				newPersonX = numGen.nextInt((int)SCREEN.getWidth() - Person.WIDTH) + (int)SCREEN.getX();
				newPersonY = numGen.nextInt((int)SCREEN.getHeight() - Person.HEIGHT) + (int)SCREEN.getY();
				newPersonLoc = SCREEN;
			}
			people.add(new Person(newPersonX, newPersonY, Person.SUSCEPTIBLE, newPersonLoc));
		}

		//store the number of timesteps
		this.totalNumTimesteps = numTimesteps;

		//get the max type integer value to set up the total counter array
		int maxTypeValue = Math.max(Math.max(Person.SUSCEPTIBLE, Person.INFECTIVE), Person.RECOVERED);

		//keep track of how many individuals we have at each timestep
		totals = new int[maxTypeValue+1][totalNumTimesteps+1];

		//set static disease variables
		DiseaseModel.alpha = alpha;
		DiseaseModel.beta = beta;
		DiseaseModel.useSIR = useSIR;
		DiseaseModel.recoveryDelay = recoveryDelay;
		this.moveAwayFromInfectives = moveAwayFromInfectives;
		this.useLocations = _useLocations;
		this.changeLocationProb = _changeLocationProb;
	}

	/* ASSUMES <people> VECTOR IS SORTED
	 * Binary searches for the person farthest to the left within the <radius>
	 * and farthest to the right within the radius, and returns a sub list of all the people in between 
	 */
	private List<Person> lookNearby(Person p, int radius) {
		//dummy people on both sides of <p>
		Person dummyLeft = new Person((int)(p.getX()-radius*p.getWidth()), 0, Person.SUSCEPTIBLE, SCREEN);
		Person dummyRight = new Person((int)(p.getX()+p.getWidth()*(1+radius)), 0, Person.SUSCEPTIBLE, SCREEN);

		//binary search of the indicies where those people should be
		int leftIndex =  Collections.binarySearch(people, dummyLeft);
		int rightIndex = Collections.binarySearch(people, dummyRight);

		//		System.out.println("p's location = " + p.getX() + ", " + p.getY());
		//		System.out.println("leftIndex = " + leftIndex + " right index = " + rightIndex);

		//fix the indexes if need be
		if(leftIndex < 0) leftIndex = -1*(leftIndex + 1);
		if(rightIndex < 0) rightIndex = -1*(rightIndex +1);
		if(rightIndex == people.size()) rightIndex = people.size() - 1;

		//return the sub list
		return people.subList(leftIndex, rightIndex);
	}

	/**
	 * Allow the Model Setup to pause the model
	 */
	public void cyclePauseModel() {
		pauseModel = !pauseModel;
	}

	
	/**
	 * See if the model is paused
	 */
	public boolean isPaused() {
		return pauseModel;
	}

	/**
	 * Stop the model at the next cycle
	 */
	public void stopModel() {
		stopModel = true;
	}

	/**
	 * Run the model
	 * Go through the required timesteps, moving everyone and checking for collisions
	 */
	public void runModel() {
		//sort everyone
		Collections.sort(people);

		//make sure when we start we don't immidiately pause or worse, stop
		pauseModel = false;
		stopModel = false;

		//get the current time for the delay
		long t = System.currentTimeMillis();
		//go through all of the timesteps
		for(curTimestep = 0; curTimestep < totalNumTimesteps && !stopModel; curTimestep++) {
			//count up how many of each type of people there is at the start of the timestep, 
			//and store the value in the <totals> array in the correct spot
			int numSusceptibles = 0, numInfectives = 0, numRecovered = 0;
			for(Person p : people) {
				if(p.getType() == Person.SUSCEPTIBLE) numSusceptibles++;
				if(p.getType() == Person.INFECTIVE) numInfectives++;
				if(p.getType() == Person.RECOVERED) numRecovered++;
			}
			totals[Person.SUSCEPTIBLE][curTimestep] = numSusceptibles;
			totals[Person.INFECTIVE][curTimestep] = numInfectives;
			totals[Person.RECOVERED][curTimestep] = numRecovered;


			//VERY BAD WAY TO PAUSE
			//but we're doing it anyway
			while(pauseModel) {}

			//make sure we have waited enough
			while(System.currentTimeMillis() - t < timestepDelay) {}
			t = System.currentTimeMillis();

			//change people's locations if needbe
			if(useLocations) {
				for(int i = 0; i < people.size(); i++) {
					if(numGen.nextDouble() < changeLocationProb) {
						int newLocCol = numGen.nextInt(locations.length);
						int newLocRow = numGen.nextInt(locations[0].length);
						people.get(i).setBoundingLocation(locations[newLocCol][newLocRow]);
					}
				}
			}

			Collections.sort(people);

			//find everyone's nearest infective
			Vector<Person> nearestInfectives = new Vector<Person>(people.size());
			for(int i = 0; i < people.size(); i++) {
				Person p = people.get(i);
				//find the nearest infective, if we are moving away from infectives and we are not infective
				if(moveAwayFromInfectives && p.getType() != Person.INFECTIVE) {
					//get a bounding box for us that has "radius" 2
					Rectangle ourRect = p.getIntersectionRectangle(2);
					Person nearestInfective = null;
					double distanceToNearestInfective = Double.MAX_VALUE;
					//check it against every other persons and find nearest infective, if they exist
					for(int j = 0; j < people.size(); j++) {
						if(i == j) continue;
						Person lookingAt = people.get(j);

						if(lookingAt.getType() == Person.INFECTIVE) {
							if(ourRect.intersects(lookingAt)) {
								if(p.getLocation().distance(lookingAt.getLocation()) < distanceToNearestInfective) {
									distanceToNearestInfective = p.getLocation().distance(lookingAt.getLocation());
									nearestInfective = lookingAt;
								}
							}
						}
					}		
					//store the nearest infective
					nearestInfectives.insertElementAt(nearestInfective, i);
				} else {
					//we are not moving away from nearest infectives, so just store a null
					nearestInfectives.insertElementAt(null, i);
				}
			}			

			//move everyone
			for(int i = 0; i < people.size(); i++) {
				Person p = people.get(i);
				p.Move(nearestInfectives.get(i));
				//make people get better or become susceptible if needbe
				if(p.getType() == Person.INFECTIVE)
					p.getWellSoon();
				if(p.getType() == Person.RECOVERED)
					p.getSickSoon();
			}

			//Sort everyone, now that everyone has moved
			Collections.sort(people);

			//check for collisions
			int collisionRadius = 2;
			for(Person outer : people) {
				//get all the people nearby
				List<Person> nearbyPeople = lookNearby(outer, collisionRadius);
				//go through all those people, and check for collisions
				for(Person inner : nearbyPeople) {
					if(Math.abs(outer.getX() - inner.getX()) < collisionRadius*outer.getWidth() &&
							Math.abs(outer.getY() - inner.getY()) < collisionRadius*outer.getHeight()) {
						Person.collision(outer, inner);
					}
				}

			}

			//repaint the screen
			repaint();	
		}
	}


	//handle the state change of the slider being moved
	public void stateChanged(ChangeEvent e) {
		Object source = e.getSource();
		if(e.getSource() == delaySlider) {
			JSlider sliderSource = (JSlider) source;
			if(!sliderSource.getValueIsAdjusting()) {
				timestepDelay = (int)sliderSource.getValue();
			}
		}
		repaint();
	}

	//handle window events.  We only care about closing
	public void windowClosing(WindowEvent e) {
		stopModel();
		setVisible(false);
	}
	public void windowActivated(WindowEvent e) {
		repaint();
	}
	public void windowClosed(WindowEvent e) {}
	public void windowDeactivated(WindowEvent e) {
		repaint();
	}
	public void windowDeiconified(WindowEvent e) {}
	public void windowIconified(WindowEvent e) {}
	public void windowOpened(WindowEvent e) {}


	//paint the screen - let the slider take care of itself
	public void paint(Graphics g) {
		super.paint(g);

		//get the graphics object, and make it into a 2D object
		g = getGraphics();
		Graphics2D g2d = (Graphics2D) g;

		//remove all the old people in the screen
		g2d.setColor(backgroundColor);
		for(Person p : people) {
			g2d.fill(p.getPreviousPerson());
		}

		//draw all the people inside the screen
		for(Person p : people) {
			//set color based on type
			g2d.setColor(Person.diseaseStateColors[p.getType()]);
			//fill the person with the correct color
			g2d.fill(p);
		}

		//draw lines along the screen's borders
		g2d.setColor(Color.BLACK);
		g2d.draw(SCREEN);
		//draw the graph
		paintGraph(g2d);
	}

	private void paintGraph(Graphics2D g2d) {
		//draw the outline of the graph bounding rectangle
		g2d.setColor(Color.black);
		g2d.draw(GRAPH_BOUNDING_RECT);

		//draw & label the axies (total num people on Y, total num timesteps on X
		g2d.setColor(Color.BLACK);
		g2d.setStroke(new BasicStroke(2));
		g2d.drawLine(getGraphXLocation(0), getGraphYLocation(0), getGraphXLocation(0), getGraphYLocation(people.size()));
		g2d.drawLine(getGraphXLocation(0), getGraphYLocation(0), getGraphXLocation(totalNumTimesteps), getGraphYLocation(0));

		//draw axis labels
		Font labelFont = new Font("Serif", Font.PLAIN, 12);
		g2d.setFont(labelFont);
		for(int i = 0; i <= totalNumTimesteps; i+= totalNumTimesteps / 10)
			g2d.drawString(""+i, getGraphXLocation(i), getGraphYLocation(0) + 20);
		g2d.drawString("Number of timesteps", getGraphXLocation(totalNumTimesteps/2), getGraphYLocation(0) + 40);
		for(int i = 0; i <= people.size(); i+= people.size()/10)
			g2d.drawString(""+i, getGraphXLocation(0) - 20, getGraphYLocation(i));
		g2d.rotate(-1*Math.PI/2, getGraphXLocation(0), getGraphYLocation(people.size()/2));
		g2d.drawString("Number of People", getGraphXLocation(0), getGraphYLocation(people.size()/2) - 30);
		g2d.rotate(Math.PI/2, getGraphXLocation(0), getGraphYLocation(people.size()/2));

		//go through the totals array.  Draw a dot for each timestep, and a line from each timestep to the next
		final int DOT_WIDTH = 2;
		final int DOT_HEGIHT = 2;
		g2d.setStroke(new BasicStroke(1));
		g2d.setColor(Person.diseaseStateColors[Person.SUSCEPTIBLE]);
		g2d.fillRect(getGraphXLocation(0), getGraphYLocation(totals[Person.SUSCEPTIBLE][0]), DOT_WIDTH, DOT_HEGIHT);
		for(int i = 1; i < curTimestep; i++) {
			g2d.drawLine(getGraphXLocation(i-1), getGraphYLocation(totals[Person.SUSCEPTIBLE][i-1]), getGraphXLocation(i), getGraphYLocation(totals[Person.SUSCEPTIBLE][i]));
			//			g2d.fillRect(getGraphXLocation(i), getGraphYLocation(totals[Person.SUSCEPTIBLE][i-1]), DOT_WIDTH, DOT_HEGIHT);
		}
		g2d.setColor(Person.diseaseStateColors[Person.INFECTIVE]);
		g2d.fillRect(getGraphXLocation(0), getGraphYLocation(totals[Person.INFECTIVE][0]), DOT_WIDTH, DOT_HEGIHT);
		for(int i = 1; i < curTimestep; i++) {
			g2d.drawLine(getGraphXLocation(i-1), getGraphYLocation(totals[Person.INFECTIVE][i-1]), getGraphXLocation(i), getGraphYLocation(totals[Person.INFECTIVE][i]));
			//			g2d.fillRect(getGraphXLocation(i), getGraphYLocation(totals[Person.INFECTIVE][i-1]), DOT_WIDTH, DOT_HEGIHT);
		}
		g2d.setColor(Person.diseaseStateColors[Person.RECOVERED]);
		g2d.fillRect(getGraphXLocation(0), getGraphYLocation(totals[Person.RECOVERED][0]), DOT_WIDTH, DOT_HEGIHT);
		for(int i = 1; i < curTimestep; i++) {
			g2d.drawLine(getGraphXLocation(i-1), getGraphYLocation(totals[Person.RECOVERED][i-1]), getGraphXLocation(i), getGraphYLocation(totals[Person.RECOVERED][i]));
		}
	}

	//returns the X Location on the UI of the passed value
	//values should be timestep values
	private int getGraphXLocation(int value) {
		int minXLocation = (int)GRAPH_BOUNDING_RECT.getX() + 50;
		int maxXLocation = (int)GRAPH_BOUNDING_RECT.getMaxX() - 10;
		return (int)((maxXLocation - minXLocation) * ((double)value / totalNumTimesteps) + minXLocation); 
	}

	//returns the Y location on the UI of the passed value
	private int getGraphYLocation(int value) {
		int minYLocation = (int) GRAPH_BOUNDING_RECT.getY() + 10;
		int maxYLocation = (int) GRAPH_BOUNDING_RECT.getMaxY() - 50;
		return (int)(maxYLocation - (maxYLocation - minYLocation) * ((double)value / people.size()));
	}

}