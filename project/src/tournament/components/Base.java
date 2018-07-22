package tournament.components;

import java.util.ArrayList;
import java.util.Collection;

public class Base extends Event{

	
	private ArrayList<School> schools;
	
	/** Constructor
	 * 
	 * @param l - Enumeration level
	 * @param host - school hosting the event
	 * @param t - Tournament this event belongs to
	 * @param schools - array of schools attending this event
	 */
	public Base(Level l, School host, Tournament t, ArrayList<School> schools) {
		super(l, host, t);
		
		this.schools = new ArrayList<School>();
		
		this.schools.addAll(schools);
		
	}

	/** Add a school to the list **
	 * 
	 * @param addition - school to add
	 */
	public void addSchool(School addition) {
		schools.add(addition);
	}
	
	/*
	 * Add a collection of schools to the school list
	 */
	public void addAllSchools(Collection<School> newSchools) {
		this.schools.addAll(newSchools);
	}
	
	/** Removes a school from the list **
	 * 
	 * @param removal - school to remove (Must be a direct ref)
	 */
	public void removeSchool(School removal) {
		schools.remove(removal);
	}
	
	/** Count of schools in the event **
	 * 
	 * @return count
	 */
	public int count() {
		return schools.size();
	}
	
	/** Get an array of all the schools in the event **
	 * 
	 * @return array of schools
	 */
	public ArrayList<School >getSchools() {
		return schools;
	}

}
