package builder;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.stream.Collectors;

import tournament.components.Advancement;
import tournament.components.Base;
import tournament.components.Event;
import tournament.components.Event.Level;
import tournament.components.School;
import tournament.components.Tournament;

public class Model extends Observable{
	
	// Enumeration
	public enum ActionPerformed { TOURNAMENT_REFRESHED, ADDED_SCHOOL, REMOVED_SCHOOL, UPDATED_SCHOOL, ADDED_HOST, DELETED_HOST,
									ADDED_TOURNAMENT, REMOVED_TOURNAMENT, SCHOOL_SELECTED, EVENT_LEVEL_SELECTED, VIEW_SWITCHED, MANAGE_HOSTS,
									SECTIONAL_UPDATED, TRANSFER_PROCESS_STARTED, TRANSFER_ADDED_TO_TLIST, TRANSFER_REMOVED_FROM_TLIST, TOURNAMENT_CHANGED, 
									TRANSFER_PROCESS_CANCELED, CHANGE_HOST_PROCESS_STARTED, EVENT_HOST_UPDATED, SHOW_ELIGIBLE_HOSTS, SHOW_REMOVE_ELIGIBLE_HOSTS, 
									SHOW_ELIGIBLE_HOSTS_TO_ADD, SHOW_CURRENT_HOSTS_TO_REMOVE, SHOW_ALL_ELIGIBLE_HOSTS, SHOW_UNSET_HOSTS};
									
	private static final String[] TABLE_NAMES = {"state", "semi_state", "regional", "sectional"};
	private static final String[] ID_NAMES = {"tourny_id", "state_id", "semi_state_id", "regional_id"};
							
	// Singleton Object
	private static Model model;

	// Connection Status
	private boolean isConnected = true;
	
	// Connection Object
	private Connection conn;
	
	// Object Variables
	private Tournament currentTournament = null;
	private Event selectedEvent = null;
	private Event.Level selectedLevel = Level.State;
	
	// Map view is showing
	private boolean mapVisible = false;
	private boolean manageHostVisible = false;
	private boolean userIsTransferring = false;
	private boolean userIsChangingHost = false;
	private ArrayList<School> transferList = null;
	
	/** Public Static Constructor
	 * 
	 * @return singleton data model
	 */
	public static Model getInstance() {
        
		if (model == null) {
        	model = new Model();
        }

        return model;
    }

	/** Private Constructor **
	 * 
	 */
	private Model() {
		
		this.conn = SqlDatabaseInterface.establishConnection();
		transferList = new ArrayList<School>();
		
		if(conn == null) {
			this.currentTournament = null;
			this.isConnected = false;
		} else {
			this.currentTournament = this.getTournaments().get(0);
			this.isConnected = true;
		}
	}
	
	public Connection getConnection() {
		return conn;
	}
	

	/** Closes opened Connections to open resources 
	 * 
	 * Should be called when the application closes
	 * 
	 */
	public void cleanUp() {
		SqlDatabaseInterface.close(conn);
	}
	
	/** Call by the controller when the class is started **
	 * 
	 * Informs the Model a new list of Tournaments is available
	 * 
	 */
	public void loadInitialTournament() {
		pushNotification(true, ActionPerformed.TOURNAMENT_REFRESHED);
	}

