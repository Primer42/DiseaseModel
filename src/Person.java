import java.awt.Color;
import java.awt.Rectangle;
import java.util.Random;

/**
 * @author Will Richard and Andrew Calkins
 * The Person class embodies a person on the screen.
 * It extends Rectangle for easy rendering.
 * Keeps track of it's type, as well as how long it should stay infective
 *
 */
public class Person extends Rectangle implements Comparable<Person>{
	private static final long serialVersionUID = 1L;

	//possible states
	public static final int SUSCEPTIBLE = 1;
	//	public static final Color SUSCEPTIBLE_COLOR = Color.GRAY;
	public static final int INFECTIVE = 2;
	//	public static final Color INFECTIVE_COLOR = Color.GREEN;
	public static final int RECOVERED = 3;
	//array of Colors, corresponding to the various states - make sure it is the size of the highest int + 1
	public static final Color[] diseaseStateColors = {Color.WHITE, Color.GRAY, Color.GREEN, Color.BLUE};

	public static Random numGenerator = new Random();

	private int type;
	private int immuntiyCounter; //counts down time until no longer immune 

	public final static int WIDTH = 7;
	public final static int HEIGHT = 7;

	//location stores the bounding rectangle in which this person needs to stay
	private Rectangle boundingLocation;

	//the previous state of this person, before the latest move
	//allows us to handle collisions more accurately and undo moves 
	private Person previousPerson;

	/**
	 * Basic Constructor - set everything up
	 * @param startX
	 * @param startY
	 * @param startType
	 * @param _location
	 */
	public Person(int startX, int startY, int startType, Rectangle _location) {
		super(startX, startY, WIDTH, HEIGHT);
		type = startType;
		boundingLocation = _location;
		previousPerson = (Person)this.clone();
	}



	//move the person within their location if they are inside it, or to their location if they are outside of it
	//passed the nearest infective person - if null, ignore that behavior
	public void Move(Person nearestInfective) {
		//change the location of the rectangle
		previousPerson = (Person)this.clone();

		if(boundingLocation.contains(this)) {

			//we are inside the bounding location - move randomly or away from infectives
			if(nearestInfective == null || this.getType() == Person.INFECTIVE) {
				//randomly determine if we're going to move in the x and y and translate the rectangle by that amount.
				int xChange, yChange;
				double xRandomNum = numGenerator.nextDouble();
				double yRandomNum = numGenerator.nextDouble();

				if(xRandomNum < 1.0/3.0)  xChange = -WIDTH;
				else if (xRandomNum < 2.0/3.0) xChange = 0;
				else xChange = WIDTH;

				if(yRandomNum < 1.0/3.0) yChange = -HEIGHT;
				else if (yRandomNum < 2.0 / 3.0) yChange = 0;
				else yChange = HEIGHT;

				this.translate(xChange, yChange);

				if(this.getLocation().equals(previousPerson.getLocation()))
					Move(nearestInfective);
			} else {
				//find difference in x and y direction and translate the rectangle by that amount.
				int xDiff = (int) (this.getX() - nearestInfective.getX());
				int yDiff = (int) (this.getY() - nearestInfective.getY());

				int xChange, yChange;

				if(xDiff < 0) xChange = -WIDTH;
				else if (xDiff == 0) xChange = 0;
				else xChange = WIDTH;

				if(yDiff < 0) yChange = -HEIGHT;
				else if(yDiff == 0) yChange = 0;
				else yChange = HEIGHT;

				this.translate(xChange, yChange);
			}
			
			//undo the move if we've moved outside the bounding location
			if(! boundingLocation.contains(this))
				undoMove();
		} else {
			//we are not inside our bounding rectangle
			//move toward the bounding rectangle
			int xDiff = (int) (this.getX() - boundingLocation.getCenterX());
			int yDiff = (int) (this.getY() - boundingLocation.getCenterY());

			//find the difference in x and y we need to move, and move that much
			int xChange, yChange;

			if(xDiff < 0) xChange = WIDTH;
			else if(xDiff == 0) xChange = 0;
			else xChange = -WIDTH;

			if(yDiff < 0) yChange = HEIGHT;
			else if(yDiff == 0) yChange = 0;
			else yChange = -HEIGHT;

			this.translate(xChange, yChange);
		}
	}

