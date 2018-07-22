package distancetests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import builder.Model;
import builder.SqlDatabaseInterface;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import tournament.components.Base;
import tournament.components.Event;
import tournament.components.Event.Level;
import tournament.components.School;

class GetLongestDistanceTest {

	private static Model model;
	private static Connection connection;
	private static int startingId = 700;
	private static int testSchoolCount = 8;
	private static int tournamentId = 101;
	private static int stateId = 102;
	private static int semistateId = 103;
	private static int regionalId = 104;
	private static int sectional1Id = 105;
	private static ArrayList<School> participants = new ArrayList<School>();	


	@BeforeAll static void init(){
		
		
		model = Model.getInstance();
		
		connection = model.getConnection();
		String sql = "";
		
		int distance = 10;
		String schoolValues = "";
		String distanceValues = "";
		String participantValues = "";
		
		
		sql = "INSERT IGNORE INTO tournaments VALUES(" + tournamentId + ", 'Test Tournament', 0)";
		SqlDatabaseInterface.runVoidQuery(sql, connection);
		
		sql = "INSERT IGNORE INTO state VALUES(" + stateId + "," + tournamentId + "," + startingId + ")";
		SqlDatabaseInterface.runVoidQuery(sql, connection);
		
		sql = "INSERT IGNORE INTO semi_state VALUES(" + semistateId + "," + stateId + "," + startingId + ")";
		SqlDatabaseInterface.runVoidQuery(sql, connection);
		
		sql = "INSERT IGNORE INTO regional VALUES(" + regionalId + "," + semistateId + "," + startingId + ")";
		SqlDatabaseInterface.runVoidQuery(sql, connection);
		
		sql = "INSERT IGNORE INTO sectional VALUES(" + sectional1Id + "," + regionalId + "," + startingId + ")";
		SqlDatabaseInterface.runVoidQuery(sql, connection);
			
		
		for(int i = 0; i < testSchoolCount; i++, distance += 25) {
			
			School school = new School(startingId + i);
			
			schoolValues = (startingId + i) + ", 'Test Name', 'Test_Address', 'Test City', 'Test Zip', 0, 0, 0, true, true";
			sql = "INSERT IGNORE INTO schools VALUES(" + schoolValues + ")";
			SqlDatabaseInterface.runVoidQuery(sql, connection);
			
			
			
			if(i != 0) {
				participants.add(school);
				
				distanceValues = startingId + "," + (startingId + i) + "," + distance;
				sql = "INSERT IGNORE INTO school_distance VALUES(" + distanceValues + ")";
				SqlDatabaseInterface.runVoidQuery(sql, connection);
				
				participantValues =  (startingId + i) + ", " + sectional1Id;
				sql = "INSERT IGNORE INTO participating VALUES(" + participantValues + ")";
				SqlDatabaseInterface.runVoidQuery(sql, connection);
			}
		}
		
		
		Event event = new Base(Level.Sec, new School(startingId), model.getTournamentById(tournamentId), participants);
		
		model.setSelectedEvent(event);
		
	}
	
	@AfterAll static void teardown() {
		model.removeTournament(model.getTournamentById(tournamentId));
		String sql = "";
		
		for(int i = 0; i < testSchoolCount; i++) {
			sql = "DELETE FROM schools WHERE ID=" + (startingId + i);
			SqlDatabaseInterface.runVoidQuery(sql, connection);
		}
		
		sql = "DELETE FROM school_distance WHERE school_from=" + startingId;
		SqlDatabaseInterface.runVoidQuery(sql, connection);
		
		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	
	
	@Test void getLongestDistancetest() {
		double expectedDistance = 185.0;
		double actualDistance = (double)model.getLongestDistanceAndSchool(model.getSelectedEvent())[0];
		
		assertEquals(expectedDistance, actualDistance, .1);
		System.out.println(actualDistance);
	}

}
