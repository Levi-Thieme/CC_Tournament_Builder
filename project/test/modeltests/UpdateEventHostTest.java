package modeltests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import builder.Model;
import builder.SqlDatabaseInterface;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Random;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import tournament.components.Advancement;
import tournament.components.Base;
import tournament.components.Event;
import tournament.components.Event.Level;
import tournament.components.School;


/*
 * This test class tests the updateEventHost method for a host that is not a participant of the currently selected event
 */
class UpdateEventHostTest {

	private static Model model;
	private static Connection connection;
	private static int startingId = 700;
	private static int testSchoolCount = 8;
	private static int tournamentId = 101;
	private static int stateId = 102;
	private static int semistateId = 103;
	private static int regionalId = 104;
	private static int sectionalId = 105;
	private static ArrayList<School> expectedParticipants = new ArrayList<School>();
	private static Event sectionalEvent = null;
	private static Event regionalEvent = null;
	private static Event semistateEvent = null;
	private static Event stateEvent = null;
	private static Event selectedEvent = null;
	private static School host = null;
	private static School newHost = null;


	@BeforeAll static void init(){
		
		
		model = Model.getInstance();
		
		connection = model.getConnection();
		String sql = "";
		String schoolValues = "";
		String participantValues = "";
		host = new School(startingId);
		
		
		sql = "INSERT IGNORE INTO tournaments VALUES(" + tournamentId + ", 'Test Tournament', 0)";
		SqlDatabaseInterface.runVoidQuery(sql, connection);
		
		sql = "INSERT IGNORE INTO state VALUES(" + stateId + "," + tournamentId + "," + startingId + ")";
		SqlDatabaseInterface.runVoidQuery(sql, connection);
		
		sql = "INSERT IGNORE INTO semi_state VALUES(" + semistateId + "," + stateId + "," + startingId + ")";
		SqlDatabaseInterface.runVoidQuery(sql, connection);
		
		sql = "INSERT IGNORE INTO regional VALUES(" + regionalId + "," + semistateId + "," + startingId + ")";
		SqlDatabaseInterface.runVoidQuery(sql, connection);
		
		sql = "INSERT IGNORE INTO sectional VALUES(" + sectionalId + "," + regionalId + "," + startingId + ")";
		SqlDatabaseInterface.runVoidQuery(sql, connection);
			
		
		for(int i = 0; i < testSchoolCount; i++) {
			
			School school = new School(startingId + i);
			
			schoolValues = (startingId + i) + ", 'Test Name', 'Test_Address', 'Test City', 'Test Zip', 0, 0, 0, true, true";
			sql = "INSERT IGNORE INTO schools VALUES(" + schoolValues + ")";
			SqlDatabaseInterface.runVoidQuery(sql, connection);
	
			expectedParticipants.add(school);

			participantValues =  (startingId + i) + ", " + sectionalId;
			sql = "INSERT IGNORE INTO participating VALUES(" + participantValues + ")";
			SqlDatabaseInterface.runVoidQuery(sql, connection);
		}
		
		
		sectionalEvent = new Base(Level.Sec, host, model.getTournamentById(tournamentId), expectedParticipants);
		Event[] regionalFeeders = {sectionalEvent};
		
		regionalEvent = new Advancement(Level.Reg, host, model.getTournamentById(tournamentId), regionalFeeders);
		
		Event[] semistateFeeders = {regionalEvent};
		semistateEvent = new Advancement(Level.Semi, host, model.getTournamentById(tournamentId), semistateFeeders);
		
		Event[] stateFeeders = {semistateEvent};
		stateEvent = new Advancement(Level.State, host, model.getTournamentById(tournamentId), stateFeeders);

	}
	