	/** Returns all schools in the database **
	 * 
	 * @return School array of all schools ordered alphabetically OR null
	 */
	public ArrayList<School> getAllSchools() {
		School s = null;
		ArrayList<School> schools = new ArrayList<School>();
		ResultSet rs = null;
		String sql = "SELECT * FROM schools ORDER BY name"; // SQL Query

		rs = SqlDatabaseInterface.runQuery(sql,conn);

		try {
			
			while(rs != null && rs.next()) {
				s = new School(rs.getInt("ID"), rs.getString("name"), rs.getString("address"), 
						rs.getString("city"),rs.getString("zip"), rs.getBoolean("hasBoysTeam"),
						rs.getBoolean("hasGirlsTeam"), rs.getInt("size"), rs.getDouble("longitude"), 
						rs.getDouble("latitude"));
				schools.add(s);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			// Close the statement object
			SqlDatabaseInterface.close(rs);
		}

		// May return null
		return schools;
	}
	
	public boolean isSchoolInHostList(School s, Tournament t) {
		
		boolean result = false;
		ResultSet rs = null;
		String sql = "SELECT * FROM hosts WHERE school_id = " + s.getId() + " AND tournament_id = " + t.getId();  // SQL Query

		rs = SqlDatabaseInterface.runQuery(sql,conn);

		try {
			
			if(rs != null && rs.next()) {
				result = true;
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			// Close the statement object
			SqlDatabaseInterface.close(rs);
		}

		// May return null
		return result;
		
	}
	
	/** Returns a School object matching the name from the database **
	 * 
	 * @param name - Name of the school to retrieve
	 * @return first school retrieved OR null if no school was found
	 */
	public School getSchool(String name) {

		School s = null;	// School Object to return
		ResultSet rs = null;
		String sql = "SELECT * FROM schools WHERE name = '" + name + "' ORDER BY name"; // SQL Query

		// Run the query
		rs = SqlDatabaseInterface.runQuery(sql,conn);

		try {

			// Extract data from result set
			if(rs != null && !rs.isClosed() && rs.next()) {
				s = new School(rs.getInt("ID"), rs.getString("name"), rs.getString("address"), 
						rs.getString("city"),rs.getString("zip"), rs.getBoolean("hasBoysTeam"),
						rs.getBoolean("hasGirlsTeam"), rs.getInt("size"), rs.getDouble("longitude"), 
						rs.getDouble("latitude"));
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			// Close the statement object
			SqlDatabaseInterface.close(rs);
		}

		// Return the school (or null)
		return s;

	}
	
	/** Returns a school object matching the given ID **
	 * 
	 * @param ID - of school to retrieve
	 * @return new School Object
	 */
	public School getSchool(int id){
		
		School s = null;
		ResultSet rs = null;
		String sql = "SELECT * FROM schools WHERE ID = " + id; // SQL Query

		// Run the query
		rs = SqlDatabaseInterface.runQuery(sql,conn);

		try {

			// Extract data from result set
			if(rs != null && rs.next()) {
				s = buildSchool(rs);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			// Close the statement object
			SqlDatabaseInterface.close(rs);
		}

		// Return the school (or null)
		return s;
	}
	
	/** Get the distance between schools
	 * 
	 * @param idOne - School id of the first
	 * @param idTwo - School id of the second
	 * @return d - returns the distance between the two given schools
	 */
	public double getSchoolDistance(int idOne, int idTwo){
		
		double distance = -1;
		ResultSet rs = null;
		String sql = "SELECT distance FROM school_distance WHERE (school_distance.school_from = " + idOne + " AND school_distance.school_to = " + idTwo + ") OR " + 
				"(school_distance.school_to = " + idOne + " AND school_distance.school_from = " + idTwo + ")"; // SQL Query

		// Run the query
		rs = SqlDatabaseInterface.runQuery(sql,conn);

		try {

			// Extract data from result set
			if(rs != null && rs.next()) {
				distance = rs.getDouble("distance");
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			// Close the statement object
			SqlDatabaseInterface.close(rs);
		}

		// Return the school (or null)
		return distance;
	}
	
	public ArrayList<School> getNonHosts(){
		return getNonHosts(this.getCurrentTournament());
	}
	
	public ArrayList<School> getNonHosts(Tournament t){
		
		ArrayList<School> results = new ArrayList<School>();
		String sql = "SELECT * FROM `schools` WHERE schools.ID " + 
				"NOT IN (SELECT hosts.school_id FROM hosts WHERE hosts.tournament_id = " + t.getId() + ") ORDER BY name";
		ResultSet rs;
		School s;
		
		// Run the query
		rs = SqlDatabaseInterface.runQuery(sql,conn);

		try {
			
			while(rs != null && !rs.isClosed() && rs.next()) {
				s = this.buildSchool(rs);
				results.add(s);
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			SqlDatabaseInterface.close(rs);
		}

		return results;
		
	}
	
	public ArrayList<School> getHostList(){
		return getHostList(this.getCurrentTournament());
	}
	
	public ArrayList<School> getHostList(Tournament t){
		
		ArrayList<School> hostList = new ArrayList<School>();
		ResultSet rs;
		School s;
		String sql = "SELECT * FROM hosts WHERE tournament_id = " + t.getId();
		
		// Run the query
		rs = SqlDatabaseInterface.runQuery(sql,conn);
		
		try {
			while(rs != null && !rs.isClosed() && rs.next()) {
				
				s = this.getSchool(rs.getInt("school_id"));
				hostList.add(s);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			SqlDatabaseInterface.close(rs);
		}

		return hostList;
	}
	
	public void deleteSchoolFromHostList(School s) {
		deleteSchoolFromHostList(s, this.getCurrentTournament());
	}
	
	public void deleteSchoolFromHostList(School s, Tournament t) {
		boolean success = false;
		
		String sql = "DELETE FROM `hosts` WHERE school_id = " + s.getId()
				+ " AND tournament_id = " + t.getId();
		
		success = SqlDatabaseInterface.runVoidQuery(sql,conn);

		pushNotification(success, ActionPerformed.DELETED_HOST);
	}
	
	public void addSchoolToHostList(School s) {
		addSchoolToHostList(s, this.getCurrentTournament());
	}
	
	public void addSchoolToHostList(School s, Tournament t) {
		boolean success = false;
		
		String sql = "INSERT INTO `hosts`(`school_id`, `tournament_id`) VALUES "
				+ "(" + s.getId() + "," + t.getId() + ")";
		
		success = SqlDatabaseInterface.runVoidQuery(sql,conn);

		pushNotification(success, ActionPerformed.ADDED_HOST);
	}
	
	
	/** Gets the feeder events that directly feed the event **
	 * 
	 * @param e - event to get the feeders from
	 * @return events that feed the @param e event
	 */
	public ArrayList<Event> getDirectFeederEvents(Event e){
		
		ArrayList<Event> s = new ArrayList<Event>();
		
		if(e instanceof Base) {
			s.add(e);
		} else if(e instanceof Advancement) {
			s.addAll(((Advancement) e).getEvents());
		}
		
		return s;
	}

	/** Get all of the tournaments in the database **
	 * 
	 * @return the array list of all of the tournament objects
	 */
	public ArrayList<Tournament> getTournaments(){

		ArrayList<Tournament> a = new ArrayList<Tournament>();
		ResultSet rs;
		String sql = "SELECT ID FROM tournaments";
		Tournament t;
		
		// Run the query
		rs = SqlDatabaseInterface.runQuery(sql,conn);
		
		try {
			// Fetch Tournaments from the database
			while(rs != null && !rs.isClosed() && rs.next()) {
				
				// Use ID method to retrieve all object entries
				t = this.getTournamentById(rs.getInt("ID"));
				
				// Add to the list
				a.add(t);
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			SqlDatabaseInterface.close(rs);
		}

		// Return the state event
		if(a.size() != 0) {
			return a;
		} else {
			return null;
		}
			
	}
	
	/** Get all of the tournaments in the database **
	 * 
	 * @return the array list of all of the tournament objects
	 */
	public Tournament getTournamentById(int id){

		Advancement state;
		ArrayList<Event> semiStates;
		ArrayList<Event> regionals;
		ResultSet rs;
		String sql = "SELECT * FROM tournaments WHERE ID = " + id;
		Tournament t = null;
		
		// Run the query
		rs = SqlDatabaseInterface.runQuery(sql,conn);
		
		try {
			// Fetch Tournament from the database
			if(rs != null && !rs.isClosed() && rs.next()) {
				// Create a new tournament object
				t = new Tournament(rs.getString("name"), null, rs.getBoolean("isClassBased"));
				
				// Set the ID
				t.setId(rs.getInt("ID"));
				
				// Check the state table
				t.setHighestEvent(getState(t));
				
				// Initialize the state event
				state = (Advancement) t.getHighestEvent();
				
				if(state != null) {
					semiStates = getSemiStates((Advancement) state);
					
					if(semiStates.size() > 0) {
						
						// Add each semi_state to state
						for(int i = 0 ; i < semiStates.size(); i++) {
							state.addEvent(semiStates.get(i));
						}
						 
						// Fetch the RegionalS for each Semi-state and add them
						for(int i = 0; i < semiStates.size(); i++) {
							regionals = getRegionals((Advancement) semiStates.get(i));
							
							if(regionals.size() > 0) {
								((Advancement) semiStates.get(i)).addAll(0, regionals);
								
								for(int j = 0; j < regionals.size(); j++) {
									((Advancement) regionals.get(j)).addAll(0, this.getSectionals((Advancement) regionals.get(j)));
								}
							}
						}
						
					}
					
				}
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			SqlDatabaseInterface.close(rs);
		}
		
		return t;
	}
	
	public void removeTournament(Tournament t) {
		
		boolean success = false;
		String sql = "DELETE FROM `tournaments` WHERE ID = " + t.getId();
		
		success = SqlDatabaseInterface.runVoidQuery(sql,conn);

		pushNotification(success, ActionPerformed.REMOVED_TOURNAMENT);
	}
	
	public void saveCopyOfTournamentUnderName(String name, Tournament t) {
		
		int high = this.getHighestId(SqlDatabaseInterface.TOURNAMENT_TABLE_NAME);
		
		if(high == -1) {
			high = 1;
		} else {
			high++;
		}
		
		saveCopyOfTournamentUnderName(name, t, high);
	}
	
	/** Copies the tournament object with a check to ensure the name is unique
	 * 
	 * @param name - name to save tournament under
	 * @param t - tournament to copy to server
	 * @param newId - tournament Id to use
	 */
	public void saveCopyOfTournamentUnderName(String name, Tournament t, int newId) {
		
		Tournament newTourny = null;
		ArrayList<Tournament> tournaments = getTournaments();
		ArrayList<Integer> hostIds = new ArrayList<Integer>();
		ArrayList<School> hosts = new ArrayList<School>();
		String sql;
		String newName = name;
		int incre = 0;
		boolean success = false;
	
		// Prevent duplicate names by renaming
		if(tournaments != null) {
			for(int i = 0; i < tournaments.size(); i++) {
				if(name.compareTo(tournaments.get(i).getName()) == 0 || newName.compareTo(tournaments.get(i).getName()) == 0) {
					newName = name + "(" + ++incre + ")";
					i = 0;
				}
			}
		}
		
		// Trim the name to ensure it fits in the database
		if(newName.length() > 20) {
			
			newName = newName.substring(0, 20);
		}
		
		try {
			
			java.sql.PreparedStatement stmt = conn.prepareStatement("INSERT INTO tournaments (id, name, isClassBased) VALUES ( ? , ? , ? )");
			stmt.setInt(1, newId);
			stmt.setString(2, newName);
			stmt.setBoolean(3, t.isClassBased());
			success = SqlDatabaseInterface.runVoidQuery(stmt,conn);
		
		} catch (SQLException e) {
			e.printStackTrace();
			success = false;
		}

		if(success) {
			
			tournaments = getTournaments();
			
			for(int i = 0; i < tournaments.size(); i++) {
				if(newName.compareTo(tournaments.get(i).getName()) == 0) {
					newTourny = tournaments.get(i);
					break;
				}
			}
			
			if(newTourny != null) {
				
				int[] offset = {0, 0, 0, 0};
				
				sql = getInsertQueriesForEvent(t.getHighestEvent(), newTourny.getId(), offset);
				
				String[] queries = sql.split("[;]+");
				
				for(int i = 0; i < queries.length; i++) {
					
					success = SqlDatabaseInterface.runVoidQuery(queries[i],conn);
					
					if(!success) {
						pushNotification(false, ActionPerformed.ADDED_TOURNAMENT);
					}
				}
				
				hosts = this.getAllHostingSchools(t.getHighestEvent(), null);
				
				// Populate Hosts Table
				sql = "INSERT INTO `hosts`(`school_id`, `tournament_id`) "
						+ "VALUES ";
				
				for(int i = 0; i < hosts.size(); i++) {
					
					if(!hostIds.contains(hosts.get(i).getId())) {
						
						hostIds.add(hosts.get(i).getId());
						sql += "(" + hosts.get(i).getId() + "," + newTourny.getId() + "),";
					
					}
				}
				
				// Remove Comma
				sql = sql.substring(0,sql.length() - 1);
				
				success = SqlDatabaseInterface.runVoidQuery(sql,conn);
				
				if(!success) {
					pushNotification(false, ActionPerformed.ADDED_TOURNAMENT);
				}
				
				
			} else {
				success = false;
			}		
		}
		
		pushNotification(success, ActionPerformed.ADDED_TOURNAMENT);
	}
	
	public ArrayList<School> getAllHostingSchools(Event e, ArrayList<School> schools){
		
		Advancement a;
		
		if(schools == null) {
			schools = new ArrayList<School>();
		}
		
		if(e instanceof Advancement) {
			a = (Advancement) e;
			
			for(int i = 0; i < a.getEvents().size(); i++) {
				getAllHostingSchools(a.getEvents().get(i), schools);
			}
			
		}
		
		schools.add(e.getHost());
		
		return schools;
	}
	
	private int getNextIdForEventLevel(Level lvl, int[] offsetMatrix) {
		
		int next = -1;
		String[] tableNames = {"state", "semi_state", "regional", "sectional"};
		
		switch(lvl) {
		case State:
			next = getHighestId(tableNames[0]);
			next += offsetMatrix[0]++;
			break;
		case Semi:
			next = getHighestId(tableNames[1]);
			next += offsetMatrix[1]++;
			break;	
		case Reg:
			next = getHighestId(tableNames[2]);
			next += offsetMatrix[2]++;
			break;
		case Sec:
			next = getHighestId(tableNames[3]);
			next += offsetMatrix[3]++;
			break;
		default:
			next = -1;
			break;
		}
		
		return next + 1;
	}
	
	private String getParticipatingInsertQueries(Base b, int sectionalId) {
		
		String sql = "INSERT INTO `participating`(`school_id`, `sectional_id`) "
				+ "VALUES";
		
		for(int j = 0; j < b.getSchools().size(); j++) {
			sql += "(" + b.getSchools().get(j).getId() + "," + sectionalId + "),";
		}

		// Remove Comma
		sql = sql.substring(0,sql.length()-1);
		
		return sql;
	}
	
	private String getInsertQueriesForEvent(Event e, int parentId, int[] offsetMatrix) {
		
		String myQuery = "";
		String result = "";
		int id = getNextIdForEventLevel(e.getLevel(), offsetMatrix);
		int index = -1;
		
		// Get Children queries
		if(e instanceof Base) {
			
			// Build Sectional Queries
			result = getParticipatingInsertQueries((Base) e, id);
			
		} else {
			
			for(int i = 0; i < ((Advancement) e).getEvents().size(); i++) {
				result += getInsertQueriesForEvent(((Advancement) e).getEvents().get(i), id, offsetMatrix);
			}
			
		}

		// Build my own query
		switch(e.getLevel()) {
		case State:
			index = 0;
			break;
		case Semi:
			index = 1;
			break;	
		case Reg:
			index = 2;
			break;
		case Sec:
			index = 3;
			break;
		default:
			index = -1;
			break;
		}
		
		myQuery = "INSERT INTO " + TABLE_NAMES[index]  + "(`ID`,`" + ID_NAMES[index] + "`, `host`) "
				+ "VALUES " + "( " + id + "," + parentId + "," + e.getHost().getId() + ")";
		
		return myQuery + "; " + result + ";";
	}
	
	/** Private Helper to retrieve schools participating in a sectional **
	 * 
	 * @param sec_Id - sectional database ID
	 * @return array of schools going to that sectional
	 */
	private ArrayList<School> getParticipating(int secId){
		
		ArrayList<School> s = new ArrayList<School>();
		ResultSet rs = null;
		String sql = "SELECT * FROM participating WHERE sectional_id = " + secId; // SQL Query

		// Run the query
		rs = SqlDatabaseInterface.runQuery(sql,conn);

		try {

			// Extract data from result set
			if(rs != null) {
				while(rs.next()){
					s.add(getSchool(rs.getInt("school_id")));
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			// Close the statement object
			SqlDatabaseInterface.close(rs);
		}
	
		return s;
	}
	
	
	/*
	 * Retrieves a participant's sectional ID
	 */
	public int getParticipantsSectionalId(int participantId) {
		String sql = "SELECT sectional_id from participating WHERE school_id=" + participantId;
		
		ResultSet rs = null;
		
		rs = SqlDatabaseInterface.runQuery(sql,conn);
		int sectionalId = -1;
		
		
		try {
			if(rs.next()) {
			sectionalId = rs.getInt("sectional_id");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	
	
		return sectionalId;
	}
	
	
	public ArrayList<Event> getAppearances(String hostName){
		School s = this.getSchool(hostName);
		return getAppearances(s);
	}
	
	public ArrayList<Event> getAppearances(School s){
		
		Advancement state = (Advancement) currentTournament.getHighestEvent();
		
		return getHostsEvents(state, s.getId());
	}
	
	public ArrayList<Event> getEventTier(Event.Level lvl){
		
		Advancement state = (Advancement) currentTournament.getHighestEvent();
		
		return getTierEvents(state, lvl);
	}
	
	public ArrayList<Event> getEventTier(Event.Level lvl, Tournament t){
		
		Advancement state = (Advancement) t.getHighestEvent();
		
		return getTierEvents(state, lvl);
	}
	
	/*
	 * Recursive Helper method - returns a list of all the schools that could attend an event
	 */
	public ArrayList<School> getFeederSchools(Event e){
		
		ArrayList<School> feed = new ArrayList<School>();
		Base b;
		Advancement a;
		
		if(e instanceof Advancement) {
			a = (Advancement) e;
			
			for(int i = 0; i < a.getEvents().size(); i++) {
				feed.addAll(getFeederSchools(a.getEvents().get(i)));
			}
			
		} else if(e instanceof Base) {
			b = (Base) e;
			
			return b.getSchools();
		}
		
		return feed;
	}
	
	public void saveTournament(){
		saveTournament(this.currentTournament);
	}
	
	public void saveTournament(Tournament t) {
		removeTournament(t);
		saveCopyOfTournamentUnderName(t.getName(), t, t.getId());
	}
	
	public Tournament getCurrentTournament() {
		return currentTournament;
	}
	
	public void setCurrentTournament(Tournament t) {
		
		t = this.getTournamentById(t.getId());
		
		if(t != null) {
			this.currentTournament = t;
			pushNotification(true, ActionPerformed.TOURNAMENT_CHANGED);
		}
	}
	
	public void setSelectedLevel(Event.Level selectedLevel) {
		this.selectedLevel = selectedLevel;
		pushNotification(true, ActionPerformed.EVENT_LEVEL_SELECTED);
	}
	
	public void setSelectedEvent(Event selectedEvent) {
		this.selectedEvent = selectedEvent;
		pushNotification(true, ActionPerformed.SCHOOL_SELECTED);
	}
	
	public Event getSelectedEvent() {
		return selectedEvent;
	}

	public Event.Level getSelectedLevel() {
		return selectedLevel;
	}
	
	public boolean isMapVisible() {
		return mapVisible;
	}
	
	public boolean isListVisible() {
		return this.manageHostVisible;
	}
	
	/*
	 * Returns false if unconnected to MySQL server
	 */
	public boolean getConnectionStatus() {
		return isConnected;
	}

	public void setMapVisible(boolean mapVisible) {
		this.mapVisible = mapVisible;
		pushNotification(true, ActionPerformed.VIEW_SWITCHED);
	}
	
	public void setManageHostVisible(boolean manageHostVisible) {
		this.manageHostVisible = manageHostVisible;
		pushNotification(true, ActionPerformed.MANAGE_HOSTS);
	}
	
	private int getHighestId(String tableName) {
		
		String sql = "SELECT * FROM `" + tableName + "` ORDER BY `" + tableName + "`.`ID` DESC";
		int id = -1;
		ResultSet rs;

		// Run the query
		rs = SqlDatabaseInterface.runQuery(sql,conn);
		
		try {
			
			if(rs.next()) {
				id = rs.getInt("ID");
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			SqlDatabaseInterface.close(rs);
		}

		// Return the events
		return id;
		
	}
	
	private void pushNotification(boolean changed, ActionPerformed ap) {
		
		Object[] successAndType = {changed, ap};
		
		setChanged();
		notifyObservers(successAndType);
	}
	
	/*
	 * Recursive Helper method - returns a list where the school.id hosts an event
	 */
	private ArrayList<Event> getHostsEvents(Advancement a, int id){
		
		ArrayList<Event> list = new ArrayList<Event>();
		
		if(a.getEvents() != null) {
			if(a.getEvents().get(0) instanceof Advancement) {
				for(int i = 0; i < a.getEvents().size(); i++) {
					list.addAll(getHostsEvents( (Advancement) a.getEvents().get(i), id));
				}
			}
		}
		
		if(a.getHost().getId() == id) {
			list.add(a);
		}
		
		return list;
	}
	
	/*
	 * Recursive Helper method - returns a list of all the events at a given level
	 */
	private ArrayList<Event> getTierEvents(Advancement a, Event.Level lvl){
		
		ArrayList<Event> list = new ArrayList<Event>();
		
		if(a.getEvents() != null) {
			
			if(a.getEvents().get(0) instanceof Advancement) {
				
				for(int i = 0; i < a.getEvents().size(); i++) {
					list.addAll(getTierEvents( (Advancement) a.getEvents().get(i), lvl));
				}
				
			} else {
				for(int i = 0; i < a.getEvents().size(); i++) {
					if(a.getEvents().get(i).getLevel() == lvl) {
						list.add(a.getEvents().get(i));
					}
				}
			}
			
		}
		
		if(a.getLevel() == lvl) {
			list.add(a);
		}
		
		return list;
	}
	

	
	/** Get all the sectionals associated with the advancement object **
	 * 
	 * @param adv Advancement from which to retrieve sectionals
	 * @return list of sectionals feeding the @param adv
	 */
	private ArrayList<Event> getSectionals(Advancement adv){

		ArrayList<Event> s = new ArrayList<Event>();
		ResultSet rs;
		String type = "regional.ID";
		String sql = "SELECT sectional.* FROM sectional JOIN ";

		switch (adv.getLevel()) {
		case State:
			// State
			type = "state.ID";
			sql = sql + 
					"regional " + 
					"ON sectional.regional_id = regional.ID " + 
					"JOIN semi_state " + 
					"ON semi_state.ID = regional.semi_state_id " + 
					"JOIN state ON state.ID = semi_state.state_id";
			break;
		case Semi:
			// Semi-State
			type = "semi_state.ID";
			sql = sql 
				+ "regional ON sectional.regional_id = regional.ID " + 
				"JOIN semi_state ON semi_state.ID = regional.semi_state_id";
			break;
		case Reg:
			// Regional
			type = "regional.ID";
			sql = sql + "regional ON sectional.regional_id = regional.ID";
			break;
		case Sec:
			// Sectional - return itself
			s.add(adv);
			return s;
		default:
			break;
		}
		
		// Add WHERE clause
		sql = sql + " WHERE " + type + " = " + adv.getId();
		
		// Run the query
		rs = SqlDatabaseInterface.runQuery(sql,conn);
		
		try {
			
			while(rs != null && rs.next()) {
				s.add(new Base(Event.Level.Sec, getSchool(rs.getInt("host")), adv.getTournament(), getParticipating(rs.getInt("ID"))));
				s.get(s.size() - 1).setId(rs.getInt("ID"));
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			SqlDatabaseInterface.close(rs);
		}

		// Return the events
		return s;
	}

	/** Return an array list of the regional's feeding the advancement **
	 * 
	 * @param adv - advancement 
	 * @return list of regional's feeding that event
	 */
	private ArrayList<Event> getRegionals(Advancement adv){
		
		ArrayList<Event> s = new ArrayList<Event>();
		ResultSet rs;
		String type = "semi_state.ID";
		String sql = "SELECT regional.* FROM regional JOIN ";

		switch (adv.getLevel()) {
		case State:
			// State
			type = "state.ID";
			sql = sql + 
					"semi_state " + 
					"ON semi_state.ID = regional.semi_state_id " + 
					"JOIN state ON state.ID = semi_state.state_id";
			break;
		case Semi:
			// Semi-State
			type = "semi_state.ID";
			sql = sql 
				+ "semi_state ON semi_state.ID = regional.semi_state_id";
			break;
		case Reg:
			// Regional - return itself
			s.add(adv);
			return s;
		case Sec:
			// Sectional - invalid return nothing
			return null;
		default:
			break;
		}
		
		// Add WHERE clause
		sql = sql + " WHERE " + type + " = " + adv.getId();
		
		// Run the query
		rs = SqlDatabaseInterface.runQuery(sql,conn);
		
		try {
			
			while(rs != null && rs.next()) {
				s.add(new Advancement(Event.Level.Reg, getSchool(rs.getInt("host")), adv.getTournament(), null));
				s.get(s.size()-1).setId(rs.getInt("ID"));
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			SqlDatabaseInterface.close(rs);
		}

		// Return the events
		return s;
	}
	
	/** Get the semi states attached to an advancement **
	 * 
	 * @param adv - advancement semi-state or above
	 * @return semi_states
	 */
	private ArrayList<Event> getSemiStates(Advancement adv){

		ArrayList<Event> s = new ArrayList<Event>();
		ResultSet rs;
		String type = "state.ID";
		String sql = "SELECT semi_state.* FROM semi_state JOIN ";

		switch (adv.getLevel()) {
		case State:
			// State
			type = "state.ID";
			sql = sql + 
					"state ON state.ID = semi_state.state_id";
			break;
		case Semi:
			// Semi-State - return itself
			s.add(adv);
			return s;
		case Reg:
			// Regional - invalid return nothing
			return null;
		case Sec:
			// Sectional - invalid return nothing
			return null;
		default:
			break;
		}
		
		// Add WHERE clause
		sql = sql + " WHERE " + type + " = " + adv.getId();
		
		// Run the query
		rs = SqlDatabaseInterface.runQuery(sql,conn);
		
		try {
			
			while(rs != null && rs.next()) {
				s.add(new Advancement(Event.Level.Semi, getSchool(rs.getInt("host")), adv.getTournament(), null));
				s.get(s.size()-1).setId(rs.getInt("ID"));
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			SqlDatabaseInterface.close(rs);
		}

		// Return the events
		return s;
	}

	/** Return the state event for the Tournament
	 * 
	 * @param t - tournament that owns the state event
	 * @return state event in the tournament
	 */
	private Advancement getState(Tournament t){

		Advancement a = null;
		ResultSet rs;
		String sql = "SELECT * from state where state.tourny_id = " + t.getId();
		
		// Run the query
		rs = SqlDatabaseInterface.runQuery(sql,conn);
		
		try {
			
			if(rs != null && rs.next()) {
				a = new Advancement(Event.Level.State, getSchool(rs.getInt("host")), t, null);
				a.setId(rs.getInt("ID"));
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			SqlDatabaseInterface.close(rs);
		}

		// Return the state event
		return a;
	}
	
	
	public void updateParticipantSectionalId(int schoolId, int sectionalId, int oldSectionId) {
		
		String sql = "UPDATE participating SET sectional_id=" + sectionalId + " WHERE school_id = " + schoolId + " AND " + " sectional_id = " + oldSectionId;
		
		boolean success = SqlDatabaseInterface.runVoidQuery(sql, conn);
		
		pushNotification(success, ActionPerformed.SECTIONAL_UPDATED);
	}
	
	public void updateEventHost(Event event, int newHostId) {
		String table = "";
		
		if(event.getLevel() == Level.Sec) {
			table = "sectional";
		} else if(event.getLevel() == Level.Reg) {
			table = "regional";
		} else if(event.getLevel() == Level.Semi) {
			table = "semi_state";
		} else if(event.getLevel() == Level.State) {
			table = "state";
		}
		
		String sql = "UPDATE " + table + " SET host=" + newHostId  + " WHERE ID=" + event.getId();
		
		selectedEvent.setHost(model.getSchool(newHostId));
		
		boolean success = SqlDatabaseInterface.runVoidQuery(sql, conn);
		
		pushNotification(success, ActionPerformed.EVENT_HOST_UPDATED);
	}
		
		
	
	/*
	 * Returns a list of non-participating schools within the radius of the selectedEvent
	 */
	public ArrayList<School> getSchoolsInRadiusofSelectedEvent(int radius) {
		
		ArrayList<School> allSchools = getFeederSchools(currentTournament.getHighestEvent());
		ArrayList<School> participants = getFeederSchools(selectedEvent);
		ArrayList<School> schoolsInRadius = new ArrayList<School>(25);
		double distance = 0.0;
		boolean isParticipant = false;
		
		for(int i = 0; i < allSchools.size(); i++) {
			
			isParticipant = false;
			
			for(int j = 0; j < participants.size(); j++) {
				if(allSchools.get(i).getId() == participants.get(j).getId()) {
					isParticipant = true;
				}	
			}
			
			if(!isParticipant) {
				distance = getSchoolDistance(selectedEvent.getHost().getId(), allSchools.get(i).getId());
				if(distance > 0 && distance <= radius) {
					schoolsInRadius.add(allSchools.get(i));
				}
			}
		}
		
		return schoolsInRadius;
	}
	
	/* 
	 * Returns distance (double) at index 0, School object at index 1
	 */
	public Object[] getLongestDistanceAndSchool(Event e) {
		
		ArrayList<School> participating = getFeederSchools(e);
		
		Object[] distanceAndSchool = {null, null};
		double distance = 0;
		double longestDistance = 0;
		School furthestSchool = null;
		
		for(int i = 0; i < participating.size(); i++) {
			distance = getSchoolDistance(e.getHost().getId(), participating.get(i).getId());
			
			if(distance > longestDistance) {
				longestDistance = distance;
				furthestSchool = participating.get(i);
			}
		}
		
		distanceAndSchool[0] = longestDistance;
		distanceAndSchool[1] = furthestSchool;
		
		return distanceAndSchool;
	}
	
	public double getAverageDistance(Event e) {
		ArrayList<School> participating = null;
		Double averageDistance = 0.0;
		boolean hostAttends = false;
		
		participating = getFeederSchools(e);
		
		for(int i = 0; i < participating.size(); i++) {
			
			if(participating.get(i).getId() == e.getHost().getId()) {
				hostAttends = true;
				continue;
			}
			
			averageDistance += getSchoolDistance(e.getHost().getId(), participating.get(i).getId());
		}
		
		if(hostAttends) {
			averageDistance /= (participating.size() - 1);
		} else {
			averageDistance /= participating.size();
		}
		
		averageDistance *= 10;
		averageDistance = (double) Math.round(averageDistance.doubleValue());
		averageDistance /= 10.0; 
		
		return averageDistance;	
	}
	
	/*
	 * Returns longest distance from possibleHost to the currently selected event's feeder schools
	 */
	public double getLongestDistanceForPossibleHost(School possibleHost) {
		ArrayList<School> participating = getFeederSchools(selectedEvent);
		double distance = 0.0;
		double longestDistance = 0.0;
		
		for(int i = 0; i < participating.size(); i++) {
			distance = getSchoolDistance(possibleHost.getId(), participating.get(i).getId());
			
			if(distance > longestDistance) {
				longestDistance = distance;
			}
		}
		
		
		return longestDistance;
	}
	
	/*
	 * Returns the average distance from possibleHost to the feeder schools for the currently selected event
	 */
	public double getAverageDistanceForPossibleHost(School possibleHost) {
		ArrayList<School> participating = getFeederSchools(selectedEvent);
		double totalDistance = 0.0;
		double averageDistance = 0.0;
		boolean possibleHostParticipates = false;
		
		for(int i = 0; i < participating.size(); i++) {
			if(possibleHost.getId() == participating.get(i).getId()) {
				possibleHostParticipates = true;
				continue;
			}
			
			totalDistance += getSchoolDistance(possibleHost.getId(), participating.get(i).getId());
		}
		
		if(possibleHostParticipates) {
			averageDistance = totalDistance / (participating.size() - 1);
		} else {
			averageDistance = totalDistance / participating.size();
		}
		
		return averageDistance;
	}
	
	
	/*
	 * Return the longest distance from selected event to all feeders schools including sectional transfer schools
	 */
	public double getLongestDistanceWithTransfers(){
		double longest = 0.0;
		double distance = 0.0;
		School selectedEventHost = selectedEvent.getHost();
		ArrayList<School> feedersAndTransfers = new ArrayList<School>();
		
		feedersAndTransfers.addAll(model.getFeederSchools(selectedEvent));
		feedersAndTransfers.addAll(transferList);
		
		for(int i = 0; i < feedersAndTransfers.size(); i++) {
			distance = model.getSchoolDistance(selectedEventHost.getId(), feedersAndTransfers.get(i).getId());
			
			if(distance > longest) {
				longest = distance;
			}
		}
		
		return longest;
	}
	
	/*
	 * Returns the average distance from selected event to all feeder schools including sectional transfer schools
	 */
	public double getAverageDistanceWithTransfers() {
		double averageDistance = 0.0;
		School selectedEventHost = selectedEvent.getHost();
		ArrayList<School> feedersAndTransfers = new ArrayList<School>();
		
		feedersAndTransfers.addAll(model.getFeederSchools(selectedEvent));
		feedersAndTransfers.addAll(transferList);
		
		
		for(int i = 0; i < feedersAndTransfers.size(); i++) {
			averageDistance += model.getSchoolDistance(selectedEventHost.getId(), feedersAndTransfers.get(i).getId());
		}
		
		averageDistance /= feedersAndTransfers.size();
		
		return averageDistance;
	}
	
	
	
	/*
	 * Adds a school to the transfer list
	 */
	public void addTransfer(School transferSchool) {
		transferList.add(transferSchool);
		
		Object[] successTypeSchool = {true, ActionPerformed.TRANSFER_ADDED_TO_TLIST, transferSchool};
		
		setChanged();
		notifyObservers(successTypeSchool);
	}
	
	public void removeTransfer(School school) {
		boolean success = false;
		
		success = transferList.remove(school);
		
		Object[] successTypeSchool = {success, ActionPerformed.TRANSFER_REMOVED_FROM_TLIST, school};
		
		setChanged();
		notifyObservers(successTypeSchool);
	}
	
	
	public void clearTransferList() {
		transferList.clear();
		pushNotification(true, ActionPerformed.TRANSFER_PROCESS_CANCELED);
	}
	
	public void commitTransfers() {
		
		updateTransfersSectionalId();
		
		if(selectedEvent instanceof Base) {
			((Base) selectedEvent).addAllSchools(transferList);
		}
		
		transferList.clear();
		userIsTransferring = false;
		
		pushNotification(true, ActionPerformed.SECTIONAL_UPDATED);
	}
	
	public void updateTransfersSectionalId() {
		
		ArrayList<School> uniqueEntries =  new ArrayList<School>();
		Base sec;
		int currentSectional = -1;
		
		for(int i = 0; i < transferList.size(); i++) {
			if(!uniqueEntries.contains(transferList.get(i))){
				uniqueEntries.add(transferList.get(i));
			}
		}
		
		for(int i = 0; i < uniqueEntries.size(); i++) {
			sec = getSectionalWithSchool(currentTournament.getHighestEvent(), uniqueEntries.get(i));
			sec.getSchools().remove(uniqueEntries.get(i));
			currentSectional = sec.getId();
			updateParticipantSectionalId(uniqueEntries.get(i).getId(), selectedEvent.getId(), currentSectional);
		}
	}
	
	public Base getSectionalWithSchool(Event e, School s) {
		
		Base res = null;
		
		if(e instanceof Base) {
			 for(int i = 0; i < ((Base) e).getSchools().size(); i++) {
				 if(s.equals(((Base) e).getSchools().get(i))) {
					 return (Base) e;
				 }
			 }
		} else {
			
			for(int i = 0; i < ((Advancement) e).getEvents().size(); i++) {
				res = getSectionalWithSchool(((Advancement) e).getEvents().get(i), s);
				
				if(res != null) {
					break;
				}
			}
			
		}
		
		return res;
	}
	
	public void setUserIsTransferring(boolean b) {
		userIsTransferring = b;
		Object[] successAndType = new Object[2];
		
		if(b) {
			successAndType[0] = true;
			successAndType[1] = ActionPerformed.TRANSFER_PROCESS_STARTED;
		}else {
			successAndType[0] = true;
			successAndType[1] = ActionPerformed.TRANSFER_PROCESS_CANCELED;
		}
		
		setChanged();
		notifyObservers(successAndType);
	}
	
	public void startHostChangeProcess() {
		userIsChangingHost = true;
		pushNotification(true, ActionPerformed.CHANGE_HOST_PROCESS_STARTED);
	}
	
	
	public boolean getUserIsTransferring() {
		return userIsTransferring;
	}
	
	public void setUserIsChangingHost(boolean b) {
		userIsChangingHost = b;
	}
	
	public boolean getUserIsChangingHost() {
		return userIsChangingHost;
	}
	
	public ArrayList<School> getTransferList(){
		return transferList;
	}
	
	public void showHosts() {
		pushNotification(true, ActionPerformed.SHOW_ELIGIBLE_HOSTS);
	}
	
	public void showAllHosts() {
		pushNotification(true, ActionPerformed.SHOW_UNSET_HOSTS);
	}
	
	public void showEligibleHosts() {
		pushNotification(true, ActionPerformed.SHOW_ALL_ELIGIBLE_HOSTS);
	}
	
	public void showRemovalHosts() {
		pushNotification(true, ActionPerformed.SHOW_REMOVE_ELIGIBLE_HOSTS);
	}
	
	/** Builds a school object from a query that has ALL FIELDS of a school
	 * 
	 * @param rs - resultSet that is non-null and contains all columns of a school
	 * @return school object
	 */
	private School buildSchool(ResultSet rs) {
		
		School s = null;
		
		try {
			
			s = new School(rs.getInt("ID"), rs.getString("name"), rs.getString("address"), 
					rs.getString("city"),rs.getString("zip"), rs.getBoolean("hasBoysTeam"),
					rs.getBoolean("hasGirlsTeam"), rs.getInt("size"), rs.getDouble("longitude"), 
					rs.getDouble("latitude"));
			
		} catch (SQLException e) {
			e.printStackTrace();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return s;
	}
	
}