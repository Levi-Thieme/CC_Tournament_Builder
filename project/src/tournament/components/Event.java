package tournament.components;

import java.util.Comparator;

public abstract class Event implements Comparable<Event>{

	public enum Level{State, Semi, Reg, Sec};
	private Level level;
	private int id;
	private School host;
	private Tournament tournament;
	
	public Event(Level l, School host, Tournament t) {
		this.level = l;
		this.host = host;
		this.tournament = t;
	}
	
	/**
	 * @return the host
	 */
	public School getHost() {
		if(host == null) {
			return new School("Unassigned", "No Address", "No City", "No Zip", false, false, 0, 0, 0);
		}
		return host;
	}
	/**
	 * @param host the host to set
	 */
	public void setHost(School host) {
		this.host = host;
	}
	/**
	 * @return the tournament
	 */
	public Tournament getTournament() {
		return tournament;
	}
	/**
	 * @param tournament the tournament to set
	 */
	public void setTournament(Tournament tournament) {
		this.tournament = tournament;
	}
	
	public Level getLevel() {
		return level;
	}
	
	public void setLevel(Level l) {
		this.level = l;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	@Override public int compareTo(Event arg0) {

		return Comparators.BY_HOST_NAME.compare(this, arg0);
		    
	}
	
	public static class Comparators {

        public static Comparator<Event> BY_HOST_NAME = new Comparator<Event>() {
            @Override public int compare(Event o1, Event o2) {
                return o1.getHost().getName().compareTo(o2.getHost().getName());
            }
        };
       
    }

}