	//moves it back - does not undo types
	public void undoMove() {
		this.setLocation(previousPerson.getLocation());
		//this.setType(previousPerson.getType());
	}

	//get a rectangle that is centered at the current rectangle, but with a width and height of 3 times the current width and height
	//pass it the "radius" of the rectangle
	public Rectangle getIntersectionRectangle(int radius) {
		Rectangle intersectionRectangle = this.getBounds();
		//translate it so its top left corner is above and to the left our our top left corner
		intersectionRectangle.translate(-WIDTH*radius, -HEIGHT*radius);
		//now, expand it
		intersectionRectangle.setSize(2*radius*WIDTH + WIDTH, 2*radius*HEIGHT + HEIGHT);
		//return it
		return intersectionRectangle;
	}

	//setter and getter for type
	public void setType(int newType) {
		type = newType;
	}

	public int getType() {
		return type;
	}

	/**
	 * @return the location
	 */
	public Rectangle getBoundingLocation() {
		return boundingLocation;
	}



	/**
	 * @param location the location to set
	 */
	public void setBoundingLocation(Rectangle location) {
		this.boundingLocation = location;
	}


	//getter for the previous person
	//the previous person allows us to see when changes happen, and undo actions
	public Person getPreviousPerson() {
		return previousPerson;
	}

	/**
	 * Handle collisions between person a and person b
	 * Called from the model when it detects a collision
	 */
	public static void collision(Person a, Person b) {
		//check collisions with the previous versions of a and b, so that we don't switch types too early
		//Susceptible to infective
		if(a.previousPerson.getType() == RECOVERED || b.previousPerson.getType() == RECOVERED) {
			return;
		}
		if(a.previousPerson.getType() == INFECTIVE || b.previousPerson.getType() == INFECTIVE) {
			if(numGenerator.nextDouble() < DiseaseModel.alpha) {
				a.setType(INFECTIVE);
				b.setType(INFECTIVE);
			}

		}

	}

	/**
	 * Makes this person recover, if probability says they should
	 */
	public void getWellSoon(){
		//For every move, the infective individual has a chance to recovery.
		//Can be modified for SIS or SIR model.
		if(getType() == Person.INFECTIVE) {
			if(numGenerator.nextDouble() < DiseaseModel.beta) {
				if(DiseaseModel.useSIR) {
					setType(Person.RECOVERED);
					//set immuntity counter to inifinty if no delay specified in DiseaseModel
					//otherwise use delay in DiseaseModel
					if(DiseaseModel.recoveryDelay == 0) 
						immuntiyCounter = Integer.MAX_VALUE;
					else
						immuntiyCounter = DiseaseModel.recoveryDelay;
				}
				else 
					setType(Person.SUSCEPTIBLE);
			}
		}
	}

	/**
	 * Handles this person becoming susceptible again if they are recovered / immune
	 */
	public void getSickSoon() {
		if(getType() == Person.RECOVERED) {
			//decrement the immunity counter.  If it equals 0, become suceptible again
			immuntiyCounter--;
			if(immuntiyCounter <= 0) {
				this.setType(Person.SUSCEPTIBLE);
			}
		}
	}

	/*
	 * Compare people using X value.
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Person other) {
		//		if(this.getX() != other.getX())
		//			return (int)(this.getY() - other.getY());
		return (int)(this.getX() - other.getX());
	}

	/* Override of toString
	 * Prints out all sorts of useful information about this person
	 * (non-Javadoc)
	 * @see java.awt.Rectangle#toString()
	 */
	@Override
	public String toString() {
		String typeString = "";
		switch(this.getType()) {
		case SUSCEPTIBLE: typeString = "Susceptible"; break;
		case INFECTIVE: typeString = "Infective"; break;
		case RECOVERED: typeString = "Recovered"; break;
		default: typeString = "Unknown"; break;
		}

		return "'" + this.getX() +", " + this.getY() + " type = " + typeString + "'";
	}
}