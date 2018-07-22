package tournament.components;

import java.util.ArrayList;


public class Advancement extends Event{
	
	private ArrayList<Event> feeders; // Feeder events
	
	/** Constructor
	 * 
	 * @param l - enumerated level
	 * @param host - school hosting the event
	 * @param t - reference to the tournament this event belongs to
	 * @param feeders - arrays of events to advance to this event (feeders)
	 */
	public Advancement(Level l, School host, Tournament t, Event[] f) {
		super(l, host, t);

		feeders = new ArrayList<Event>();
	
		if(f != null) {
			for(int i = 0; i < f.length; i++) {
				feeders.add(f[i]);	
			}
		}
	}

	public void addEvent(Event addition) {
		feeders.add(addition);
	}
	
	
	public ArrayList<Event> getEvents() {
		return feeders;
	
	}
	
	public void removeEvent(Event remove) {
		feeders.remove(remove);
	}
	
	public int count() {
		return feeders.size();
	}

	public void addAll(int i, ArrayList<Event> events) {
		feeders.addAll(i, events);
		
	}
}
