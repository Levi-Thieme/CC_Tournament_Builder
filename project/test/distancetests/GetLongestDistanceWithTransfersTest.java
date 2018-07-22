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





class GetLongestDistanceWithTransfersTest {
	
	private static Model model;
	private static Connection connection;
	private static int startingId = 700;
	private static int testSchoolCount = 8;
	private static int tournamentId = 101;
	private static int stateId = 102;
	private static int semistateId = 103;
	private static int regionalId = 104;
	private static int sectional1Id = 105;
	private static int sectional2Id = 106;


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
		
		sql = "INSERT IGNORE INTO sectional VALUES(" + sectional2Id + "," + regionalId + "," + startingId + ")";
		SqlDatabaseInterface.runVoidQuery(sql, connection);
		
		
		
		
		ArrayList<School> sectional1Participants = new ArrayList<School>();		
		
		for(int i = 0; i < testSchoolCount/2; i++, distance += 5) {
			
			School school = new School(startingId + i);
			
			schoolValues = (startingId + i) + ", 'Test Name', 'Test_Address', 'Test City', 'Test Zip', 0, 0, 0, true, true";
			sql = "INSERT IGNORE INTO schools VALUES(" + schoolValues + ")";
			SqlDatabaseInterface.runVoidQuery(sql, connection);
			
			
			
			if(i != 0) {
				distanceValues = startingId + "," + (startingId + i) + "," + distance;
				sql = "INSERT IGNORE INTO school_distance VALUES(" + distanceValues + ")";
				SqlDatabaseInterface.runVoidQuery(sql, connection);
				
				participantValues =  (startingId + i) + ", " + sectional1Id;
				sql = "INSERT IGNORE INTO participating VALUES(" + participantValues + ")";
				SqlDatabaseInterface.runVoidQuery(sql, connection);
				
				sectional1Participants.add(school);
				}
		}
		
		for(int j = testSchoolCount/2; j < testSchoolCount; j++, distance += 5) {
			School school = new School(startingId + j);
			model.addTransfer(school);
			
			schoolValues = (startingId + j) + ", 'Test Name', 'Test_Address', 'Test City', 'Test Zip', 0, 0, 0, true, true";
			sql = "INSERT IGNORE INTO schools VALUES(" + schoolValues + ")";
			SqlDatabaseInterface.runVoidQuery(sql, connection);
			
			String transferValues =  (startingId + j) + ", " + sectional2Id;
			sql = "INSERT IGNORE INTO participating VALUES(" + transferValues + ")";
			SqlDatabaseInterface.runVoidQuery(sql, connection);
			
			distanceValues = startingId + "," + (startingId + j) + "," + distance;
			sql = "INSERT IGNORE INTO school_distance VALUES(" + distanceValues + ")";
			SqlDatabaseInterface.runVoidQuery(sql, connection);
			
			participantValues =  (startingId + j) + ", " + sectional2Id;
			sql = "INSERT IGNORE INTO participating VALUES(" + participantValues + ")";
			SqlDatabaseInterface.runVoidQuery(sql, connection);
		}
		
		Event event = new Base(Level.Sec, new School(startingId), model.getTournamentById(tournamentId), sectional1Participants);
		
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
	
	
	@Test void test() {
		double actualDistance = (double) model.getLongestDistanceWithTransfers();
		double expectedDistance = 45;
		
		assertEquals(expectedDistance, actualDistance);
		System.out.println(actualDistance);
		
	}
	

	
	

}















