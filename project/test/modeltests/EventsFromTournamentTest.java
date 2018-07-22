package modeltests;

import builder.Model;
import java.util.ArrayList;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import tournament.components.Advancement;
import tournament.components.Event;
import tournament.components.Tournament;


class EventsFromTournamentTest {

	@Test void test() {
		
		Model model = Model.getInstance();
		
		Tournament initialTournament = model.getTournaments().get(0);
		
		Advancement stateEvent = (Advancement) initialTournament.getHighestEvent();
		
		ArrayList<Event> semiStates = model.getDirectFeederEvents(stateEvent);
		ArrayList<Event> regionals = new ArrayList<Event>();
		ArrayList<Event> sectionals = new ArrayList<Event>();
		
		
		for(int i = 0; i < semiStates.size(); i++) {
			regionals.addAll(model.getDirectFeederEvents((Advancement) semiStates.get(i)));
		}
		
		for(int i = 0; i < regionals.size(); i++) {
			sectionals.addAll(model.getDirectFeederEvents((Advancement) regionals.get(i)));
		}
		
		
		if(semiStates.size() == 0 || regionals.size() == 0 || sectionals.size() == 0) {
			Assert.fail("Test: Failed\nEmpty list of either semi-states, regionals, or sectionals.");
		} else {
			System.out.println("Test: Successful\n");
		}
	}

}