	@AfterAll static void teardown() {
		model.removeTournament(model.getTournamentById(tournamentId));
		String sql = "";
		
		for(int i = 0; i < testSchoolCount; i++) {
			sql = "DELETE FROM schools WHERE ID=" + (startingId + i);
			SqlDatabaseInterface.runVoidQuery(sql, connection);
		}
		
		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Test void testSectionalHostUpdateWithNonParticipant() {
		selectedEvent = sectionalEvent;
		model.setSelectedEvent(selectedEvent);
		
		newHost = new School(startingId + testSchoolCount);
		
		model.updateEventHost(selectedEvent, newHost.getId());
		
		ArrayList<School> actualParticipants = model.getFeederSchools(selectedEvent);
		
		assertEquals(expectedParticipants, actualParticipants);
		assertEquals(newHost.getId(), model.getSelectedEvent().getHost().getId());
	}
	
	@Test void testRegionalHostUpdateWithNonParticipant() {
		selectedEvent = regionalEvent;
		model.setSelectedEvent(selectedEvent);
		
		newHost = new School(startingId + testSchoolCount);
		
		model.updateEventHost(selectedEvent, newHost.getId());
		
		ArrayList<School> actualParticipants = model.getFeederSchools(selectedEvent);
		
		assertEquals(expectedParticipants, actualParticipants);
		assertEquals(newHost.getId(), model.getSelectedEvent().getHost().getId());
	}
	
	@Test void testSemistateHostUpdateWithNonParticipant() {
		selectedEvent = semistateEvent;
		model.setSelectedEvent(selectedEvent);
		
		newHost = new School(startingId + testSchoolCount);
		
		model.updateEventHost(selectedEvent, newHost.getId());
		
		ArrayList<School> actualParticipants = model.getFeederSchools(selectedEvent);
		
		assertEquals(expectedParticipants, actualParticipants);
		assertEquals(newHost.getId(), model.getSelectedEvent().getHost().getId());
	}
	
	@Test void testStateHostUpdateWithNonParticipant() {
		selectedEvent = stateEvent;
		model.setSelectedEvent(selectedEvent);
		
		newHost = new School(startingId + testSchoolCount);
		
		model.updateEventHost(selectedEvent, newHost.getId());
		
		ArrayList<School> actualParticipants = model.getFeederSchools(selectedEvent);
		
		assertEquals(expectedParticipants, actualParticipants);
		assertEquals(newHost.getId(), model.getSelectedEvent().getHost().getId());
	}
	
	
	@Test void testSectionalHostUpdateWithParticipant() {
		selectedEvent = sectionalEvent;
		model.setSelectedEvent(selectedEvent);
		
		Random r = new Random();
		
		newHost = expectedParticipants.get(r.nextInt(expectedParticipants.size()));
		
		model.updateEventHost(selectedEvent, newHost.getId());
		
		ArrayList<School> actualParticipants = model.getFeederSchools(selectedEvent);
		
		assertEquals(expectedParticipants, actualParticipants);
		assertEquals(newHost.getId(), model.getSelectedEvent().getHost().getId());
	}
	
	@Test void testRegionalHostUpdateWithParticipant() {
		selectedEvent = regionalEvent;
		model.setSelectedEvent(selectedEvent);
		
		Random r = new Random();
		
		newHost = expectedParticipants.get(r.nextInt(expectedParticipants.size()));
		
		model.updateEventHost(selectedEvent, newHost.getId());
		
		ArrayList<School> actualParticipants = model.getFeederSchools(selectedEvent);
		
		assertEquals(expectedParticipants, actualParticipants);
		assertEquals(newHost.getId(), model.getSelectedEvent().getHost().getId());
	}
	
	@Test void testSemistateHostUpdateWithParticipant() {
		selectedEvent = semistateEvent;
		model.setSelectedEvent(selectedEvent);
		
		Random r = new Random();
		
		newHost = expectedParticipants.get(r.nextInt(expectedParticipants.size()));
		
		model.updateEventHost(selectedEvent, newHost.getId());
		
		ArrayList<School> actualParticipants = model.getFeederSchools(selectedEvent);
		
		assertEquals(expectedParticipants, actualParticipants);
		assertEquals(newHost.getId(), model.getSelectedEvent().getHost().getId());
	}
	
	@Test void testStateHostUpdateWithParticipant() {
		selectedEvent = stateEvent;
		model.setSelectedEvent(selectedEvent);
		
		Random r = new Random();
		
		newHost = expectedParticipants.get(r.nextInt(expectedParticipants.size()));
		
		model.updateEventHost(selectedEvent, newHost.getId());
		
		ArrayList<School> actualParticipants = model.getFeederSchools(selectedEvent);
		
		assertEquals(expectedParticipants, actualParticipants);
		assertEquals(newHost.getId(), model.getSelectedEvent().getHost().getId());
	}
	
	
}
