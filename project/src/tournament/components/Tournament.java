package tournament.components;

/**
 * Represents a tournament structure and contains fields with the relevant information
 * @author CS360 Team 5
 *
 */
public class Tournament {
	private int id;
	private String name;
	private Event highestEvent;
	private boolean classBased;
	
	/** Constructor **
	 * 
	 * @param name Name of the tournament
	 * @param highest Reference to the highest event in the tournament
	 * @param isClassBased whether the tournament is class based or not
	 */
	public Tournament(String name, Event highest, boolean isClassBased) {
		
		this.name = name;
		this.highestEvent = highest;
		this.classBased = isClassBased;
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the classBased
	 */
	public boolean isClassBased() {
		return classBased;
	}
	/**
	 * @param classBased the classBased to set
	 */
	public void setClassBased(boolean classBased) {
		this.classBased = classBased;
	}
	
	/**
	 * @return the iD
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param iD the iD to set
	 */
	public void setId(int id) {
		this.id = id;
	}
	
	public void setHighestEvent(Event e) {
		this.highestEvent = e;
	}
	
	public Event getHighestEvent() {
		return this.highestEvent;
	}
	
	public String toString() {
		return this.name;
	}

}
